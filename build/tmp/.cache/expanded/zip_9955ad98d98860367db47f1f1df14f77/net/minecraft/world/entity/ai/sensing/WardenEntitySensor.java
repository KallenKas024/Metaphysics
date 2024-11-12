package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenEntitySensor extends NearestLivingEntitySensor<Warden> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.copyOf(Iterables.concat(super.requires(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
   }

   protected void doTick(ServerLevel pLevel, Warden pEntity) {
      super.doTick(pLevel, pEntity);
      getClosest(pEntity, (p_289409_) -> {
         return p_289409_.getType() == EntityType.PLAYER;
      }).or(() -> {
         return getClosest(pEntity, (p_289408_) -> {
            return p_289408_.getType() != EntityType.PLAYER;
         });
      }).ifPresentOrElse((p_217841_) -> {
         pEntity.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, p_217841_);
      }, () -> {
         pEntity.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE);
      });
   }

   private static Optional<LivingEntity> getClosest(Warden pWarden, Predicate<LivingEntity> pPredicate) {
      return pWarden.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).stream().flatMap(Collection::stream).filter(pWarden::canTargetEntity).filter(pPredicate).findFirst();
   }

   protected int radiusXZ() {
      return 24;
   }

   protected int radiusY() {
      return 24;
   }
}