package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class EndRodBlock extends RodBlock {
   public EndRodBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Direction direction = pContext.getClickedFace();
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos().relative(direction.getOpposite()));
      return blockstate.is(this) && blockstate.getValue(FACING) == direction ? this.defaultBlockState().setValue(FACING, direction.getOpposite()) : this.defaultBlockState().setValue(FACING, direction);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      Direction direction = pState.getValue(FACING);
      double d0 = (double)pPos.getX() + 0.55D - (double)(pRandom.nextFloat() * 0.1F);
      double d1 = (double)pPos.getY() + 0.55D - (double)(pRandom.nextFloat() * 0.1F);
      double d2 = (double)pPos.getZ() + 0.55D - (double)(pRandom.nextFloat() * 0.1F);
      double d3 = (double)(0.4F - (pRandom.nextFloat() + pRandom.nextFloat()) * 0.4F);
      if (pRandom.nextInt(5) == 0) {
         pLevel.addParticle(ParticleTypes.END_ROD, d0 + (double)direction.getStepX() * d3, d1 + (double)direction.getStepY() * d3, d2 + (double)direction.getStepZ() * d3, pRandom.nextGaussian() * 0.005D, pRandom.nextGaussian() * 0.005D, pRandom.nextGaussian() * 0.005D);
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING);
   }
}