package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class GoalUtils {
   public static boolean hasGroundPathNavigation(Mob pMob) {
      return pMob.getNavigation() instanceof GroundPathNavigation;
   }

   /**
    * @return if a mob is stuck, within a certain radius beyond it's restriction radius
    */
   public static boolean mobRestricted(PathfinderMob pMob, int pRadius) {
      return pMob.hasRestriction() && pMob.getRestrictCenter().closerToCenterThan(pMob.position(), (double)(pMob.getRestrictRadius() + (float)pRadius) + 1.0D);
   }

   /**
    * @return if a mob is above or below the map
    */
   public static boolean isOutsideLimits(BlockPos pPos, PathfinderMob pMob) {
      return pPos.getY() < pMob.level().getMinBuildHeight() || pPos.getY() > pMob.level().getMaxBuildHeight();
   }

   /**
    * @return if a mob is restricted. The first parameter short circuits the operation.
    */
   public static boolean isRestricted(boolean pShortCircuit, PathfinderMob pMob, BlockPos pPos) {
      return pShortCircuit && !pMob.isWithinRestriction(pPos);
   }

   /**
    * @return if the destination can't be pathfinded to
    */
   public static boolean isNotStable(PathNavigation pNavigation, BlockPos pPos) {
      return !pNavigation.isStableDestination(pPos);
   }

   /**
    * @return if the position is water in the mob's level
    */
   public static boolean isWater(PathfinderMob pMob, BlockPos pPos) {
      return pMob.level().getFluidState(pPos).is(FluidTags.WATER);
   }

   /**
    * @return if the pathfinding malus exists
    */
   public static boolean hasMalus(PathfinderMob pMob, BlockPos pPos) {
      return pMob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(pMob.level(), pPos.mutable())) != 0.0F;
   }

   /**
    * @return if the mob is standing on a solid material
    */
   public static boolean isSolid(PathfinderMob pMob, BlockPos pPos) {
      return pMob.level().getBlockState(pPos).isSolid();
   }
}