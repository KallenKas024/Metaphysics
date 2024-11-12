package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class BushBlock extends Block implements net.minecraftforge.common.IPlantable {
   public BushBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return pState.is(BlockTags.DIRT) || pState.is(Blocks.FARMLAND);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      if (pState.getBlock() == this) //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the pre-check.
         return pLevel.getBlockState(blockpos).canSustainPlant(pLevel, blockpos, Direction.UP, this);
      return this.mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
   }

   public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return pState.getFluidState().isEmpty();
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return pType == PathComputationType.AIR && !this.hasCollision ? true : super.isPathfindable(pState, pLevel, pPos, pType);
   }

   @Override
   public BlockState getPlant(BlockGetter world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() != this) return defaultBlockState();
      return state;
   }
}
