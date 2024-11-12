package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStroll {
   private static final int MAX_XZ_DIST = 10;
   private static final int MAX_Y_DIST = 7;
   private static final int[][] SWIM_XY_DISTANCE_TIERS = new int[][]{{1, 1}, {3, 3}, {5, 5}, {6, 5}, {7, 7}, {10, 7}};

   public static OneShot<PathfinderMob> stroll(float pSpeedModifier) {
      return stroll(pSpeedModifier, true);
   }

   public static OneShot<PathfinderMob> stroll(float pSpeedModifier, boolean pMayStrollFromWater) {
      return strollFlyOrSwim(pSpeedModifier, (p_258601_) -> {
         return LandRandomPos.getPos(p_258601_, 10, 7);
      }, pMayStrollFromWater ? (p_258615_) -> {
         return true;
      } : (p_289370_) -> {
         return !p_289370_.isInWaterOrBubble();
      });
   }

   public static BehaviorControl<PathfinderMob> stroll(float pSpeedModifier, int pMaxHorizontalDistance, int pMaxVerticalDistance) {
      return strollFlyOrSwim(pSpeedModifier, (p_258605_) -> {
         return LandRandomPos.getPos(p_258605_, pMaxHorizontalDistance, pMaxVerticalDistance);
      }, (p_258616_) -> {
         return true;
      });
   }

   public static BehaviorControl<PathfinderMob> fly(float pSpeedModifier) {
      return strollFlyOrSwim(pSpeedModifier, (p_258614_) -> {
         return getTargetFlyPos(p_258614_, 10, 7);
      }, (p_258602_) -> {
         return true;
      });
   }

   public static BehaviorControl<PathfinderMob> swim(float pSpeedModifier) {
      return strollFlyOrSwim(pSpeedModifier, RandomStroll::getTargetSwimPos, Entity::isInWaterOrBubble);
   }

   private static OneShot<PathfinderMob> strollFlyOrSwim(float pSpeedModifier, Function<PathfinderMob, Vec3> pTarget, Predicate<PathfinderMob> pCanStroll) {
      return BehaviorBuilder.create((p_258620_) -> {
         return p_258620_.group(p_258620_.absent(MemoryModuleType.WALK_TARGET)).apply(p_258620_, (p_258600_) -> {
            return (p_258610_, p_258611_, p_258612_) -> {
               if (!pCanStroll.test(p_258611_)) {
                  return false;
               } else {
                  Optional<Vec3> optional = Optional.ofNullable(pTarget.apply(p_258611_));
                  p_258600_.setOrErase(optional.map((p_258622_) -> {
                     return new WalkTarget(p_258622_, pSpeedModifier, 0);
                  }));
                  return true;
               }
            };
         });
      });
   }

   @Nullable
   private static Vec3 getTargetSwimPos(PathfinderMob p_259491_) {
      Vec3 vec3 = null;
      Vec3 vec31 = null;

      for(int[] aint : SWIM_XY_DISTANCE_TIERS) {
         if (vec3 == null) {
            vec31 = BehaviorUtils.getRandomSwimmablePos(p_259491_, aint[0], aint[1]);
         } else {
            vec31 = p_259491_.position().add(p_259491_.position().vectorTo(vec3).normalize().multiply((double)aint[0], (double)aint[1], (double)aint[0]));
         }

         if (vec31 == null || p_259491_.level().getFluidState(BlockPos.containing(vec31)).isEmpty()) {
            return vec3;
         }

         vec3 = vec31;
      }

      return vec31;
   }

   @Nullable
   private static Vec3 getTargetFlyPos(PathfinderMob pMob, int pMaxDistance, int pYRange) {
      Vec3 vec3 = pMob.getViewVector(0.0F);
      return AirAndWaterRandomPos.getPos(pMob, pMaxDistance, pYRange, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
   }
}