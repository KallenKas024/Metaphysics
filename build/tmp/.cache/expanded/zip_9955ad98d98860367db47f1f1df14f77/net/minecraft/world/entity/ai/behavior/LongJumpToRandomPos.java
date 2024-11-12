package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class LongJumpToRandomPos<E extends Mob> extends Behavior<E> {
   protected static final int FIND_JUMP_TRIES = 20;
   private static final int PREPARE_JUMP_DURATION = 40;
   protected static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
   private static final int TIME_OUT_DURATION = 200;
   private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(65, 70, 75, 80);
   private final UniformInt timeBetweenLongJumps;
   protected final int maxLongJumpHeight;
   protected final int maxLongJumpWidth;
   protected final float maxJumpVelocity;
   protected List<LongJumpToRandomPos.PossibleJump> jumpCandidates = Lists.newArrayList();
   protected Optional<Vec3> initialPosition = Optional.empty();
   @Nullable
   protected Vec3 chosenJump;
   protected int findJumpTries;
   protected long prepareJumpStart;
   private final Function<E, SoundEvent> getJumpSound;
   private final BiPredicate<E, BlockPos> acceptableLandingSpot;

   public LongJumpToRandomPos(UniformInt pTimeBetweenLongJumps, int pMaxLongJumpHeight, int pMaxLongJumpWidth, float pMaxJumpVelocity, Function<E, SoundEvent> pGetJumpSound) {
      this(pTimeBetweenLongJumps, pMaxLongJumpHeight, pMaxLongJumpWidth, pMaxJumpVelocity, pGetJumpSound, LongJumpToRandomPos::defaultAcceptableLandingSpot);
   }

   public static <E extends Mob> boolean defaultAcceptableLandingSpot(E p_251540_, BlockPos p_248879_) {
      Level level = p_251540_.level();
      BlockPos blockpos = p_248879_.below();
      return level.getBlockState(blockpos).isSolidRender(level, blockpos) && p_251540_.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(level, p_248879_.mutable())) == 0.0F;
   }

   public LongJumpToRandomPos(UniformInt pTimeBetweenLongJumps, int pMaxLongJumpHeight, int pMaxLongJumpWidth, float pMaxJumpVelocity, Function<E, SoundEvent> pGetJumpSound, BiPredicate<E, BlockPos> pAcceptableLandingSpot) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT), 200);
      this.timeBetweenLongJumps = pTimeBetweenLongJumps;
      this.maxLongJumpHeight = pMaxLongJumpHeight;
      this.maxLongJumpWidth = pMaxLongJumpWidth;
      this.maxJumpVelocity = pMaxJumpVelocity;
      this.getJumpSound = pGetJumpSound;
      this.acceptableLandingSpot = pAcceptableLandingSpot;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Mob pOwner) {
      boolean flag = pOwner.onGround() && !pOwner.isInWater() && !pOwner.isInLava() && !pLevel.getBlockState(pOwner.blockPosition()).is(Blocks.HONEY_BLOCK);
      if (!flag) {
         pOwner.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(pLevel.random) / 2);
      }

      return flag;
   }

   protected boolean canStillUse(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      boolean flag = this.initialPosition.isPresent() && this.initialPosition.get().equals(pEntity.position()) && this.findJumpTries > 0 && !pEntity.isInWaterOrBubble() && (this.chosenJump != null || !this.jumpCandidates.isEmpty());
      if (!flag && pEntity.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
         pEntity.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(pLevel.random) / 2);
         pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      }

      return flag;
   }

   protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
      this.chosenJump = null;
      this.findJumpTries = 20;
      this.initialPosition = Optional.of(pEntity.position());
      BlockPos blockpos = pEntity.blockPosition();
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      this.jumpCandidates = BlockPos.betweenClosedStream(i - this.maxLongJumpWidth, j - this.maxLongJumpHeight, k - this.maxLongJumpWidth, i + this.maxLongJumpWidth, j + this.maxLongJumpHeight, k + this.maxLongJumpWidth).filter((p_217317_) -> {
         return !p_217317_.equals(blockpos);
      }).map((p_217314_) -> {
         return new LongJumpToRandomPos.PossibleJump(p_217314_.immutable(), Mth.ceil(blockpos.distSqr(p_217314_)));
      }).collect(Collectors.toCollection(Lists::newArrayList));
   }

   protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
      if (this.chosenJump != null) {
         if (pGameTime - this.prepareJumpStart >= 40L) {
            pOwner.setYRot(pOwner.yBodyRot);
            pOwner.setDiscardFriction(true);
            double d0 = this.chosenJump.length();
            double d1 = d0 + (double)pOwner.getJumpBoostPower();
            pOwner.setDeltaMovement(this.chosenJump.scale(d1 / d0));
            pOwner.getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
            pLevel.playSound((Player)null, pOwner, this.getJumpSound.apply(pOwner), SoundSource.NEUTRAL, 1.0F, 1.0F);
         }
      } else {
         --this.findJumpTries;
         this.pickCandidate(pLevel, pOwner, pGameTime);
      }

   }

   protected void pickCandidate(ServerLevel pLevel, E pEntity, long pPrepareJumpStart) {
      while(true) {
         if (!this.jumpCandidates.isEmpty()) {
            Optional<LongJumpToRandomPos.PossibleJump> optional = this.getJumpCandidate(pLevel);
            if (optional.isEmpty()) {
               continue;
            }

            LongJumpToRandomPos.PossibleJump longjumptorandompos$possiblejump = optional.get();
            BlockPos blockpos = longjumptorandompos$possiblejump.getJumpTarget();
            if (!this.isAcceptableLandingPosition(pLevel, pEntity, blockpos)) {
               continue;
            }

            Vec3 vec3 = Vec3.atCenterOf(blockpos);
            Vec3 vec31 = this.calculateOptimalJumpVector(pEntity, vec3);
            if (vec31 == null) {
               continue;
            }

            pEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockpos));
            PathNavigation pathnavigation = pEntity.getNavigation();
            Path path = pathnavigation.createPath(blockpos, 0, 8);
            if (path != null && path.canReach()) {
               continue;
            }

            this.chosenJump = vec31;
            this.prepareJumpStart = pPrepareJumpStart;
            return;
         }

         return;
      }
   }

   protected Optional<LongJumpToRandomPos.PossibleJump> getJumpCandidate(ServerLevel pLevel) {
      Optional<LongJumpToRandomPos.PossibleJump> optional = WeightedRandom.getRandomItem(pLevel.random, this.jumpCandidates);
      optional.ifPresent(this.jumpCandidates::remove);
      return optional;
   }

   private boolean isAcceptableLandingPosition(ServerLevel pLevel, E pEntity, BlockPos pPos) {
      BlockPos blockpos = pEntity.blockPosition();
      int i = blockpos.getX();
      int j = blockpos.getZ();
      return i == pPos.getX() && j == pPos.getZ() ? false : this.acceptableLandingSpot.test(pEntity, pPos);
   }

   @Nullable
   protected Vec3 calculateOptimalJumpVector(Mob pMob, Vec3 pTarget) {
      List<Integer> list = Lists.newArrayList(ALLOWED_ANGLES);
      Collections.shuffle(list);

      for(int i : list) {
         Vec3 vec3 = this.calculateJumpVectorForAngle(pMob, pTarget, i);
         if (vec3 != null) {
            return vec3;
         }
      }

      return null;
   }

   @Nullable
   private Vec3 calculateJumpVectorForAngle(Mob pMob, Vec3 pTarget, int pAngle) {
      Vec3 vec3 = pMob.position();
      Vec3 vec31 = (new Vec3(pTarget.x - vec3.x, 0.0D, pTarget.z - vec3.z)).normalize().scale(0.5D);
      pTarget = pTarget.subtract(vec31);
      Vec3 vec32 = pTarget.subtract(vec3);
      float f = (float)pAngle * (float)Math.PI / 180.0F;
      double d0 = Math.atan2(vec32.z, vec32.x);
      double d1 = vec32.subtract(0.0D, vec32.y, 0.0D).lengthSqr();
      double d2 = Math.sqrt(d1);
      double d3 = vec32.y;
      double d4 = Math.sin((double)(2.0F * f));
      double d5 = 0.08D;
      double d6 = Math.pow(Math.cos((double)f), 2.0D);
      double d7 = Math.sin((double)f);
      double d8 = Math.cos((double)f);
      double d9 = Math.sin(d0);
      double d10 = Math.cos(d0);
      double d11 = d1 * 0.08D / (d2 * d4 - 2.0D * d3 * d6);
      if (d11 < 0.0D) {
         return null;
      } else {
         double d12 = Math.sqrt(d11);
         if (d12 > (double)this.maxJumpVelocity) {
            return null;
         } else {
            double d13 = d12 * d8;
            double d14 = d12 * d7;
            int i = Mth.ceil(d2 / d13) * 2;
            double d15 = 0.0D;
            Vec3 vec33 = null;
            EntityDimensions entitydimensions = pMob.getDimensions(Pose.LONG_JUMPING);

            for(int j = 0; j < i - 1; ++j) {
               d15 += d2 / (double)i;
               double d16 = d7 / d8 * d15 - Math.pow(d15, 2.0D) * 0.08D / (2.0D * d11 * Math.pow(d8, 2.0D));
               double d17 = d15 * d10;
               double d18 = d15 * d9;
               Vec3 vec34 = new Vec3(vec3.x + d17, vec3.y + d16, vec3.z + d18);
               if (vec33 != null && !this.isClearTransition(pMob, entitydimensions, vec33, vec34)) {
                  return null;
               }

               vec33 = vec34;
            }

            return (new Vec3(d13 * d10, d14, d13 * d9)).scale((double)0.95F);
         }
      }
   }

   private boolean isClearTransition(Mob pMob, EntityDimensions pDimensions, Vec3 pStart, Vec3 pEnd) {
      Vec3 vec3 = pEnd.subtract(pStart);
      double d0 = (double)Math.min(pDimensions.width, pDimensions.height);
      int i = Mth.ceil(vec3.length() / d0);
      Vec3 vec31 = vec3.normalize();
      Vec3 vec32 = pStart;

      for(int j = 0; j < i; ++j) {
         vec32 = j == i - 1 ? pEnd : vec32.add(vec31.scale(d0 * (double)0.9F));
         if (!pMob.level().noCollision(pMob, pDimensions.makeBoundingBox(vec32))) {
            return false;
         }
      }

      return true;
   }

   public static class PossibleJump extends WeightedEntry.IntrusiveBase {
      private final BlockPos jumpTarget;

      public PossibleJump(BlockPos pJumpTarget, int pWeight) {
         super(pWeight);
         this.jumpTarget = pJumpTarget;
      }

      public BlockPos getJumpTarget() {
         return this.jumpTarget;
      }
   }
}