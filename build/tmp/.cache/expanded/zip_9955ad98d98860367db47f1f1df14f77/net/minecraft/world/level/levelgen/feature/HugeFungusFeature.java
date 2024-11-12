package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class HugeFungusFeature extends Feature<HugeFungusConfiguration> {
   private static final float HUGE_PROBABILITY = 0.06F;

   public HugeFungusFeature(Codec<HugeFungusConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<HugeFungusConfiguration> pContext) {
      WorldGenLevel worldgenlevel = pContext.level();
      BlockPos blockpos = pContext.origin();
      RandomSource randomsource = pContext.random();
      ChunkGenerator chunkgenerator = pContext.chunkGenerator();
      HugeFungusConfiguration hugefungusconfiguration = pContext.config();
      Block block = hugefungusconfiguration.validBaseState.getBlock();
      BlockPos blockpos1 = null;
      BlockState blockstate = worldgenlevel.getBlockState(blockpos.below());
      if (blockstate.is(block)) {
         blockpos1 = blockpos;
      }

      if (blockpos1 == null) {
         return false;
      } else {
         int i = Mth.nextInt(randomsource, 4, 13);
         if (randomsource.nextInt(12) == 0) {
            i *= 2;
         }

         if (!hugefungusconfiguration.planted) {
            int j = chunkgenerator.getGenDepth();
            if (blockpos1.getY() + i + 1 >= j) {
               return false;
            }
         }

         boolean flag = !hugefungusconfiguration.planted && randomsource.nextFloat() < 0.06F;
         worldgenlevel.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 4);
         this.placeStem(worldgenlevel, randomsource, hugefungusconfiguration, blockpos1, i, flag);
         this.placeHat(worldgenlevel, randomsource, hugefungusconfiguration, blockpos1, i, flag);
         return true;
      }
   }

   private static boolean isReplaceable(WorldGenLevel pLevel, BlockPos pPos, HugeFungusConfiguration pConfig, boolean pCheckConfig) {
      if (pLevel.isStateAtPosition(pPos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
         return true;
      } else {
         return pCheckConfig ? pConfig.replaceableBlocks.test(pLevel, pPos) : false;
      }
   }

   private void placeStem(WorldGenLevel pLevel, RandomSource pRandom, HugeFungusConfiguration pConfig, BlockPos pPos, int pHeight, boolean pHuge) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      BlockState blockstate = pConfig.stemState;
      int i = pHuge ? 1 : 0;

      for(int j = -i; j <= i; ++j) {
         for(int k = -i; k <= i; ++k) {
            boolean flag = pHuge && Mth.abs(j) == i && Mth.abs(k) == i;

            for(int l = 0; l < pHeight; ++l) {
               blockpos$mutableblockpos.setWithOffset(pPos, j, l, k);
               if (isReplaceable(pLevel, blockpos$mutableblockpos, pConfig, true)) {
                  if (pConfig.planted) {
                     if (!pLevel.getBlockState(blockpos$mutableblockpos.below()).isAir()) {
                        pLevel.destroyBlock(blockpos$mutableblockpos, true);
                     }

                     pLevel.setBlock(blockpos$mutableblockpos, blockstate, 3);
                  } else if (flag) {
                     if (pRandom.nextFloat() < 0.1F) {
                        this.setBlock(pLevel, blockpos$mutableblockpos, blockstate);
                     }
                  } else {
                     this.setBlock(pLevel, blockpos$mutableblockpos, blockstate);
                  }
               }
            }
         }
      }

   }

   private void placeHat(WorldGenLevel pLevel, RandomSource pRandom, HugeFungusConfiguration pConfig, BlockPos pPos, int pHeight, boolean pHuge) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      boolean flag = pConfig.hatState.is(Blocks.NETHER_WART_BLOCK);
      int i = Math.min(pRandom.nextInt(1 + pHeight / 3) + 5, pHeight);
      int j = pHeight - i;

      for(int k = j; k <= pHeight; ++k) {
         int l = k < pHeight - pRandom.nextInt(3) ? 2 : 1;
         if (i > 8 && k < j + 4) {
            l = 3;
         }

         if (pHuge) {
            ++l;
         }

         for(int i1 = -l; i1 <= l; ++i1) {
            for(int j1 = -l; j1 <= l; ++j1) {
               boolean flag1 = i1 == -l || i1 == l;
               boolean flag2 = j1 == -l || j1 == l;
               boolean flag3 = !flag1 && !flag2 && k != pHeight;
               boolean flag4 = flag1 && flag2;
               boolean flag5 = k < j + 3;
               blockpos$mutableblockpos.setWithOffset(pPos, i1, k, j1);
               if (isReplaceable(pLevel, blockpos$mutableblockpos, pConfig, false)) {
                  if (pConfig.planted && !pLevel.getBlockState(blockpos$mutableblockpos.below()).isAir()) {
                     pLevel.destroyBlock(blockpos$mutableblockpos, true);
                  }

                  if (flag5) {
                     if (!flag3) {
                        this.placeHatDropBlock(pLevel, pRandom, blockpos$mutableblockpos, pConfig.hatState, flag);
                     }
                  } else if (flag3) {
                     this.placeHatBlock(pLevel, pRandom, pConfig, blockpos$mutableblockpos, 0.1F, 0.2F, flag ? 0.1F : 0.0F);
                  } else if (flag4) {
                     this.placeHatBlock(pLevel, pRandom, pConfig, blockpos$mutableblockpos, 0.01F, 0.7F, flag ? 0.083F : 0.0F);
                  } else {
                     this.placeHatBlock(pLevel, pRandom, pConfig, blockpos$mutableblockpos, 5.0E-4F, 0.98F, flag ? 0.07F : 0.0F);
                  }
               }
            }
         }
      }

   }

   private void placeHatBlock(LevelAccessor pLevel, RandomSource pRandom, HugeFungusConfiguration pConfig, BlockPos.MutableBlockPos pPos, float pDecorationChance, float pHatChance, float pWeepingVineChance) {
      if (pRandom.nextFloat() < pDecorationChance) {
         this.setBlock(pLevel, pPos, pConfig.decorState);
      } else if (pRandom.nextFloat() < pHatChance) {
         this.setBlock(pLevel, pPos, pConfig.hatState);
         if (pRandom.nextFloat() < pWeepingVineChance) {
            tryPlaceWeepingVines(pPos, pLevel, pRandom);
         }
      }

   }

   private void placeHatDropBlock(LevelAccessor pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState, boolean pWeepingVines) {
      if (pLevel.getBlockState(pPos.below()).is(pState.getBlock())) {
         this.setBlock(pLevel, pPos, pState);
      } else if ((double)pRandom.nextFloat() < 0.15D) {
         this.setBlock(pLevel, pPos, pState);
         if (pWeepingVines && pRandom.nextInt(11) == 0) {
            tryPlaceWeepingVines(pPos, pLevel, pRandom);
         }
      }

   }

   private static void tryPlaceWeepingVines(BlockPos pPos, LevelAccessor pLevel, RandomSource pRandom) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable().move(Direction.DOWN);
      if (pLevel.isEmptyBlock(blockpos$mutableblockpos)) {
         int i = Mth.nextInt(pRandom, 1, 5);
         if (pRandom.nextInt(7) == 0) {
            i *= 2;
         }

         int j = 23;
         int k = 25;
         WeepingVinesFeature.placeWeepingVinesColumn(pLevel, pRandom, blockpos$mutableblockpos, i, 23, 25);
      }
   }
}