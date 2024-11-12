package net.minecraft.world.level.levelgen.feature;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FeaturePlaceContext<FC extends FeatureConfiguration> {
   private final Optional<ConfiguredFeature<?, ?>> topFeature;
   private final WorldGenLevel level;
   private final ChunkGenerator chunkGenerator;
   private final RandomSource random;
   private final BlockPos origin;
   private final FC config;

   public FeaturePlaceContext(Optional<ConfiguredFeature<?, ?>> pTopFeature, WorldGenLevel pLevel, ChunkGenerator pChunkGenerator, RandomSource pRandom, BlockPos pOrigin, FC pConfig) {
      this.topFeature = pTopFeature;
      this.level = pLevel;
      this.chunkGenerator = pChunkGenerator;
      this.random = pRandom;
      this.origin = pOrigin;
      this.config = pConfig;
   }

   public Optional<ConfiguredFeature<?, ?>> topFeature() {
      return this.topFeature;
   }

   public WorldGenLevel level() {
      return this.level;
   }

   public ChunkGenerator chunkGenerator() {
      return this.chunkGenerator;
   }

   public RandomSource random() {
      return this.random;
   }

   public BlockPos origin() {
      return this.origin;
   }

   public FC config() {
      return this.config;
   }
}