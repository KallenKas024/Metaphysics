package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;

public class AmphibiousNodeEvaluator extends WalkNodeEvaluator {
   private final boolean prefersShallowSwimming;
   private float oldWalkableCost;
   private float oldWaterBorderCost;

   public AmphibiousNodeEvaluator(boolean pPrefersShallowSwimming) {
      this.prefersShallowSwimming = pPrefersShallowSwimming;
   }

   public void prepare(PathNavigationRegion pLevel, Mob pMob) {
      super.prepare(pLevel, pMob);
      pMob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
      this.oldWalkableCost = pMob.getPathfindingMalus(BlockPathTypes.WALKABLE);
      pMob.setPathfindingMalus(BlockPathTypes.WALKABLE, 6.0F);
      this.oldWaterBorderCost = pMob.getPathfindingMalus(BlockPathTypes.WATER_BORDER);
      pMob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 4.0F);
   }

   /**
    * This method is called when all nodes have been processed and PathEntity is created.
    */
   public void done() {
      this.mob.setPathfindingMalus(BlockPathTypes.WALKABLE, this.oldWalkableCost);
      this.mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, this.oldWaterBorderCost);
      super.done();
   }

   public Node getStart() {
      return !this.mob.isInWater() ? super.getStart() : this.getStartNode(new BlockPos(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5D), Mth.floor(this.mob.getBoundingBox().minZ)));
   }

   public Target getGoal(double pX, double pY, double pZ) {
      return this.getTargetFromNode(this.getNode(Mth.floor(pX), Mth.floor(pY + 0.5D), Mth.floor(pZ)));
   }

   public int getNeighbors(Node[] pOutputArray, Node pNode) {
      int i = super.getNeighbors(pOutputArray, pNode);
      BlockPathTypes blockpathtypes = this.getCachedBlockType(this.mob, pNode.x, pNode.y + 1, pNode.z);
      BlockPathTypes blockpathtypes1 = this.getCachedBlockType(this.mob, pNode.x, pNode.y, pNode.z);
      int j;
      if (this.mob.getPathfindingMalus(blockpathtypes) >= 0.0F && blockpathtypes1 != BlockPathTypes.STICKY_HONEY) {
         j = Mth.floor(Math.max(1.0F, this.mob.getStepHeight()));
      } else {
         j = 0;
      }

      double d0 = this.getFloorLevel(new BlockPos(pNode.x, pNode.y, pNode.z));
      Node node = this.findAcceptedNode(pNode.x, pNode.y + 1, pNode.z, Math.max(0, j - 1), d0, Direction.UP, blockpathtypes1);
      Node node1 = this.findAcceptedNode(pNode.x, pNode.y - 1, pNode.z, j, d0, Direction.DOWN, blockpathtypes1);
      if (this.isVerticalNeighborValid(node, pNode)) {
         pOutputArray[i++] = node;
      }

      if (this.isVerticalNeighborValid(node1, pNode) && blockpathtypes1 != BlockPathTypes.TRAPDOOR) {
         pOutputArray[i++] = node1;
      }

      for(int k = 0; k < i; ++k) {
         Node node2 = pOutputArray[k];
         if (node2.type == BlockPathTypes.WATER && this.prefersShallowSwimming && node2.y < this.mob.level().getSeaLevel() - 10) {
            ++node2.costMalus;
         }
      }

      return i;
   }

   private boolean isVerticalNeighborValid(@Nullable Node pNeighbor, Node pNode) {
      return this.isNeighborValid(pNeighbor, pNode) && pNeighbor.type == BlockPathTypes.WATER;
   }

   protected boolean isAmphibious() {
      return true;
   }

   /**
    * Returns the node type at the specified postion taking the block below into account
    */
   public BlockPathTypes getBlockPathType(BlockGetter pLevel, int pX, int pY, int pZ) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      BlockPathTypes blockpathtypes = getBlockPathTypeRaw(pLevel, blockpos$mutableblockpos.set(pX, pY, pZ));
      if (blockpathtypes == BlockPathTypes.WATER) {
         for(Direction direction : Direction.values()) {
            BlockPathTypes blockpathtypes1 = getBlockPathTypeRaw(pLevel, blockpos$mutableblockpos.set(pX, pY, pZ).move(direction));
            if (blockpathtypes1 == BlockPathTypes.BLOCKED) {
               return BlockPathTypes.WATER_BORDER;
            }
         }

         return BlockPathTypes.WATER;
      } else {
         return getBlockPathTypeStatic(pLevel, blockpos$mutableblockpos);
      }
   }
}
