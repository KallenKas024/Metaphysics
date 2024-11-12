package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.stream.IntStream;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

/**
 * This class wraps three individual perlin noise octaves samplers.
 * It computes the octaves of the main noise, and then uses that as a linear interpolation value between the minimum and
 * maximum limit noises.
 */
public class BlendedNoise implements DensityFunction.SimpleFunction {
   private static final Codec<Double> SCALE_RANGE = Codec.doubleRange(0.001D, 1000.0D);
   private static final MapCodec<BlendedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec((p_230486_) -> {
      return p_230486_.group(SCALE_RANGE.fieldOf("xz_scale").forGetter((p_230497_) -> {
         return p_230497_.xzScale;
      }), SCALE_RANGE.fieldOf("y_scale").forGetter((p_230495_) -> {
         return p_230495_.yScale;
      }), SCALE_RANGE.fieldOf("xz_factor").forGetter((p_230493_) -> {
         return p_230493_.xzFactor;
      }), SCALE_RANGE.fieldOf("y_factor").forGetter((p_230490_) -> {
         return p_230490_.yFactor;
      }), Codec.doubleRange(1.0D, 8.0D).fieldOf("smear_scale_multiplier").forGetter((p_230488_) -> {
         return p_230488_.smearScaleMultiplier;
      })).apply(p_230486_, BlendedNoise::createUnseeded);
   });
   public static final KeyDispatchDataCodec<BlendedNoise> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);
   private final PerlinNoise minLimitNoise;
   private final PerlinNoise maxLimitNoise;
   private final PerlinNoise mainNoise;
   private final double xzMultiplier;
   private final double yMultiplier;
   private final double xzFactor;
   private final double yFactor;
   private final double smearScaleMultiplier;
   private final double maxValue;
   private final double xzScale;
   private final double yScale;

   public static BlendedNoise createUnseeded(double p_230478_, double p_230479_, double p_230480_, double p_230481_, double p_230482_) {
      return new BlendedNoise(new XoroshiroRandomSource(0L), p_230478_, p_230479_, p_230480_, p_230481_, p_230482_);
   }

   private BlendedNoise(PerlinNoise pMinLimitNoise, PerlinNoise pMaxLimitNoise, PerlinNoise pMainNoise, double pXzScale, double pYScale, double pXzFactor, double pYFactor, double pSmearScaleMultiplier) {
      this.minLimitNoise = pMinLimitNoise;
      this.maxLimitNoise = pMaxLimitNoise;
      this.mainNoise = pMainNoise;
      this.xzScale = pXzScale;
      this.yScale = pYScale;
      this.xzFactor = pXzFactor;
      this.yFactor = pYFactor;
      this.smearScaleMultiplier = pSmearScaleMultiplier;
      this.xzMultiplier = 684.412D * this.xzScale;
      this.yMultiplier = 684.412D * this.yScale;
      this.maxValue = pMinLimitNoise.maxBrokenValue(this.yMultiplier);
   }

   @VisibleForTesting
   public BlendedNoise(RandomSource pRandom, double pXzScale, double pYScale, double pXzFactor, double pYFactor, double pSmearScaleMultiplier) {
      this(PerlinNoise.createLegacyForBlendedNoise(pRandom, IntStream.rangeClosed(-15, 0)), PerlinNoise.createLegacyForBlendedNoise(pRandom, IntStream.rangeClosed(-15, 0)), PerlinNoise.createLegacyForBlendedNoise(pRandom, IntStream.rangeClosed(-7, 0)), pXzScale, pYScale, pXzFactor, pYFactor, pSmearScaleMultiplier);
   }

   public BlendedNoise withNewRandom(RandomSource pRandom) {
      return new BlendedNoise(pRandom, this.xzScale, this.yScale, this.xzFactor, this.yFactor, this.smearScaleMultiplier);
   }

   public double compute(DensityFunction.FunctionContext pContext) {
      double d0 = (double)pContext.blockX() * this.xzMultiplier;
      double d1 = (double)pContext.blockY() * this.yMultiplier;
      double d2 = (double)pContext.blockZ() * this.xzMultiplier;
      double d3 = d0 / this.xzFactor;
      double d4 = d1 / this.yFactor;
      double d5 = d2 / this.xzFactor;
      double d6 = this.yMultiplier * this.smearScaleMultiplier;
      double d7 = d6 / this.yFactor;
      double d8 = 0.0D;
      double d9 = 0.0D;
      double d10 = 0.0D;
      boolean flag = true;
      double d11 = 1.0D;

      for(int i = 0; i < 8; ++i) {
         ImprovedNoise improvednoise = this.mainNoise.getOctaveNoise(i);
         if (improvednoise != null) {
            d10 += improvednoise.noise(PerlinNoise.wrap(d3 * d11), PerlinNoise.wrap(d4 * d11), PerlinNoise.wrap(d5 * d11), d7 * d11, d4 * d11) / d11;
         }

         d11 /= 2.0D;
      }

      double d16 = (d10 / 10.0D + 1.0D) / 2.0D;
      boolean flag1 = d16 >= 1.0D;
      boolean flag2 = d16 <= 0.0D;
      d11 = 1.0D;

      for(int j = 0; j < 16; ++j) {
         double d12 = PerlinNoise.wrap(d0 * d11);
         double d13 = PerlinNoise.wrap(d1 * d11);
         double d14 = PerlinNoise.wrap(d2 * d11);
         double d15 = d6 * d11;
         if (!flag1) {
            ImprovedNoise improvednoise1 = this.minLimitNoise.getOctaveNoise(j);
            if (improvednoise1 != null) {
               d8 += improvednoise1.noise(d12, d13, d14, d15, d1 * d11) / d11;
            }
         }

         if (!flag2) {
            ImprovedNoise improvednoise2 = this.maxLimitNoise.getOctaveNoise(j);
            if (improvednoise2 != null) {
               d9 += improvednoise2.noise(d12, d13, d14, d15, d1 * d11) / d11;
            }
         }

         d11 /= 2.0D;
      }

      return Mth.clampedLerp(d8 / 512.0D, d9 / 512.0D, d16) / 128.0D;
   }

   public double minValue() {
      return -this.maxValue();
   }

   public double maxValue() {
      return this.maxValue;
   }

   @VisibleForTesting
   public void parityConfigString(StringBuilder pBuilder) {
      pBuilder.append("BlendedNoise{minLimitNoise=");
      this.minLimitNoise.parityConfigString(pBuilder);
      pBuilder.append(", maxLimitNoise=");
      this.maxLimitNoise.parityConfigString(pBuilder);
      pBuilder.append(", mainNoise=");
      this.mainNoise.parityConfigString(pBuilder);
      pBuilder.append(String.format(Locale.ROOT, ", xzScale=%.3f, yScale=%.3f, xzMainScale=%.3f, yMainScale=%.3f, cellWidth=4, cellHeight=8", 684.412D, 684.412D, 8.555150000000001D, 4.277575000000001D)).append('}');
   }

   public KeyDispatchDataCodec<? extends DensityFunction> codec() {
      return CODEC;
   }
}