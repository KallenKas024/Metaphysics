package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public abstract class NearestVisibleLivingEntitySensor extends Sensor<LivingEntity> {
   /**
    * @return if the second entity is hostile to the axolotl or is huntable by it
    */
   protected abstract boolean isMatchingEntity(LivingEntity pAttacker, LivingEntity pTarget);

   protected abstract MemoryModuleType<LivingEntity> getMemory();

   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(this.getMemory());
   }

   protected void doTick(ServerLevel pLevel, LivingEntity pEntity) {
      pEntity.getBrain().setMemory(this.getMemory(), this.getNearestEntity(pEntity));
   }

   private Optional<LivingEntity> getNearestEntity(LivingEntity pEntity) {
      return this.getVisibleEntities(pEntity).flatMap((p_186153_) -> {
         return p_186153_.findClosest((p_148301_) -> {
            return this.isMatchingEntity(pEntity, p_148301_);
         });
      });
   }

   protected Optional<NearestVisibleLivingEntities> getVisibleEntities(LivingEntity pEntity) {
      return pEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
   }
}