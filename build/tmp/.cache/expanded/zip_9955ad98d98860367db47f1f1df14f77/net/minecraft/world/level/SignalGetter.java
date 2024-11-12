package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface SignalGetter extends BlockGetter {
   Direction[] DIRECTIONS = Direction.values();

   /**
    * Returns the direct redstone signal emitted from the given position in the given direction.
    * 
    * <p>
    * NOTE: directions in redstone signal related methods are backwards, so this method
    * checks for the signal emitted in the <i>opposite</i> direction of the one given.
    */
   default int getDirectSignal(BlockPos pPos, Direction pDirection) {
      return this.getBlockState(pPos).getDirectSignal(this, pPos, pDirection);
   }

   /**
    * Returns the direct redstone signal the given position receives from neighboring blocks.
    */
   default int getDirectSignalTo(BlockPos pPos) {
      int i = 0;
      i = Math.max(i, this.getDirectSignal(pPos.below(), Direction.DOWN));
      if (i >= 15) {
         return i;
      } else {
         i = Math.max(i, this.getDirectSignal(pPos.above(), Direction.UP));
         if (i >= 15) {
            return i;
         } else {
            i = Math.max(i, this.getDirectSignal(pPos.north(), Direction.NORTH));
            if (i >= 15) {
               return i;
            } else {
               i = Math.max(i, this.getDirectSignal(pPos.south(), Direction.SOUTH));
               if (i >= 15) {
                  return i;
               } else {
                  i = Math.max(i, this.getDirectSignal(pPos.west(), Direction.WEST));
                  if (i >= 15) {
                     return i;
                  } else {
                     i = Math.max(i, this.getDirectSignal(pPos.east(), Direction.EAST));
                     return i >= 15 ? i : i;
                  }
               }
            }
         }
      }
   }

   /**
    * Returns the control signal emitted from the given position in the given direction.
    * If {@code diodesOnly} is {@code true}, this method returns the direct signal emitted if
    * and only if this position is occupied by a diode (i.e. a repeater or comparator).
    * Otherwise, if this position is occupied by a
    * {@linkplain net.minecraft.world.level.block.Blocks#REDSTONE_BLOCK redstone block},
    * this method will return the redstone signal emitted by it. If not, this method will
    * return the direct signal emitted from this position in the given direction.
    * 
    * <p>
    * NOTE: directions in redstone signal related methods are backwards, so this method
    * checks for the signal emitted in the <i>opposite</i> direction of the one given.
    */
   default int getControlInputSignal(BlockPos pPos, Direction pDirection, boolean pDiodesOnly) {
      BlockState blockstate = this.getBlockState(pPos);
      if (pDiodesOnly) {
         return DiodeBlock.isDiode(blockstate) ? this.getDirectSignal(pPos, pDirection) : 0;
      } else if (blockstate.is(Blocks.REDSTONE_BLOCK)) {
         return 15;
      } else if (blockstate.is(Blocks.REDSTONE_WIRE)) {
         return blockstate.getValue(RedStoneWireBlock.POWER);
      } else {
         return blockstate.isSignalSource() ? this.getDirectSignal(pPos, pDirection) : 0;
      }
   }

   /**
    * Returns whether a redstone signal is emitted from the given position in the given direction.
    * 
    * <p>
    * NOTE: directions in redstone signal related methods are backwards, so this method
    * checks for the signal emitted in the <i>opposite</i> direction of the one given.
    */
   default boolean hasSignal(BlockPos pPos, Direction pDirection) {
      return this.getSignal(pPos, pDirection) > 0;
   }

   /**
    * Returns the redstone signal emitted from the given position in the given direction.
    * This is the highest value between the signal emitted by the block itself, and the direct signal
    * received from neighboring blocks if the block is a redstone conductor.
    * 
    * <p>
    * NOTE: directions in redstone signal related methods are backwards, so this method
    * checks for the signal emitted in the <i>opposite</i> direction of the one given.
    */
   default int getSignal(BlockPos pPos, Direction pDirection) {
      BlockState blockstate = this.getBlockState(pPos);
      int i = blockstate.getSignal(this, pPos, pDirection);
      return blockstate.shouldCheckWeakPower(this, pPos, pDirection) ? Math.max(i, this.getDirectSignalTo(pPos)) : i;
   }

   /**
    * Returns whether the given position receives any redstone signal from neighboring blocks.
    */
   default boolean hasNeighborSignal(BlockPos pPos) {
      if (this.getSignal(pPos.below(), Direction.DOWN) > 0) {
         return true;
      } else if (this.getSignal(pPos.above(), Direction.UP) > 0) {
         return true;
      } else if (this.getSignal(pPos.north(), Direction.NORTH) > 0) {
         return true;
      } else if (this.getSignal(pPos.south(), Direction.SOUTH) > 0) {
         return true;
      } else if (this.getSignal(pPos.west(), Direction.WEST) > 0) {
         return true;
      } else {
         return this.getSignal(pPos.east(), Direction.EAST) > 0;
      }
   }

   /**
    * Returns the highest redstone signal the given position receives from neighboring blocks.
    */
   default int getBestNeighborSignal(BlockPos pPos) {
      int i = 0;

      for(Direction direction : DIRECTIONS) {
         int j = this.getSignal(pPos.relative(direction), direction);
         if (j >= 15) {
            return 15;
         }

         if (j > i) {
            i = j;
         }
      }

      return i;
   }
}
