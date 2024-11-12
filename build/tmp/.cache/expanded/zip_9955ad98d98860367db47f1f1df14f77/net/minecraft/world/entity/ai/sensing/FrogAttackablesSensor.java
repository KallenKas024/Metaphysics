package net.minecraft.world.entity.ai.sensing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.frog.Frog;

public class FrogAttackablesSensor extends NearestVisibleLivingEntitySensor {
   public static final float TARGET_DETECTION_DISTANCE = 10.0F;

   /**
    * @return if the second entity is hostile to the axolotl or is huntable by it
    */
   protected boolean isMatchingEntity(LivingEntity pAttacker, LivingEntity pTarget) {
      return !pAttacker.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN) && Sensor.isEntityAttackable(pAttacker, pTarget) && Frog.canEat(pTarget) && !this.isUnreachableAttackTarget(pAttacker, pTarget) ? pTarget.closerThan(pAttacker, 10.0D) : false;
   }

   private boolean isUnreachableAttackTarget(LivingEntity pAttacker, LivingEntity pTarget) {
      List<UUID> list = pAttacker.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TONGUE_TARGETS).orElseGet(ArrayList::new);
      return list.contains(pTarget.getUUID());
   }

   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}