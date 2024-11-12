package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BasaltPillarFeature extends Feature<NoneFeatureConfiguration> {
   public BasaltPillarFeature(Codec<NoneFeatureConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
      BlockPos blockpos = pContext.origin();
      WorldGenLevel worldgenlevel = pContext.level();
      RandomSource randomsource = pContext.random();
      if (worldgenlevel.isEmptyBlock(blockpos) && !worldgenlevel.isEmptyBlock(blockpos.above())) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos.mutable();
         BlockPos.MutableBlockPos blockpos$mutableblockpos1 = blockpos.mutable();
         boolean flag = true;
         boolean flag1 = true;
         boolean flag2 = true;
         boolean flag3 = true;

         while(worldgenlevel.isEmptyBlock(blockpos$mutableblockpos)) {
            if (worldgenlevel.isOutsideBuildHeight(blockpos$mutableblockpos)) {
               return true;
            }

            worldgenlevel.setBlock(blockpos$mutableblockpos, Blocks.BASALT.defaultBlockState(), 2);
            flag = flag && this.placeHangOff(worldgenlevel, randomsource, blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos, Direction.NORTH));
            flag1 = flag1 && this.placeHangOff(worldgenlevel, randomsource, blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos, Direction.SOUTH));
            flag2 = flag2 && this.placeHangOff(worldgenlevel, randomsource, blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos, Direction.WEST));
            flag3 = flag3 && this.placeHangOff(worldgenlevel, randomsource, blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos, Direction.EAST));
            blockpos$mutableblockpos.move(Direction.DOWN);
         }

         blockpos$mutableblockpos.move(Direction.UP);
         this.placeBaseHangOff(worldgenlevel, randomsource, blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos, Direction.NORTH));
         this.placeBaseHangOff(worldgenlevel, randomsource, blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos, Direction.SOUTH));
         this.placeBaseHangOff(worldgenlevel, randomsource, blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos, Direction.WEST));
         this.placeBaseHangOff(worldgenlevel, randomsource, blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos, Direction.EAST));
         blockpos$mutableblockpos.move(Direction.DOWN);
         BlockPos.MutableBlockPos blockpos$mutableblockpos2 = new BlockPos.MutableBlockPos();

         for(int i = -3; i < 4; ++i) {
            for(int j = -3; j < 4; ++j) {
               int k = Mth.abs(i) * Mth.abs(j);
               if (randomsource.nextInt(10) < 10 - k) {
                  blockpos$mutableblockpos2.set(blockpos$mutableblockpos.offset(i, 0, j));
                  int l = 3;

                  while(worldgenlevel.isEmptyBlock(blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos2, Direction.DOWN))) {
                     blockpos$mutableblockpos2.move(Direction.DOWN);
                     --l;
                     if (l <= 0) {
                        break;
                     }
                  }

                  if (!worldgenlevel.isEmptyBlock(blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos2, Direction.DOWN))) {
                     worldgenlevel.setBlock(blockpos$mutableblockpos2, Blocks.BASALT.defaultBlockState(), 2);
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void placeBaseHangOff(LevelAccessor pLevel, RandomSource pRandom, BlockPos pPos) {
      if (pRandom.nextBoolean()) {
         pLevel.setBlock(pPos, Blocks.BASALT.defaultBlockState(), 2);
      }

   }

   private boolean placeHangOff(LevelAccessor pLevel, RandomSource pRandom, BlockPos pPos) {
      if (pRandom.nextInt(10) != 0) {
         pLevel.setBlock(pPos, Blocks.BASALT.defaultBlockState(), 2);
         return true;
      } else {
         return false;
      }
   }
}