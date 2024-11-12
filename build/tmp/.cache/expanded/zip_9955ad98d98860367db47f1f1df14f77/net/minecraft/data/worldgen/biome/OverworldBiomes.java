package net.minecraft.data.worldgen.biome;

import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class OverworldBiomes {
   // TODO: getAdditionalOverworldBiomes, likely in this class. -C

   protected static final int NORMAL_WATER_COLOR = 4159204;
   protected static final int NORMAL_WATER_FOG_COLOR = 329011;
   private static final int OVERWORLD_FOG_COLOR = 12638463;
   @Nullable
   private static final Music NORMAL_MUSIC = null;

   protected static int calculateSkyColor(float pTemperature) {
      float $$1 = pTemperature / 3.0F;
      $$1 = Mth.clamp($$1, -1.0F, 1.0F);
      return Mth.hsvToRgb(0.62222224F - $$1 * 0.05F, 0.5F + $$1 * 0.1F, 1.0F);
   }

   private static Biome biome(boolean pHasPercipitation, float pTemperature, float pDownfall, MobSpawnSettings.Builder pMobSpawnSettings, BiomeGenerationSettings.Builder pGenerationSettings, @Nullable Music pBackgroundMusic) {
      return biome(pHasPercipitation, pTemperature, pDownfall, 4159204, 329011, (Integer)null, (Integer)null, pMobSpawnSettings, pGenerationSettings, pBackgroundMusic);
   }

   private static Biome biome(boolean pHasPrecipitation, float pTemperature, float pDownfall, int pWaterColor, int pWaterFogColor, @Nullable Integer pGrassColorOverride, @Nullable Integer pFoliageColorOverride, MobSpawnSettings.Builder pMobSpawnSettings, BiomeGenerationSettings.Builder pGenerationSettings, @Nullable Music pBackgroundMusic) {
      BiomeSpecialEffects.Builder biomespecialeffects$builder = (new BiomeSpecialEffects.Builder()).waterColor(pWaterColor).waterFogColor(pWaterFogColor).fogColor(12638463).skyColor(calculateSkyColor(pTemperature)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(pBackgroundMusic);
      if (pGrassColorOverride != null) {
         biomespecialeffects$builder.grassColorOverride(pGrassColorOverride);
      }

      if (pFoliageColorOverride != null) {
         biomespecialeffects$builder.foliageColorOverride(pFoliageColorOverride);
      }

      return (new Biome.BiomeBuilder()).hasPrecipitation(pHasPrecipitation).temperature(pTemperature).downfall(pDownfall).specialEffects(biomespecialeffects$builder.build()).mobSpawnSettings(pMobSpawnSettings.build()).generationSettings(pGenerationSettings.build()).build();
   }

   private static void globalOverworldGeneration(BiomeGenerationSettings.Builder pGenerationSettings) {
      BiomeDefaultFeatures.addDefaultCarversAndLakes(pGenerationSettings);
      BiomeDefaultFeatures.addDefaultCrystalFormations(pGenerationSettings);
      BiomeDefaultFeatures.addDefaultMonsterRoom(pGenerationSettings);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(pGenerationSettings);
      BiomeDefaultFeatures.addDefaultSprings(pGenerationSettings);
      BiomeDefaultFeatures.addSurfaceFreezing(pGenerationSettings);
   }

   public static Biome oldGrowthTaiga(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsSpruce) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4));
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
      if (pIsSpruce) {
         BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      } else {
         BiomeDefaultFeatures.caveSpawns(mobspawnsettings$builder);
         BiomeDefaultFeatures.monsters(mobspawnsettings$builder, 100, 25, 100, false);
      }

      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addMossyStoneBlock(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addFerns(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, pIsSpruce ? VegetationPlacements.TREES_OLD_GROWTH_SPRUCE_TAIGA : VegetationPlacements.TREES_OLD_GROWTH_PINE_TAIGA);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addGiantTaigaVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addCommonBerryBushes(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_OLD_GROWTH_TAIGA);
      return biome(true, pIsSpruce ? 0.25F : 0.3F, 0.8F, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }

   public static Biome sparseJungle(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
      return baseJungle(pPlacedFeatures, pWorldCarvers, 0.8F, false, true, false, mobspawnsettings$builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SPARSE_JUNGLE));
   }

   public static Biome jungle(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 40, 1, 2)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 1, 1, 2));
      return baseJungle(pPlacedFeatures, pWorldCarvers, 0.9F, false, false, true, mobspawnsettings$builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_JUNGLE));
   }

   public static Biome bambooJungle(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 40, 1, 2)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 80, 1, 2)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
      return baseJungle(pPlacedFeatures, pWorldCarvers, 0.9F, true, false, true, mobspawnsettings$builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BAMBOO_JUNGLE));
   }

   private static Biome baseJungle(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, float pDownfall, boolean pIsBambooJungle, boolean pIsSparse, boolean pAddBamboo, MobSpawnSettings.Builder pMobSpawnSettings, Music pBackgroudMusic) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pIsBambooJungle) {
         BiomeDefaultFeatures.addBambooVegetation(biomegenerationsettings$builder);
      } else {
         if (pAddBamboo) {
            BiomeDefaultFeatures.addLightBambooVegetation(biomegenerationsettings$builder);
         }

         if (pIsSparse) {
            BiomeDefaultFeatures.addSparseJungleTrees(biomegenerationsettings$builder);
         } else {
            BiomeDefaultFeatures.addJungleTrees(biomegenerationsettings$builder);
         }
      }

      BiomeDefaultFeatures.addWarmFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addJungleGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addJungleVines(biomegenerationsettings$builder);
      if (pIsSparse) {
         BiomeDefaultFeatures.addSparseJungleMelons(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addJungleMelons(biomegenerationsettings$builder);
      }

      return biome(true, 0.95F, pDownfall, pMobSpawnSettings, biomegenerationsettings$builder, pBackgroudMusic);
   }

   public static Biome windsweptHills(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsForest) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 5, 4, 6));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pIsForest) {
         BiomeDefaultFeatures.addMountainForestTrees(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addMountainTrees(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings$builder);
      return biome(true, 0.2F, 0.3F, mobspawnsettings$builder, biomegenerationsettings$builder, NORMAL_MUSIC);
   }

   public static Biome desert(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.desertSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings$builder);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDesertVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDesertExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDesertExtraDecoration(biomegenerationsettings$builder);
      return biome(false, 2.0F, 0.0F, mobspawnsettings$builder, biomegenerationsettings$builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DESERT));
   }

   public static Biome plains(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsSunflowerPlains, boolean pIsCold, boolean pIsIceSpikes) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      if (pIsCold) {
         mobspawnsettings$builder.creatureGenerationProbability(0.07F);
         BiomeDefaultFeatures.snowySpawns(mobspawnsettings$builder);
         if (pIsIceSpikes) {
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_SPIKE);
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_PATCH);
         }
      } else {
         BiomeDefaultFeatures.plainsSpawns(mobspawnsettings$builder);
         BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings$builder);
         if (pIsSunflowerPlains) {
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUNFLOWER);
         }
      }

      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pIsCold) {
         BiomeDefaultFeatures.addSnowyTrees(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addPlainVegetation(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      if (pIsSunflowerPlains) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE);
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
      } else {
         BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      }

      float f = pIsCold ? 0.0F : 0.8F;
      return biome(true, f, pIsCold ? 0.5F : 0.4F, mobspawnsettings$builder, biomegenerationsettings$builder, NORMAL_MUSIC);
   }

   public static Biome mushroomFields(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.mooshroomSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addMushroomFieldVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      return biome(true, 0.9F, 1.0F, mobspawnsettings$builder, biomegenerationsettings$builder, NORMAL_MUSIC);
   }

   public static Biome savanna(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsShatteredSavanna, boolean pIsPlateau) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      if (!pIsShatteredSavanna) {
         BiomeDefaultFeatures.addSavannaGrass(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pIsShatteredSavanna) {
         BiomeDefaultFeatures.addShatteredSavannaTrees(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addShatteredSavannaGrass(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addSavannaTrees(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addWarmFlowers(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addSavannaExtraGrass(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 1, 2, 6)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 1));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      if (pIsPlateau) {
         mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 8, 4, 4));
      }

      return biome(false, 2.0F, 0.0F, mobspawnsettings$builder, biomegenerationsettings$builder, NORMAL_MUSIC);
   }

   public static Biome badlands(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pTrees) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addExtraGold(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pTrees) {
         BiomeDefaultFeatures.addBadlandsTrees(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addBadlandGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addBadlandExtraVegetation(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).hasPrecipitation(false).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0F)).foliageColorOverride(10387789).grassColorOverride(9470285).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BADLANDS)).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   private static Biome baseOcean(MobSpawnSettings.Builder pMobSpawnSettings, int pWaterColor, int pWaterFogColor, BiomeGenerationSettings.Builder pGenerationSettings) {
      return biome(true, 0.5F, 0.5F, pWaterColor, pWaterFogColor, (Integer)null, (Integer)null, pMobSpawnSettings, pGenerationSettings, NORMAL_MUSIC);
   }

   private static BiomeGenerationSettings.Builder baseOceanGeneration(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addWaterTrees(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      return biomegenerationsettings$builder;
   }

   public static Biome coldOcean(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsDeep) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 3, 4, 15);
      mobspawnsettings$builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(pPlacedFeatures, pWorldCarvers);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, pIsDeep ? AquaticPlacements.SEAGRASS_DEEP_COLD : AquaticPlacements.SEAGRASS_COLD);
      BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addColdOceanExtraVegetation(biomegenerationsettings$builder);
      return baseOcean(mobspawnsettings$builder, 4020182, 329011, biomegenerationsettings$builder);
   }

   public static Biome ocean(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsDeep) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 1, 4, 10);
      mobspawnsettings$builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(pPlacedFeatures, pWorldCarvers);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, pIsDeep ? AquaticPlacements.SEAGRASS_DEEP : AquaticPlacements.SEAGRASS_NORMAL);
      BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addColdOceanExtraVegetation(biomegenerationsettings$builder);
      return baseOcean(mobspawnsettings$builder, 4159204, 329011, biomegenerationsettings$builder);
   }

   public static Biome lukeWarmOcean(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsDeep) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      if (pIsDeep) {
         BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 8, 4, 8);
      } else {
         BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 10, 2, 15);
      }

      mobspawnsettings$builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8)).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(pPlacedFeatures, pWorldCarvers);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, pIsDeep ? AquaticPlacements.SEAGRASS_DEEP_WARM : AquaticPlacements.SEAGRASS_WARM);
      if (pIsDeep) {
         BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addLukeWarmKelp(biomegenerationsettings$builder);
      return baseOcean(mobspawnsettings$builder, 4566514, 267827, biomegenerationsettings$builder);
   }

   public static Biome warmOcean(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
      BiomeDefaultFeatures.warmOceanSpawns(mobspawnsettings$builder, 10, 4);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(pPlacedFeatures, pWorldCarvers).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.WARM_OCEAN_VEGETATION).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_WARM).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEA_PICKLE);
      return baseOcean(mobspawnsettings$builder, 4445678, 270131, biomegenerationsettings$builder);
   }

   public static Biome frozenOcean(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsDeep) {
      MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
      float f = pIsDeep ? 0.5F : 0.0F;
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      BiomeDefaultFeatures.addIcebergs(biomegenerationsettings$builder);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addBlueIce(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addWaterTrees(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).hasPrecipitation(true).temperature(f).temperatureAdjustment(Biome.TemperatureModifier.FROZEN).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(3750089).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome forest(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsBirchForest, boolean pTallBirchTrees, boolean pIsFlowerForest) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      Music music;
      if (pIsFlowerForest) {
         music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FLOWER_FOREST);
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FOREST_FLOWERS);
      } else {
         music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST);
         BiomeDefaultFeatures.addForestFlowers(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pIsFlowerForest) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_FLOWER_FOREST);
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FLOWER_FOREST);
         BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      } else {
         if (pIsBirchForest) {
            if (pTallBirchTrees) {
               BiomeDefaultFeatures.addTallBirchTrees(biomegenerationsettings$builder);
            } else {
               BiomeDefaultFeatures.addBirchTrees(biomegenerationsettings$builder);
            }
         } else {
            BiomeDefaultFeatures.addOtherBirchTrees(biomegenerationsettings$builder);
         }

         BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addForestGrass(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      if (pIsFlowerForest) {
         mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
      } else if (!pIsBirchForest) {
         mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 5, 4, 4));
      }

      float f = pIsBirchForest ? 0.6F : 0.7F;
      return biome(true, f, pIsBirchForest ? 0.6F : 0.8F, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }

   public static Biome taiga(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsCold) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      float f = pIsCold ? -0.5F : 0.25F;
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addFerns(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addTaigaTrees(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addTaigaGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      if (pIsCold) {
         BiomeDefaultFeatures.addRareBerryBushes(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addCommonBerryBushes(biomegenerationsettings$builder);
      }

      return biome(true, f, pIsCold ? 0.4F : 0.8F, pIsCold ? 4020182 : 4159204, 329011, (Integer)null, (Integer)null, mobspawnsettings$builder, biomegenerationsettings$builder, NORMAL_MUSIC);
   }

   public static Biome darkForest(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.DARK_FOREST_VEGETATION);
      BiomeDefaultFeatures.addForestFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addForestGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST);
      return (new Biome.BiomeBuilder()).hasPrecipitation(true).temperature(0.7F).downfall(0.8F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7F)).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome swamp(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FROG, 10, 2, 5));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings$builder);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSwampClayDisk(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSwampVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSwampExtraVegetation(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SWAMP);
      return (new Biome.BiomeBuilder()).hasPrecipitation(true).temperature(0.8F).downfall(0.9F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(6388580).waterFogColor(2302743).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).foliageColorOverride(6975545).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome mangroveSwamp(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FROG, 10, 2, 5));
      mobspawnsettings$builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings$builder);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addMangroveSwampDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addMangroveSwampVegetation(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SWAMP);
      return (new Biome.BiomeBuilder()).hasPrecipitation(true).temperature(0.8F).downfall(0.9F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(3832426).waterFogColor(5077600).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).foliageColorOverride(9285927).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome river(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsCold) {
      MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, pIsCold ? 1 : 100, 1, 1));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addWaterTrees(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      if (!pIsCold) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_RIVER);
      }

      float f = pIsCold ? 0.0F : 0.5F;
      return biome(true, f, 0.5F, pIsCold ? 3750089 : 4159204, 329011, (Integer)null, (Integer)null, mobspawnsettings$builder, biomegenerationsettings$builder, NORMAL_MUSIC);
   }

   public static Biome beach(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsCold, boolean pIsStony) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      boolean flag = !pIsStony && !pIsCold;
      if (flag) {
         mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.TURTLE, 5, 2, 5));
      }

      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      float f;
      if (pIsCold) {
         f = 0.05F;
      } else if (pIsStony) {
         f = 0.2F;
      } else {
         f = 0.8F;
      }

      return biome(true, f, flag ? 0.4F : 0.3F, pIsCold ? 4020182 : 4159204, 329011, (Integer)null, (Integer)null, mobspawnsettings$builder, biomegenerationsettings$builder, NORMAL_MUSIC);
   }

   public static Biome theVoid(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, MiscOverworldPlacements.VOID_START_PLATFORM);
      return biome(false, 0.5F, 0.5F, new MobSpawnSettings.Builder(), biomegenerationsettings$builder, NORMAL_MUSIC);
   }

   public static Biome meadowOrCherryGrove(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers, boolean pIsCherryGrove) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(pIsCherryGrove ? EntityType.PIG : EntityType.DONKEY, 1, 1, 2)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 2, 6)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 2, 2, 4));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (pIsCherryGrove) {
         BiomeDefaultFeatures.addCherryGroveVegetation(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addMeadowVegetation(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(pIsCherryGrove ? SoundEvents.MUSIC_BIOME_CHERRY_GROVE : SoundEvents.MUSIC_BIOME_MEADOW);
      return pIsCherryGrove ? biome(true, 0.5F, 0.8F, 6141935, 6141935, 11983713, 11983713, mobspawnsettings$builder, biomegenerationsettings$builder, music) : biome(true, 0.5F, 0.8F, 937679, 329011, (Integer)null, (Integer)null, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }

   public static Biome frozenPeaks(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addFrozenSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FROZEN_PEAKS);
      return biome(true, -0.7F, 0.9F, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }

   public static Biome jaggedPeaks(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addFrozenSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_JAGGED_PEAKS);
      return biome(true, -0.7F, 0.9F, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }

   public static Biome stonyPeaks(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_STONY_PEAKS);
      return biome(true, 1.0F, 0.3F, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }

   public static Biome snowySlopes(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addFrozenSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SNOWY_SLOPES);
      return biome(true, -0.3F, 0.9F, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }

   public static Biome grove(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addFrozenSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addGroveTrees(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_GROVE);
      return biome(true, -0.2F, 0.8F, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }

   public static Biome lushCaves(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      mobspawnsettings$builder.addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 10, 4, 6));
      mobspawnsettings$builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addLushCavesSpecialOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addLushCavesVegetationFeatures(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
      return biome(true, 0.5F, 0.5F, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }

   public static Biome dripstoneCaves(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.dripstoneCavesSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      globalOverworldGeneration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder, true);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDripstone(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DRIPSTONE_CAVES);
      return biome(true, 0.8F, 0.4F, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }

   public static Biome deepDark(HolderGetter<PlacedFeature> pPlacedFeatures, HolderGetter<ConfiguredWorldCarver<?>> pWorldCarvers) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder(pPlacedFeatures, pWorldCarvers);
      biomegenerationsettings$builder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
      biomegenerationsettings$builder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE_EXTRA_UNDERGROUND);
      biomegenerationsettings$builder.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSculk(biomegenerationsettings$builder);
      Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DEEP_DARK);
      return biome(true, 0.8F, 0.4F, mobspawnsettings$builder, biomegenerationsettings$builder, music);
   }
}
