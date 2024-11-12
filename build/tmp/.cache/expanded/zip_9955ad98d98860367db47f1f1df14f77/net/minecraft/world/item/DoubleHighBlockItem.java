package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleHighBlockItem extends BlockItem {
   public DoubleHighBlockItem(Block pBlock, Item.Properties pProperties) {
      super(pBlock, pProperties);
   }

   protected boolean placeBlock(BlockPlaceContext pContext, BlockState pState) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos().above();
      BlockState blockstate = level.isWaterAt(blockpos) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
      level.setBlock(blockpos, blockstate, 27);
      return super.placeBlock(pContext, pState);
   }
}