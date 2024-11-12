package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class FlatLevelGeneratorPresets {
   public static final ResourceKey<FlatLevelGeneratorPreset> CLASSIC_FLAT = register("classic_flat");
   public static final ResourceKey<FlatLevelGeneratorPreset> TUNNELERS_DREAM = register("tunnelers_dream");
   public static final ResourceKey<FlatLevelGeneratorPreset> WATER_WORLD = register("water_world");
   public static final ResourceKey<FlatLevelGeneratorPreset> OVERWORLD = register("overworld");
   public static final ResourceKey<FlatLevelGeneratorPreset> SNOWY_KINGDOM = register("snowy_kingdom");
   public static final ResourceKey<FlatLevelGeneratorPreset> BOTTOMLESS_PIT = register("bottomless_pit");
   public static final ResourceKey<FlatLevelGeneratorPreset> DESERT = register("desert");
   public static final ResourceKey<FlatLevelGeneratorPreset> REDSTONE_READY = register("redstone_ready");
   public static final ResourceKey<FlatLevelGeneratorPreset> THE_VOID = register("the_void");

   public static void bootstrap(BootstapContext<FlatLevelGeneratorPreset> pContext) {
      (new FlatLevelGeneratorPresets.Bootstrap(pContext)).run();
   }

   private static ResourceKey<FlatLevelGeneratorPreset> register(String pName) {
      return ResourceKey.create(Registries.FLAT_LEVEL_GENERATOR_PRESET, new ResourceLocation(pName));
   }

   static class Bootstrap {
      private final BootstapContext<FlatLevelGeneratorPreset> context;

      Bootstrap(BootstapContext<FlatLevelGeneratorPreset> pContext) {
         this.context = pContext;
      }

      private void register(ResourceKey<FlatLevelGeneratorPreset> pPresetKey, ItemLike pDisplayItem, ResourceKey<Biome> pBiomeKey, Set<ResourceKey<StructureSet>> pStructureSetKeys, boolean pSetDecoration, boolean pAddLakes, FlatLayerInfo... pFlatLayerInfos) {
         HolderGetter<StructureSet> holdergetter = this.context.lookup(Registries.STRUCTURE_SET);
         HolderGetter<PlacedFeature> holdergetter1 = this.context.lookup(Registries.PLACED_FEATURE);
         HolderGetter<Biome> holdergetter2 = this.context.lookup(Registries.BIOME);
         HolderSet.Direct<StructureSet> direct = HolderSet.direct(pStructureSetKeys.stream().map(holdergetter::getOrThrow).collect(Collectors.toList()));
         FlatLevelGeneratorSettings flatlevelgeneratorsettings = new FlatLevelGeneratorSettings(Optional.of(direct), holdergetter2.getOrThrow(pBiomeKey), FlatLevelGeneratorSettings.createLakesList(holdergetter1));
         if (pSetDecoration) {
            flatlevelgeneratorsettings.setDecoration();
         }

         if (pAddLakes) {
            flatlevelgeneratorsettings.setAddLakes();
         }

         for(int i = pFlatLayerInfos.length - 1; i >= 0; --i) {
            flatlevelgeneratorsettings.getLayersInfo().add(pFlatLayerInfos[i]);
         }

         this.context.register(pPresetKey, new FlatLevelGeneratorPreset(pDisplayItem.asItem().builtInRegistryHolder(), flatlevelgeneratorsettings));
      }

      public void run() {
         this.register(FlatLevelGeneratorPresets.CLASSIC_FLAT, Blocks.GRASS_BLOCK, Biomes.PLAINS, ImmutableSet.of(BuiltinStructureSets.VILLAGES), false, false, new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(2, Blocks.DIRT), new FlatLayerInfo(1, Blocks.BEDROCK));
         this.register(FlatLevelGeneratorPresets.TUNNELERS_DREAM, Blocks.STONE, Biomes.WINDSWEPT_HILLS, ImmutableSet.of(BuiltinStructureSets.MINESHAFTS, BuiltinStructureSets.STRONGHOLDS), true, false, new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(230, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
         this.register(FlatLevelGeneratorPresets.WATER_WORLD, Items.WATER_BUCKET, Biomes.DEEP_OCEAN, ImmutableSet.of(BuiltinStructureSets.OCEAN_RUINS, BuiltinStructureSets.SHIPWRECKS, BuiltinStructureSets.OCEAN_MONUMENTS), false, false, new FlatLayerInfo(90, Blocks.WATER), new FlatLayerInfo(5, Blocks.GRAVEL), new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(5, Blocks.STONE), new FlatLayerInfo(64, Blocks.DEEPSLATE), new FlatLayerInfo(1, Blocks.BEDROCK));
         this.register(FlatLevelGeneratorPresets.OVERWORLD, Blocks.GRASS, Biomes.PLAINS, ImmutableSet.of(BuiltinStructureSets.VILLAGES, BuiltinStructureSets.MINESHAFTS, BuiltinStructureSets.PILLAGER_OUTPOSTS, BuiltinStructureSets.RUINED_PORTALS, BuiltinStructureSets.STRONGHOLDS), true, true, new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
         this.register(FlatLevelGeneratorPresets.SNOWY_KINGDOM, Blocks.SNOW, Biomes.SNOWY_PLAINS, ImmutableSet.of(BuiltinStructureSets.VILLAGES, BuiltinStructureSets.IGLOOS), false, false, new FlatLayerInfo(1, Blocks.SNOW), new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
         this.register(FlatLevelGeneratorPresets.BOTTOMLESS_PIT, Items.FEATHER, Biomes.PLAINS, ImmutableSet.of(BuiltinStructureSets.VILLAGES), false, false, new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(2, Blocks.COBBLESTONE));
         this.register(FlatLevelGeneratorPresets.DESERT, Blocks.SAND, Biomes.DESERT, ImmutableSet.of(BuiltinStructureSets.VILLAGES, BuiltinStructureSets.DESERT_PYRAMIDS, BuiltinStructureSets.MINESHAFTS, BuiltinStructureSets.STRONGHOLDS), true, false, new FlatLayerInfo(8, Blocks.SAND), new FlatLayerInfo(52, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
         this.register(FlatLevelGeneratorPresets.REDSTONE_READY, Items.REDSTONE, Biomes.DESERT, ImmutableSet.of(), false, false, new FlatLayerInfo(116, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
         this.register(FlatLevelGeneratorPresets.THE_VOID, Blocks.BARRIER, Biomes.THE_VOID, ImmutableSet.of(), true, false, new FlatLayerInfo(1, Blocks.AIR));
      }
   }
}