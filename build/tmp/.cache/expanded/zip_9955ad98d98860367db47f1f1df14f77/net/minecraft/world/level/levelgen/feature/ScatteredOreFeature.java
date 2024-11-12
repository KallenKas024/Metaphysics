package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class ScatteredOreFeature extends Feature<OreConfiguration> {
   private static final int MAX_DIST_FROM_ORIGIN = 7;

   ScatteredOreFeature(Codec<OreConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<OreConfiguration> pContext) {
      WorldGenLevel worldgenlevel = pContext.level();
      RandomSource randomsource = pContext.random();
      OreConfiguration oreconfiguration = pContext.config();
      BlockPos blockpos = pContext.origin();
      int i = randomsource.nextInt(oreconfiguration.size + 1);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = 0; j < i; ++j) {
         this.offsetTargetPos(blockpos$mutableblockpos, randomsource, blockpos, Math.min(j, 7));
         BlockState blockstate = worldgenlevel.getBlockState(blockpos$mutableblockpos);

         for(OreConfiguration.TargetBlockState oreconfiguration$targetblockstate : oreconfiguration.targetStates) {
            if (OreFeature.canPlaceOre(blockstate, worldgenlevel::getBlockState, randomsource, oreconfiguration, oreconfiguration$targetblockstate, blockpos$mutableblockpos)) {
               worldgenlevel.setBlock(blockpos$mutableblockpos, oreconfiguration$targetblockstate.state, 2);
               break;
            }
         }
      }

      return true;
   }

   private void offsetTargetPos(BlockPos.MutableBlockPos pMutablePos, RandomSource pRandom, BlockPos pPos, int pMagnitude) {
      int i = this.getRandomPlacementInOneAxisRelativeToOrigin(pRandom, pMagnitude);
      int j = this.getRandomPlacementInOneAxisRelativeToOrigin(pRandom, pMagnitude);
      int k = this.getRandomPlacementInOneAxisRelativeToOrigin(pRandom, pMagnitude);
      pMutablePos.setWithOffset(pPos, i, j, k);
   }

   private int getRandomPlacementInOneAxisRelativeToOrigin(RandomSource pRandom, int pMagnitude) {
      return Math.round((pRandom.nextFloat() - pRandom.nextFloat()) * (float)pMagnitude);
   }
}