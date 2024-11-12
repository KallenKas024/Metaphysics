package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class HoverRandomPos {
   /**
    * Tries to generate a random position a couple different ways, and if failing, sees if swimming vertically is an
    * option.
    */
   @Nullable
   public static Vec3 getPos(PathfinderMob pMob, int pRadius, int pYRange, double pX, double pZ, float pAmplifier, int pMaxSwimUp, int pMinSwimUp) {
      boolean flag = GoalUtils.mobRestricted(pMob, pRadius);
      return RandomPos.generateRandomPos(pMob, () -> {
         BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pMob.getRandom(), pRadius, pYRange, 0, pX, pZ, (double)pAmplifier);
         if (blockpos == null) {
            return null;
         } else {
            BlockPos blockpos1 = LandRandomPos.generateRandomPosTowardDirection(pMob, pRadius, flag, blockpos);
            if (blockpos1 == null) {
               return null;
            } else {
               blockpos1 = RandomPos.moveUpToAboveSolid(blockpos1, pMob.getRandom().nextInt(pMaxSwimUp - pMinSwimUp + 1) + pMinSwimUp, pMob.level().getMaxBuildHeight(), (p_148486_) -> {
                  return GoalUtils.isSolid(pMob, p_148486_);
               });
               return !GoalUtils.isWater(pMob, blockpos1) && !GoalUtils.hasMalus(pMob, blockpos1) ? blockpos1 : null;
            }
         }
      });
   }
}