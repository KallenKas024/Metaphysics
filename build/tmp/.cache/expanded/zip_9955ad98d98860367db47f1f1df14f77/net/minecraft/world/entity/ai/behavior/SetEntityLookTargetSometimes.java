package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

/** @deprecated */
@Deprecated
public class SetEntityLookTargetSometimes {
   public static BehaviorControl<LivingEntity> create(float pMaxDist, UniformInt pInterval) {
      return create(pMaxDist, pInterval, (p_259715_) -> {
         return true;
      });
   }

   public static BehaviorControl<LivingEntity> create(EntityType<?> pEntityType, float pMaxDist, UniformInt pInterval) {
      return create(pMaxDist, pInterval, (p_289379_) -> {
         return pEntityType.equals(p_289379_.getType());
      });
   }

   private static BehaviorControl<LivingEntity> create(float pMaxDist, UniformInt pInterval, Predicate<LivingEntity> pCanLookAtTarget) {
      float f = pMaxDist * pMaxDist;
      SetEntityLookTargetSometimes.Ticker setentitylooktargetsometimes$ticker = new SetEntityLookTargetSometimes.Ticker(pInterval);
      return BehaviorBuilder.create((p_259288_) -> {
         return p_259288_.group(p_259288_.absent(MemoryModuleType.LOOK_TARGET), p_259288_.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(p_259288_, (p_259350_, p_260134_) -> {
            return (p_264952_, p_264953_, p_264954_) -> {
               Optional<LivingEntity> optional = p_259288_.<NearestVisibleLivingEntities>get(p_260134_).findClosest(pCanLookAtTarget.and((p_259358_) -> {
                  return p_259358_.distanceToSqr(p_264953_) <= (double)f;
               }));
               if (optional.isEmpty()) {
                  return false;
               } else if (!setentitylooktargetsometimes$ticker.tickDownAndCheck(p_264952_.random)) {
                  return false;
               } else {
                  p_259350_.set(new EntityTracker(optional.get(), true));
                  return true;
               }
            };
         });
      });
   }

   public static final class Ticker {
      private final UniformInt interval;
      private int ticksUntilNextStart;

      public Ticker(UniformInt pInterval) {
         if (pInterval.getMinValue() <= 1) {
            throw new IllegalArgumentException();
         } else {
            this.interval = pInterval;
         }
      }

      public boolean tickDownAndCheck(RandomSource pRandom) {
         if (this.ticksUntilNextStart == 0) {
            this.ticksUntilNextStart = this.interval.sample(pRandom) - 1;
            return false;
         } else {
            return --this.ticksUntilNextStart == 0;
         }
      }
   }
}