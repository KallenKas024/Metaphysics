package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
   public ReplaceBlockFeature(Codec<ReplaceBlockConfiguration> p_66651_) {
      super(p_66651_);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<ReplaceBlockConfiguration> p_160216_) {
      WorldGenLevel worldgenlevel = p_160216_.level();
      BlockPos blockpos = p_160216_.origin();
      ReplaceBlockConfiguration replaceblockconfiguration = p_160216_.config();

      for(OreConfiguration.TargetBlockState oreconfiguration$targetblockstate : replaceblockconfiguration.targetStates) {
         if (oreconfiguration$targetblockstate.target.test(worldgenlevel.getBlockState(blockpos), p_160216_.random())) {
            worldgenlevel.setBlock(blockpos, oreconfiguration$targetblockstate.state, 2);
            break;
         }
      }

      return true;
   }
}