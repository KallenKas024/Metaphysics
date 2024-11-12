package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractBannerBlock extends BaseEntityBlock {
   private final DyeColor color;

   protected AbstractBannerBlock(DyeColor pColor, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.color = pColor;
   }

   public boolean isPossibleToRespawnInThis(BlockState pState) {
      return true;
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new BannerBlockEntity(pPos, pState, this.color);
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      if (pLevel.isClientSide) {
         pLevel.getBlockEntity(pPos, BlockEntityType.BANNER).ifPresent((p_187404_) -> {
            p_187404_.fromItem(pStack);
         });
      } else if (pStack.hasCustomHoverName()) {
         pLevel.getBlockEntity(pPos, BlockEntityType.BANNER).ifPresent((p_187401_) -> {
            p_187401_.setCustomName(pStack.getHoverName());
         });
      }

   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      return blockentity instanceof BannerBlockEntity ? ((BannerBlockEntity)blockentity).getItem() : super.getCloneItemStack(pLevel, pPos, pState);
   }

   public DyeColor getColor() {
      return this.color;
   }
}