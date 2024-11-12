package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class WaterBoundPathNavigation extends PathNavigation {
   private boolean allowBreaching;

   public WaterBoundPathNavigation(Mob pMob, Level pLevel) {
      super(pMob, pLevel);
   }

   protected PathFinder createPathFinder(int pMaxVisitedNodes) {
      this.allowBreaching = this.mob.getType() == EntityType.DOLPHIN;
      this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching);
      return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
   }

   /**
    * If on ground or swimming and can swim
    */
   protected boolean canUpdatePath() {
      return this.allowBreaching || this.isInLiquid();
   }

   protected Vec3 getTempMobPos() {
      return new Vec3(this.mob.getX(), this.mob.getY(0.5D), this.mob.getZ());
   }

   protected double getGroundY(Vec3 pVec) {
      return pVec.y;
   }

   /**
    * Checks if the specified entity can safely walk to the specified location.
    */
   protected boolean canMoveDirectly(Vec3 pPosVec31, Vec3 pPosVec32) {
      return isClearForMovementBetween(this.mob, pPosVec31, pPosVec32, false);
   }

   public boolean isStableDestination(BlockPos pPos) {
      return !this.level.getBlockState(pPos).isSolidRender(this.level, pPos);
   }

   public void setCanFloat(boolean pCanSwim) {
   }
}