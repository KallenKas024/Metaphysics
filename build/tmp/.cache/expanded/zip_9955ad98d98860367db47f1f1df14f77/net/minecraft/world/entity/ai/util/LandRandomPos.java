package net.minecraft.world.entity.ai.util;

import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class LandRandomPos {
   @Nullable
   public static Vec3 getPos(PathfinderMob pMob, int pRadius, int pVerticalRange) {
      return getPos(pMob, pRadius, pVerticalRange, pMob::getWalkTargetValue);
   }

   @Nullable
   public static Vec3 getPos(PathfinderMob pMob, int pRadius, int pYRange, ToDoubleFunction<BlockPos> pToDoubleFunction) {
      boolean flag = GoalUtils.mobRestricted(pMob, pRadius);
      return RandomPos.generateRandomPos(() -> {
         BlockPos blockpos = RandomPos.generateRandomDirection(pMob.getRandom(), pRadius, pYRange);
         BlockPos blockpos1 = generateRandomPosTowardDirection(pMob, pRadius, flag, blockpos);
         return blockpos1 == null ? null : movePosUpOutOfSolid(pMob, blockpos1);
      }, pToDoubleFunction);
   }

   @Nullable
   public static Vec3 getPosTowards(PathfinderMob pMob, int pRadius, int pYRange, Vec3 pVectorPosition) {
      Vec3 vec3 = pVectorPosition.subtract(pMob.getX(), pMob.getY(), pMob.getZ());
      boolean flag = GoalUtils.mobRestricted(pMob, pRadius);
      return getPosInDirection(pMob, pRadius, pYRange, vec3, flag);
   }

   @Nullable
   public static Vec3 getPosAway(PathfinderMob pMob, int pRadius, int pYRange, Vec3 pVectorPosition) {
      Vec3 vec3 = pMob.position().subtract(pVectorPosition);
      boolean flag = GoalUtils.mobRestricted(pMob, pRadius);
      return getPosInDirection(pMob, pRadius, pYRange, vec3, flag);
   }

   @Nullable
   private static Vec3 getPosInDirection(PathfinderMob pMob, int pRadius, int pYRange, Vec3 pVectorPosition, boolean pShortCircuit) {
      return RandomPos.generateRandomPos(pMob, () -> {
         BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pMob.getRandom(), pRadius, pYRange, 0, pVectorPosition.x, pVectorPosition.z, (double)((float)Math.PI / 2F));
         if (blockpos == null) {
            return null;
         } else {
            BlockPos blockpos1 = generateRandomPosTowardDirection(pMob, pRadius, pShortCircuit, blockpos);
            return blockpos1 == null ? null : movePosUpOutOfSolid(pMob, blockpos1);
         }
      });
   }

   @Nullable
   public static BlockPos movePosUpOutOfSolid(PathfinderMob pMob, BlockPos pPos) {
      pPos = RandomPos.moveUpOutOfSolid(pPos, pMob.level().getMaxBuildHeight(), (p_148534_) -> {
         return GoalUtils.isSolid(pMob, p_148534_);
      });
      return !GoalUtils.isWater(pMob, pPos) && !GoalUtils.hasMalus(pMob, pPos) ? pPos : null;
   }

   @Nullable
   public static BlockPos generateRandomPosTowardDirection(PathfinderMob pMob, int pRadius, boolean pShortCircuit, BlockPos pPos) {
      BlockPos blockpos = RandomPos.generateRandomPosTowardDirection(pMob, pRadius, pMob.getRandom(), pPos);
      return !GoalUtils.isOutsideLimits(blockpos, pMob) && !GoalUtils.isRestricted(pShortCircuit, pMob, blockpos) && !GoalUtils.isNotStable(pMob.getNavigation(), blockpos) ? blockpos : null;
   }
}