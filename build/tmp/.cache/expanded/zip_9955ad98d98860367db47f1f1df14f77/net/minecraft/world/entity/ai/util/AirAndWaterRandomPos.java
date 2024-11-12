package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class AirAndWaterRandomPos {
   @Nullable
   public static Vec3 getPos(PathfinderMob pMob, int pMaxDistance, int pYRange, int pY, double pX, double pZ, double pAmplifier) {
      boolean flag = GoalUtils.mobRestricted(pMob, pMaxDistance);
      return RandomPos.generateRandomPos(pMob, () -> {
         return generateRandomPos(pMob, pMaxDistance, pYRange, pY, pX, pZ, pAmplifier, flag);
      });
   }

   @Nullable
   public static BlockPos generateRandomPos(PathfinderMob pMob, int pMaxDistance, int pYRange, int pY, double pX, double pZ, double pAmplifier, boolean pShortCircuit) {
      BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pMob.getRandom(), pMaxDistance, pYRange, pY, pX, pZ, pAmplifier);
      if (blockpos == null) {
         return null;
      } else {
         BlockPos blockpos1 = RandomPos.generateRandomPosTowardDirection(pMob, pMaxDistance, pMob.getRandom(), blockpos);
         if (!GoalUtils.isOutsideLimits(blockpos1, pMob) && !GoalUtils.isRestricted(pShortCircuit, pMob, blockpos1)) {
            blockpos1 = RandomPos.moveUpOutOfSolid(blockpos1, pMob.level().getMaxBuildHeight(), (p_148376_) -> {
               return GoalUtils.isSolid(pMob, p_148376_);
            });
            return GoalUtils.hasMalus(pMob, blockpos1) ? null : blockpos1;
         } else {
            return null;
         }
      }
   }
}