package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.BecomePassiveIfMemoryPresent;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;

public class HoglinAi {
   public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
   public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
   private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
   private static final int ATTACK_DURATION = 200;
   private static final int DESIRED_DISTANCE_FROM_PIGLIN_WHEN_IDLING = 8;
   private static final int DESIRED_DISTANCE_FROM_PIGLIN_WHEN_RETREATING = 15;
   private static final int ATTACK_INTERVAL = 40;
   private static final int BABY_ATTACK_INTERVAL = 15;
   private static final int REPELLENT_PACIFY_TIME = 200;
   private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
   private static final float SPEED_MULTIPLIER_WHEN_AVOIDING_REPELLENT = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.3F;
   private static final float SPEED_MULTIPLIER_WHEN_MAKING_LOVE = 0.6F;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.4F;
   private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 0.6F;

   protected static Brain<?> makeBrain(Brain<Hoglin> pBrain) {
      initCoreActivity(pBrain);
      initIdleActivity(pBrain);
      initFightActivity(pBrain);
      initRetreatActivity(pBrain);
      pBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      pBrain.setDefaultActivity(Activity.IDLE);
      pBrain.useDefaultActivity();
      return pBrain;
   }

   private static void initCoreActivity(Brain<Hoglin> pBrain) {
      pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
   }

   private static void initIdleActivity(Brain<Hoglin> pBrain) {
      pBrain.addActivity(Activity.IDLE, 10, ImmutableList.of(BecomePassiveIfMemoryPresent.create(MemoryModuleType.NEAREST_REPELLENT, 200), new AnimalMakeLove(EntityType.HOGLIN, 0.6F), SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, true), StartAttacking.create(HoglinAi::findNearestValidAttackTarget), BehaviorBuilder.triggerIf(Hoglin::isAdult, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, 0.4F, 8, false)), SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)), BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 0.6F), createIdleMovementBehaviors()));
   }

   private static void initFightActivity(Brain<Hoglin> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(BecomePassiveIfMemoryPresent.create(MemoryModuleType.NEAREST_REPELLENT, 200), new AnimalMakeLove(EntityType.HOGLIN, 0.6F), SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F), BehaviorBuilder.triggerIf(Hoglin::isAdult, MeleeAttack.create(40)), BehaviorBuilder.triggerIf(AgeableMob::isBaby, MeleeAttack.create(15)), StopAttackingIfTargetInvalid.create(), EraseMemoryIf.create(HoglinAi::isBreeding, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
   }

   private static void initRetreatActivity(Brain<Hoglin> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.3F, 15, false), createIdleMovementBehaviors(), SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)), EraseMemoryIf.create(HoglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
   }

   private static RunOne<Hoglin> createIdleMovementBehaviors() {
      return new RunOne<>(ImmutableList.of(Pair.of(RandomStroll.stroll(0.4F), 2), Pair.of(SetWalkTargetFromLookTarget.create(0.4F, 3), 2), Pair.of(new DoNothing(30, 60), 1)));
   }

   protected static void updateActivity(Hoglin pHoglin) {
      Brain<Hoglin> brain = pHoglin.getBrain();
      Activity activity = brain.getActiveNonCoreActivity().orElse((Activity)null);
      brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
      Activity activity1 = brain.getActiveNonCoreActivity().orElse((Activity)null);
      if (activity != activity1) {
         getSoundForCurrentActivity(pHoglin).ifPresent(pHoglin::playSoundEvent);
      }

      pHoglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
   }

   protected static void onHitTarget(Hoglin pHoglin, LivingEntity pTarget) {
      if (!pHoglin.isBaby()) {
         if (pTarget.getType() == EntityType.PIGLIN && piglinsOutnumberHoglins(pHoglin)) {
            setAvoidTarget(pHoglin, pTarget);
            broadcastRetreat(pHoglin, pTarget);
         } else {
            broadcastAttackTarget(pHoglin, pTarget);
         }
      }
   }

   private static void broadcastRetreat(Hoglin pHoglin, LivingEntity pTarget) {
      getVisibleAdultHoglins(pHoglin).forEach((p_34590_) -> {
         retreatFromNearestTarget(p_34590_, pTarget);
      });
   }

   private static void retreatFromNearestTarget(Hoglin pHoglin, LivingEntity pTarget) {
      Brain<Hoglin> brain = pHoglin.getBrain();
      LivingEntity $$2 = BehaviorUtils.getNearestTarget(pHoglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), pTarget);
      $$2 = BehaviorUtils.getNearestTarget(pHoglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), $$2);
      setAvoidTarget(pHoglin, $$2);
   }

   private static void setAvoidTarget(Hoglin pHoglin, LivingEntity pTarget) {
      pHoglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
      pHoglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pHoglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, pTarget, (long)RETREAT_DURATION.sample(pHoglin.level().random));
   }

   private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Hoglin p_34611_) {
      return !isPacified(p_34611_) && !isBreeding(p_34611_) ? p_34611_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) : Optional.empty();
   }

   static boolean isPosNearNearestRepellent(Hoglin pHoglin, BlockPos pPos) {
      Optional<BlockPos> optional = pHoglin.getBrain().getMemory(MemoryModuleType.NEAREST_REPELLENT);
      return optional.isPresent() && optional.get().closerThan(pPos, 8.0D);
   }

   private static boolean wantsToStopFleeing(Hoglin p_34618_) {
      return p_34618_.isAdult() && !piglinsOutnumberHoglins(p_34618_);
   }

   private static boolean piglinsOutnumberHoglins(Hoglin pHoglin) {
      if (pHoglin.isBaby()) {
         return false;
      } else {
         int i = pHoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0);
         int j = pHoglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0) + 1;
         return i > j;
      }
   }

   protected static void wasHurtBy(Hoglin pHoglin, LivingEntity pLivingEntity) {
      Brain<Hoglin> brain = pHoglin.getBrain();
      brain.eraseMemory(MemoryModuleType.PACIFIED);
      brain.eraseMemory(MemoryModuleType.BREED_TARGET);
      if (pHoglin.isBaby()) {
         retreatFromNearestTarget(pHoglin, pLivingEntity);
      } else {
         maybeRetaliate(pHoglin, pLivingEntity);
      }
   }

   private static void maybeRetaliate(Hoglin pHoglin, LivingEntity pLivingEntity) {
      if (!pHoglin.getBrain().isActive(Activity.AVOID) || pLivingEntity.getType() != EntityType.PIGLIN) {
         if (pLivingEntity.getType() != EntityType.HOGLIN) {
            if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(pHoglin, pLivingEntity, 4.0D)) {
               if (Sensor.isEntityAttackable(pHoglin, pLivingEntity)) {
                  setAttackTarget(pHoglin, pLivingEntity);
                  broadcastAttackTarget(pHoglin, pLivingEntity);
               }
            }
         }
      }
   }

   private static void setAttackTarget(Hoglin pHoglin, LivingEntity pTarget) {
      Brain<Hoglin> brain = pHoglin.getBrain();
      brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      brain.eraseMemory(MemoryModuleType.BREED_TARGET);
      brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, pTarget, 200L);
   }

   private static void broadcastAttackTarget(Hoglin pHoglin, LivingEntity pTarget) {
      getVisibleAdultHoglins(pHoglin).forEach((p_34574_) -> {
         setAttackTargetIfCloserThanCurrent(p_34574_, pTarget);
      });
   }

   private static void setAttackTargetIfCloserThanCurrent(Hoglin pHoglin, LivingEntity pTarget) {
      if (!isPacified(pHoglin)) {
         Optional<LivingEntity> optional = pHoglin.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
         LivingEntity livingentity = BehaviorUtils.getNearestTarget(pHoglin, optional, pTarget);
         setAttackTarget(pHoglin, livingentity);
      }
   }

   public static Optional<SoundEvent> getSoundForCurrentActivity(Hoglin pHoglin) {
      return pHoglin.getBrain().getActiveNonCoreActivity().map((p_34600_) -> {
         return getSoundForActivity(pHoglin, p_34600_);
      });
   }

   private static SoundEvent getSoundForActivity(Hoglin pHoglin, Activity pActivity) {
      if (pActivity != Activity.AVOID && !pHoglin.isConverting()) {
         if (pActivity == Activity.FIGHT) {
            return SoundEvents.HOGLIN_ANGRY;
         } else {
            return isNearRepellent(pHoglin) ? SoundEvents.HOGLIN_RETREAT : SoundEvents.HOGLIN_AMBIENT;
         }
      } else {
         return SoundEvents.HOGLIN_RETREAT;
      }
   }

   private static List<Hoglin> getVisibleAdultHoglins(Hoglin pHoglin) {
      return pHoglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS).orElse(ImmutableList.of());
   }

   private static boolean isNearRepellent(Hoglin pHoglin) {
      return pHoglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
   }

   private static boolean isBreeding(Hoglin p_34638_) {
      return p_34638_.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
   }

   protected static boolean isPacified(Hoglin pHoglin) {
      return pHoglin.getBrain().hasMemoryValue(MemoryModuleType.PACIFIED);
   }
}