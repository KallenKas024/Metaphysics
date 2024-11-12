package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class BlockPileFeature extends Feature<BlockPileConfiguration> {
   public BlockPileFeature(Codec<BlockPileConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<BlockPileConfiguration> pContext) {
      BlockPos blockpos = pContext.origin();
      WorldGenLevel worldgenlevel = pContext.level();
      RandomSource randomsource = pContext.random();
      BlockPileConfiguration blockpileconfiguration = pContext.config();
      if (blockpos.getY() < worldgenlevel.getMinBuildHeight() + 5) {
         return false;
      } else {
         int i = 2 + randomsource.nextInt(2);
         int j = 2 + randomsource.nextInt(2);

         for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-i, 0, -j), blockpos.offset(i, 1, j))) {
            int k = blockpos.getX() - blockpos1.getX();
            int l = blockpos.getZ() - blockpos1.getZ();
            if ((float)(k * k + l * l) <= randomsource.nextFloat() * 10.0F - randomsource.nextFloat() * 6.0F) {
               this.tryPlaceBlock(worldgenlevel, blockpos1, randomsource, blockpileconfiguration);
            } else if ((double)randomsource.nextFloat() < 0.031D) {
               this.tryPlaceBlock(worldgenlevel, blockpos1, randomsource, blockpileconfiguration);
            }
         }

         return true;
      }
   }

   private boolean mayPlaceOn(LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom) {
      BlockPos blockpos = pPos.below();
      BlockState blockstate = pLevel.getBlockState(blockpos);
      return blockstate.is(Blocks.DIRT_PATH) ? pRandom.nextBoolean() : blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP);
   }

   private void tryPlaceBlock(LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom, BlockPileConfiguration pConfig) {
      if (pLevel.isEmptyBlock(pPos) && this.mayPlaceOn(pLevel, pPos, pRandom)) {
         pLevel.setBlock(pPos, pConfig.stateProvider.getState(pRandom, pPos), 4);
      }

   }
}