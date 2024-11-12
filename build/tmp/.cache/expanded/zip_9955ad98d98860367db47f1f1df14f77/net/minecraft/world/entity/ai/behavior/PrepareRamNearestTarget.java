package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class PrepareRamNearestTarget<E extends PathfinderMob> extends Behavior<E> {
   public static final int TIME_OUT_DURATION = 160;
   private final ToIntFunction<E> getCooldownOnFail;
   private final int minRamDistance;
   private final int maxRamDistance;
   private final float walkSpeed;
   private final TargetingConditions ramTargeting;
   private final int ramPrepareTime;
   private final Function<E, SoundEvent> getPrepareRamSound;
   private Optional<Long> reachedRamPositionTimestamp = Optional.empty();
   private Optional<PrepareRamNearestTarget.RamCandidate> ramCandidate = Optional.empty();

   public PrepareRamNearestTarget(ToIntFunction<E> pGetCooldownOnFall, int pMinRamDistance, int pMaxRamDistance, float pWalkSpeed, TargetingConditions pRamTargeting, int pRamPrepareTime, Function<E, SoundEvent> pGetPrepareRamSound) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT, MemoryModuleType.RAM_TARGET, MemoryStatus.VALUE_ABSENT), 160);
      this.getCooldownOnFail = pGetCooldownOnFall;
      this.minRamDistance = pMinRamDistance;
      this.maxRamDistance = pMaxRamDistance;
      this.walkSpeed = pWalkSpeed;
      this.ramTargeting = pRamTargeting;
      this.ramPrepareTime = pRamPrepareTime;
      this.getPrepareRamSound = pGetPrepareRamSound;
   }

   protected void start(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).flatMap((p_186049_) -> {
         return p_186049_.findClosest((p_147789_) -> {
            return this.ramTargeting.test(pEntity, p_147789_);
         });
      }).ifPresent((p_147778_) -> {
         this.chooseRamPosition(pEntity, p_147778_);
      });
   }

   protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      if (!brain.hasMemoryValue(MemoryModuleType.RAM_TARGET)) {
         pLevel.broadcastEntityEvent(pEntity, (byte)59);
         brain.setMemory(MemoryModuleType.RAM_COOLDOWN_TICKS, this.getCooldownOnFail.applyAsInt(pEntity));
      }

   }

   protected boolean canStillUse(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      return this.ramCandidate.isPresent() && this.ramCandidate.get().getTarget().isAlive();
   }

   protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
      if (!this.ramCandidate.isEmpty()) {
         pOwner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.ramCandidate.get().getStartPosition(), this.walkSpeed, 0));
         pOwner.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.ramCandidate.get().getTarget(), true));
         boolean flag = !this.ramCandidate.get().getTarget().blockPosition().equals(this.ramCandidate.get().getTargetPosition());
         if (flag) {
            pLevel.broadcastEntityEvent(pOwner, (byte)59);
            pOwner.getNavigation().stop();
            this.chooseRamPosition(pOwner, (this.ramCandidate.get()).target);
         } else {
            BlockPos blockpos = pOwner.blockPosition();
            if (blockpos.equals(this.ramCandidate.get().getStartPosition())) {
               pLevel.broadcastEntityEvent(pOwner, (byte)58);
               if (this.reachedRamPositionTimestamp.isEmpty()) {
                  this.reachedRamPositionTimestamp = Optional.of(pGameTime);
               }

               if (pGameTime - this.reachedRamPositionTimestamp.get() >= (long)this.ramPrepareTime) {
                  pOwner.getBrain().setMemory(MemoryModuleType.RAM_TARGET, this.getEdgeOfBlock(blockpos, this.ramCandidate.get().getTargetPosition()));
                  pLevel.playSound((Player)null, pOwner, this.getPrepareRamSound.apply(pOwner), SoundSource.NEUTRAL, 1.0F, pOwner.getVoicePitch());
                  this.ramCandidate = Optional.empty();
               }
            }
         }

      }
   }

   private Vec3 getEdgeOfBlock(BlockPos pPos, BlockPos pOther) {
      double d0 = 0.5D;
      double d1 = 0.5D * (double)Mth.sign((double)(pOther.getX() - pPos.getX()));
      double d2 = 0.5D * (double)Mth.sign((double)(pOther.getZ() - pPos.getZ()));
      return Vec3.atBottomCenterOf(pOther).add(d1, 0.0D, d2);
   }

   private Optional<BlockPos> calculateRammingStartPosition(PathfinderMob pPathfinder, LivingEntity pEntity) {
      BlockPos blockpos = pEntity.blockPosition();
      if (!this.isWalkableBlock(pPathfinder, blockpos)) {
         return Optional.empty();
      } else {
         List<BlockPos> list = Lists.newArrayList();
         BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos.mutable();

         for(Direction direction : Direction.Plane.HORIZONTAL) {
            blockpos$mutableblockpos.set(blockpos);

            for(int i = 0; i < this.maxRamDistance; ++i) {
               if (!this.isWalkableBlock(pPathfinder, blockpos$mutableblockpos.move(direction))) {
                  blockpos$mutableblockpos.move(direction.getOpposite());
                  break;
               }
            }

            if (blockpos$mutableblockpos.distManhattan(blockpos) >= this.minRamDistance) {
               list.add(blockpos$mutableblockpos.immutable());
            }
         }

         PathNavigation pathnavigation = pPathfinder.getNavigation();
         return list.stream().sorted(Comparator.comparingDouble(pPathfinder.blockPosition()::distSqr)).filter((p_147753_) -> {
            Path path = pathnavigation.createPath(p_147753_, 0);
            return path != null && path.canReach();
         }).findFirst();
      }
   }

   private boolean isWalkableBlock(PathfinderMob pPathfinder, BlockPos pPos) {
      return pPathfinder.getNavigation().isStableDestination(pPos) && pPathfinder.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(pPathfinder.level(), pPos.mutable())) == 0.0F;
   }

   private void chooseRamPosition(PathfinderMob pPathfinder, LivingEntity pEntity) {
      this.reachedRamPositionTimestamp = Optional.empty();
      this.ramCandidate = this.calculateRammingStartPosition(pPathfinder, pEntity).map((p_289369_) -> {
         return new PrepareRamNearestTarget.RamCandidate(p_289369_, pEntity.blockPosition(), pEntity);
      });
   }

   public static class RamCandidate {
      private final BlockPos startPosition;
      private final BlockPos targetPosition;
      final LivingEntity target;

      public RamCandidate(BlockPos pStartPosition, BlockPos pTargetPosition, LivingEntity pTarget) {
         this.startPosition = pStartPosition;
         this.targetPosition = pTargetPosition;
         this.target = pTarget;
      }

      public BlockPos getStartPosition() {
         return this.startPosition;
      }

      public BlockPos getTargetPosition() {
         return this.targetPosition;
      }

      public LivingEntity getTarget() {
         return this.target;
      }
   }
}