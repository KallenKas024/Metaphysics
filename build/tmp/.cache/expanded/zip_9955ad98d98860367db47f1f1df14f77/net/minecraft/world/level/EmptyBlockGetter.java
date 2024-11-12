package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public enum EmptyBlockGetter implements BlockGetter {
   INSTANCE;

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pPos) {
      return null;
   }

   public BlockState getBlockState(BlockPos pPos) {
      return Blocks.AIR.defaultBlockState();
   }

   public FluidState getFluidState(BlockPos pPos) {
      return Fluids.EMPTY.defaultFluidState();
   }

   public int getMinBuildHeight() {
      return 0;
   }

   public int getHeight() {
      return 0;
   }
}