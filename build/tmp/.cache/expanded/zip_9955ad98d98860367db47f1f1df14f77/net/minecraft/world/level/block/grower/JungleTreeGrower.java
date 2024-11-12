package net.minecraft.world.level.block.grower;

import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class JungleTreeGrower extends AbstractMegaTreeGrower {
   /**
    * @return the key of this tree
    */
   protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource p_255992_, boolean p_255946_) {
      return TreeFeatures.JUNGLE_TREE_NO_VINE;
   }

   /**
    * @return the key of the huge variant of this tree
    */
   protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource p_256359_) {
      return TreeFeatures.MEGA_JUNGLE_TREE;
   }
}