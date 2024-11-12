package net.minecraft.world.entity.ai.sensing;

import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class Sensor<E extends LivingEntity> {
   private static final RandomSource RANDOM = RandomSource.createThreadSafe();
   private static final int DEFAULT_SCAN_RATE = 20;
   protected static final int TARGETING_RANGE = 16;
   private static final TargetingConditions TARGET_CONDITIONS = TargetingConditions.forNonCombat().range(16.0D);
   private static final TargetingConditions TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forNonCombat().range(16.0D).ignoreInvisibilityTesting();
   private static final TargetingConditions ATTACK_TARGET_CONDITIONS = TargetingConditions.forCombat().range(16.0D);
   private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forCombat().range(16.0D).ignoreInvisibilityTesting();
   private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0D).ignoreLineOfSight();
   private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0D).ignoreLineOfSight().ignoreInvisibilityTesting();
   private final int scanRate;
   private long timeToTick;

   public Sensor(int pScanRate) {
      this.scanRate = pScanRate;
      this.timeToTick = (long)RANDOM.nextInt(pScanRate);
   }

   public Sensor() {
      this(20);
   }

   public final void tick(ServerLevel pLevel, E pEntity) {
      if (--this.timeToTick <= 0L) {
         this.timeToTick = (long)this.scanRate;
         this.doTick(pLevel, pEntity);
      }

   }

   protected abstract void doTick(ServerLevel pLevel, E pEntity);

   public abstract Set<MemoryModuleType<?>> requires();

   /**
    * @return if the entity is remembered as a target and then tests the condition
    */
   public static boolean isEntityTargetable(LivingEntity pLivingEntity, LivingEntity pTarget) {
      return pLivingEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, pTarget) ? TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(pLivingEntity, pTarget) : TARGET_CONDITIONS.test(pLivingEntity, pTarget);
   }

   /**
    * @return if entity is remembered as an attack target and is valid to attack
    */
   public static boolean isEntityAttackable(LivingEntity pAttacker, LivingEntity pTarget) {
      return pAttacker.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, pTarget) ? ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(pAttacker, pTarget) : ATTACK_TARGET_CONDITIONS.test(pAttacker, pTarget);
   }

   public static boolean isEntityAttackableIgnoringLineOfSight(LivingEntity pAttacker, LivingEntity pTarget) {
      return pAttacker.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, pTarget) ? ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.test(pAttacker, pTarget) : ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.test(pAttacker, pTarget);
   }
}