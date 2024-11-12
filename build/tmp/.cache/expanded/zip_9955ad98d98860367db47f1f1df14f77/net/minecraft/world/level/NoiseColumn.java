package net.minecraft.world.level;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public final class NoiseColumn implements BlockColumn {
   private final int minY;
   private final BlockState[] column;

   public NoiseColumn(int pMinY, BlockState[] pColumn) {
      this.minY = pMinY;
      this.column = pColumn;
   }

   public BlockState getBlock(int pPos) {
      int i = pPos - this.minY;
      return i >= 0 && i < this.column.length ? this.column[i] : Blocks.AIR.defaultBlockState();
   }

   public void setBlock(int pPos, BlockState pState) {
      int i = pPos - this.minY;
      if (i >= 0 && i < this.column.length) {
         this.column[i] = pState;
      } else {
         throw new IllegalArgumentException("Outside of column height: " + pPos);
      }
   }
}