package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;

/**
 * This samples the sum of two individual samplers of perlin noise octaves.
 * The input coordinates are scaled by {@link #INPUT_FACTOR}, and the result is scaled by {@link #valueFactor}.
 */
public class NormalNoise {
   private static final double INPUT_FACTOR = 1.0181268882175227D;
   private static final double TARGET_DEVIATION = 0.3333333333333333D;
   private final double valueFactor;
   private final PerlinNoise first;
   private final PerlinNoise second;
   private final double maxValue;
   private final NormalNoise.NoiseParameters parameters;

   /** @deprecated */
   @Deprecated
   public static NormalNoise createLegacyNetherBiome(RandomSource pRandom, NormalNoise.NoiseParameters pParameters) {
      return new NormalNoise(pRandom, pParameters, false);
   }

   public static NormalNoise create(RandomSource pRandom, int pFirstOctave, double... pAmplitudes) {
      return create(pRandom, new NormalNoise.NoiseParameters(pFirstOctave, new DoubleArrayList(pAmplitudes)));
   }

   public static NormalNoise create(RandomSource pRandom, NormalNoise.NoiseParameters pParameters) {
      return new NormalNoise(pRandom, pParameters, true);
   }

   private NormalNoise(RandomSource pRandom, NormalNoise.NoiseParameters pParameters, boolean pUseLegacyNetherBiome) {
      int i = pParameters.firstOctave;
      DoubleList doublelist = pParameters.amplitudes;
      this.parameters = pParameters;
      if (pUseLegacyNetherBiome) {
         this.first = PerlinNoise.create(pRandom, i, doublelist);
         this.second = PerlinNoise.create(pRandom, i, doublelist);
      } else {
         this.first = PerlinNoise.createLegacyForLegacyNetherBiome(pRandom, i, doublelist);
         this.second = PerlinNoise.createLegacyForLegacyNetherBiome(pRandom, i, doublelist);
      }

      int j = Integer.MAX_VALUE;
      int k = Integer.MIN_VALUE;
      DoubleListIterator doublelistiterator = doublelist.iterator();

      while(doublelistiterator.hasNext()) {
         int l = doublelistiterator.nextIndex();
         double d0 = doublelistiterator.nextDouble();
         if (d0 != 0.0D) {
            j = Math.min(j, l);
            k = Math.max(k, l);
         }
      }

      this.valueFactor = 0.16666666666666666D / expectedDeviation(k - j);
      this.maxValue = (this.first.maxValue() + this.second.maxValue()) * this.valueFactor;
   }

   public double maxValue() {
      return this.maxValue;
   }

   private static double expectedDeviation(int pOctaves) {
      return 0.1D * (1.0D + 1.0D / (double)(pOctaves + 1));
   }

   public double getValue(double pX, double pY, double pZ) {
      double d0 = pX * 1.0181268882175227D;
      double d1 = pY * 1.0181268882175227D;
      double d2 = pZ * 1.0181268882175227D;
      return (this.first.getValue(pX, pY, pZ) + this.second.getValue(d0, d1, d2)) * this.valueFactor;
   }

   public NormalNoise.NoiseParameters parameters() {
      return this.parameters;
   }

   @VisibleForTesting
   public void parityConfigString(StringBuilder pBuilder) {
      pBuilder.append("NormalNoise {");
      pBuilder.append("first: ");
      this.first.parityConfigString(pBuilder);
      pBuilder.append(", second: ");
      this.second.parityConfigString(pBuilder);
      pBuilder.append("}");
   }

   public static record NoiseParameters(int firstOctave, DoubleList amplitudes) {
      public static final Codec<NormalNoise.NoiseParameters> DIRECT_CODEC = RecordCodecBuilder.create((p_192865_) -> {
         return p_192865_.group(Codec.INT.fieldOf("firstOctave").forGetter(NormalNoise.NoiseParameters::firstOctave), Codec.DOUBLE.listOf().fieldOf("amplitudes").forGetter(NormalNoise.NoiseParameters::amplitudes)).apply(p_192865_, NormalNoise.NoiseParameters::new);
      });
      public static final Codec<Holder<NormalNoise.NoiseParameters>> CODEC = RegistryFileCodec.create(Registries.NOISE, DIRECT_CODEC);

      public NoiseParameters(int p_192861_, List<Double> p_192862_) {
         this(p_192861_, new DoubleArrayList(p_192862_));
      }

      public NoiseParameters(int pFirstOctave, double pAmplitude, double... pOtherAmplitudes) {
         this(pFirstOctave, Util.make(new DoubleArrayList(pOtherAmplitudes), (p_210636_) -> {
            p_210636_.add(0, pAmplitude);
         }));
      }
   }
}