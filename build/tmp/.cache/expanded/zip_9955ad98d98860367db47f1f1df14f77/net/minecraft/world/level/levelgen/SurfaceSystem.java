package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceSystem {
   private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
   private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
   private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
   private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
   private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
   private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
   private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
   private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
   private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
   private final BlockState defaultBlock;
   private final int seaLevel;
   private final BlockState[] clayBands;
   private final NormalNoise clayBandsOffsetNoise;
   private final NormalNoise badlandsPillarNoise;
   private final NormalNoise badlandsPillarRoofNoise;
   private final NormalNoise badlandsSurfaceNoise;
   private final NormalNoise icebergPillarNoise;
   private final NormalNoise icebergPillarRoofNoise;
   private final NormalNoise icebergSurfaceNoise;
   private final PositionalRandomFactory noiseRandom;
   private final NormalNoise surfaceNoise;
   private final NormalNoise surfaceSecondaryNoise;

   public SurfaceSystem(RandomState pRandomState, BlockState pDefaultBlock, int pSeaLevel, PositionalRandomFactory pNoiseRandom) {
      this.defaultBlock = pDefaultBlock;
      this.seaLevel = pSeaLevel;
      this.noiseRandom = pNoiseRandom;
      this.clayBandsOffsetNoise = pRandomState.getOrCreateNoise(Noises.CLAY_BANDS_OFFSET);
      this.clayBands = generateBands(pNoiseRandom.fromHashOf(new ResourceLocation("clay_bands")));
      this.surfaceNoise = pRandomState.getOrCreateNoise(Noises.SURFACE);
      this.surfaceSecondaryNoise = pRandomState.getOrCreateNoise(Noises.SURFACE_SECONDARY);
      this.badlandsPillarNoise = pRandomState.getOrCreateNoise(Noises.BADLANDS_PILLAR);
      this.badlandsPillarRoofNoise = pRandomState.getOrCreateNoise(Noises.BADLANDS_PILLAR_ROOF);
      this.badlandsSurfaceNoise = pRandomState.getOrCreateNoise(Noises.BADLANDS_SURFACE);
      this.icebergPillarNoise = pRandomState.getOrCreateNoise(Noises.ICEBERG_PILLAR);
      this.icebergPillarRoofNoise = pRandomState.getOrCreateNoise(Noises.ICEBERG_PILLAR_ROOF);
      this.icebergSurfaceNoise = pRandomState.getOrCreateNoise(Noises.ICEBERG_SURFACE);
   }

   public void buildSurface(RandomState pRandomState, BiomeManager pBiomeManager, Registry<Biome> pBiomes, boolean pUseLegacyRandomSource, WorldGenerationContext pContext, final ChunkAccess pChunk, NoiseChunk pNoiseChunk, SurfaceRules.RuleSource pRuleSource) {
      final BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      final ChunkPos chunkpos = pChunk.getPos();
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      BlockColumn blockcolumn = new BlockColumn() {
         public BlockState getBlock(int p_190006_) {
            return pChunk.getBlockState(blockpos$mutableblockpos.setY(p_190006_));
         }

         public void setBlock(int p_190008_, BlockState p_190009_) {
            LevelHeightAccessor levelheightaccessor = pChunk.getHeightAccessorForGeneration();
            if (p_190008_ >= levelheightaccessor.getMinBuildHeight() && p_190008_ < levelheightaccessor.getMaxBuildHeight()) {
               pChunk.setBlockState(blockpos$mutableblockpos.setY(p_190008_), p_190009_, false);
               if (!p_190009_.getFluidState().isEmpty()) {
                  pChunk.markPosForPostprocessing(blockpos$mutableblockpos);
               }
            }

         }

         public String toString() {
            return "ChunkBlockColumn " + chunkpos;
         }
      };
      SurfaceRules.Context surfacerules$context = new SurfaceRules.Context(this, pRandomState, pChunk, pNoiseChunk, pBiomeManager::getBiome, pBiomes, pContext);
      SurfaceRules.SurfaceRule surfacerules$surfacerule = pRuleSource.apply(surfacerules$context);
      BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            int i1 = i + k;
            int j1 = j + l;
            int k1 = pChunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
            blockpos$mutableblockpos.setX(i1).setZ(j1);
            Holder<Biome> holder = pBiomeManager.getBiome(blockpos$mutableblockpos1.set(i1, pUseLegacyRandomSource ? 0 : k1, j1));
            if (holder.is(Biomes.ERODED_BADLANDS)) {
               this.erodedBadlandsExtension(blockcolumn, i1, j1, k1, pChunk);
            }

            int l1 = pChunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
            surfacerules$context.updateXZ(i1, j1);
            int i2 = 0;
            int j2 = Integer.MIN_VALUE;
            int k2 = Integer.MAX_VALUE;
            int l2 = pChunk.getMinBuildHeight();

            for(int i3 = l1; i3 >= l2; --i3) {
               BlockState blockstate = blockcolumn.getBlock(i3);
               if (blockstate.isAir()) {
                  i2 = 0;
                  j2 = Integer.MIN_VALUE;
               } else if (!blockstate.getFluidState().isEmpty()) {
                  if (j2 == Integer.MIN_VALUE) {
                     j2 = i3 + 1;
                  }
               } else {
                  if (k2 >= i3) {
                     k2 = DimensionType.WAY_BELOW_MIN_Y;

                     for(int j3 = i3 - 1; j3 >= l2 - 1; --j3) {
                        BlockState blockstate1 = blockcolumn.getBlock(j3);
                        if (!this.isStone(blockstate1)) {
                           k2 = j3 + 1;
                           break;
                        }
                     }
                  }

                  ++i2;
                  int k3 = i3 - k2 + 1;
                  surfacerules$context.updateY(i2, k3, j2, i1, i3, j1);
                  if (blockstate == this.defaultBlock) {
                     BlockState blockstate2 = surfacerules$surfacerule.tryApply(i1, i3, j1);
                     if (blockstate2 != null) {
                        blockcolumn.setBlock(i3, blockstate2);
                     }
                  }
               }
            }

            if (holder.is(Biomes.FROZEN_OCEAN) || holder.is(Biomes.DEEP_FROZEN_OCEAN)) {
               this.frozenOceanExtension(surfacerules$context.getMinSurfaceLevel(), holder.value(), blockcolumn, blockpos$mutableblockpos1, i1, j1, k1);
            }
         }
      }

   }

   protected int getSurfaceDepth(int pX, int pZ) {
      double d0 = this.surfaceNoise.getValue((double)pX, 0.0D, (double)pZ);
      return (int)(d0 * 2.75D + 3.0D + this.noiseRandom.at(pX, 0, pZ).nextDouble() * 0.25D);
   }

   protected double getSurfaceSecondary(int pX, int pZ) {
      return this.surfaceSecondaryNoise.getValue((double)pX, 0.0D, (double)pZ);
   }

   private boolean isStone(BlockState pState) {
      return !pState.isAir() && pState.getFluidState().isEmpty();
   }

   /** @deprecated */
   @Deprecated
   public Optional<BlockState> topMaterial(SurfaceRules.RuleSource pRule, CarvingContext pContext, Function<BlockPos, Holder<Biome>> pBiomeGetter, ChunkAccess pChunk, NoiseChunk pNoiseChunk, BlockPos pPos, boolean pHasFluid) {
      SurfaceRules.Context surfacerules$context = new SurfaceRules.Context(this, pContext.randomState(), pChunk, pNoiseChunk, pBiomeGetter, pContext.registryAccess().registryOrThrow(Registries.BIOME), pContext);
      SurfaceRules.SurfaceRule surfacerules$surfacerule = pRule.apply(surfacerules$context);
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();
      surfacerules$context.updateXZ(i, k);
      surfacerules$context.updateY(1, 1, pHasFluid ? j + 1 : Integer.MIN_VALUE, i, j, k);
      BlockState blockstate = surfacerules$surfacerule.tryApply(i, j, k);
      return Optional.ofNullable(blockstate);
   }

   private void erodedBadlandsExtension(BlockColumn pBlockColumn, int pX, int pZ, int pHeight, LevelHeightAccessor pLevel) {
      double d0 = 0.2D;
      double d1 = Math.min(Math.abs(this.badlandsSurfaceNoise.getValue((double)pX, 0.0D, (double)pZ) * 8.25D), this.badlandsPillarNoise.getValue((double)pX * 0.2D, 0.0D, (double)pZ * 0.2D) * 15.0D);
      if (!(d1 <= 0.0D)) {
         double d2 = 0.75D;
         double d3 = 1.5D;
         double d4 = Math.abs(this.badlandsPillarRoofNoise.getValue((double)pX * 0.75D, 0.0D, (double)pZ * 0.75D) * 1.5D);
         double d5 = 64.0D + Math.min(d1 * d1 * 2.5D, Math.ceil(d4 * 50.0D) + 24.0D);
         int i = Mth.floor(d5);
         if (pHeight <= i) {
            for(int j = i; j >= pLevel.getMinBuildHeight(); --j) {
               BlockState blockstate = pBlockColumn.getBlock(j);
               if (blockstate.is(this.defaultBlock.getBlock())) {
                  break;
               }

               if (blockstate.is(Blocks.WATER)) {
                  return;
               }
            }

            for(int k = i; k >= pLevel.getMinBuildHeight() && pBlockColumn.getBlock(k).isAir(); --k) {
               pBlockColumn.setBlock(k, this.defaultBlock);
            }

         }
      }
   }

   private void frozenOceanExtension(int pMinSurfaceLevel, Biome pBiome, BlockColumn pBlockColumn, BlockPos.MutableBlockPos pTopWaterPos, int pX, int pZ, int pHeight) {
      double d0 = 1.28D;
      double d1 = Math.min(Math.abs(this.icebergSurfaceNoise.getValue((double)pX, 0.0D, (double)pZ) * 8.25D), this.icebergPillarNoise.getValue((double)pX * 1.28D, 0.0D, (double)pZ * 1.28D) * 15.0D);
      if (!(d1 <= 1.8D)) {
         double d3 = 1.17D;
         double d4 = 1.5D;
         double d5 = Math.abs(this.icebergPillarRoofNoise.getValue((double)pX * 1.17D, 0.0D, (double)pZ * 1.17D) * 1.5D);
         double d6 = Math.min(d1 * d1 * 1.2D, Math.ceil(d5 * 40.0D) + 14.0D);
         if (pBiome.shouldMeltFrozenOceanIcebergSlightly(pTopWaterPos.set(pX, 63, pZ))) {
            d6 -= 2.0D;
         }

         double d2;
         if (d6 > 2.0D) {
            d2 = (double)this.seaLevel - d6 - 7.0D;
            d6 += (double)this.seaLevel;
         } else {
            d6 = 0.0D;
            d2 = 0.0D;
         }

         double d7 = d6;
         RandomSource randomsource = this.noiseRandom.at(pX, 0, pZ);
         int i = 2 + randomsource.nextInt(4);
         int j = this.seaLevel + 18 + randomsource.nextInt(10);
         int k = 0;

         for(int l = Math.max(pHeight, (int)d6 + 1); l >= pMinSurfaceLevel; --l) {
            if (pBlockColumn.getBlock(l).isAir() && l < (int)d7 && randomsource.nextDouble() > 0.01D || pBlockColumn.getBlock(l).is(Blocks.WATER) && l > (int)d2 && l < this.seaLevel && d2 != 0.0D && randomsource.nextDouble() > 0.15D) {
               if (k <= i && l > j) {
                  pBlockColumn.setBlock(l, SNOW_BLOCK);
                  ++k;
               } else {
                  pBlockColumn.setBlock(l, PACKED_ICE);
               }
            }
         }

      }
   }

   private static BlockState[] generateBands(RandomSource pRandom) {
      BlockState[] ablockstate = new BlockState[192];
      Arrays.fill(ablockstate, TERRACOTTA);

      for(int k = 0; k < ablockstate.length; ++k) {
         k += pRandom.nextInt(5) + 1;
         if (k < ablockstate.length) {
            ablockstate[k] = ORANGE_TERRACOTTA;
         }
      }

      makeBands(pRandom, ablockstate, 1, YELLOW_TERRACOTTA);
      makeBands(pRandom, ablockstate, 2, BROWN_TERRACOTTA);
      makeBands(pRandom, ablockstate, 1, RED_TERRACOTTA);
      int l = pRandom.nextIntBetweenInclusive(9, 15);
      int i = 0;

      for(int j = 0; i < l && j < ablockstate.length; j += pRandom.nextInt(16) + 4) {
         ablockstate[j] = WHITE_TERRACOTTA;
         if (j - 1 > 0 && pRandom.nextBoolean()) {
            ablockstate[j - 1] = LIGHT_GRAY_TERRACOTTA;
         }

         if (j + 1 < ablockstate.length && pRandom.nextBoolean()) {
            ablockstate[j + 1] = LIGHT_GRAY_TERRACOTTA;
         }

         ++i;
      }

      return ablockstate;
   }

   private static void makeBands(RandomSource pRandom, BlockState[] pOutput, int pMinSize, BlockState pState) {
      int i = pRandom.nextIntBetweenInclusive(6, 15);

      for(int j = 0; j < i; ++j) {
         int k = pMinSize + pRandom.nextInt(3);
         int l = pRandom.nextInt(pOutput.length);

         for(int i1 = 0; l + i1 < pOutput.length && i1 < k; ++i1) {
            pOutput[l + i1] = pState;
         }
      }

   }

   protected BlockState getBand(int pX, int pY, int pZ) {
      int i = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)pX, 0.0D, (double)pZ) * 4.0D);
      return this.clayBands[(pY + i + this.clayBands.length) % this.clayBands.length];
   }
}