package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class VillagerBabiesSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
   }

   protected void doTick(ServerLevel pLevel, LivingEntity pEntity) {
      pEntity.getBrain().setMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES, this.getNearestVillagerBabies(pEntity));
   }

   private List<LivingEntity> getNearestVillagerBabies(LivingEntity pLivingEntity) {
      return ImmutableList.copyOf(this.getVisibleEntities(pLivingEntity).findAll(this::isVillagerBaby));
   }

   private boolean isVillagerBaby(LivingEntity p_26839_) {
      return p_26839_.getType() == EntityType.VILLAGER && p_26839_.isBaby();
   }

   private NearestVisibleLivingEntities getVisibleEntities(LivingEntity pLivingEntity) {
      return pLivingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
   }
}