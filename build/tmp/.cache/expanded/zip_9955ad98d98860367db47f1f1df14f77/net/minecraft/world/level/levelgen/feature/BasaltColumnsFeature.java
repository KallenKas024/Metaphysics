package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;

public class BasaltColumnsFeature extends Feature<ColumnFeatureConfiguration> {
   private static final ImmutableList<Block> CANNOT_PLACE_ON = ImmutableList.of(Blocks.LAVA, Blocks.BEDROCK, Blocks.MAGMA_BLOCK, Blocks.SOUL_SAND, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER);
   private static final int CLUSTERED_REACH = 5;
   private static final int CLUSTERED_SIZE = 50;
   private static final int UNCLUSTERED_REACH = 8;
   private static final int UNCLUSTERED_SIZE = 15;

   public BasaltColumnsFeature(Codec<ColumnFeatureConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<ColumnFeatureConfiguration> pContext) {
      int i = pContext.chunkGenerator().getSeaLevel();
      BlockPos blockpos = pContext.origin();
      WorldGenLevel worldgenlevel = pContext.level();
      RandomSource randomsource = pContext.random();
      ColumnFeatureConfiguration columnfeatureconfiguration = pContext.config();
      if (!canPlaceAt(worldgenlevel, i, blockpos.mutable())) {
         return false;
      } else {
         int j = columnfeatureconfiguration.height().sample(randomsource);
         boolean flag = randomsource.nextFloat() < 0.9F;
         int k = Math.min(j, flag ? 5 : 8);
         int l = flag ? 50 : 15;
         boolean flag1 = false;

         for(BlockPos blockpos1 : BlockPos.randomBetweenClosed(randomsource, l, blockpos.getX() - k, blockpos.getY(), blockpos.getZ() - k, blockpos.getX() + k, blockpos.getY(), blockpos.getZ() + k)) {
            int i1 = j - blockpos1.distManhattan(blockpos);
            if (i1 >= 0) {
               flag1 |= this.placeColumn(worldgenlevel, i, blockpos1, i1, columnfeatureconfiguration.reach().sample(randomsource));
            }
         }

         return flag1;
      }
   }

   private boolean placeColumn(LevelAccessor pLevel, int pSeaLevel, BlockPos pPos, int pDistance, int pReach) {
      boolean flag = false;

      for(BlockPos blockpos : BlockPos.betweenClosed(pPos.getX() - pReach, pPos.getY(), pPos.getZ() - pReach, pPos.getX() + pReach, pPos.getY(), pPos.getZ() + pReach)) {
         int i = blockpos.distManhattan(pPos);
         BlockPos blockpos1 = isAirOrLavaOcean(pLevel, pSeaLevel, blockpos) ? findSurface(pLevel, pSeaLevel, blockpos.mutable(), i) : findAir(pLevel, blockpos.mutable(), i);
         if (blockpos1 != null) {
            int j = pDistance - i / 2;

            for(BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos1.mutable(); j >= 0; --j) {
               if (isAirOrLavaOcean(pLevel, pSeaLevel, blockpos$mutableblockpos)) {
                  this.setBlock(pLevel, blockpos$mutableblockpos, Blocks.BASALT.defaultBlockState());
                  blockpos$mutableblockpos.move(Direction.UP);
                  flag = true;
               } else {
                  if (!pLevel.getBlockState(blockpos$mutableblockpos).is(Blocks.BASALT)) {
                     break;
                  }

                  blockpos$mutableblockpos.move(Direction.UP);
               }
            }
         }
      }

      return flag;
   }

   @Nullable
   private static BlockPos findSurface(LevelAccessor pLevel, int pSeaLevel, BlockPos.MutableBlockPos pPos, int pDistance) {
      while(pPos.getY() > pLevel.getMinBuildHeight() + 1 && pDistance > 0) {
         --pDistance;
         if (canPlaceAt(pLevel, pSeaLevel, pPos)) {
            return pPos;
         }

         pPos.move(Direction.DOWN);
      }

      return null;
   }

   private static boolean canPlaceAt(LevelAccessor pLevel, int pSeaLevel, BlockPos.MutableBlockPos pPos) {
      if (!isAirOrLavaOcean(pLevel, pSeaLevel, pPos)) {
         return false;
      } else {
         BlockState blockstate = pLevel.getBlockState(pPos.move(Direction.DOWN));
         pPos.move(Direction.UP);
         return !blockstate.isAir() && !CANNOT_PLACE_ON.contains(blockstate.getBlock());
      }
   }

   @Nullable
   private static BlockPos findAir(LevelAccessor pLevel, BlockPos.MutableBlockPos pPos, int pDistance) {
      while(pPos.getY() < pLevel.getMaxBuildHeight() && pDistance > 0) {
         --pDistance;
         BlockState blockstate = pLevel.getBlockState(pPos);
         if (CANNOT_PLACE_ON.contains(blockstate.getBlock())) {
            return null;
         }

         if (blockstate.isAir()) {
            return pPos;
         }

         pPos.move(Direction.UP);
      }

      return null;
   }

   private static boolean isAirOrLavaOcean(LevelAccessor pLevel, int pSeaLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return blockstate.isAir() || blockstate.is(Blocks.LAVA) && pPos.getY() <= pSeaLevel;
   }
}