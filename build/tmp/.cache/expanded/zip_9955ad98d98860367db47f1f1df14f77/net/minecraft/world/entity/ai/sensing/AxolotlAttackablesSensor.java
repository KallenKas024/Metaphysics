package net.minecraft.world.entity.ai.sensing;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class AxolotlAttackablesSensor extends NearestVisibleLivingEntitySensor {
   public static final float TARGET_DETECTION_DISTANCE = 8.0F;

   /**
    * @return if the second entity is hostile to the axolotl or is huntable by it
    */
   protected boolean isMatchingEntity(LivingEntity pAttacker, LivingEntity pTarget) {
      return this.isClose(pAttacker, pTarget) && pTarget.isInWaterOrBubble() && (this.isHostileTarget(pTarget) || this.isHuntTarget(pAttacker, pTarget)) && Sensor.isEntityAttackable(pAttacker, pTarget);
   }

   private boolean isHuntTarget(LivingEntity pAttacker, LivingEntity pTarget) {
      return !pAttacker.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && pTarget.getType().is(EntityTypeTags.AXOLOTL_HUNT_TARGETS);
   }

   private boolean isHostileTarget(LivingEntity pTarget) {
      return pTarget.getType().is(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES);
   }

   private boolean isClose(LivingEntity pAttacker, LivingEntity pTarget) {
      return pTarget.distanceToSqr(pAttacker) <= 64.0D;
   }

   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}