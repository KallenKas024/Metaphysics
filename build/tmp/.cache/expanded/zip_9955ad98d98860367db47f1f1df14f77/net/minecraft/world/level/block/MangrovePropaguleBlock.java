package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.grower.MangroveTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MangrovePropaguleBlock extends SaplingBlock implements SimpleWaterloggedBlock {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
   public static final int MAX_AGE = 4;
   private static final VoxelShape[] SHAPE_PER_AGE = new VoxelShape[]{Block.box(7.0D, 13.0D, 7.0D, 9.0D, 16.0D, 9.0D), Block.box(7.0D, 10.0D, 7.0D, 9.0D, 16.0D, 9.0D), Block.box(7.0D, 7.0D, 7.0D, 9.0D, 16.0D, 9.0D), Block.box(7.0D, 3.0D, 7.0D, 9.0D, 16.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D)};
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
   private static final float GROW_TALL_MANGROVE_PROBABILITY = 0.85F;

   public MangrovePropaguleBlock(BlockBehaviour.Properties p_221449_) {
      super(new MangroveTreeGrower(0.85F), p_221449_);
      this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, Integer.valueOf(0)).setValue(AGE, Integer.valueOf(0)).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(HANGING, Boolean.valueOf(false)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(STAGE).add(AGE).add(WATERLOGGED).add(HANGING);
   }

   protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return super.mayPlaceOn(pState, pLevel, pPos) || pState.is(Blocks.CLAY);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      boolean flag = fluidstate.getType() == Fluids.WATER;
      return super.getStateForPlacement(pContext).setValue(WATERLOGGED, Boolean.valueOf(flag)).setValue(AGE, Integer.valueOf(4));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      Vec3 vec3 = pState.getOffset(pLevel, pPos);
      VoxelShape voxelshape;
      if (!pState.getValue(HANGING)) {
         voxelshape = SHAPE_PER_AGE[4];
      } else {
         voxelshape = SHAPE_PER_AGE[pState.getValue(AGE)];
      }

      return voxelshape.move(vec3.x, vec3.y, vec3.z);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return isHanging(pState) ? pLevel.getBlockState(pPos.above()).is(Blocks.MANGROVE_LEAVES) : super.canSurvive(pState, pLevel, pPos);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return pFacing == Direction.UP && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!isHanging(pState)) {
         if (pRandom.nextInt(7) == 0) {
            this.advanceTree(pLevel, pPos, pState, pRandom);
         }

      } else {
         if (!isFullyGrown(pState)) {
            pLevel.setBlock(pPos, pState.cycle(AGE), 2);
         }

      }
   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return !isHanging(pState) || !isFullyGrown(pState);
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return isHanging(pState) ? !isFullyGrown(pState) : super.isBonemealSuccess(pLevel, pRandom, pPos, pState);
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      if (isHanging(pState) && !isFullyGrown(pState)) {
         pLevel.setBlock(pPos, pState.cycle(AGE), 2);
      } else {
         super.performBonemeal(pLevel, pRandom, pPos, pState);
      }

   }

   private static boolean isHanging(BlockState pState) {
      return pState.getValue(HANGING);
   }

   private static boolean isFullyGrown(BlockState pState) {
      return pState.getValue(AGE) == 4;
   }

   public static BlockState createNewHangingPropagule() {
      return createNewHangingPropagule(0);
   }

   public static BlockState createNewHangingPropagule(int pAge) {
      return Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(HANGING, Boolean.valueOf(true)).setValue(AGE, Integer.valueOf(pAge));
   }
}