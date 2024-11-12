package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.schedule.Activity;

public class PiglinBruteAi {
   private static final int ANGER_DURATION = 600;
   private static final int MELEE_ATTACK_COOLDOWN = 20;
   private static final double ACTIVITY_SOUND_LIKELIHOOD_PER_TICK = 0.0125D;
   private static final int MAX_LOOK_DIST = 8;
   private static final int INTERACTION_RANGE = 8;
   private static final double TARGETING_RANGE = 12.0D;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;
   private static final int HOME_CLOSE_ENOUGH_DISTANCE = 2;
   private static final int HOME_TOO_FAR_DISTANCE = 100;
   private static final int HOME_STROLL_AROUND_DISTANCE = 5;

   protected static Brain<?> makeBrain(PiglinBrute pPiglinBrute, Brain<PiglinBrute> pBrain) {
      initCoreActivity(pPiglinBrute, pBrain);
      initIdleActivity(pPiglinBrute, pBrain);
      initFightActivity(pPiglinBrute, pBrain);
      pBrain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      pBrain.setDefaultActivity(Activity.IDLE);
      pBrain.useDefaultActivity();
      return pBrain;
   }

   protected static void initMemories(PiglinBrute pPiglinBrute) {
      GlobalPos globalpos = GlobalPos.of(pPiglinBrute.level().dimension(), pPiglinBrute.blockPosition());
      pPiglinBrute.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
   }

   private static void initCoreActivity(PiglinBrute pPiglinBrute, Brain<PiglinBrute> pBrain) {
      pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), InteractWithDoor.create(), StopBeingAngryIfTargetDead.create()));
   }

   private static void initIdleActivity(PiglinBrute pPiglinBrute, Brain<PiglinBrute> pBrain) {
      pBrain.addActivity(Activity.IDLE, 10, ImmutableList.of(StartAttacking.<PiglinBrute>create(PiglinBruteAi::findNearestValidAttackTarget), createIdleLookBehaviors(), createIdleMovementBehaviors(), SetLookAndInteract.create(EntityType.PLAYER, 4)));
   }

   private static void initFightActivity(PiglinBrute pPiglinBrute, Brain<PiglinBrute> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(StopAttackingIfTargetInvalid.<Mob>create((p_35118_) -> {
         return !isNearestValidAttackTarget(pPiglinBrute, p_35118_);
      }), SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F), MeleeAttack.create(20)), MemoryModuleType.ATTACK_TARGET);
   }

   private static RunOne<PiglinBrute> createIdleLookBehaviors() {
      return new RunOne<>(ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1), Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1), Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN_BRUTE, 8.0F), 1), Pair.of(SetEntityLookTarget.create(8.0F), 1), Pair.of(new DoNothing(30, 60), 1)));
   }

   private static RunOne<PiglinBrute> createIdleMovementBehaviors() {
      return new RunOne<>(ImmutableList.of(Pair.of(RandomStroll.stroll(0.6F), 2), Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(InteractWith.of(EntityType.PIGLIN_BRUTE, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(StrollToPoi.create(MemoryModuleType.HOME, 0.6F, 2, 100), 2), Pair.of(StrollAroundPoi.create(MemoryModuleType.HOME, 0.6F, 5), 2), Pair.of(new DoNothing(30, 60), 1)));
   }

   protected static void updateActivity(PiglinBrute pPiglinBrute) {
      Brain<PiglinBrute> brain = pPiglinBrute.getBrain();
      Activity activity = brain.getActiveNonCoreActivity().orElse((Activity)null);
      brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
      Activity activity1 = brain.getActiveNonCoreActivity().orElse((Activity)null);
      if (activity != activity1) {
         playActivitySound(pPiglinBrute);
      }

      pPiglinBrute.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
   }

   private static boolean isNearestValidAttackTarget(AbstractPiglin pPiglinBrute, LivingEntity pTarget) {
      return findNearestValidAttackTarget(pPiglinBrute).filter((p_35085_) -> {
         return p_35085_ == pTarget;
      }).isPresent();
   }

   private static Optional<? extends LivingEntity> findNearestValidAttackTarget(AbstractPiglin p_35087_) {
      Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(p_35087_, MemoryModuleType.ANGRY_AT);
      if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(p_35087_, optional.get())) {
         return optional;
      } else {
         Optional<? extends LivingEntity> optional1 = getTargetIfWithinRange(p_35087_, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
         return optional1.isPresent() ? optional1 : p_35087_.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
      }
   }

   private static Optional<? extends LivingEntity> getTargetIfWithinRange(AbstractPiglin pPiglinBrute, MemoryModuleType<? extends LivingEntity> pMemoryType) {
      return pPiglinBrute.getBrain().getMemory(pMemoryType).filter((p_35108_) -> {
         return p_35108_.closerThan(pPiglinBrute, 12.0D);
      });
   }

   protected static void wasHurtBy(PiglinBrute pPiglinBrute, LivingEntity pTarget) {
      if (!(pTarget instanceof AbstractPiglin)) {
         PiglinAi.maybeRetaliate(pPiglinBrute, pTarget);
      }
   }

   protected static void setAngerTarget(PiglinBrute pPiglinBrute, LivingEntity pAngerTarget) {
      pPiglinBrute.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      pPiglinBrute.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, pAngerTarget.getUUID(), 600L);
   }

   protected static void maybePlayActivitySound(PiglinBrute pPiglinBrute) {
      if ((double)pPiglinBrute.level().random.nextFloat() < 0.0125D) {
         playActivitySound(pPiglinBrute);
      }

   }

   private static void playActivitySound(PiglinBrute pPiglinBrute) {
      pPiglinBrute.getBrain().getActiveNonCoreActivity().ifPresent((p_35104_) -> {
         if (p_35104_ == Activity.FIGHT) {
            pPiglinBrute.playAngrySound();
         }

      });
   }
}