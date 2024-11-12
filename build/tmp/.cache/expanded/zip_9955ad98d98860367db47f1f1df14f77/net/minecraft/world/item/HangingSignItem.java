package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignItem extends SignItem {
   public HangingSignItem(Block pBlock, Block pWallBlock, Item.Properties pProperties) {
      super(pProperties, pBlock, pWallBlock, Direction.UP);
   }

   protected boolean canPlace(LevelReader pLevel, BlockState pState, BlockPos pPos) {
      Block block = pState.getBlock();
      if (block instanceof WallHangingSignBlock wallhangingsignblock) {
         if (!wallhangingsignblock.canPlace(pState, pLevel, pPos)) {
            return false;
         }
      }

      return super.canPlace(pLevel, pState, pPos);
   }
}