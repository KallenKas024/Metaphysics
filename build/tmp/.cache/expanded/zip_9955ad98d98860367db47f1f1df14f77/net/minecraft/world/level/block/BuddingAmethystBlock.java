package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingAmethystBlock extends AmethystBlock {
   public static final int GROWTH_CHANCE = 5;
   private static final Direction[] DIRECTIONS = Direction.values();

   public BuddingAmethystBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pRandom.nextInt(5) == 0) {
         Direction direction = DIRECTIONS[pRandom.nextInt(DIRECTIONS.length)];
         BlockPos blockpos = pPos.relative(direction);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         Block block = null;
         if (canClusterGrowAtState(blockstate)) {
            block = Blocks.SMALL_AMETHYST_BUD;
         } else if (blockstate.is(Blocks.SMALL_AMETHYST_BUD) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
            block = Blocks.MEDIUM_AMETHYST_BUD;
         } else if (blockstate.is(Blocks.MEDIUM_AMETHYST_BUD) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
            block = Blocks.LARGE_AMETHYST_BUD;
         } else if (blockstate.is(Blocks.LARGE_AMETHYST_BUD) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
            block = Blocks.AMETHYST_CLUSTER;
         }

         if (block != null) {
            BlockState blockstate1 = block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction).setValue(AmethystClusterBlock.WATERLOGGED, Boolean.valueOf(blockstate.getFluidState().getType() == Fluids.WATER));
            pLevel.setBlockAndUpdate(blockpos, blockstate1);
         }

      }
   }

   public static boolean canClusterGrowAtState(BlockState pState) {
      return pState.isAir() || pState.is(Blocks.WATER) && pState.getFluidState().getAmount() == 8;
   }
}