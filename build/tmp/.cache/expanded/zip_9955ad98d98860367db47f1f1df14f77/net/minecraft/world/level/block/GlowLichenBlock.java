package net.minecraft.world.level.block;

import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
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

public class GlowLichenBlock extends MultifaceBlock implements BonemealableBlock, SimpleWaterloggedBlock {
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private final MultifaceSpreader spreader = new MultifaceSpreader(this);

   public GlowLichenBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public static ToIntFunction<BlockState> emission(int pLight) {
      return (p_181221_) -> {
         return MultifaceBlock.hasAnyFace(p_181221_) ? pLight : 0;
      };
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(WATERLOGGED);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
   }

   public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
      return !pUseContext.getItemInHand().is(Items.GLOW_LICHEN) || super.canBeReplaced(pState, pUseContext);
   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return Direction.stream().anyMatch((p_153316_) -> {
         return this.spreader.canSpreadInAnyDirection(pState, pLevel, pPos, p_153316_.getOpposite());
      });
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      this.spreader.spreadFromRandomFaceTowardRandomDirection(pState, pLevel, pPos, pRandom);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return pState.getFluidState().isEmpty();
   }

   public MultifaceSpreader getSpreader() {
      return this.spreader;
   }
}