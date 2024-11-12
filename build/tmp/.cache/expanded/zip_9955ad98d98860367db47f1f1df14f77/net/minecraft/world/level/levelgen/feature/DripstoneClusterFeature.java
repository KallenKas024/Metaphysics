package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;

public class DripstoneClusterFeature extends Feature<DripstoneClusterConfiguration> {
   public DripstoneClusterFeature(Codec<DripstoneClusterConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<DripstoneClusterConfiguration> pContext) {
      WorldGenLevel worldgenlevel = pContext.level();
      BlockPos blockpos = pContext.origin();
      DripstoneClusterConfiguration dripstoneclusterconfiguration = pContext.config();
      RandomSource randomsource = pContext.random();
      if (!DripstoneUtils.isEmptyOrWater(worldgenlevel, blockpos)) {
         return false;
      } else {
         int i = dripstoneclusterconfiguration.height.sample(randomsource);
         float f = dripstoneclusterconfiguration.wetness.sample(randomsource);
         float f1 = dripstoneclusterconfiguration.density.sample(randomsource);
         int j = dripstoneclusterconfiguration.radius.sample(randomsource);
         int k = dripstoneclusterconfiguration.radius.sample(randomsource);

         for(int l = -j; l <= j; ++l) {
            for(int i1 = -k; i1 <= k; ++i1) {
               double d0 = this.getChanceOfStalagmiteOrStalactite(j, k, l, i1, dripstoneclusterconfiguration);
               BlockPos blockpos1 = blockpos.offset(l, 0, i1);
               this.placeColumn(worldgenlevel, randomsource, blockpos1, l, i1, f, d0, i, f1, dripstoneclusterconfiguration);
            }
         }

         return true;
      }
   }

   private void placeColumn(WorldGenLevel pLevel, RandomSource pRandom, BlockPos pPos, int pX, int pZ, float pWetness, double pChance, int pHeight, float pDensity, DripstoneClusterConfiguration pConfig) {
      Optional<Column> optional = Column.scan(pLevel, pPos, pConfig.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isNeitherEmptyNorWater);
      if (optional.isPresent()) {
         OptionalInt optionalint = optional.get().getCeiling();
         OptionalInt optionalint1 = optional.get().getFloor();
         if (optionalint.isPresent() || optionalint1.isPresent()) {
            boolean flag = pRandom.nextFloat() < pWetness;
            Column column;
            if (flag && optionalint1.isPresent() && this.canPlacePool(pLevel, pPos.atY(optionalint1.getAsInt()))) {
               int i = optionalint1.getAsInt();
               column = optional.get().withFloor(OptionalInt.of(i - 1));
               pLevel.setBlock(pPos.atY(i), Blocks.WATER.defaultBlockState(), 2);
            } else {
               column = optional.get();
            }

            OptionalInt optionalint2 = column.getFloor();
            boolean flag1 = pRandom.nextDouble() < pChance;
            int j;
            if (optionalint.isPresent() && flag1 && !this.isLava(pLevel, pPos.atY(optionalint.getAsInt()))) {
               int k = pConfig.dripstoneBlockLayerThickness.sample(pRandom);
               this.replaceBlocksWithDripstoneBlocks(pLevel, pPos.atY(optionalint.getAsInt()), k, Direction.UP);
               int l;
               if (optionalint2.isPresent()) {
                  l = Math.min(pHeight, optionalint.getAsInt() - optionalint2.getAsInt());
               } else {
                  l = pHeight;
               }

               j = this.getDripstoneHeight(pRandom, pX, pZ, pDensity, l, pConfig);
            } else {
               j = 0;
            }

            boolean flag2 = pRandom.nextDouble() < pChance;
            int i3;
            if (optionalint2.isPresent() && flag2 && !this.isLava(pLevel, pPos.atY(optionalint2.getAsInt()))) {
               int i1 = pConfig.dripstoneBlockLayerThickness.sample(pRandom);
               this.replaceBlocksWithDripstoneBlocks(pLevel, pPos.atY(optionalint2.getAsInt()), i1, Direction.DOWN);
               if (optionalint.isPresent()) {
                  i3 = Math.max(0, j + Mth.randomBetweenInclusive(pRandom, -pConfig.maxStalagmiteStalactiteHeightDiff, pConfig.maxStalagmiteStalactiteHeightDiff));
               } else {
                  i3 = this.getDripstoneHeight(pRandom, pX, pZ, pDensity, pHeight, pConfig);
               }
            } else {
               i3 = 0;
            }

            int j1;
            int j3;
            if (optionalint.isPresent() && optionalint2.isPresent() && optionalint.getAsInt() - j <= optionalint2.getAsInt() + i3) {
               int k1 = optionalint2.getAsInt();
               int l1 = optionalint.getAsInt();
               int i2 = Math.max(l1 - j, k1 + 1);
               int j2 = Math.min(k1 + i3, l1 - 1);
               int k2 = Mth.randomBetweenInclusive(pRandom, i2, j2 + 1);
               int l2 = k2 - 1;
               j3 = l1 - k2;
               j1 = l2 - k1;
            } else {
               j3 = j;
               j1 = i3;
            }

            boolean flag3 = pRandom.nextBoolean() && j3 > 0 && j1 > 0 && column.getHeight().isPresent() && j3 + j1 == column.getHeight().getAsInt();
            if (optionalint.isPresent()) {
               DripstoneUtils.growPointedDripstone(pLevel, pPos.atY(optionalint.getAsInt() - 1), Direction.DOWN, j3, flag3);
            }

            if (optionalint2.isPresent()) {
               DripstoneUtils.growPointedDripstone(pLevel, pPos.atY(optionalint2.getAsInt() + 1), Direction.UP, j1, flag3);
            }

         }
      }
   }

   private boolean isLava(LevelReader pLevel, BlockPos pPos) {
      return pLevel.getBlockState(pPos).is(Blocks.LAVA);
   }

   private int getDripstoneHeight(RandomSource pRandom, int pX, int pZ, float pChance, int pHeight, DripstoneClusterConfiguration pConfig) {
      if (pRandom.nextFloat() > pChance) {
         return 0;
      } else {
         int i = Math.abs(pX) + Math.abs(pZ);
         float f = (float)Mth.clampedMap((double)i, 0.0D, (double)pConfig.maxDistanceFromCenterAffectingHeightBias, (double)pHeight / 2.0D, 0.0D);
         return (int)randomBetweenBiased(pRandom, 0.0F, (float)pHeight, f, (float)pConfig.heightDeviation);
      }
   }

   private boolean canPlacePool(WorldGenLevel pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      if (!blockstate.is(Blocks.WATER) && !blockstate.is(Blocks.DRIPSTONE_BLOCK) && !blockstate.is(Blocks.POINTED_DRIPSTONE)) {
         if (pLevel.getBlockState(pPos.above()).getFluidState().is(FluidTags.WATER)) {
            return false;
         } else {
            for(Direction direction : Direction.Plane.HORIZONTAL) {
               if (!this.canBeAdjacentToWater(pLevel, pPos.relative(direction))) {
                  return false;
               }
            }

            return this.canBeAdjacentToWater(pLevel, pPos.below());
         }
      } else {
         return false;
      }
   }

   private boolean canBeAdjacentToWater(LevelAccessor pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return blockstate.is(BlockTags.BASE_STONE_OVERWORLD) || blockstate.getFluidState().is(FluidTags.WATER);
   }

   private void replaceBlocksWithDripstoneBlocks(WorldGenLevel pLevel, BlockPos pPos, int pThickness, Direction pDirection) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();

      for(int i = 0; i < pThickness; ++i) {
         if (!DripstoneUtils.placeDripstoneBlockIfPossible(pLevel, blockpos$mutableblockpos)) {
            return;
         }

         blockpos$mutableblockpos.move(pDirection);
      }

   }

   private double getChanceOfStalagmiteOrStalactite(int pXRadius, int pZRadius, int pX, int pZ, DripstoneClusterConfiguration pConfig) {
      int i = pXRadius - Math.abs(pX);
      int j = pZRadius - Math.abs(pZ);
      int k = Math.min(i, j);
      return (double)Mth.clampedMap((float)k, 0.0F, (float)pConfig.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn, pConfig.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0F);
   }

   private static float randomBetweenBiased(RandomSource pRandom, float pMin, float pMax, float pMean, float pDeviation) {
      return ClampedNormalFloat.sample(pRandom, pMean, pDeviation, pMin, pMax);
   }
}