package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafStemBlock extends HorizontalDirectionalBlock implements BonemealableBlock, SimpleWaterloggedBlock {
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final int STEM_WIDTH = 6;
   protected static final VoxelShape NORTH_SHAPE = Block.box(5.0D, 0.0D, 9.0D, 11.0D, 16.0D, 15.0D);
   protected static final VoxelShape SOUTH_SHAPE = Block.box(5.0D, 0.0D, 1.0D, 11.0D, 16.0D, 7.0D);
   protected static final VoxelShape EAST_SHAPE = Block.box(1.0D, 0.0D, 5.0D, 7.0D, 16.0D, 11.0D);
   protected static final VoxelShape WEST_SHAPE = Block.box(9.0D, 0.0D, 5.0D, 15.0D, 16.0D, 11.0D);

   public BigDripleafStemBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      switch ((Direction)pState.getValue(FACING)) {
         case SOUTH:
            return SOUTH_SHAPE;
         case NORTH:
         default:
            return NORTH_SHAPE;
         case WEST:
            return WEST_SHAPE;
         case EAST:
            return EAST_SHAPE;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(WATERLOGGED, FACING);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      BlockState blockstate = pLevel.getBlockState(blockpos);
      BlockState blockstate1 = pLevel.getBlockState(pPos.above());
      return (blockstate.is(this) || blockstate.is(BlockTags.BIG_DRIPLEAF_PLACEABLE)) && (blockstate1.is(this) || blockstate1.is(Blocks.BIG_DRIPLEAF));
   }

   protected static boolean place(LevelAccessor pLevel, BlockPos pPos, FluidState pFluidState, Direction pDirection) {
      BlockState blockstate = Blocks.BIG_DRIPLEAF_STEM.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(pFluidState.isSourceOfType(Fluids.WATER))).setValue(FACING, pDirection);
      return pLevel.setBlock(pPos, blockstate, 3);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
      if ((pDirection == Direction.DOWN || pDirection == Direction.UP) && !pState.canSurvive(pLevel, pPos)) {
         pLevel.scheduleTick(pPos, this, 1);
      }

      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!pState.canSurvive(pLevel, pPos)) {
         pLevel.destroyBlock(pPos, true);
      }

   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      Optional<BlockPos> optional = BlockUtil.getTopConnectedBlock(pLevel, pPos, pState.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
      if (!optional.isPresent()) {
         return false;
      } else {
         BlockPos blockpos = optional.get().above();
         BlockState blockstate = pLevel.getBlockState(blockpos);
         return BigDripleafBlock.canPlaceAt(pLevel, blockpos, blockstate);
      }
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      Optional<BlockPos> optional = BlockUtil.getTopConnectedBlock(pLevel, pPos, pState.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
      if (optional.isPresent()) {
         BlockPos blockpos = optional.get();
         BlockPos blockpos1 = blockpos.above();
         Direction direction = pState.getValue(FACING);
         place(pLevel, blockpos, pLevel.getFluidState(blockpos), direction);
         BigDripleafBlock.place(pLevel, blockpos1, pLevel.getFluidState(blockpos1), direction);
      }
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return new ItemStack(Blocks.BIG_DRIPLEAF);
   }
}