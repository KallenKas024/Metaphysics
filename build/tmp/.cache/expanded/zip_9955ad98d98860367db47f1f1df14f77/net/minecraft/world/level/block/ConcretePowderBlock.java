package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePowderBlock extends FallingBlock {
   private final BlockState concrete;

   public ConcretePowderBlock(Block pConcrete, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.concrete = pConcrete.defaultBlockState();
   }

   public void onLand(Level pLevel, BlockPos pPos, BlockState pState, BlockState pReplaceableState, FallingBlockEntity pFallingBlock) {
      if (shouldSolidify(pLevel, pPos, pState, pReplaceableState.getFluidState())) { // Forge: Use block of falling entity instead of block at replaced position, and check if shouldSolidify with the FluidState of the replaced block
         pLevel.setBlock(pPos, this.concrete, 3);
      }

   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockGetter blockgetter = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = blockgetter.getBlockState(blockpos);
      return shouldSolidify(blockgetter, blockpos, blockstate) ? this.concrete : super.getStateForPlacement(pContext);
   }

   private static boolean shouldSolidify(BlockGetter pLevel, BlockPos pPos, BlockState pState, net.minecraft.world.level.material.FluidState fluidState) {
      return pState.canBeHydrated(pLevel, pPos, fluidState, pPos) || touchesLiquid(pLevel, pPos, pState);
   }

   private static boolean shouldSolidify(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return shouldSolidify(pLevel, pPos, pState, pLevel.getFluidState(pPos));
   }

   private static boolean touchesLiquid(BlockGetter pLevel, BlockPos pPos, BlockState state) {
      boolean flag = false;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();

      for(Direction direction : Direction.values()) {
         BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
         if (direction != Direction.DOWN || state.canBeHydrated(pLevel, pPos, blockstate.getFluidState(), blockpos$mutableblockpos)) {
            blockpos$mutableblockpos.setWithOffset(pPos, direction);
            blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
            if (state.canBeHydrated(pLevel, pPos, blockstate.getFluidState(), blockpos$mutableblockpos) && !blockstate.isFaceSturdy(pLevel, pPos, direction.getOpposite())) {
               flag = true;
               break;
            }
         }
      }

      return flag;
   }

   private static boolean canSolidify(BlockState pState) {
      return pState.getFluidState().is(FluidTags.WATER);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return touchesLiquid(pLevel, pCurrentPos, pState) ? this.concrete : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public int getDustColor(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return pState.getMapColor(pReader, pPos).col;
   }
}
