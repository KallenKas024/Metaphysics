package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerHostilesSensor extends NearestVisibleLivingEntitySensor {
   private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.<EntityType<?>, Float>builder().put(EntityType.DROWNED, 8.0F).put(EntityType.EVOKER, 12.0F).put(EntityType.HUSK, 8.0F).put(EntityType.ILLUSIONER, 12.0F).put(EntityType.PILLAGER, 15.0F).put(EntityType.RAVAGER, 12.0F).put(EntityType.VEX, 8.0F).put(EntityType.VINDICATOR, 10.0F).put(EntityType.ZOGLIN, 10.0F).put(EntityType.ZOMBIE, 8.0F).put(EntityType.ZOMBIE_VILLAGER, 8.0F).build();

   /**
    * @return if the second entity is hostile to the axolotl or is huntable by it
    */
   protected boolean isMatchingEntity(LivingEntity pAttacker, LivingEntity pTarget) {
      return this.isHostile(pTarget) && this.isClose(pAttacker, pTarget);
   }

   private boolean isClose(LivingEntity pAttacker, LivingEntity pTarget) {
      float f = ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(pTarget.getType());
      return pTarget.distanceToSqr(pAttacker) <= (double)(f * f);
   }

   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_HOSTILE;
   }

   private boolean isHostile(LivingEntity pEntity) {
      return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(pEntity.getType());
   }
}