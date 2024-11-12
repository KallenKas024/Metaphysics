package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class ChorusPlantBlock extends PipeBlock {
   public ChorusPlantBlock(BlockBehaviour.Properties p_51707_) {
      super(0.3125F, p_51707_);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)).setValue(UP, Boolean.valueOf(false)).setValue(DOWN, Boolean.valueOf(false)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.getStateForPlacement(pContext.getLevel(), pContext.getClickedPos());
   }

   public BlockState getStateForPlacement(BlockGetter pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      BlockState blockstate1 = pLevel.getBlockState(pPos.above());
      BlockState blockstate2 = pLevel.getBlockState(pPos.north());
      BlockState blockstate3 = pLevel.getBlockState(pPos.east());
      BlockState blockstate4 = pLevel.getBlockState(pPos.south());
      BlockState blockstate5 = pLevel.getBlockState(pPos.west());
      return this.defaultBlockState().setValue(DOWN, Boolean.valueOf(blockstate.is(this) || blockstate.is(Blocks.CHORUS_FLOWER) || blockstate.is(Blocks.END_STONE))).setValue(UP, Boolean.valueOf(blockstate1.is(this) || blockstate1.is(Blocks.CHORUS_FLOWER))).setValue(NORTH, Boolean.valueOf(blockstate2.is(this) || blockstate2.is(Blocks.CHORUS_FLOWER))).setValue(EAST, Boolean.valueOf(blockstate3.is(this) || blockstate3.is(Blocks.CHORUS_FLOWER))).setValue(SOUTH, Boolean.valueOf(blockstate4.is(this) || blockstate4.is(Blocks.CHORUS_FLOWER))).setValue(WEST, Boolean.valueOf(blockstate5.is(this) || blockstate5.is(Blocks.CHORUS_FLOWER)));
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (!pState.canSurvive(pLevel, pCurrentPos)) {
         pLevel.scheduleTick(pCurrentPos, this, 1);
         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      } else {
         boolean flag = pFacingState.is(this) || pFacingState.is(Blocks.CHORUS_FLOWER) || pFacing == Direction.DOWN && pFacingState.is(Blocks.END_STONE);
         return pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), Boolean.valueOf(flag));
      }
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!pState.canSurvive(pLevel, pPos)) {
         pLevel.destroyBlock(pPos, true);
      }

   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      boolean flag = !pLevel.getBlockState(pPos.above()).isAir() && !blockstate.isAir();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction);
         BlockState blockstate1 = pLevel.getBlockState(blockpos);
         if (blockstate1.is(this)) {
            if (flag) {
               return false;
            }

            BlockState blockstate2 = pLevel.getBlockState(blockpos.below());
            if (blockstate2.is(this) || blockstate2.is(Blocks.END_STONE)) {
               return true;
            }
         }
      }

      return blockstate.is(this) || blockstate.is(Blocks.END_STONE);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}