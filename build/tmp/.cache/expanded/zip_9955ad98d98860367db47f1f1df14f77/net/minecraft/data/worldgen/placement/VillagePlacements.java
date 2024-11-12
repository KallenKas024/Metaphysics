package net.minecraft.data.worldgen.placement;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.PileFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VillagePlacements {
   public static final ResourceKey<PlacedFeature> PILE_HAY_VILLAGE = PlacementUtils.createKey("pile_hay");
   public static final ResourceKey<PlacedFeature> PILE_MELON_VILLAGE = PlacementUtils.createKey("pile_melon");
   public static final ResourceKey<PlacedFeature> PILE_SNOW_VILLAGE = PlacementUtils.createKey("pile_snow");
   public static final ResourceKey<PlacedFeature> PILE_ICE_VILLAGE = PlacementUtils.createKey("pile_ice");
   public static final ResourceKey<PlacedFeature> PILE_PUMPKIN_VILLAGE = PlacementUtils.createKey("pile_pumpkin");
   public static final ResourceKey<PlacedFeature> OAK_VILLAGE = PlacementUtils.createKey("oak");
   public static final ResourceKey<PlacedFeature> ACACIA_VILLAGE = PlacementUtils.createKey("acacia");
   public static final ResourceKey<PlacedFeature> SPRUCE_VILLAGE = PlacementUtils.createKey("spruce");
   public static final ResourceKey<PlacedFeature> PINE_VILLAGE = PlacementUtils.createKey("pine");
   public static final ResourceKey<PlacedFeature> PATCH_CACTUS_VILLAGE = PlacementUtils.createKey("patch_cactus");
   public static final ResourceKey<PlacedFeature> FLOWER_PLAIN_VILLAGE = PlacementUtils.createKey("flower_plain");
   public static final ResourceKey<PlacedFeature> PATCH_TAIGA_GRASS_VILLAGE = PlacementUtils.createKey("patch_taiga_grass");
   public static final ResourceKey<PlacedFeature> PATCH_BERRY_BUSH_VILLAGE = PlacementUtils.createKey("patch_berry_bush");

   public static void bootstrap(BootstapContext<PlacedFeature> pContext) {
      HolderGetter<ConfiguredFeature<?, ?>> holdergetter = pContext.lookup(Registries.CONFIGURED_FEATURE);
      Holder<ConfiguredFeature<?, ?>> holder = holdergetter.getOrThrow(PileFeatures.PILE_HAY);
      Holder<ConfiguredFeature<?, ?>> holder1 = holdergetter.getOrThrow(PileFeatures.PILE_MELON);
      Holder<ConfiguredFeature<?, ?>> holder2 = holdergetter.getOrThrow(PileFeatures.PILE_SNOW);
      Holder<ConfiguredFeature<?, ?>> holder3 = holdergetter.getOrThrow(PileFeatures.PILE_ICE);
      Holder<ConfiguredFeature<?, ?>> holder4 = holdergetter.getOrThrow(PileFeatures.PILE_PUMPKIN);
      Holder<ConfiguredFeature<?, ?>> holder5 = holdergetter.getOrThrow(TreeFeatures.OAK);
      Holder<ConfiguredFeature<?, ?>> holder6 = holdergetter.getOrThrow(TreeFeatures.ACACIA);
      Holder<ConfiguredFeature<?, ?>> holder7 = holdergetter.getOrThrow(TreeFeatures.SPRUCE);
      Holder<ConfiguredFeature<?, ?>> holder8 = holdergetter.getOrThrow(TreeFeatures.PINE);
      Holder<ConfiguredFeature<?, ?>> holder9 = holdergetter.getOrThrow(VegetationFeatures.PATCH_CACTUS);
      Holder<ConfiguredFeature<?, ?>> holder10 = holdergetter.getOrThrow(VegetationFeatures.FLOWER_PLAIN);
      Holder<ConfiguredFeature<?, ?>> holder11 = holdergetter.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
      Holder<ConfiguredFeature<?, ?>> holder12 = holdergetter.getOrThrow(VegetationFeatures.PATCH_BERRY_BUSH);
      PlacementUtils.register(pContext, PILE_HAY_VILLAGE, holder);
      PlacementUtils.register(pContext, PILE_MELON_VILLAGE, holder1);
      PlacementUtils.register(pContext, PILE_SNOW_VILLAGE, holder2);
      PlacementUtils.register(pContext, PILE_ICE_VILLAGE, holder3);
      PlacementUtils.register(pContext, PILE_PUMPKIN_VILLAGE, holder4);
      PlacementUtils.register(pContext, OAK_VILLAGE, holder5, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
      PlacementUtils.register(pContext, ACACIA_VILLAGE, holder6, PlacementUtils.filteredByBlockSurvival(Blocks.ACACIA_SAPLING));
      PlacementUtils.register(pContext, SPRUCE_VILLAGE, holder7, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
      PlacementUtils.register(pContext, PINE_VILLAGE, holder8, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
      PlacementUtils.register(pContext, PATCH_CACTUS_VILLAGE, holder9);
      PlacementUtils.register(pContext, FLOWER_PLAIN_VILLAGE, holder10);
      PlacementUtils.register(pContext, PATCH_TAIGA_GRASS_VILLAGE, holder11);
      PlacementUtils.register(pContext, PATCH_BERRY_BUSH_VILLAGE, holder12);
   }
}