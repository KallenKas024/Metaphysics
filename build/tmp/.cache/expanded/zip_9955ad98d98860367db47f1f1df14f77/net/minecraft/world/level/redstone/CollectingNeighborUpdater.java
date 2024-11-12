package net.minecraft.world.level.redstone;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class CollectingNeighborUpdater implements NeighborUpdater {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Level level;
   private final int maxChainedNeighborUpdates;
   private final ArrayDeque<CollectingNeighborUpdater.NeighborUpdates> stack = new ArrayDeque<>();
   private final List<CollectingNeighborUpdater.NeighborUpdates> addedThisLayer = new ArrayList<>();
   private int count = 0;

   public CollectingNeighborUpdater(Level pLevel, int pMaxChainedNeighborUpdates) {
      this.level = pLevel;
      this.maxChainedNeighborUpdates = pMaxChainedNeighborUpdates;
   }

   public void shapeUpdate(Direction pDirection, BlockState pState, BlockPos pPos, BlockPos pNeighborPos, int pFlags, int pRecursionLevel) {
      this.addAndRun(pPos, new CollectingNeighborUpdater.ShapeUpdate(pDirection, pState, pPos.immutable(), pNeighborPos.immutable(), pFlags, pRecursionLevel));
   }

   public void neighborChanged(BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos) {
      this.addAndRun(pPos, new CollectingNeighborUpdater.SimpleNeighborUpdate(pPos, pNeighborBlock, pNeighborPos.immutable()));
   }

   public void neighborChanged(BlockState pState, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
      this.addAndRun(pPos, new CollectingNeighborUpdater.FullNeighborUpdate(pState, pPos.immutable(), pNeighborBlock, pNeighborPos.immutable(), pMovedByPiston));
   }

   public void updateNeighborsAtExceptFromFacing(BlockPos pPos, Block pBlock, @Nullable Direction pFacing) {
      this.addAndRun(pPos, new CollectingNeighborUpdater.MultiNeighborUpdate(pPos.immutable(), pBlock, pFacing));
   }

   private void addAndRun(BlockPos pPos, CollectingNeighborUpdater.NeighborUpdates pUpdates) {
      boolean flag = this.count > 0;
      boolean flag1 = this.maxChainedNeighborUpdates >= 0 && this.count >= this.maxChainedNeighborUpdates;
      ++this.count;
      if (!flag1) {
         if (flag) {
            this.addedThisLayer.add(pUpdates);
         } else {
            this.stack.push(pUpdates);
         }
      } else if (this.count - 1 == this.maxChainedNeighborUpdates) {
         LOGGER.error("Too many chained neighbor updates. Skipping the rest. First skipped position: " + pPos.toShortString());
      }

      if (!flag) {
         this.runUpdates();
      }

   }

   private void runUpdates() {
      try {
         while(!this.stack.isEmpty() || !this.addedThisLayer.isEmpty()) {
            for(int i = this.addedThisLayer.size() - 1; i >= 0; --i) {
               this.stack.push(this.addedThisLayer.get(i));
            }

            this.addedThisLayer.clear();
            CollectingNeighborUpdater.NeighborUpdates collectingneighborupdater$neighborupdates = this.stack.peek();

            while(this.addedThisLayer.isEmpty()) {
               if (!collectingneighborupdater$neighborupdates.runNext(this.level)) {
                  this.stack.pop();
                  break;
               }
            }
         }
      } finally {
         this.stack.clear();
         this.addedThisLayer.clear();
         this.count = 0;
      }

   }

   static record FullNeighborUpdate(BlockState state, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) implements CollectingNeighborUpdater.NeighborUpdates {
      public boolean runNext(Level p_230683_) {
         NeighborUpdater.executeUpdate(p_230683_, this.state, this.pos, this.block, this.neighborPos, this.movedByPiston);
         return false;
      }
   }

   static final class MultiNeighborUpdate implements CollectingNeighborUpdater.NeighborUpdates {
      private final BlockPos sourcePos;
      private final Block sourceBlock;
      @Nullable
      private final Direction skipDirection;
      private int idx = 0;

      MultiNeighborUpdate(BlockPos pSourcePos, Block pSourceBlock, @Nullable Direction pSkipDirection) {
         this.sourcePos = pSourcePos;
         this.sourceBlock = pSourceBlock;
         this.skipDirection = pSkipDirection;
         if (NeighborUpdater.UPDATE_ORDER[this.idx] == pSkipDirection) {
            ++this.idx;
         }

      }

      public boolean runNext(Level pLevel) {
         BlockPos blockpos = this.sourcePos.relative(NeighborUpdater.UPDATE_ORDER[this.idx++]);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         blockstate.neighborChanged(pLevel, blockpos, this.sourceBlock, this.sourcePos, false);
         if (this.idx < NeighborUpdater.UPDATE_ORDER.length && NeighborUpdater.UPDATE_ORDER[this.idx] == this.skipDirection) {
            ++this.idx;
         }

         return this.idx < NeighborUpdater.UPDATE_ORDER.length;
      }
   }

   interface NeighborUpdates {
      boolean runNext(Level pLevel);
   }

   static record ShapeUpdate(Direction direction, BlockState state, BlockPos pos, BlockPos neighborPos, int updateFlags, int updateLimit) implements CollectingNeighborUpdater.NeighborUpdates {
      public boolean runNext(Level p_230716_) {
         NeighborUpdater.executeShapeUpdate(p_230716_, this.direction, this.state, this.pos, this.neighborPos, this.updateFlags, this.updateLimit);
         return false;
      }
   }

   static record SimpleNeighborUpdate(BlockPos pos, Block block, BlockPos neighborPos) implements CollectingNeighborUpdater.NeighborUpdates {
      public boolean runNext(Level p_230734_) {
         BlockState blockstate = p_230734_.getBlockState(this.pos);
         NeighborUpdater.executeUpdate(p_230734_, blockstate, this.pos, this.block, this.neighborPos, false);
         return false;
      }
   }
}