package net.minecraft.world.entity.ai.behavior;

import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromLookTarget {
   public static OneShot<LivingEntity> create(float pSpeedModifier, int pCloseEnoughDist) {
      return create((p_182369_) -> {
         return true;
      }, (p_182364_) -> {
         return pSpeedModifier;
      }, pCloseEnoughDist);
   }

   public static OneShot<LivingEntity> create(Predicate<LivingEntity> pCanSetWalkTarget, Function<LivingEntity, Float> pSpeedModifier, int pCloseEnoughDist) {
      return BehaviorBuilder.create((p_258748_) -> {
         return p_258748_.group(p_258748_.absent(MemoryModuleType.WALK_TARGET), p_258748_.present(MemoryModuleType.LOOK_TARGET)).apply(p_258748_, (p_258743_, p_258744_) -> {
            return (p_258736_, p_258737_, p_258738_) -> {
               if (!pCanSetWalkTarget.test(p_258737_)) {
                  return false;
               } else {
                  p_258743_.set(new WalkTarget(p_258748_.get(p_258744_), pSpeedModifier.apply(p_258737_), pCloseEnoughDist));
                  return true;
               }
            };
         });
      });
   }
}