package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class MangroveTreeGrower extends AbstractTreeGrower {
   private final float tallProbability;

   public MangroveTreeGrower(float pTallProbability) {
      this.tallProbability = pTallProbability;
   }

   /**
    * @return the key of this tree
    */
   @Nullable
   protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom, boolean pHasFlowers) {
      return pRandom.nextFloat() < this.tallProbability ? TreeFeatures.TALL_MANGROVE : TreeFeatures.MANGROVE;
   }
}