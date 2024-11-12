package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KelpBlock extends GrowingPlantHeadBlock implements LiquidBlockContainer {
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D);
   private static final double GROW_PER_TICK_PROBABILITY = 0.14D;

   public KelpBlock(BlockBehaviour.Properties p_54300_) {
      super(p_54300_, Direction.UP, SHAPE, true, 0.14D);
   }

   protected boolean canGrowInto(BlockState pState) {
      return pState.is(Blocks.WATER);
   }

   protected Block getBodyBlock() {
      return Blocks.KELP_PLANT;
   }

   protected boolean canAttachTo(BlockState pState) {
      return !pState.is(Blocks.MAGMA_BLOCK);
   }

   public boolean canPlaceLiquid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
      return false;
   }

   public boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
      return false;
   }

   protected int getBlocksToGrowWhenBonemealed(RandomSource pRandom) {
      return 1;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      return fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8 ? super.getStateForPlacement(pContext) : null;
   }

   public FluidState getFluidState(BlockState pState) {
      return Fluids.WATER.getSource(false);
   }
}