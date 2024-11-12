package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class MoveToTargetSink extends Behavior<Mob> {
   private static final int MAX_COOLDOWN_BEFORE_RETRYING = 40;
   private int remainingCooldown;
   @Nullable
   private Path path;
   @Nullable
   private BlockPos lastTargetPos;
   private float speedModifier;

   public MoveToTargetSink() {
      this(150, 250);
   }

   public MoveToTargetSink(int pMinDuration, int pMaxDuration) {
      super(ImmutableMap.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED, MemoryModuleType.PATH, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT), pMinDuration, pMaxDuration);
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Mob pOwner) {
      if (this.remainingCooldown > 0) {
         --this.remainingCooldown;
         return false;
      } else {
         Brain<?> brain = pOwner.getBrain();
         WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
         boolean flag = this.reachedTarget(pOwner, walktarget);
         if (!flag && this.tryComputePath(pOwner, walktarget, pLevel.getGameTime())) {
            this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
            return true;
         } else {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            if (flag) {
               brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            }

            return false;
         }
      }
   }

   protected boolean canStillUse(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      if (this.path != null && this.lastTargetPos != null) {
         Optional<WalkTarget> optional = pEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
         boolean flag = optional.map(MoveToTargetSink::isWalkTargetSpectator).orElse(false);
         PathNavigation pathnavigation = pEntity.getNavigation();
         return !pathnavigation.isDone() && optional.isPresent() && !this.reachedTarget(pEntity, optional.get()) && !flag;
      } else {
         return false;
      }
   }

   protected void stop(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      if (pEntity.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) && !this.reachedTarget(pEntity, pEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).get()) && pEntity.getNavigation().isStuck()) {
         this.remainingCooldown = pLevel.getRandom().nextInt(40);
      }

      pEntity.getNavigation().stop();
      pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.PATH);
      this.path = null;
   }

   protected void start(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      pEntity.getBrain().setMemory(MemoryModuleType.PATH, this.path);
      pEntity.getNavigation().moveTo(this.path, (double)this.speedModifier);
   }

   protected void tick(ServerLevel pLevel, Mob pOwner, long pGameTime) {
      Path path = pOwner.getNavigation().getPath();
      Brain<?> brain = pOwner.getBrain();
      if (this.path != path) {
         this.path = path;
         brain.setMemory(MemoryModuleType.PATH, path);
      }

      if (path != null && this.lastTargetPos != null) {
         WalkTarget walktarget = brain.getMemory(MemoryModuleType.WALK_TARGET).get();
         if (walktarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0D && this.tryComputePath(pOwner, walktarget, pLevel.getGameTime())) {
            this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
            this.start(pLevel, pOwner, pGameTime);
         }

      }
   }

   private boolean tryComputePath(Mob pMob, WalkTarget pTarget, long pTime) {
      BlockPos blockpos = pTarget.getTarget().currentBlockPosition();
      this.path = pMob.getNavigation().createPath(blockpos, 0);
      this.speedModifier = pTarget.getSpeedModifier();
      Brain<?> brain = pMob.getBrain();
      if (this.reachedTarget(pMob, pTarget)) {
         brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      } else {
         boolean flag = this.path != null && this.path.canReach();
         if (flag) {
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         } else if (!brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
            brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, pTime);
         }

         if (this.path != null) {
            return true;
         }

         Vec3 vec3 = DefaultRandomPos.getPosTowards((PathfinderMob)pMob, 10, 7, Vec3.atBottomCenterOf(blockpos), (double)((float)Math.PI / 2F));
         if (vec3 != null) {
            this.path = pMob.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
            return this.path != null;
         }
      }

      return false;
   }

   private boolean reachedTarget(Mob pMob, WalkTarget pTarget) {
      return pTarget.getTarget().currentBlockPosition().distManhattan(pMob.blockPosition()) <= pTarget.getCloseEnoughDist();
   }

   private static boolean isWalkTargetSpectator(WalkTarget p_277420_) {
      PositionTracker positiontracker = p_277420_.getTarget();
      if (positiontracker instanceof EntityTracker entitytracker) {
         return entitytracker.getEntity().isSpectator();
      } else {
         return false;
      }
   }
}