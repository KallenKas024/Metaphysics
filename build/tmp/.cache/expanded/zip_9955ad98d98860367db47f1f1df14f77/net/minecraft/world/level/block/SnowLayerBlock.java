package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SnowLayerBlock extends Block {
   public static final int MAX_HEIGHT = 8;
   public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
   protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[]{Shapes.empty(), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};
   public static final int HEIGHT_IMPASSABLE = 5;

   public SnowLayerBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1)));
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      switch (pType) {
         case LAND:
            return pState.getValue(LAYERS) < 5;
         case WATER:
            return false;
         case AIR:
            return false;
         default:
            return false;
      }
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE_BY_LAYER[pState.getValue(LAYERS)];
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE_BY_LAYER[pState.getValue(LAYERS) - 1];
   }

   public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return SHAPE_BY_LAYER[pState.getValue(LAYERS)];
   }

   public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
      return SHAPE_BY_LAYER[pState.getValue(LAYERS)];
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return pState.getValue(LAYERS) == 8 ? 0.2F : 1.0F;
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      if (blockstate.is(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
         return false;
      } else if (blockstate.is(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON)) {
         return true;
      } else {
         return Block.isFaceFull(blockstate.getCollisionShape(pLevel, pPos.below()), Direction.UP) || blockstate.is(this) && blockstate.getValue(LAYERS) == 8;
      }
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

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pLevel.getBrightness(LightLayer.BLOCK, pPos) > 11) {
         dropResources(pState, pLevel, pPos);
         pLevel.removeBlock(pPos, false);
      }

   }

   public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
      int i = pState.getValue(LAYERS);
      if (pUseContext.getItemInHand().is(this.asItem()) && i < 8) {
         if (pUseContext.replacingClickedOnBlock()) {
            return pUseContext.getClickedFace() == Direction.UP;
         } else {
            return true;
         }
      } else {
         return i == 1;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos());
      if (blockstate.is(this)) {
         int i = blockstate.getValue(LAYERS);
         return blockstate.setValue(LAYERS, Integer.valueOf(Math.min(8, i + 1)));
      } else {
         return super.getStateForPlacement(pContext);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LAYERS);
   }
}