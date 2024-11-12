package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ClampedNormalFloat extends FloatProvider {
   public static final Codec<ClampedNormalFloat> CODEC = RecordCodecBuilder.<ClampedNormalFloat>create((p_146431_) -> {
      return p_146431_.group(Codec.FLOAT.fieldOf("mean").forGetter((p_146449_) -> {
         return p_146449_.mean;
      }), Codec.FLOAT.fieldOf("deviation").forGetter((p_146447_) -> {
         return p_146447_.deviation;
      }), Codec.FLOAT.fieldOf("min").forGetter((p_146445_) -> {
         return p_146445_.min;
      }), Codec.FLOAT.fieldOf("max").forGetter((p_146442_) -> {
         return p_146442_.max;
      })).apply(p_146431_, ClampedNormalFloat::new);
   }).comapFlatMap((p_274935_) -> {
      return p_274935_.max < p_274935_.min ? DataResult.error(() -> {
         return "Max must be larger than min: [" + p_274935_.min + ", " + p_274935_.max + "]";
      }) : DataResult.success(p_274935_);
   }, Function.identity());
   private final float mean;
   private final float deviation;
   private final float min;
   private final float max;

   public static ClampedNormalFloat of(float pMean, float pDeviation, float pMin, float pMax) {
      return new ClampedNormalFloat(pMean, pDeviation, pMin, pMax);
   }

   private ClampedNormalFloat(float p_146418_, float p_146419_, float p_146420_, float p_146421_) {
      this.mean = p_146418_;
      this.deviation = p_146419_;
      this.min = p_146420_;
      this.max = p_146421_;
   }

   public float sample(RandomSource pRandom) {
      return sample(pRandom, this.mean, this.deviation, this.min, this.max);
   }

   public static float sample(RandomSource pRandom, float pMean, float pDeviation, float pMin, float pMax) {
      return Mth.clamp(Mth.normal(pRandom, pMean, pDeviation), pMin, pMax);
   }

   public float getMinValue() {
      return this.min;
   }

   public float getMaxValue() {
      return this.max;
   }

   public FloatProviderType<?> getType() {
      return FloatProviderType.CLAMPED_NORMAL;
   }

   public String toString() {
      return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min + "-" + this.max + "]";
   }
}