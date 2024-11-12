package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class FlyingPathNavigation extends PathNavigation {
   public FlyingPathNavigation(Mob pMob, Level pLevel) {
      super(pMob, pLevel);
   }

   protected PathFinder createPathFinder(int pMaxVisitedNodes) {
      this.nodeEvaluator = new FlyNodeEvaluator();
      this.nodeEvaluator.setCanPassDoors(true);
      return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
   }

   /**
    * Checks if the specified entity can safely walk to the specified location.
    */
   protected boolean canMoveDirectly(Vec3 pPosVec31, Vec3 pPosVec32) {
      return isClearForMovementBetween(this.mob, pPosVec31, pPosVec32, true);
   }

   /**
    * If on ground or swimming and can swim
    */
   protected boolean canUpdatePath() {
      return this.canFloat() && this.isInLiquid() || !this.mob.isPassenger();
   }

   protected Vec3 getTempMobPos() {
      return this.mob.position();
   }

   /**
    * Returns a path to the given entity or null
    */
   public Path createPath(Entity pEntity, int pAccuracy) {
      return this.createPath(pEntity.blockPosition(), pAccuracy);
   }

   public void tick() {
      ++this.tick;
      if (this.hasDelayedRecomputation) {
         this.recomputePath();
      }

      if (!this.isDone()) {
         if (this.canUpdatePath()) {
            this.followThePath();
         } else if (this.path != null && !this.path.isDone()) {
            Vec3 vec3 = this.path.getNextEntityPos(this.mob);
            if (this.mob.getBlockX() == Mth.floor(vec3.x) && this.mob.getBlockY() == Mth.floor(vec3.y) && this.mob.getBlockZ() == Mth.floor(vec3.z)) {
               this.path.advance();
            }
         }

         DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
         if (!this.isDone()) {
            Vec3 vec31 = this.path.getNextEntityPos(this.mob);
            this.mob.getMoveControl().setWantedPosition(vec31.x, vec31.y, vec31.z, this.speedModifier);
         }
      }
   }

   public void setCanOpenDoors(boolean pCanOpenDoors) {
      this.nodeEvaluator.setCanOpenDoors(pCanOpenDoors);
   }

   public boolean canPassDoors() {
      return this.nodeEvaluator.canPassDoors();
   }

   public void setCanPassDoors(boolean pCanEnterDoors) {
      this.nodeEvaluator.setCanPassDoors(pCanEnterDoors);
   }

   public boolean canOpenDoors() {
      return this.nodeEvaluator.canPassDoors();
   }

   public boolean isStableDestination(BlockPos pPos) {
      return this.level.getBlockState(pPos).entityCanStandOn(this.level, pPos, this.mob);
   }
}