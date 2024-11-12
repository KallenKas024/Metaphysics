package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith {
   public static <T extends LivingEntity> BehaviorControl<LivingEntity> of(EntityType<? extends T> pType, int pInteractionRange, MemoryModuleType<T> pInteractMemory, float pSpeedModifier, int pMaxDist) {
      return of(pType, pInteractionRange, (p_23287_) -> {
         return true;
      }, (p_23285_) -> {
         return true;
      }, pInteractMemory, pSpeedModifier, pMaxDist);
   }

   public static <E extends LivingEntity, T extends LivingEntity> BehaviorControl<E> of(EntityType<? extends T> pType, int pInteractionRange, Predicate<E> pSelfFilter, Predicate<T> pTargetFilter, MemoryModuleType<T> pMemory, float pSpeedModifier, int pMaxDist) {
      int i = pInteractionRange * pInteractionRange;
      Predicate<LivingEntity> predicate = (p_289327_) -> {
         return pType.equals(p_289327_.getType()) && pTargetFilter.test((T)p_289327_);
      };
      return BehaviorBuilder.create((p_258426_) -> {
         return p_258426_.group(p_258426_.registered(pMemory), p_258426_.registered(MemoryModuleType.LOOK_TARGET), p_258426_.absent(MemoryModuleType.WALK_TARGET), p_258426_.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(p_258426_, (p_258439_, p_258440_, p_258441_, p_258442_) -> {
            return (p_258413_, p_258414_, p_258415_) -> {
               NearestVisibleLivingEntities nearestvisiblelivingentities = p_258426_.get(p_258442_);
               if (pSelfFilter.test(p_258414_) && nearestvisiblelivingentities.contains(predicate)) {
                  Optional<LivingEntity> optional = nearestvisiblelivingentities.findClosest((p_258419_) -> {
                     return p_258419_.distanceToSqr(p_258414_) <= (double)i && predicate.test(p_258419_);
                  });
                  optional.ifPresent((p_258432_) -> {
                     p_258439_.set((T)p_258432_);
                     p_258440_.set(new EntityTracker(p_258432_, true));
                     p_258441_.set(new WalkTarget(new EntityTracker(p_258432_, false), pSpeedModifier, pMaxDist));
                  });
                  return true;
               } else {
                  return false;
               }
            };
         });
      });
   }
}