package net.minecraft.world.level.levelgen;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class RandomState {
   final PositionalRandomFactory random;
   private final HolderGetter<NormalNoise.NoiseParameters> noises;
   private final NoiseRouter router;
   private final Climate.Sampler sampler;
   private final SurfaceSystem surfaceSystem;
   private final PositionalRandomFactory aquiferRandom;
   private final PositionalRandomFactory oreRandom;
   private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances;
   private final Map<ResourceLocation, PositionalRandomFactory> positionalRandoms;

   public static RandomState create(HolderGetter.Provider pRegistries, ResourceKey<NoiseGeneratorSettings> pSettingsKey, long pLevelSeed) {
      return create(pRegistries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(pSettingsKey).value(), pRegistries.lookupOrThrow(Registries.NOISE), pLevelSeed);
   }

   public static RandomState create(NoiseGeneratorSettings pSettings, HolderGetter<NormalNoise.NoiseParameters> pNoiseParametersGetter, long pLevelSeed) {
      return new RandomState(pSettings, pNoiseParametersGetter, pLevelSeed);
   }

   private RandomState(NoiseGeneratorSettings pSettings, HolderGetter<NormalNoise.NoiseParameters> pNoiseParametersGetter, final long pLevelSeed) {
      this.random = pSettings.getRandomSource().newInstance(pLevelSeed).forkPositional();
      this.noises = pNoiseParametersGetter;
      this.aquiferRandom = this.random.fromHashOf(new ResourceLocation("aquifer")).forkPositional();
      this.oreRandom = this.random.fromHashOf(new ResourceLocation("ore")).forkPositional();
      this.noiseIntances = new ConcurrentHashMap<>();
      this.positionalRandoms = new ConcurrentHashMap<>();
      this.surfaceSystem = new SurfaceSystem(this, pSettings.defaultBlock(), pSettings.seaLevel(), this.random);
      final boolean flag = pSettings.useLegacyRandomSource();

      class NoiseWiringHelper implements DensityFunction.Visitor {
         private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();

         private RandomSource newLegacyInstance(long p_224592_) {
            return new LegacyRandomSource(pLevelSeed + p_224592_);
         }

         public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder p_224594_) {
            Holder<NormalNoise.NoiseParameters> holder = p_224594_.noiseData();
            if (flag) {
               if (holder.is(Noises.TEMPERATURE)) {
                  NormalNoise normalnoise3 = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(0L), new NormalNoise.NoiseParameters(-7, 1.0D, 1.0D));
                  return new DensityFunction.NoiseHolder(holder, normalnoise3);
               }

               if (holder.is(Noises.VEGETATION)) {
                  NormalNoise normalnoise2 = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(1L), new NormalNoise.NoiseParameters(-7, 1.0D, 1.0D));
                  return new DensityFunction.NoiseHolder(holder, normalnoise2);
               }

               if (holder.is(Noises.SHIFT)) {
                  NormalNoise normalnoise1 = NormalNoise.create(RandomState.this.random.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0D));
                  return new DensityFunction.NoiseHolder(holder, normalnoise1);
               }
            }

            NormalNoise normalnoise = RandomState.this.getOrCreateNoise(holder.unwrapKey().orElseThrow());
            return new DensityFunction.NoiseHolder(holder, normalnoise);
         }

         private DensityFunction wrapNew(DensityFunction p_224596_) {
            if (p_224596_ instanceof BlendedNoise blendednoise) {
               RandomSource randomsource = flag ? this.newLegacyInstance(0L) : RandomState.this.random.fromHashOf(new ResourceLocation("terrain"));
               return blendednoise.withNewRandom(randomsource);
            } else {
               return (DensityFunction)(p_224596_ instanceof DensityFunctions.EndIslandDensityFunction ? new DensityFunctions.EndIslandDensityFunction(pLevelSeed) : p_224596_);
            }
         }

         public DensityFunction apply(DensityFunction p_224598_) {
            return this.wrapped.computeIfAbsent(p_224598_, this::wrapNew);
         }
      }

      this.router = pSettings.noiseRouter().mapAll(new NoiseWiringHelper());
      DensityFunction.Visitor densityfunction$visitor = new DensityFunction.Visitor() {
         private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();

         private DensityFunction wrapNew(DensityFunction p_249732_) {
            if (p_249732_ instanceof DensityFunctions.HolderHolder densityfunctions$holderholder) {
               return densityfunctions$holderholder.function().value();
            } else if (p_249732_ instanceof DensityFunctions.Marker densityfunctions$marker) {
               return densityfunctions$marker.wrapped();
            } else {
               return p_249732_;
            }
         }

         public DensityFunction apply(DensityFunction p_248616_) {
            return this.wrapped.computeIfAbsent(p_248616_, this::wrapNew);
         }
      };
      this.sampler = new Climate.Sampler(this.router.temperature().mapAll(densityfunction$visitor), this.router.vegetation().mapAll(densityfunction$visitor), this.router.continents().mapAll(densityfunction$visitor), this.router.erosion().mapAll(densityfunction$visitor), this.router.depth().mapAll(densityfunction$visitor), this.router.ridges().mapAll(densityfunction$visitor), pSettings.spawnTarget());
   }

   public NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> pResourceKey) {
      return this.noiseIntances.computeIfAbsent(pResourceKey, (p_255589_) -> {
         return Noises.instantiate(this.noises, this.random, pResourceKey);
      });
   }

   public PositionalRandomFactory getOrCreateRandomFactory(ResourceLocation pLocation) {
      return this.positionalRandoms.computeIfAbsent(pLocation, (p_224569_) -> {
         return this.random.fromHashOf(pLocation).forkPositional();
      });
   }

   public NoiseRouter router() {
      return this.router;
   }

   public Climate.Sampler sampler() {
      return this.sampler;
   }

   public SurfaceSystem surfaceSystem() {
      return this.surfaceSystem;
   }

   public PositionalRandomFactory aquiferRandom() {
      return this.aquiferRandom;
   }

   public PositionalRandomFactory oreRandom() {
      return this.oreRandom;
   }
}