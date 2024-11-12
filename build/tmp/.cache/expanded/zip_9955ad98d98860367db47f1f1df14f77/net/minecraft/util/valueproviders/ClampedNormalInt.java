package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ClampedNormalInt extends IntProvider {
   public static final Codec<ClampedNormalInt> CODEC = RecordCodecBuilder.<ClampedNormalInt>create((p_185887_) -> {
      return p_185887_.group(Codec.FLOAT.fieldOf("mean").forGetter((p_185905_) -> {
         return p_185905_.mean;
      }), Codec.FLOAT.fieldOf("deviation").forGetter((p_185903_) -> {
         return p_185903_.deviation;
      }), Codec.INT.fieldOf("min_inclusive").forGetter((p_185901_) -> {
         return p_185901_.min_inclusive;
      }), Codec.INT.fieldOf("max_inclusive").forGetter((p_185898_) -> {
         return p_185898_.max_inclusive;
      })).apply(p_185887_, ClampedNormalInt::new);
   }).comapFlatMap((p_274937_) -> {
      return p_274937_.max_inclusive < p_274937_.min_inclusive ? DataResult.error(() -> {
         return "Max must be larger than min: [" + p_274937_.min_inclusive + ", " + p_274937_.max_inclusive + "]";
      }) : DataResult.success(p_274937_);
   }, Function.identity());
   private final float mean;
   private final float deviation;
   private final int min_inclusive;
   private final int max_inclusive;

   public static ClampedNormalInt of(float pMean, float pDeviation, int pMinInclusive, int pMaxInclusive) {
      return new ClampedNormalInt(pMean, pDeviation, pMinInclusive, pMaxInclusive);
   }

   private ClampedNormalInt(float p_185874_, float p_185875_, int p_185876_, int p_185877_) {
      this.mean = p_185874_;
      this.deviation = p_185875_;
      this.min_inclusive = p_185876_;
      this.max_inclusive = p_185877_;
   }

   public int sample(RandomSource pRandom) {
      return sample(pRandom, this.mean, this.deviation, (float)this.min_inclusive, (float)this.max_inclusive);
   }

   public static int sample(RandomSource pRandom, float pMean, float pDeviation, float pMinInclusive, float pMaxInclusive) {
      return (int)Mth.clamp(Mth.normal(pRandom, pMean, pDeviation), pMinInclusive, pMaxInclusive);
   }

   public int getMinValue() {
      return this.min_inclusive;
   }

   public int getMaxValue() {
      return this.max_inclusive;
   }

   public IntProviderType<?> getType() {
      return IntProviderType.CLAMPED_NORMAL;
   }

   public String toString() {
      return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min_inclusive + "-" + this.max_inclusive + "]";
   }
}