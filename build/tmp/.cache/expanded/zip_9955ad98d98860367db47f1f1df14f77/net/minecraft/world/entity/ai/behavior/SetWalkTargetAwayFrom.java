package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetAwayFrom {
   public static BehaviorControl<PathfinderMob> pos(MemoryModuleType<BlockPos> pWalkTargetAwayFromMemory, float pSpeedModifier, int pDesiredDistance, boolean pHasTarget) {
      return create(pWalkTargetAwayFromMemory, pSpeedModifier, pDesiredDistance, pHasTarget, Vec3::atBottomCenterOf);
   }

   public static OneShot<PathfinderMob> entity(MemoryModuleType<? extends Entity> pWalkTargetAwayFromMemory, float pSpeedModifier, int pDesiredDistance, boolean pHasTarget) {
      return create(pWalkTargetAwayFromMemory, pSpeedModifier, pDesiredDistance, pHasTarget, Entity::position);
   }

   private static <T> OneShot<PathfinderMob> create(MemoryModuleType<T> pWalkTargetAwayFromMemory, float pSpeedModifier, int pDesiredDistance, boolean pHasTarget, Function<T, Vec3> pToPosition) {
      return BehaviorBuilder.create((p_259292_) -> {
         return p_259292_.group(p_259292_.registered(MemoryModuleType.WALK_TARGET), p_259292_.present(pWalkTargetAwayFromMemory)).apply(p_259292_, (p_260063_, p_260053_) -> {
            return (p_259973_, p_259323_, p_259275_) -> {
               Optional<WalkTarget> optional = p_259292_.tryGet(p_260063_);
               if (optional.isPresent() && !pHasTarget) {
                  return false;
               } else {
                  Vec3 vec3 = p_259323_.position();
                  Vec3 vec31 = pToPosition.apply(p_259292_.get(p_260053_));
                  if (!vec3.closerThan(vec31, (double)pDesiredDistance)) {
                     return false;
                  } else {
                     if (optional.isPresent() && optional.get().getSpeedModifier() == pSpeedModifier) {
                        Vec3 vec32 = optional.get().getTarget().currentPosition().subtract(vec3);
                        Vec3 vec33 = vec31.subtract(vec3);
                        if (vec32.dot(vec33) < 0.0D) {
                           return false;
                        }
                     }

                     for(int i = 0; i < 10; ++i) {
                        Vec3 vec34 = LandRandomPos.getPosAway(p_259323_, 16, 7, vec31);
                        if (vec34 != null) {
                           p_260063_.set(new WalkTarget(vec34, pSpeedModifier, 0));
                           break;
                        }
                     }

                     return true;
                  }
               }
            };
         });
      });
   }
}