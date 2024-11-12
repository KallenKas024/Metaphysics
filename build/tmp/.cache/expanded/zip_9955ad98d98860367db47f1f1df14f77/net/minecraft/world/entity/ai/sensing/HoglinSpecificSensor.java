package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class HoglinSpecificSensor extends Sensor<Hoglin> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT);
   }

   protected void doTick(ServerLevel pLevel, Hoglin pEntity) {
      Brain<?> brain = pEntity.getBrain();
      brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, this.findNearestRepellent(pLevel, pEntity));
      Optional<Piglin> optional = Optional.empty();
      int i = 0;
      List<Hoglin> list = Lists.newArrayList();
      NearestVisibleLivingEntities nearestvisiblelivingentities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

      for(LivingEntity livingentity : nearestvisiblelivingentities.findAll((p_186150_) -> {
         return !p_186150_.isBaby() && (p_186150_ instanceof Piglin || p_186150_ instanceof Hoglin);
      })) {
         if (livingentity instanceof Piglin piglin) {
            ++i;
            if (optional.isEmpty()) {
               optional = Optional.of(piglin);
            }
         }

         if (livingentity instanceof Hoglin hoglin) {
            list.add(hoglin);
         }
      }

      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, optional);
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, list);
      brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, i);
      brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, list.size());
   }

   private Optional<BlockPos> findNearestRepellent(ServerLevel pLevel, Hoglin pHoglin) {
      return BlockPos.findClosestMatch(pHoglin.blockPosition(), 8, 4, (p_186148_) -> {
         return pLevel.getBlockState(p_186148_).is(BlockTags.HOGLIN_REPELLENTS);
      });
   }
}