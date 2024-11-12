package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SignItem extends StandingAndWallBlockItem {
   public SignItem(Item.Properties pProperties, Block pStandingBlock, Block pWallBlock) {
      super(pStandingBlock, pWallBlock, pProperties, Direction.DOWN);
   }

   public SignItem(Item.Properties pProperties, Block pStandingBlock, Block pWallBlock, Direction pAttachmentDirection) {
      super(pStandingBlock, pWallBlock, pProperties, pAttachmentDirection);
   }

   protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState) {
      boolean flag = super.updateCustomBlockEntityTag(pPos, pLevel, pPlayer, pStack, pState);
      if (!pLevel.isClientSide && !flag && pPlayer != null) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof SignBlockEntity) {
            SignBlockEntity signblockentity = (SignBlockEntity)blockentity;
            Block block = pLevel.getBlockState(pPos).getBlock();
            if (block instanceof SignBlock) {
               SignBlock signblock = (SignBlock)block;
               signblock.openTextEdit(pPlayer, signblockentity, true);
            }
         }
      }

      return flag;
   }
}