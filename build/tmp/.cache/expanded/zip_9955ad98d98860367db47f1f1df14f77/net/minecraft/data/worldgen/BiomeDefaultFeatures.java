package net.minecraft.data.worldgen;

import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeDefaultFeatures {
   public static void addDefaultCarversAndLakes(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
      pBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE_EXTRA_UNDERGROUND);
      pBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
      pBuilder.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND);
      pBuilder.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_SURFACE);
   }

   public static void addDefaultMonsterRoom(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.MONSTER_ROOM);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.MONSTER_ROOM_DEEP);
   }

   public static void addDefaultUndergroundVariety(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIRT);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GRAVEL);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GRANITE_UPPER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GRANITE_LOWER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIORITE_UPPER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIORITE_LOWER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_ANDESITE_UPPER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_ANDESITE_LOWER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_TUFF);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.GLOW_LICHEN);
   }

   public static void addDripstone(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.LARGE_DRIPSTONE);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.DRIPSTONE_CLUSTER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.POINTED_DRIPSTONE);
   }

   public static void addSculk(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.SCULK_VEIN);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, CavePlacements.SCULK_PATCH_DEEP_DARK);
   }

   public static void addDefaultOres(BiomeGenerationSettings.Builder pBuilder) {
      addDefaultOres(pBuilder, false);
   }

   public static void addDefaultOres(BiomeGenerationSettings.Builder pBuilder, boolean pLargeOres) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_COAL_UPPER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_COAL_LOWER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_IRON_UPPER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_IRON_MIDDLE);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_IRON_SMALL);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GOLD);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GOLD_LOWER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_REDSTONE);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_REDSTONE_LOWER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIAMOND);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIAMOND_LARGE);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIAMOND_BURIED);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_LAPIS);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_LAPIS_BURIED);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, pLargeOres ? OrePlacements.ORE_COPPER_LARGE : OrePlacements.ORE_COPPER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, CavePlacements.UNDERWATER_MAGMA);
   }

   public static void addExtraGold(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_GOLD_EXTRA);
   }

   public static void addExtraEmeralds(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_EMERALD);
   }

   public static void addInfestedStone(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_INFESTED);
   }

   public static void addDefaultSoftDisks(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_SAND);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_CLAY);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_GRAVEL);
   }

   public static void addSwampClayDisk(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_CLAY);
   }

   public static void addMangroveSwampDisks(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_GRASS);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiscOverworldPlacements.DISK_CLAY);
   }

   public static void addMossyStoneBlock(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.FOREST_ROCK);
   }

   public static void addFerns(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_LARGE_FERN);
   }

   public static void addRareBerryBushes(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_BERRY_RARE);
   }

   public static void addCommonBerryBushes(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_BERRY_COMMON);
   }

   public static void addLightBambooVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BAMBOO_LIGHT);
   }

   public static void addBambooVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BAMBOO);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BAMBOO_VEGETATION);
   }

   public static void addTaigaTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_TAIGA);
   }

   public static void addGroveTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_GROVE);
   }

   public static void addWaterTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WATER);
   }

   public static void addBirchTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BIRCH);
   }

   public static void addOtherBirchTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BIRCH_AND_OAK);
   }

   public static void addTallBirchTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BIRCH_TALL);
   }

   public static void addSavannaTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SAVANNA);
   }

   public static void addShatteredSavannaTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WINDSWEPT_SAVANNA);
   }

   public static void addLushCavesVegetationFeatures(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_CEILING_VEGETATION);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CAVE_VINES);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_CLAY);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_VEGETATION);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.ROOTED_AZALEA_TREE);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.SPORE_BLOSSOM);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CLASSIC_VINES);
   }

   public static void addLushCavesSpecialOres(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
   }

   public static void addMountainTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WINDSWEPT_HILLS);
   }

   public static void addMountainForestTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_WINDSWEPT_FOREST);
   }

   public static void addJungleTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_JUNGLE);
   }

   public static void addSparseJungleTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SPARSE_JUNGLE);
   }

   public static void addBadlandsTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BADLANDS);
   }

   public static void addSnowyTrees(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SNOWY);
   }

   public static void addJungleGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_JUNGLE);
   }

   public static void addSavannaGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_TALL_GRASS);
   }

   public static void addShatteredSavannaGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_NORMAL);
   }

   public static void addSavannaExtraGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_SAVANNA);
   }

   public static void addBadlandGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_BADLANDS);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH_BADLANDS);
   }

   public static void addForestFlowers(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FOREST_FLOWERS);
   }

   public static void addForestGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_FOREST);
   }

   public static void addSwampVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SWAMP);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_SWAMP);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_NORMAL);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_SWAMP);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_SWAMP);
   }

   public static void addMangroveSwampVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_MANGROVE);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_NORMAL);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
   }

   public static void addMushroomFieldVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.MUSHROOM_ISLAND_VEGETATION);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_TAIGA);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_TAIGA);
   }

   public static void addPlainVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_PLAINS);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_PLAINS);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
   }

   public static void addDesertVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH_2);
   }

   public static void addGiantTaigaVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_TAIGA);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_OLD_GROWTH);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_OLD_GROWTH);
   }

   public static void addDefaultFlowers(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_DEFAULT);
   }

   public static void addCherryGroveVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_CHERRY);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_CHERRY);
   }

   public static void addMeadowVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_MEADOW);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_MEADOW);
   }

   public static void addWarmFlowers(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_WARM);
   }

   public static void addDefaultGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_BADLANDS);
   }

   public static void addTaigaGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_TAIGA_2);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_TAIGA);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_TAIGA);
   }

   public static void addPlainGrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_TALL_GRASS_2);
   }

   public static void addDefaultMushrooms(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_NORMAL);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_NORMAL);
   }

   public static void addDefaultExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
   }

   public static void addBadlandExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE_BADLANDS);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_CACTUS_DECORATED);
   }

   public static void addJungleMelons(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_MELON);
   }

   public static void addSparseJungleMelons(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_MELON_SPARSE);
   }

   public static void addJungleVines(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.VINES);
   }

   public static void addDesertExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE_DESERT);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_CACTUS_DESERT);
   }

   public static void addSwampExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE_SWAMP);
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
   }

   public static void addDesertExtraDecoration(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.DESERT_WELL);
   }

   public static void addFossilDecoration(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.FOSSIL_UPPER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, CavePlacements.FOSSIL_LOWER);
   }

   public static void addColdOceanExtraVegetation(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.KELP_COLD);
   }

   public static void addDefaultSeagrass(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SIMPLE);
   }

   public static void addLukeWarmKelp(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.KELP_WARM);
   }

   public static void addDefaultSprings(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_WATER);
      pBuilder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_LAVA);
   }

   public static void addFrozenSprings(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, MiscOverworldPlacements.SPRING_LAVA_FROZEN);
   }

   public static void addIcebergs(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.ICEBERG_PACKED);
      pBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.ICEBERG_BLUE);
   }

   public static void addBlueIce(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.BLUE_ICE);
   }

   public static void addSurfaceFreezing(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, MiscOverworldPlacements.FREEZE_TOP_LAYER);
   }

   public static void addNetherDefaultOres(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_GRAVEL_NETHER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_BLACKSTONE);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_GOLD_NETHER);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_QUARTZ_NETHER);
      addAncientDebris(pBuilder);
   }

   public static void addAncientDebris(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_ANCIENT_DEBRIS_LARGE);
      pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_ANCIENT_DEBRIS_SMALL);
   }

   public static void addDefaultCrystalFormations(BiomeGenerationSettings.Builder pBuilder) {
      pBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.AMETHYST_GEODE);
   }

   public static void farmAnimals(MobSpawnSettings.Builder pBuilder) {
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 12, 4, 4));
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 10, 4, 4));
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 4, 4));
   }

   public static void caveSpawns(MobSpawnSettings.Builder pBuilder) {
      pBuilder.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 10, 8, 8));
      pBuilder.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 4, 6));
   }

   public static void commonSpawns(MobSpawnSettings.Builder pBuilder) {
      caveSpawns(pBuilder);
      monsters(pBuilder, 95, 5, 100, false);
   }

   public static void oceanSpawns(MobSpawnSettings.Builder pBuilder, int pSquidWeight, int pSquidMaxCount, int pCodWeight) {
      pBuilder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, pSquidWeight, 1, pSquidMaxCount));
      pBuilder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, pCodWeight, 3, 6));
      commonSpawns(pBuilder);
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
   }

   public static void warmOceanSpawns(MobSpawnSettings.Builder pBuilder, int pSquidWeight, int pSquidMinCount) {
      pBuilder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, pSquidWeight, pSquidMinCount, 4));
      pBuilder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
      pBuilder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
      commonSpawns(pBuilder);
   }

   public static void plainsSpawns(MobSpawnSettings.Builder pBuilder) {
      farmAnimals(pBuilder);
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 5, 2, 6));
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 3));
      commonSpawns(pBuilder);
   }

   public static void snowySpawns(MobSpawnSettings.Builder pBuilder) {
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 10, 2, 3));
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
      caveSpawns(pBuilder);
      monsters(pBuilder, 95, 5, 20, false);
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 80, 4, 4));
   }

   public static void desertSpawns(MobSpawnSettings.Builder pBuilder) {
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
      caveSpawns(pBuilder);
      monsters(pBuilder, 19, 1, 100, false);
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 80, 4, 4));
   }

   public static void dripstoneCavesSpawns(MobSpawnSettings.Builder pBuilder) {
      caveSpawns(pBuilder);
      int i = 95;
      monsters(pBuilder, 95, 5, 100, false);
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 95, 4, 4));
   }

   public static void monsters(MobSpawnSettings.Builder pBuilder, int pZombieWeight, int pZombieVillageWeight, int pSkeletonWeight, boolean pIsUnderwater) {
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 100, 4, 4));
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(pIsUnderwater ? EntityType.DROWNED : EntityType.ZOMBIE, pZombieWeight, 4, 4));
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, pZombieVillageWeight, 1, 1));
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, pSkeletonWeight, 4, 4));
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 100, 4, 4));
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 100, 4, 4));
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1));
   }

   public static void mooshroomSpawns(MobSpawnSettings.Builder pBuilder) {
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.MOOSHROOM, 8, 4, 8));
      caveSpawns(pBuilder);
   }

   public static void baseJungleSpawns(MobSpawnSettings.Builder pBuilder) {
      farmAnimals(pBuilder);
      pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
      commonSpawns(pBuilder);
   }

   public static void endSpawns(MobSpawnSettings.Builder pBuilder) {
      pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 4, 4));
   }
}