package net.minecraft.data.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseData {
   /** @deprecated */
   @Deprecated
   public static final NormalNoise.NoiseParameters DEFAULT_SHIFT = new NormalNoise.NoiseParameters(-3, 1.0D, 1.0D, 1.0D, 0.0D);

   public static void bootstrap(BootstapContext<NormalNoise.NoiseParameters> pContext) {
      registerBiomeNoises(pContext, 0, Noises.TEMPERATURE, Noises.VEGETATION, Noises.CONTINENTALNESS, Noises.EROSION);
      registerBiomeNoises(pContext, -2, Noises.TEMPERATURE_LARGE, Noises.VEGETATION_LARGE, Noises.CONTINENTALNESS_LARGE, Noises.EROSION_LARGE);
      register(pContext, Noises.RIDGE, -7, 1.0D, 2.0D, 1.0D, 0.0D, 0.0D, 0.0D);
      pContext.register(Noises.SHIFT, DEFAULT_SHIFT);
      register(pContext, Noises.AQUIFER_BARRIER, -3, 1.0D);
      register(pContext, Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS, -7, 1.0D);
      register(pContext, Noises.AQUIFER_LAVA, -1, 1.0D);
      register(pContext, Noises.AQUIFER_FLUID_LEVEL_SPREAD, -5, 1.0D);
      register(pContext, Noises.PILLAR, -7, 1.0D, 1.0D);
      register(pContext, Noises.PILLAR_RARENESS, -8, 1.0D);
      register(pContext, Noises.PILLAR_THICKNESS, -8, 1.0D);
      register(pContext, Noises.SPAGHETTI_2D, -7, 1.0D);
      register(pContext, Noises.SPAGHETTI_2D_ELEVATION, -8, 1.0D);
      register(pContext, Noises.SPAGHETTI_2D_MODULATOR, -11, 1.0D);
      register(pContext, Noises.SPAGHETTI_2D_THICKNESS, -11, 1.0D);
      register(pContext, Noises.SPAGHETTI_3D_1, -7, 1.0D);
      register(pContext, Noises.SPAGHETTI_3D_2, -7, 1.0D);
      register(pContext, Noises.SPAGHETTI_3D_RARITY, -11, 1.0D);
      register(pContext, Noises.SPAGHETTI_3D_THICKNESS, -8, 1.0D);
      register(pContext, Noises.SPAGHETTI_ROUGHNESS, -5, 1.0D);
      register(pContext, Noises.SPAGHETTI_ROUGHNESS_MODULATOR, -8, 1.0D);
      register(pContext, Noises.CAVE_ENTRANCE, -7, 0.4D, 0.5D, 1.0D);
      register(pContext, Noises.CAVE_LAYER, -8, 1.0D);
      register(pContext, Noises.CAVE_CHEESE, -8, 0.5D, 1.0D, 2.0D, 1.0D, 2.0D, 1.0D, 0.0D, 2.0D, 0.0D);
      register(pContext, Noises.ORE_VEININESS, -8, 1.0D);
      register(pContext, Noises.ORE_VEIN_A, -7, 1.0D);
      register(pContext, Noises.ORE_VEIN_B, -7, 1.0D);
      register(pContext, Noises.ORE_GAP, -5, 1.0D);
      register(pContext, Noises.NOODLE, -8, 1.0D);
      register(pContext, Noises.NOODLE_THICKNESS, -8, 1.0D);
      register(pContext, Noises.NOODLE_RIDGE_A, -7, 1.0D);
      register(pContext, Noises.NOODLE_RIDGE_B, -7, 1.0D);
      register(pContext, Noises.JAGGED, -16, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.SURFACE, -6, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.SURFACE_SECONDARY, -6, 1.0D, 1.0D, 0.0D, 1.0D);
      register(pContext, Noises.CLAY_BANDS_OFFSET, -8, 1.0D);
      register(pContext, Noises.BADLANDS_PILLAR, -2, 1.0D, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.BADLANDS_PILLAR_ROOF, -8, 1.0D);
      register(pContext, Noises.BADLANDS_SURFACE, -6, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.ICEBERG_PILLAR, -6, 1.0D, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.ICEBERG_PILLAR_ROOF, -3, 1.0D);
      register(pContext, Noises.ICEBERG_SURFACE, -6, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.SWAMP, -2, 1.0D);
      register(pContext, Noises.CALCITE, -9, 1.0D, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.GRAVEL, -8, 1.0D, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.POWDER_SNOW, -6, 1.0D, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.PACKED_ICE, -7, 1.0D, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.ICE, -4, 1.0D, 1.0D, 1.0D, 1.0D);
      register(pContext, Noises.SOUL_SAND_LAYER, -8, 1.0D, 1.0D, 1.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.013333333333333334D);
      register(pContext, Noises.GRAVEL_LAYER, -8, 1.0D, 1.0D, 1.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.013333333333333334D);
      register(pContext, Noises.PATCH, -5, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.013333333333333334D);
      register(pContext, Noises.NETHERRACK, -3, 1.0D, 0.0D, 0.0D, 0.35D);
      register(pContext, Noises.NETHER_WART, -3, 1.0D, 0.0D, 0.0D, 0.9D);
      register(pContext, Noises.NETHER_STATE_SELECTOR, -4, 1.0D);
   }

   private static void registerBiomeNoises(BootstapContext<NormalNoise.NoiseParameters> pContext, int pFirstOctaveOffset, ResourceKey<NormalNoise.NoiseParameters> pTemperature, ResourceKey<NormalNoise.NoiseParameters> pVegetation, ResourceKey<NormalNoise.NoiseParameters> pContinentalness, ResourceKey<NormalNoise.NoiseParameters> pErosion) {
      register(pContext, pTemperature, -10 + pFirstOctaveOffset, 1.5D, 0.0D, 1.0D, 0.0D, 0.0D, 0.0D);
      register(pContext, pVegetation, -8 + pFirstOctaveOffset, 1.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D);
      register(pContext, pContinentalness, -9 + pFirstOctaveOffset, 1.0D, 1.0D, 2.0D, 2.0D, 2.0D, 1.0D, 1.0D, 1.0D, 1.0D);
      register(pContext, pErosion, -9 + pFirstOctaveOffset, 1.0D, 1.0D, 0.0D, 1.0D, 1.0D);
   }

   private static void register(BootstapContext<NormalNoise.NoiseParameters> pContext, ResourceKey<NormalNoise.NoiseParameters> pKey, int pFirstOctave, double pAmplitude, double... pOtherAmplitudes) {
      pContext.register(pKey, new NormalNoise.NoiseParameters(pFirstOctave, pAmplitude, pOtherAmplitudes));
   }
}