package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class FeatureUtils {
   public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> pContext) {
      AquaticFeatures.bootstrap(pContext);
      CaveFeatures.bootstrap(pContext);
      EndFeatures.bootstrap(pContext);
      MiscOverworldFeatures.bootstrap(pContext);
      NetherFeatures.bootstrap(pContext);
      OreFeatures.bootstrap(pContext);
      PileFeatures.bootstrap(pContext);
      TreeFeatures.bootstrap(pContext);
      VegetationFeatures.bootstrap(pContext);
   }

   private static BlockPredicate simplePatchPredicate(List<Block> pBlocks) {
      BlockPredicate blockpredicate;
      if (!pBlocks.isEmpty()) {
         blockpredicate = BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), pBlocks));
      } else {
         blockpredicate = BlockPredicate.ONLY_IN_AIR_PREDICATE;
      }

      return blockpredicate;
   }

   public static RandomPatchConfiguration simpleRandomPatchConfiguration(int pTries, Holder<PlacedFeature> pFeature) {
      return new RandomPatchConfiguration(pTries, 7, 3, pFeature);
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F pFeature, FC pConfig, List<Block> pBlocks, int pTries) {
      return simpleRandomPatchConfiguration(pTries, PlacementUtils.filtered(pFeature, pConfig, simplePatchPredicate(pBlocks)));
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F pFeature, FC pConfig, List<Block> pBlocks) {
      return simplePatchConfiguration(pFeature, pConfig, pBlocks, 96);
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> RandomPatchConfiguration simplePatchConfiguration(F pFeature, FC pConfig) {
      return simplePatchConfiguration(pFeature, pConfig, List.of(), 96);
   }

   public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String pName) {
      return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(pName));
   }

   public static void register(BootstapContext<ConfiguredFeature<?, ?>> pContext, ResourceKey<ConfiguredFeature<?, ?>> pKey, Feature<NoneFeatureConfiguration> pFeature) {
      register(pContext, pKey, pFeature, FeatureConfiguration.NONE);
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> pContext, ResourceKey<ConfiguredFeature<?, ?>> pKey, F pFeature, FC pConfig) {
      pContext.register(pKey, new ConfiguredFeature(pFeature, pConfig));
   }
}