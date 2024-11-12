package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class AirRandomPos {
   @Nullable
   public static Vec3 getPosTowards(PathfinderMob pMob, int pRadius, int pYRange, int pY, Vec3 pVectorPosition, double pAmplifier) {
      Vec3 vec3 = pVectorPosition.subtract(pMob.getX(), pMob.getY(), pMob.getZ());
      boolean flag = GoalUtils.mobRestricted(pMob, pRadius);
      return RandomPos.generateRandomPos(pMob, () -> {
         BlockPos blockpos = AirAndWaterRandomPos.generateRandomPos(pMob, pRadius, pYRange, pY, vec3.x, vec3.z, pAmplifier, flag);
         return blockpos != null && !GoalUtils.isWater(pMob, blockpos) ? blockpos : null;
      });
   }
}