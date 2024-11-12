package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

public class RandomSelectorFeature extends Feature<RandomFeatureConfiguration> {
   public RandomSelectorFeature(Codec<RandomFeatureConfiguration> p_66619_) {
      super(p_66619_);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<RandomFeatureConfiguration> p_160212_) {
      RandomFeatureConfiguration randomfeatureconfiguration = p_160212_.config();
      RandomSource randomsource = p_160212_.random();
      WorldGenLevel worldgenlevel = p_160212_.level();
      ChunkGenerator chunkgenerator = p_160212_.chunkGenerator();
      BlockPos blockpos = p_160212_.origin();

      for(WeightedPlacedFeature weightedplacedfeature : randomfeatureconfiguration.features) {
         if (randomsource.nextFloat() < weightedplacedfeature.chance) {
            return weightedplacedfeature.place(worldgenlevel, chunkgenerator, randomsource, blockpos);
         }
      }

      return randomfeatureconfiguration.defaultFeature.value().place(worldgenlevel, chunkgenerator, randomsource, blockpos);
   }
}