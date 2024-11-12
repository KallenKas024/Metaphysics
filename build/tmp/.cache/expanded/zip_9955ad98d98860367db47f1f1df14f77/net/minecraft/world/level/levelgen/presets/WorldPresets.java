package net.minecraft.world.level.levelgen.presets;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class WorldPresets {
   public static final ResourceKey<WorldPreset> NORMAL = register("normal");
   public static final ResourceKey<WorldPreset> FLAT = register("flat");
   public static final ResourceKey<WorldPreset> LARGE_BIOMES = register("large_biomes");
   public static final ResourceKey<WorldPreset> AMPLIFIED = register("amplified");
   public static final ResourceKey<WorldPreset> SINGLE_BIOME_SURFACE = register("single_biome_surface");
   public static final ResourceKey<WorldPreset> DEBUG = register("debug_all_block_states");

   public static void bootstrap(BootstapContext<WorldPreset> pContext) {
      (new WorldPresets.Bootstrap(pContext)).bootstrap();
   }

   private static ResourceKey<WorldPreset> register(String pName) {
      return ResourceKey.create(Registries.WORLD_PRESET, new ResourceLocation(pName));
   }

   public static Optional<ResourceKey<WorldPreset>> fromSettings(Registry<LevelStem> pRegistry) {
      return pRegistry.getOptional(LevelStem.OVERWORLD).flatMap((p_251294_) -> {
         ChunkGenerator chunkgenerator = p_251294_.generator();
         if (chunkgenerator instanceof FlatLevelSource) {
            return Optional.of(FLAT);
         } else {
            return chunkgenerator instanceof DebugLevelSource ? Optional.of(DEBUG) : Optional.empty();
         }
      });
   }

   public static WorldDimensions createNormalWorldDimensions(RegistryAccess pRegistry) {
      return pRegistry.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(NORMAL).value().createWorldDimensions();
   }

   public static LevelStem getNormalOverworld(RegistryAccess pRegistry) {
      return pRegistry.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(NORMAL).value().overworld().orElseThrow();
   }

   static class Bootstrap {
      private final BootstapContext<WorldPreset> context;
      private final HolderGetter<NoiseGeneratorSettings> noiseSettings;
      private final HolderGetter<Biome> biomes;
      private final HolderGetter<PlacedFeature> placedFeatures;
      private final HolderGetter<StructureSet> structureSets;
      private final HolderGetter<MultiNoiseBiomeSourceParameterList> multiNoiseBiomeSourceParameterLists;
      private final Holder<DimensionType> overworldDimensionType;
      private final LevelStem netherStem;
      private final LevelStem endStem;

      Bootstrap(BootstapContext<WorldPreset> pContext) {
         this.context = pContext;
         HolderGetter<DimensionType> holdergetter = pContext.lookup(Registries.DIMENSION_TYPE);
         this.noiseSettings = pContext.lookup(Registries.NOISE_SETTINGS);
         this.biomes = pContext.lookup(Registries.BIOME);
         this.placedFeatures = pContext.lookup(Registries.PLACED_FEATURE);
         this.structureSets = pContext.lookup(Registries.STRUCTURE_SET);
         this.multiNoiseBiomeSourceParameterLists = pContext.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);
         this.overworldDimensionType = holdergetter.getOrThrow(BuiltinDimensionTypes.OVERWORLD);
         Holder<DimensionType> holder = holdergetter.getOrThrow(BuiltinDimensionTypes.NETHER);
         Holder<NoiseGeneratorSettings> holder1 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
         Holder.Reference<MultiNoiseBiomeSourceParameterList> reference = this.multiNoiseBiomeSourceParameterLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER);
         this.netherStem = new LevelStem(holder, new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromPreset(reference), holder1));
         Holder<DimensionType> holder2 = holdergetter.getOrThrow(BuiltinDimensionTypes.END);
         Holder<NoiseGeneratorSettings> holder3 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.END);
         this.endStem = new LevelStem(holder2, new NoiseBasedChunkGenerator(TheEndBiomeSource.create(this.biomes), holder3));
      }

      private LevelStem makeOverworld(ChunkGenerator pGenerator) {
         return new LevelStem(this.overworldDimensionType, pGenerator);
      }

      private LevelStem makeNoiseBasedOverworld(BiomeSource pBiomeSource, Holder<NoiseGeneratorSettings> pSettings) {
         return this.makeOverworld(new NoiseBasedChunkGenerator(pBiomeSource, pSettings));
      }

      private WorldPreset createPresetWithCustomOverworld(LevelStem pOverworldStem) {
         return new WorldPreset(Map.of(LevelStem.OVERWORLD, pOverworldStem, LevelStem.NETHER, this.netherStem, LevelStem.END, this.endStem));
      }

      private void registerCustomOverworldPreset(ResourceKey<WorldPreset> pDimensionKey, LevelStem pLevelStem) {
         this.context.register(pDimensionKey, this.createPresetWithCustomOverworld(pLevelStem));
      }

      private void registerOverworlds(BiomeSource pBiomeSource) {
         Holder<NoiseGeneratorSettings> holder = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
         this.registerCustomOverworldPreset(WorldPresets.NORMAL, this.makeNoiseBasedOverworld(pBiomeSource, holder));
         Holder<NoiseGeneratorSettings> holder1 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.LARGE_BIOMES);
         this.registerCustomOverworldPreset(WorldPresets.LARGE_BIOMES, this.makeNoiseBasedOverworld(pBiomeSource, holder1));
         Holder<NoiseGeneratorSettings> holder2 = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.AMPLIFIED);
         this.registerCustomOverworldPreset(WorldPresets.AMPLIFIED, this.makeNoiseBasedOverworld(pBiomeSource, holder2));
      }

      public void bootstrap() {
         Holder.Reference<MultiNoiseBiomeSourceParameterList> reference = this.multiNoiseBiomeSourceParameterLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
         this.registerOverworlds(MultiNoiseBiomeSource.createFromPreset(reference));
         Holder<NoiseGeneratorSettings> holder = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
         Holder.Reference<Biome> reference1 = this.biomes.getOrThrow(Biomes.PLAINS);
         this.registerCustomOverworldPreset(WorldPresets.SINGLE_BIOME_SURFACE, this.makeNoiseBasedOverworld(new FixedBiomeSource(reference1), holder));
         this.registerCustomOverworldPreset(WorldPresets.FLAT, this.makeOverworld(new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(this.biomes, this.structureSets, this.placedFeatures))));
         this.registerCustomOverworldPreset(WorldPresets.DEBUG, this.makeOverworld(new DebugLevelSource(reference1)));
      }
   }
}