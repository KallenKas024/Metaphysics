package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Generates a single octave of Perlin noise.
 */
public final class ImprovedNoise {
   private static final float SHIFT_UP_EPSILON = 1.0E-7F;
   /**
    * A permutation array used in noise calculation.
    * This is populated with the values [0, 256) and shuffled per instance of {@code ImprovedNoise}.
    * 
    * @see #p(int)
    */
   private final byte[] p;
   public final double xo;
   public final double yo;
   public final double zo;

   public ImprovedNoise(RandomSource pRandom) {
      this.xo = pRandom.nextDouble() * 256.0D;
      this.yo = pRandom.nextDouble() * 256.0D;
      this.zo = pRandom.nextDouble() * 256.0D;
      this.p = new byte[256];

      for(int i = 0; i < 256; ++i) {
         this.p[i] = (byte)i;
      }

      for(int k = 0; k < 256; ++k) {
         int j = pRandom.nextInt(256 - k);
         byte b0 = this.p[k];
         this.p[k] = this.p[k + j];
         this.p[k + j] = b0;
      }

   }

   public double noise(double pX, double pY, double pZ) {
      return this.noise(pX, pY, pZ, 0.0D, 0.0D);
   }

   /** @deprecated */
   @Deprecated
   public double noise(double pX, double pY, double pZ, double pYScale, double pYMax) {
      double d0 = pX + this.xo;
      double d1 = pY + this.yo;
      double d2 = pZ + this.zo;
      int i = Mth.floor(d0);
      int j = Mth.floor(d1);
      int k = Mth.floor(d2);
      double d3 = d0 - (double)i;
      double d4 = d1 - (double)j;
      double d5 = d2 - (double)k;
      double d6;
      if (pYScale != 0.0D) {
         double d7;
         if (pYMax >= 0.0D && pYMax < d4) {
            d7 = pYMax;
         } else {
            d7 = d4;
         }

         d6 = (double)Mth.floor(d7 / pYScale + (double)1.0E-7F) * pYScale;
      } else {
         d6 = 0.0D;
      }

      return this.sampleAndLerp(i, j, k, d3, d4 - d6, d5, d4);
   }

   public double noiseWithDerivative(double pX, double pY, double pZ, double[] pValues) {
      double d0 = pX + this.xo;
      double d1 = pY + this.yo;
      double d2 = pZ + this.zo;
      int i = Mth.floor(d0);
      int j = Mth.floor(d1);
      int k = Mth.floor(d2);
      double d3 = d0 - (double)i;
      double d4 = d1 - (double)j;
      double d5 = d2 - (double)k;
      return this.sampleWithDerivative(i, j, k, d3, d4, d5, pValues);
   }

   private static double gradDot(int pGradIndex, double pXFactor, double pYFactor, double pZFactor) {
      return SimplexNoise.dot(SimplexNoise.GRADIENT[pGradIndex & 15], pXFactor, pYFactor, pZFactor);
   }

   private int p(int pIndex) {
      return this.p[pIndex & 255] & 255;
   }

   private double sampleAndLerp(int pGridX, int pGridY, int pGridZ, double pDeltaX, double pWeirdDeltaY, double pDeltaZ, double pDeltaY) {
      int i = this.p(pGridX);
      int j = this.p(pGridX + 1);
      int k = this.p(i + pGridY);
      int l = this.p(i + pGridY + 1);
      int i1 = this.p(j + pGridY);
      int j1 = this.p(j + pGridY + 1);
      double d0 = gradDot(this.p(k + pGridZ), pDeltaX, pWeirdDeltaY, pDeltaZ);
      double d1 = gradDot(this.p(i1 + pGridZ), pDeltaX - 1.0D, pWeirdDeltaY, pDeltaZ);
      double d2 = gradDot(this.p(l + pGridZ), pDeltaX, pWeirdDeltaY - 1.0D, pDeltaZ);
      double d3 = gradDot(this.p(j1 + pGridZ), pDeltaX - 1.0D, pWeirdDeltaY - 1.0D, pDeltaZ);
      double d4 = gradDot(this.p(k + pGridZ + 1), pDeltaX, pWeirdDeltaY, pDeltaZ - 1.0D);
      double d5 = gradDot(this.p(i1 + pGridZ + 1), pDeltaX - 1.0D, pWeirdDeltaY, pDeltaZ - 1.0D);
      double d6 = gradDot(this.p(l + pGridZ + 1), pDeltaX, pWeirdDeltaY - 1.0D, pDeltaZ - 1.0D);
      double d7 = gradDot(this.p(j1 + pGridZ + 1), pDeltaX - 1.0D, pWeirdDeltaY - 1.0D, pDeltaZ - 1.0D);
      double d8 = Mth.smoothstep(pDeltaX);
      double d9 = Mth.smoothstep(pDeltaY);
      double d10 = Mth.smoothstep(pDeltaZ);
      return Mth.lerp3(d8, d9, d10, d0, d1, d2, d3, d4, d5, d6, d7);
   }

   private double sampleWithDerivative(int pGridX, int pGridY, int pGridZ, double pDeltaX, double pDeltaY, double pDeltaZ, double[] pNoiseValues) {
      int i = this.p(pGridX);
      int j = this.p(pGridX + 1);
      int k = this.p(i + pGridY);
      int l = this.p(i + pGridY + 1);
      int i1 = this.p(j + pGridY);
      int j1 = this.p(j + pGridY + 1);
      int k1 = this.p(k + pGridZ);
      int l1 = this.p(i1 + pGridZ);
      int i2 = this.p(l + pGridZ);
      int j2 = this.p(j1 + pGridZ);
      int k2 = this.p(k + pGridZ + 1);
      int l2 = this.p(i1 + pGridZ + 1);
      int i3 = this.p(l + pGridZ + 1);
      int j3 = this.p(j1 + pGridZ + 1);
      int[] aint = SimplexNoise.GRADIENT[k1 & 15];
      int[] aint1 = SimplexNoise.GRADIENT[l1 & 15];
      int[] aint2 = SimplexNoise.GRADIENT[i2 & 15];
      int[] aint3 = SimplexNoise.GRADIENT[j2 & 15];
      int[] aint4 = SimplexNoise.GRADIENT[k2 & 15];
      int[] aint5 = SimplexNoise.GRADIENT[l2 & 15];
      int[] aint6 = SimplexNoise.GRADIENT[i3 & 15];
      int[] aint7 = SimplexNoise.GRADIENT[j3 & 15];
      double d0 = SimplexNoise.dot(aint, pDeltaX, pDeltaY, pDeltaZ);
      double d1 = SimplexNoise.dot(aint1, pDeltaX - 1.0D, pDeltaY, pDeltaZ);
      double d2 = SimplexNoise.dot(aint2, pDeltaX, pDeltaY - 1.0D, pDeltaZ);
      double d3 = SimplexNoise.dot(aint3, pDeltaX - 1.0D, pDeltaY - 1.0D, pDeltaZ);
      double d4 = SimplexNoise.dot(aint4, pDeltaX, pDeltaY, pDeltaZ - 1.0D);
      double d5 = SimplexNoise.dot(aint5, pDeltaX - 1.0D, pDeltaY, pDeltaZ - 1.0D);
      double d6 = SimplexNoise.dot(aint6, pDeltaX, pDeltaY - 1.0D, pDeltaZ - 1.0D);
      double d7 = SimplexNoise.dot(aint7, pDeltaX - 1.0D, pDeltaY - 1.0D, pDeltaZ - 1.0D);
      double d8 = Mth.smoothstep(pDeltaX);
      double d9 = Mth.smoothstep(pDeltaY);
      double d10 = Mth.smoothstep(pDeltaZ);
      double d11 = Mth.lerp3(d8, d9, d10, (double)aint[0], (double)aint1[0], (double)aint2[0], (double)aint3[0], (double)aint4[0], (double)aint5[0], (double)aint6[0], (double)aint7[0]);
      double d12 = Mth.lerp3(d8, d9, d10, (double)aint[1], (double)aint1[1], (double)aint2[1], (double)aint3[1], (double)aint4[1], (double)aint5[1], (double)aint6[1], (double)aint7[1]);
      double d13 = Mth.lerp3(d8, d9, d10, (double)aint[2], (double)aint1[2], (double)aint2[2], (double)aint3[2], (double)aint4[2], (double)aint5[2], (double)aint6[2], (double)aint7[2]);
      double d14 = Mth.lerp2(d9, d10, d1 - d0, d3 - d2, d5 - d4, d7 - d6);
      double d15 = Mth.lerp2(d10, d8, d2 - d0, d6 - d4, d3 - d1, d7 - d5);
      double d16 = Mth.lerp2(d8, d9, d4 - d0, d5 - d1, d6 - d2, d7 - d3);
      double d17 = Mth.smoothstepDerivative(pDeltaX);
      double d18 = Mth.smoothstepDerivative(pDeltaY);
      double d19 = Mth.smoothstepDerivative(pDeltaZ);
      double d20 = d11 + d17 * d14;
      double d21 = d12 + d18 * d15;
      double d22 = d13 + d19 * d16;
      pNoiseValues[0] += d20;
      pNoiseValues[1] += d21;
      pNoiseValues[2] += d22;
      return Mth.lerp3(d8, d9, d10, d0, d1, d2, d3, d4, d5, d6, d7);
   }

   @VisibleForTesting
   public void parityConfigString(StringBuilder pBuilder) {
      NoiseUtils.parityNoiseOctaveConfigString(pBuilder, this.xo, this.yo, this.zo, this.p);
   }
}