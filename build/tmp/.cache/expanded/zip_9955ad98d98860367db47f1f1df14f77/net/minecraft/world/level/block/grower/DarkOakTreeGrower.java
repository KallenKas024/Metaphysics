package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class DarkOakTreeGrower extends AbstractMegaTreeGrower {
   /**
    * @return the key of this tree
    */
   @Nullable
   protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource p_222924_, boolean p_222925_) {
      return null;
   }

   /**
    * @return the key of the huge variant of this tree
    */
   @Nullable
   protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource p_255891_) {
      return TreeFeatures.DARK_OAK;
   }
}