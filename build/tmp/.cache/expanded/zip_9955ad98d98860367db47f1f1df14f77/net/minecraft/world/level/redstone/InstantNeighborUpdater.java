package net.minecraft.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class InstantNeighborUpdater implements NeighborUpdater {
   private final Level level;

   public InstantNeighborUpdater(Level pLevel) {
      this.level = pLevel;
   }

   public void shapeUpdate(Direction pDirection, BlockState pState, BlockPos pPos, BlockPos pNeighborPos, int pFlags, int pRecursionLevel) {
      NeighborUpdater.executeShapeUpdate(this.level, pDirection, pState, pPos, pNeighborPos, pFlags, pRecursionLevel - 1);
   }

   public void neighborChanged(BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos) {
      BlockState blockstate = this.level.getBlockState(pPos);
      this.neighborChanged(blockstate, pPos, pNeighborBlock, pNeighborPos, false);
   }

   public void neighborChanged(BlockState pState, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
      NeighborUpdater.executeUpdate(this.level, pState, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
   }
}