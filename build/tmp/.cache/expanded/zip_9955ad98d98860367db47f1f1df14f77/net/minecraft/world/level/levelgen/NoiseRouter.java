package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

public record NoiseRouter(DensityFunction barrierNoise, DensityFunction fluidLevelFloodednessNoise, DensityFunction fluidLevelSpreadNoise, DensityFunction lavaNoise, DensityFunction temperature, DensityFunction vegetation, DensityFunction continents, DensityFunction erosion, DensityFunction depth, DensityFunction ridges, DensityFunction initialDensityWithoutJaggedness, DensityFunction finalDensity, DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap) {
   public static final Codec<NoiseRouter> CODEC = RecordCodecBuilder.create((p_224411_) -> {
      return p_224411_.group(field("barrier", NoiseRouter::barrierNoise), field("fluid_level_floodedness", NoiseRouter::fluidLevelFloodednessNoise), field("fluid_level_spread", NoiseRouter::fluidLevelSpreadNoise), field("lava", NoiseRouter::lavaNoise), field("temperature", NoiseRouter::temperature), field("vegetation", NoiseRouter::vegetation), field("continents", NoiseRouter::continents), field("erosion", NoiseRouter::erosion), field("depth", NoiseRouter::depth), field("ridges", NoiseRouter::ridges), field("initial_density_without_jaggedness", NoiseRouter::initialDensityWithoutJaggedness), field("final_density", NoiseRouter::finalDensity), field("vein_toggle", NoiseRouter::veinToggle), field("vein_ridged", NoiseRouter::veinRidged), field("vein_gap", NoiseRouter::veinGap)).apply(p_224411_, NoiseRouter::new);
   });

   private static RecordCodecBuilder<NoiseRouter, DensityFunction> field(String pName, Function<NoiseRouter, DensityFunction> pGetter) {
      return DensityFunction.HOLDER_HELPER_CODEC.fieldOf(pName).forGetter(pGetter);
   }

   public NoiseRouter mapAll(DensityFunction.Visitor pVisitor) {
      return new NoiseRouter(this.barrierNoise.mapAll(pVisitor), this.fluidLevelFloodednessNoise.mapAll(pVisitor), this.fluidLevelSpreadNoise.mapAll(pVisitor), this.lavaNoise.mapAll(pVisitor), this.temperature.mapAll(pVisitor), this.vegetation.mapAll(pVisitor), this.continents.mapAll(pVisitor), this.erosion.mapAll(pVisitor), this.depth.mapAll(pVisitor), this.ridges.mapAll(pVisitor), this.initialDensityWithoutJaggedness.mapAll(pVisitor), this.finalDensity.mapAll(pVisitor), this.veinToggle.mapAll(pVisitor), this.veinRidged.mapAll(pVisitor), this.veinGap.mapAll(pVisitor));
   }
}