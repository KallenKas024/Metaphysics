package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public interface SimpleWaterloggedBlock extends BucketPickup, LiquidBlockContainer {
   default boolean canPlaceLiquid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
      return pFluid == Fluids.WATER;
   }

   default boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
      if (!pState.getValue(BlockStateProperties.WATERLOGGED) && pFluidState.getType() == Fluids.WATER) {
         if (!pLevel.isClientSide()) {
            pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 3);
            pLevel.scheduleTick(pPos, pFluidState.getType(), pFluidState.getType().getTickDelay(pLevel));
         }

         return true;
      } else {
         return false;
      }
   }

   default ItemStack pickupBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
      if (pState.getValue(BlockStateProperties.WATERLOGGED)) {
         pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)), 3);
         if (!pState.canSurvive(pLevel, pPos)) {
            pLevel.destroyBlock(pPos, true);
         }

         return new ItemStack(Items.WATER_BUCKET);
      } else {
         return ItemStack.EMPTY;
      }
   }

   default Optional<SoundEvent> getPickupSound() {
      return Fluids.WATER.getPickupSound();
   }
}