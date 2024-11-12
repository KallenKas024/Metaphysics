package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class StayCloseToTarget {
   public static BehaviorControl<LivingEntity> create(Function<LivingEntity, Optional<PositionTracker>> pTargetPositionGetter, Predicate<LivingEntity> pPredicate, int pCloseEnoughDist, int pTooClose, float pSpeedModifier) {
      return BehaviorBuilder.create((p_272460_) -> {
         return p_272460_.group(p_272460_.registered(MemoryModuleType.LOOK_TARGET), p_272460_.registered(MemoryModuleType.WALK_TARGET)).apply(p_272460_, (p_272466_, p_272467_) -> {
            return (p_260054_, p_260069_, p_259517_) -> {
               Optional<PositionTracker> optional = pTargetPositionGetter.apply(p_260069_);
               if (!optional.isEmpty() && pPredicate.test(p_260069_)) {
                  PositionTracker positiontracker = optional.get();
                  if (p_260069_.position().closerThan(positiontracker.currentPosition(), (double)pTooClose)) {
                     return false;
                  } else {
                     PositionTracker positiontracker1 = optional.get();
                     p_272466_.set(positiontracker1);
                     p_272467_.set(new WalkTarget(positiontracker1, pSpeedModifier, pCloseEnoughDist));
                     return true;
                  }
               } else {
                  return false;
               }
            };
         });
      });
   }
}