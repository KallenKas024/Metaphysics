package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SoulFireBlock extends BaseFireBlock {
   public SoulFireBlock(BlockBehaviour.Properties p_56653_) {
      super(p_56653_, 2.0F);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return this.canSurvive(pState, pLevel, pCurrentPos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return canSurviveOnBlock(pLevel.getBlockState(pPos.below()));
   }

   public static boolean canSurviveOnBlock(BlockState pState) {
      return pState.is(BlockTags.SOUL_FIRE_BASE_BLOCKS);
   }

   protected boolean canBurn(BlockState pState) {
      return true;
   }
}