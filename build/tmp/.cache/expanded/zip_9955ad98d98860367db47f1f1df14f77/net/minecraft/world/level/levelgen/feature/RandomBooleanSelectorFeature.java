package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfiguration> {
   public RandomBooleanSelectorFeature(Codec<RandomBooleanFeatureConfiguration> p_66591_) {
      super(p_66591_);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<RandomBooleanFeatureConfiguration> p_160208_) {
      RandomSource randomsource = p_160208_.random();
      RandomBooleanFeatureConfiguration randombooleanfeatureconfiguration = p_160208_.config();
      WorldGenLevel worldgenlevel = p_160208_.level();
      ChunkGenerator chunkgenerator = p_160208_.chunkGenerator();
      BlockPos blockpos = p_160208_.origin();
      boolean flag = randomsource.nextBoolean();
      return ((PlacedFeature)(flag ? randombooleanfeatureconfiguration.featureTrue : randombooleanfeatureconfiguration.featureFalse).value()).place(worldgenlevel, chunkgenerator, randomsource, blockpos);
   }
}