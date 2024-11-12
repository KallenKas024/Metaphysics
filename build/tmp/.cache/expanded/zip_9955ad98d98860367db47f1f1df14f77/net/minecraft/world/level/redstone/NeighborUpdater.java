package net.minecraft.world.level.redstone;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface NeighborUpdater {
   Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

   void shapeUpdate(Direction pDirection, BlockState pState, BlockPos pPos, BlockPos pNeighborPos, int pFlags, int pRecursionLevel);

   void neighborChanged(BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos);

   void neighborChanged(BlockState pState, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston);

   default void updateNeighborsAtExceptFromFacing(BlockPos pPos, Block pBlock, @Nullable Direction pFacing) {
      for(Direction direction : UPDATE_ORDER) {
         if (direction != pFacing) {
            this.neighborChanged(pPos.relative(direction), pBlock, pPos);
         }
      }

   }

   static void executeShapeUpdate(LevelAccessor pLevel, Direction pDirection, BlockState pState, BlockPos pPos, BlockPos pNeighborPos, int pFlags, int pRecursionLevel) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      BlockState blockstate1 = blockstate.updateShape(pDirection, pState, pLevel, pPos, pNeighborPos);
      Block.updateOrDestroy(blockstate, blockstate1, pLevel, pPos, pFlags, pRecursionLevel);
   }

   static void executeUpdate(Level pLevel, BlockState pState, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
      try {
         pState.neighborChanged(pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being updated");
         crashreportcategory.setDetail("Source block type", () -> {
            try {
               return String.format(Locale.ROOT, "ID #%s (%s // %s)", BuiltInRegistries.BLOCK.getKey(pNeighborBlock), pNeighborBlock.getDescriptionId(), pNeighborBlock.getClass().getCanonicalName());
            } catch (Throwable throwable1) {
               return "ID #" + BuiltInRegistries.BLOCK.getKey(pNeighborBlock);
            }
         });
         CrashReportCategory.populateBlockDetails(crashreportcategory, pLevel, pPos, pState);
         throw new ReportedException(crashreport);
      }
   }
}