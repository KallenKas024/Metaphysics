package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;

public class DoublePlantBlock extends BushBlock {
   public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

   public DoublePlantBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      DoubleBlockHalf doubleblockhalf = pState.getValue(HALF);
      if (pFacing.getAxis() != Direction.Axis.Y || doubleblockhalf == DoubleBlockHalf.LOWER != (pFacing == Direction.UP) || pFacingState.is(this) && pFacingState.getValue(HALF) != doubleblockhalf) {
         return doubleblockhalf == DoubleBlockHalf.LOWER && pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      } else {
         return Blocks.AIR.defaultBlockState();
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      Level level = pContext.getLevel();
      return blockpos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(blockpos.above()).canBeReplaced(pContext) ? super.getStateForPlacement(pContext) : null;
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      BlockPos blockpos = pPos.above();
      pLevel.setBlock(blockpos, copyWaterloggedFrom(pLevel, blockpos, this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER)), 3);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      if (pState.getValue(HALF) != DoubleBlockHalf.UPPER) {
         return super.canSurvive(pState, pLevel, pPos);
      } else {
         BlockState blockstate = pLevel.getBlockState(pPos.below());
         if (pState.getBlock() != this) return super.canSurvive(pState, pLevel, pPos); //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the pre-check.
         return blockstate.is(this) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER;
      }
   }

   public static void placeAt(LevelAccessor pLevel, BlockState pState, BlockPos pPos, int pFlags) {
      BlockPos blockpos = pPos.above();
      pLevel.setBlock(pPos, copyWaterloggedFrom(pLevel, pPos, pState.setValue(HALF, DoubleBlockHalf.LOWER)), pFlags);
      pLevel.setBlock(blockpos, copyWaterloggedFrom(pLevel, blockpos, pState.setValue(HALF, DoubleBlockHalf.UPPER)), pFlags);
   }

   public static BlockState copyWaterloggedFrom(LevelReader pLevel, BlockPos pPos, BlockState pState) {
      return pState.hasProperty(BlockStateProperties.WATERLOGGED) ? pState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(pLevel.isWaterAt(pPos))) : pState;
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      if (!pLevel.isClientSide) {
         if (pPlayer.isCreative()) {
            preventCreativeDropFromBottomPart(pLevel, pPos, pState, pPlayer);
         } else {
            dropResources(pState, pLevel, pPos, (BlockEntity)null, pPlayer, pPlayer.getMainHandItem());
         }
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   /**
    * Called after a player has successfully harvested this block. This method will only be called if the player has
    * used the correct tool and drops should be spawned.
    */
   public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pTe, ItemStack pStack) {
      super.playerDestroy(pLevel, pPlayer, pPos, Blocks.AIR.defaultBlockState(), pTe, pStack);
   }

   protected static void preventCreativeDropFromBottomPart(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      DoubleBlockHalf doubleblockhalf = pState.getValue(HALF);
      if (doubleblockhalf == DoubleBlockHalf.UPPER) {
         BlockPos blockpos = pPos.below();
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (blockstate.is(pState.getBlock()) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockState blockstate1 = blockstate.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
            pLevel.setBlock(blockpos, blockstate1, 35);
            pLevel.levelEvent(pPlayer, 2001, blockpos, Block.getId(blockstate));
         }
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(HALF);
   }

   /**
    * Return a random long to be passed to {@link net.minecraft.client.resources.model.BakedModel#getQuads}, used for
    * random model rotations
    */
   public long getSeed(BlockState pState, BlockPos pPos) {
      return Mth.getSeed(pPos.getX(), pPos.below(pState.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pPos.getZ());
   }
}
