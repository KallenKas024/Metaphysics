package net.minecraft.world.phys;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockHitResult extends HitResult {
   private final Direction direction;
   private final BlockPos blockPos;
   private final boolean miss;
   private final boolean inside;

   /**
    * Creates a new BlockRayTraceResult marked as a miss.
    */
   public static BlockHitResult miss(Vec3 pLocation, Direction pDirection, BlockPos pPos) {
      return new BlockHitResult(true, pLocation, pDirection, pPos, false);
   }

   public BlockHitResult(Vec3 pLocation, Direction pDirection, BlockPos pBlockPos, boolean pInside) {
      this(false, pLocation, pDirection, pBlockPos, pInside);
   }

   private BlockHitResult(boolean pMiss, Vec3 pLocation, Direction pDirection, BlockPos pBlockPos, boolean pInside) {
      super(pLocation);
      this.miss = pMiss;
      this.direction = pDirection;
      this.blockPos = pBlockPos;
      this.inside = pInside;
   }

   /**
    * Creates a new BlockRayTraceResult, with the clicked face replaced with the given one
    */
   public BlockHitResult withDirection(Direction pNewFace) {
      return new BlockHitResult(this.miss, this.location, pNewFace, this.blockPos, this.inside);
   }

   public BlockHitResult withPosition(BlockPos pPos) {
      return new BlockHitResult(this.miss, this.location, this.direction, pPos, this.inside);
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   /**
    * Gets the face of the block that was clicked
    */
   public Direction getDirection() {
      return this.direction;
   }

   public HitResult.Type getType() {
      return this.miss ? HitResult.Type.MISS : HitResult.Type.BLOCK;
   }

   /**
    * @return {@code true} if the player's head is inside a block (used by scaffolding)
    */
   public boolean isInside() {
      return this.inside;
   }
}