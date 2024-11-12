package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ChorusPlantFeature extends Feature<NoneFeatureConfiguration> {
   public ChorusPlantFeature(Codec<NoneFeatureConfiguration> p_65360_) {
      super(p_65360_);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> p_159521_) {
      WorldGenLevel worldgenlevel = p_159521_.level();
      BlockPos blockpos = p_159521_.origin();
      RandomSource randomsource = p_159521_.random();
      if (worldgenlevel.isEmptyBlock(blockpos) && worldgenlevel.getBlockState(blockpos.below()).is(Blocks.END_STONE)) {
         ChorusFlowerBlock.generatePlant(worldgenlevel, blockpos, randomsource, 8);
         return true;
      } else {
         return false;
      }
   }
}