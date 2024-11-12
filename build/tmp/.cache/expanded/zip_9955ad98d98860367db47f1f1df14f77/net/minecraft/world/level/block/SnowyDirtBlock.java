package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SnowyDirtBlock extends Block {
   public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

   public SnowyDirtBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(SNOWY, Boolean.valueOf(false)));
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing == Direction.UP ? pState.setValue(SNOWY, Boolean.valueOf(isSnowySetting(pFacingState))) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos().above());
      return this.defaultBlockState().setValue(SNOWY, Boolean.valueOf(isSnowySetting(blockstate)));
   }

   private static boolean isSnowySetting(BlockState pState) {
      return pState.is(BlockTags.SNOW);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(SNOWY);
   }
}