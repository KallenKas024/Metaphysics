package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.PrimaryLevelData;

public record WorldDimensions(Registry<LevelStem> dimensions) {
   public static final MapCodec<WorldDimensions> CODEC = RecordCodecBuilder.mapCodec((p_258996_) -> {
      return p_258996_.group(RegistryCodecs.fullCodec(Registries.LEVEL_STEM, Lifecycle.stable(), LevelStem.CODEC).fieldOf("dimensions").forGetter(WorldDimensions::dimensions)).apply(p_258996_, p_258996_.stable(WorldDimensions::new));
   });
   private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = ImmutableSet.of(LevelStem.OVERWORLD, LevelStem.NETHER, LevelStem.END);
   private static final int VANILLA_DIMENSION_COUNT = BUILTIN_ORDER.size();

   public WorldDimensions {
      LevelStem levelstem = dimensions.get(LevelStem.OVERWORLD);
      if (levelstem == null) {
         throw new IllegalStateException("Overworld settings missing");
      }
   }

   public static Stream<ResourceKey<LevelStem>> keysInOrder(Stream<ResourceKey<LevelStem>> pStemKeys) {
      return Stream.concat(BUILTIN_ORDER.stream(), pStemKeys.filter((p_251885_) -> {
         return !BUILTIN_ORDER.contains(p_251885_);
      }));
   }

   public WorldDimensions replaceOverworldGenerator(RegistryAccess pRegistryAccess, ChunkGenerator pChunkGenerator) {
      Registry<DimensionType> registry = pRegistryAccess.registryOrThrow(Registries.DIMENSION_TYPE);
      Registry<LevelStem> registry1 = withOverworld(registry, this.dimensions, pChunkGenerator);
      return new WorldDimensions(registry1);
   }

   public static Registry<LevelStem> withOverworld(Registry<DimensionType> pDimensionTypeRegistry, Registry<LevelStem> pStemRegistry, ChunkGenerator pChunkGenerator) {
      LevelStem levelstem = pStemRegistry.get(LevelStem.OVERWORLD);
      Holder<DimensionType> holder = (Holder<DimensionType>)(levelstem == null ? pDimensionTypeRegistry.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD) : levelstem.type());
      return withOverworld(pStemRegistry, holder, pChunkGenerator);
   }

   public static Registry<LevelStem> withOverworld(Registry<LevelStem> pStemRegistry, Holder<DimensionType> pDimensionType, ChunkGenerator pChunkGenerator) {
      WritableRegistry<LevelStem> writableregistry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.experimental());
      writableregistry.register(LevelStem.OVERWORLD, new LevelStem(pDimensionType, pChunkGenerator), Lifecycle.stable());

      for(Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : pStemRegistry.entrySet()) {
         ResourceKey<LevelStem> resourcekey = entry.getKey();
         if (resourcekey != LevelStem.OVERWORLD) {
            writableregistry.register(resourcekey, entry.getValue(), pStemRegistry.lifecycle(entry.getValue()));
         }
      }

      return writableregistry.freeze();
   }

   public ChunkGenerator overworld() {
      LevelStem levelstem = this.dimensions.get(LevelStem.OVERWORLD);
      if (levelstem == null) {
         throw new IllegalStateException("Overworld settings missing");
      } else {
         return levelstem.generator();
      }
   }

   public Optional<LevelStem> get(ResourceKey<LevelStem> pStemKey) {
      return this.dimensions.getOptional(pStemKey);
   }

   public ImmutableSet<ResourceKey<Level>> levels() {
      return this.dimensions().entrySet().stream().map(Map.Entry::getKey).map(Registries::levelStemToLevel).collect(ImmutableSet.toImmutableSet());
   }

   public boolean isDebug() {
      return this.overworld() instanceof DebugLevelSource;
   }

   private static PrimaryLevelData.SpecialWorldProperty specialWorldProperty(Registry<LevelStem> pStemRegistry) {
      return pStemRegistry.getOptional(LevelStem.OVERWORLD).map((p_251481_) -> {
         ChunkGenerator chunkgenerator = p_251481_.generator();
         if (chunkgenerator instanceof DebugLevelSource) {
            return PrimaryLevelData.SpecialWorldProperty.DEBUG;
         } else {
            return chunkgenerator instanceof FlatLevelSource ? PrimaryLevelData.SpecialWorldProperty.FLAT : PrimaryLevelData.SpecialWorldProperty.NONE;
         }
      }).orElse(PrimaryLevelData.SpecialWorldProperty.NONE);
   }

   static Lifecycle checkStability(ResourceKey<LevelStem> pKey, LevelStem pStem) {
      return isVanillaLike(pKey, pStem) ? Lifecycle.stable() : Lifecycle.experimental();
   }

   private static boolean isVanillaLike(ResourceKey<LevelStem> pKey, LevelStem pStem) {
      if (pKey == LevelStem.OVERWORLD) {
         return isStableOverworld(pStem);
      } else if (pKey == LevelStem.NETHER) {
         return isStableNether(pStem);
      } else {
         return pKey == LevelStem.END ? isStableEnd(pStem) : false;
      }
   }

   private static boolean isStableOverworld(LevelStem pLevelStem) {
      Holder<DimensionType> holder = pLevelStem.type();
      if (!holder.is(BuiltinDimensionTypes.OVERWORLD) && !holder.is(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
         return false;
      } else {
         BiomeSource biomesource = pLevelStem.generator().getBiomeSource();
         if (biomesource instanceof MultiNoiseBiomeSource) {
            MultiNoiseBiomeSource multinoisebiomesource = (MultiNoiseBiomeSource)biomesource;
            if (!multinoisebiomesource.stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD)) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean isStableNether(LevelStem pLevelStem) {
      if (pLevelStem.type().is(BuiltinDimensionTypes.NETHER)) {
         ChunkGenerator chunkgenerator = pLevelStem.generator();
         if (chunkgenerator instanceof NoiseBasedChunkGenerator) {
            NoiseBasedChunkGenerator noisebasedchunkgenerator = (NoiseBasedChunkGenerator)chunkgenerator;
            if (noisebasedchunkgenerator.stable(NoiseGeneratorSettings.NETHER)) {
               BiomeSource biomesource = noisebasedchunkgenerator.getBiomeSource();
               if (biomesource instanceof MultiNoiseBiomeSource) {
                  MultiNoiseBiomeSource multinoisebiomesource = (MultiNoiseBiomeSource)biomesource;
                  if (multinoisebiomesource.stable(MultiNoiseBiomeSourceParameterLists.NETHER)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static boolean isStableEnd(LevelStem pLevelStem) {
      if (pLevelStem.type().is(BuiltinDimensionTypes.END)) {
         ChunkGenerator chunkgenerator = pLevelStem.generator();
         if (chunkgenerator instanceof NoiseBasedChunkGenerator) {
            NoiseBasedChunkGenerator noisebasedchunkgenerator = (NoiseBasedChunkGenerator)chunkgenerator;
            if (noisebasedchunkgenerator.stable(NoiseGeneratorSettings.END) && noisebasedchunkgenerator.getBiomeSource() instanceof TheEndBiomeSource) {
               return true;
            }
         }
      }

      return false;
   }

   public WorldDimensions.Complete bake(Registry<LevelStem> pStemRegistry) {
      record Entry(ResourceKey<LevelStem> key, LevelStem value) {
         Lifecycle lifecycle() {
            return WorldDimensions.checkStability(this.key, this.value);
         }
      }
      Stream<ResourceKey<LevelStem>> stream = Stream.concat(pStemRegistry.registryKeySet().stream(), this.dimensions.registryKeySet().stream()).distinct();
      List<Entry> list = new ArrayList<>();
      keysInOrder(stream).forEach((p_248571_) -> {
         pStemRegistry.getOptional(p_248571_).or(() -> {
            return this.dimensions.getOptional(p_248571_);
         }).ifPresent((p_250263_) -> {
            list.add(new Entry(p_248571_, p_250263_));
         });
      });
      Lifecycle lifecycle = list.size() == VANILLA_DIMENSION_COUNT ? Lifecycle.stable() : Lifecycle.experimental();
      WritableRegistry<LevelStem> writableregistry = new MappedRegistry<>(Registries.LEVEL_STEM, lifecycle);
      list.forEach((p_259001_) -> {
         writableregistry.register(p_259001_.key, p_259001_.value, p_259001_.lifecycle());
      });
      Registry<LevelStem> registry = writableregistry.freeze();
      PrimaryLevelData.SpecialWorldProperty primaryleveldata$specialworldproperty = specialWorldProperty(registry);
      return new WorldDimensions.Complete(registry.freeze(), primaryleveldata$specialworldproperty);
   }

   public static record Complete(Registry<LevelStem> dimensions, PrimaryLevelData.SpecialWorldProperty specialWorldProperty) {
      public Lifecycle lifecycle() {
         return this.dimensions.registryLifecycle();
      }

      public RegistryAccess.Frozen dimensionsRegistryAccess() {
         return (new RegistryAccess.ImmutableRegistryAccess(List.of(this.dimensions))).freeze();
      }
   }
}