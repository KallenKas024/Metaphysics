package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class DefaultRandomPos {
   @Nullable
   public static Vec3 getPos(PathfinderMob pMob, int pRadius, int pVerticalDistance) {
      boolean flag = GoalUtils.mobRestricted(pMob, pRadius);
      return RandomPos.generateRandomPos(pMob, () -> {
         BlockPos blockpos = RandomPos.generateRandomDirection(pMob.getRandom(), pRadius, pVerticalDistance);
         return generateRandomPosTowardDirection(pMob, pRadius, flag, blockpos);
      });
   }

   @Nullable
   public static Vec3 getPosTowards(PathfinderMob pMob, int pRadius, int pYRange, Vec3 pVectorPosition, double pAmplifier) {
      Vec3 vec3 = pVectorPosition.subtract(pMob.getX(), pMob.getY(), pMob.getZ());
      boolean flag = GoalUtils.mobRestricted(pMob, pRadius);
      return RandomPos.generateRandomPos(pMob, () -> {
         BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pMob.getRandom(), pRadius, pYRange, 0, vec3.x, vec3.z, pAmplifier);
         return blockpos == null ? null : generateRandomPosTowardDirection(pMob, pRadius, flag, blockpos);
      });
   }

   @Nullable
   public static Vec3 getPosAway(PathfinderMob pMob, int pRadius, int pYRange, Vec3 pVectorPosition) {
      Vec3 vec3 = pMob.position().subtract(pVectorPosition);
      boolean flag = GoalUtils.mobRestricted(pMob, pRadius);
      return RandomPos.generateRandomPos(pMob, () -> {
         BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pMob.getRandom(), pRadius, pYRange, 0, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
         return blockpos == null ? null : generateRandomPosTowardDirection(pMob, pRadius, flag, blockpos);
      });
   }

   @Nullable
   private static BlockPos generateRandomPosTowardDirection(PathfinderMob pMob, int pRadius, boolean pShortCircuit, BlockPos pPos) {
      BlockPos blockpos = RandomPos.generateRandomPosTowardDirection(pMob, pRadius, pMob.getRandom(), pPos);
      return !GoalUtils.isOutsideLimits(blockpos, pMob) && !GoalUtils.isRestricted(pShortCircuit, pMob, blockpos) && !GoalUtils.isNotStable(pMob.getNavigation(), blockpos) && !GoalUtils.hasMalus(pMob, blockpos) ? blockpos : null;
   }
}