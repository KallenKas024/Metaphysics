package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration;

public class MultifaceGrowthFeature extends Feature<MultifaceGrowthConfiguration> {
   public MultifaceGrowthFeature(Codec<MultifaceGrowthConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<MultifaceGrowthConfiguration> pContext) {
      WorldGenLevel worldgenlevel = pContext.level();
      BlockPos blockpos = pContext.origin();
      RandomSource randomsource = pContext.random();
      MultifaceGrowthConfiguration multifacegrowthconfiguration = pContext.config();
      if (!isAirOrWater(worldgenlevel.getBlockState(blockpos))) {
         return false;
      } else {
         List<Direction> list = multifacegrowthconfiguration.getShuffledDirections(randomsource);
         if (placeGrowthIfPossible(worldgenlevel, blockpos, worldgenlevel.getBlockState(blockpos), multifacegrowthconfiguration, randomsource, list)) {
            return true;
         } else {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos.mutable();

            for(Direction direction : list) {
               blockpos$mutableblockpos.set(blockpos);
               List<Direction> list1 = multifacegrowthconfiguration.getShuffledDirectionsExcept(randomsource, direction.getOpposite());

               for(int i = 0; i < multifacegrowthconfiguration.searchRange; ++i) {
                  blockpos$mutableblockpos.setWithOffset(blockpos, direction);
                  BlockState blockstate = worldgenlevel.getBlockState(blockpos$mutableblockpos);
                  if (!isAirOrWater(blockstate) && !blockstate.is(multifacegrowthconfiguration.placeBlock)) {
                     break;
                  }

                  if (placeGrowthIfPossible(worldgenlevel, blockpos$mutableblockpos, blockstate, multifacegrowthconfiguration, randomsource, list1)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }
   }

   public static boolean placeGrowthIfPossible(WorldGenLevel pLevel, BlockPos pPos, BlockState pState, MultifaceGrowthConfiguration pConfig, RandomSource pRandom, List<Direction> pDirections) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();

      for(Direction direction : pDirections) {
         BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(pPos, direction));
         if (blockstate.is(pConfig.canBePlacedOn)) {
            BlockState blockstate1 = pConfig.placeBlock.getStateForPlacement(pState, pLevel, pPos, direction);
            if (blockstate1 == null) {
               return false;
            }

            pLevel.setBlock(pPos, blockstate1, 3);
            pLevel.getChunk(pPos).markPosForPostprocessing(pPos);
            if (pRandom.nextFloat() < pConfig.chanceOfSpreading) {
               pConfig.placeBlock.getSpreader().spreadFromFaceTowardRandomDirection(blockstate1, pLevel, pPos, direction, pRandom, true);
            }

            return true;
         }
      }

      return false;
   }

   private static boolean isAirOrWater(BlockState pState) {
      return pState.isAir() || pState.is(Blocks.WATER);
   }
}