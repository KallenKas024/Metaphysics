package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SandBlock extends FallingBlock {
   private final int dustColor;

   public SandBlock(int pDustColor, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.dustColor = pDustColor;
   }

   public int getDustColor(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return this.dustColor;
   }
}