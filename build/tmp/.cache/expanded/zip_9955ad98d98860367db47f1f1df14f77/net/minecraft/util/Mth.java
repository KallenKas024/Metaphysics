package net.minecraft.util;

import java.util.Locale;
import java.util.UUID;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.math.NumberUtils;

public class Mth {
   private static final long UUID_VERSION = 61440L;
   private static final long UUID_VERSION_TYPE_4 = 16384L;
   private static final long UUID_VARIANT = -4611686018427387904L;
   private static final long UUID_VARIANT_2 = Long.MIN_VALUE;
   public static final float PI = (float)Math.PI;
   public static final float HALF_PI = ((float)Math.PI / 2F);
   public static final float TWO_PI = ((float)Math.PI * 2F);
   public static final float DEG_TO_RAD = ((float)Math.PI / 180F);
   public static final float RAD_TO_DEG = (180F / (float)Math.PI);
   public static final float EPSILON = 1.0E-5F;
   public static final float SQRT_OF_TWO = sqrt(2.0F);
   private static final float SIN_SCALE = 10430.378F;
   private static final float[] SIN = Util.make(new float[65536], (p_14077_) -> {
      for(int i = 0; i < p_14077_.length; ++i) {
         p_14077_[i] = (float)Math.sin((double)i * Math.PI * 2.0D / 65536.0D);
      }

   });
   private static final RandomSource RANDOM = RandomSource.createThreadSafe();
   /**
    * Though it looks like an array, this is really more like a mapping. Key (index of this array) is the upper 5 bits
    * of the result of multiplying a 32-bit unsigned integer by the B(2, 5) De Bruijn sequence 0x077CB531. Value (value
    * stored in the array) is the unique index (from the right) of the leftmo
    */
   private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
   private static final double ONE_SIXTH = 0.16666666666666666D;
   private static final int FRAC_EXP = 8;
   private static final int LUT_SIZE = 257;
   private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
   private static final double[] ASIN_TAB = new double[257];
   private static final double[] COS_TAB = new double[257];

   /**
    * sin looked up in a table
    */
   public static float sin(float pValue) {
      return SIN[(int)(pValue * 10430.378F) & '\uffff'];
   }

   /**
    * cos looked up in the sin table with the appropriate offset
    */
   public static float cos(float pValue) {
      return SIN[(int)(pValue * 10430.378F + 16384.0F) & '\uffff'];
   }

   public static float sqrt(float pValue) {
      return (float)Math.sqrt((double)pValue);
   }

   /**
    * {@return the greatest integer less than or equal to the float argument}
    */
   public static int floor(float pValue) {
      int i = (int)pValue;
      return pValue < (float)i ? i - 1 : i;
   }

   /**
    * {@return the greatest integer less than or equal to the double argument}
    */
   public static int floor(double pValue) {
      int i = (int)pValue;
      return pValue < (double)i ? i - 1 : i;
   }

   /**
    * Long version of floor()
    */
   public static long lfloor(double pValue) {
      long i = (long)pValue;
      return pValue < (double)i ? i - 1L : i;
   }

   public static float abs(float pValue) {
      return Math.abs(pValue);
   }

   /**
    * {@return the unsigned value of an int}
    */
   public static int abs(int pValue) {
      return Math.abs(pValue);
   }

   public static int ceil(float pValue) {
      int i = (int)pValue;
      return pValue > (float)i ? i + 1 : i;
   }

   public static int ceil(double pValue) {
      int i = (int)pValue;
      return pValue > (double)i ? i + 1 : i;
   }

   /**
    * {@return the given value if between the lower and the upper bound. If the value is less than the lower bound,
    * returns the lower bound} If the value is greater than the upper bound, returns the upper bound.
    * @param pValue The value that is clamped.
    * @param pMin The lower bound for the clamp.
    * @param pMax The upper bound for the clamp.
    */
   public static int clamp(int pValue, int pMin, int pMax) {
      return Math.min(Math.max(pValue, pMin), pMax);
   }

   /**
    * {@return the given value if between the lower and the upper bound. If the value is less than the lower bound,
    * returns the lower bound} If the value is greater than the upper bound, returns the upper bound.
    * @param pValue The value that is clamped.
    * @param pMin The lower bound for the clamp.
    * @param pMax The upper bound for the clamp.
    */
   public static float clamp(float pValue, float pMin, float pMax) {
      return pValue < pMin ? pMin : Math.min(pValue, pMax);
   }

   /**
    * {@return the given value if between the lower and the upper bound. If the value is less than the lower bound,
    * returns the lower bound} If the value is greater than the upper bound, returns the upper bound.
    * @param pValue The value that is clamped.
    * @param pMin The lower bound for the clamp.
    * @param pMax The upper bound for the clamp.
    */
   public static double clamp(double pValue, double pMin, double pMax) {
      return pValue < pMin ? pMin : Math.min(pValue, pMax);
   }

   /**
    * Method for linear interpolation of doubles.
    * @param pStart Start value for the lerp.
    * @param pEnd End value for the lerp.
    * @param pDelta A value between 0 and 1 that indicates the percentage of the lerp. (0 will give the start value and
    * 1 will give the end value) If the value is not between 0 and 1, it is clamped.
    */
   public static double clampedLerp(double pStart, double pEnd, double pDelta) {
      if (pDelta < 0.0D) {
         return pStart;
      } else {
         return pDelta > 1.0D ? pEnd : lerp(pDelta, pStart, pEnd);
      }
   }

   /**
    * Method for linear interpolation of floats.
    * @param pStart Start value for the lerp.
    * @param pEnd End value for the lerp.
    * @param pDelta A value between 0 and 1 that indicates the percentage of the lerp. (0 will give the start value and
    * 1 will give the end value) If the value is not between 0 and 1, it is clamped.
    */
   public static float clampedLerp(float pStart, float pEnd, float pDelta) {
      if (pDelta < 0.0F) {
         return pStart;
      } else {
         return pDelta > 1.0F ? pEnd : lerp(pDelta, pStart, pEnd);
      }
   }

   /**
    * {@return the maximum of the absolute value of two numbers}
    */
   public static double absMax(double pX, double pY) {
      if (pX < 0.0D) {
         pX = -pX;
      }

      if (pY < 0.0D) {
         pY = -pY;
      }

      return Math.max(pX, pY);
   }

   public static int floorDiv(int pDividend, int pDivisor) {
      return Math.floorDiv(pDividend, pDivisor);
   }

   public static int nextInt(RandomSource pRandom, int pMinimum, int pMaximum) {
      return pMinimum >= pMaximum ? pMinimum : pRandom.nextInt(pMaximum - pMinimum + 1) + pMinimum;
   }

   public static float nextFloat(RandomSource pRandom, float pMinimum, float pMaximum) {
      return pMinimum >= pMaximum ? pMinimum : pRandom.nextFloat() * (pMaximum - pMinimum) + pMinimum;
   }

   public static double nextDouble(RandomSource pRandom, double pMinimum, double pMaximum) {
      return pMinimum >= pMaximum ? pMinimum : pRandom.nextDouble() * (pMaximum - pMinimum) + pMinimum;
   }

   public static boolean equal(float pX, float pY) {
      return Math.abs(pY - pX) < 1.0E-5F;
   }

   public static boolean equal(double pX, double pY) {
      return Math.abs(pY - pX) < (double)1.0E-5F;
   }

   public static int positiveModulo(int pX, int pY) {
      return Math.floorMod(pX, pY);
   }

   public static float positiveModulo(float pNumerator, float pDenominator) {
      return (pNumerator % pDenominator + pDenominator) % pDenominator;
   }

   public static double positiveModulo(double pNumerator, double pDenominator) {
      return (pNumerator % pDenominator + pDenominator) % pDenominator;
   }

   public static boolean isMultipleOf(int pNumber, int pMultiple) {
      return pNumber % pMultiple == 0;
   }

   /**
    * Adjust the angle so that its value is in the range [-180;180)
    */
   public static int wrapDegrees(int pAngle) {
      int i = pAngle % 360;
      if (i >= 180) {
         i -= 360;
      }

      if (i < -180) {
         i += 360;
      }

      return i;
   }

   /**
    * The angle is reduced to an angle between -180 and +180 by mod, and a 360 check.
    */
   public static float wrapDegrees(float pValue) {
      float f = pValue % 360.0F;
      if (f >= 180.0F) {
         f -= 360.0F;
      }

      if (f < -180.0F) {
         f += 360.0F;
      }

      return f;
   }

   /**
    * The angle is reduced to an angle between -180 and +180 by mod, and a 360 check.
    */
   public static double wrapDegrees(double pValue) {
      double d0 = pValue % 360.0D;
      if (d0 >= 180.0D) {
         d0 -= 360.0D;
      }

      if (d0 < -180.0D) {
         d0 += 360.0D;
      }

      return d0;
   }

   /**
    * {@return the difference between two angles in degrees}
    */
   public static float degreesDifference(float pStart, float pEnd) {
      return wrapDegrees(pEnd - pStart);
   }

   /**
    * {@return the absolute of the difference between two angles in degrees}
    */
   public static float degreesDifferenceAbs(float pStart, float pEnd) {
      return abs(degreesDifference(pStart, pEnd));
   }

   /**
    * Takes a rotation and compares it to another rotation.
    * If the difference is greater than a given maximum, clamps the original rotation between to have at most the given
    * difference to the actual rotation.
    * This is used to match the body rotation of entities to their head rotation.
    * @return The new value for the rotation that was adjusted
    */
   public static float rotateIfNecessary(float pRotationToAdjust, float pActualRotation, float pMaxDifference) {
      float f = degreesDifference(pRotationToAdjust, pActualRotation);
      float f1 = clamp(f, -pMaxDifference, pMaxDifference);
      return pActualRotation - f1;
   }

   /**
    * Changes value by stepSize towards the limit and returns the result.
    * If value is smaller than limit, the result will never be bigger than limit.
    * If value is bigger than limit, the result will never be smaller than limit.
    */
   public static float approach(float pValue, float pLimit, float pStepSize) {
      pStepSize = abs(pStepSize);
      return pValue < pLimit ? clamp(pValue + pStepSize, pValue, pLimit) : clamp(pValue - pStepSize, pLimit, pValue);
   }

   /**
    * Changes the angle by stepSize towards the limit in the direction where the distance is smaller.
    * {@see #approach(float, float, float)}
    */
   public static float approachDegrees(float pAngle, float pLimit, float pStepSize) {
      float f = degreesDifference(pAngle, pLimit);
      return approach(pAngle, pAngle + f, pStepSize);
   }

   /**
    * Parses the string as an integer or returns the second parameter if it fails.
    */
   public static int getInt(String pValue, int pDefaultValue) {
      return NumberUtils.toInt(pValue, pDefaultValue);
   }

   /**
    * {@return the input value rounded up to the next highest power of two}
    */
   public static int smallestEncompassingPowerOfTwo(int pValue) {
      int i = pValue - 1;
      i |= i >> 1;
      i |= i >> 2;
      i |= i >> 4;
      i |= i >> 8;
      i |= i >> 16;
      return i + 1;
   }

   /**
    * Is the given value a power of two?  (1, 2, 4, 8, 16, ...)
    */
   public static boolean isPowerOfTwo(int pValue) {
      return pValue != 0 && (pValue & pValue - 1) == 0;
   }

   /**
    * Uses a B(2, 5) De Bruijn sequence and a lookup table to efficiently calculate the log-base-two of the given value.
    * Optimized for cases where the input value is a power-of-two. If the input value is not a power-of-two, then
    * subtract 1 from the return value.
    */
   public static int ceillog2(int pValue) {
      pValue = isPowerOfTwo(pValue) ? pValue : smallestEncompassingPowerOfTwo(pValue);
      return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)pValue * 125613361L >> 27) & 31];
   }

   /**
    * Efficiently calculates the floor of the base-2 log of an integer value.  This is effectively the index of the
    * highest bit that is set.  For example, if the number in binary is 0...100101, this will return 5.
    */
   public static int log2(int pValue) {
      return ceillog2(pValue) - (isPowerOfTwo(pValue) ? 0 : 1);
   }

   /**
    * Makes an integer color from the given red, green, and blue float values
    */
   public static int color(float pR, float pG, float pB) {
      return FastColor.ARGB32.color(0, floor(pR * 255.0F), floor(pG * 255.0F), floor(pB * 255.0F));
   }

   public static float frac(float pNumber) {
      return pNumber - (float)floor(pNumber);
   }

   /**
    * Gets the decimal portion of the given double. For instance, {@code frac(5.5)} returns {@code .5}.
    */
   public static double frac(double pNumber) {
      return pNumber - (double)lfloor(pNumber);
   }

   /** @deprecated */
   @Deprecated
   public static long getSeed(Vec3i pPos) {
      return getSeed(pPos.getX(), pPos.getY(), pPos.getZ());
   }

   /** @deprecated */
   @Deprecated
   public static long getSeed(int pX, int pY, int pZ) {
      long i = (long)(pX * 3129871) ^ (long)pZ * 116129781L ^ (long)pY;
      i = i * i * 42317861L + i * 11L;
      return i >> 16;
   }

   public static UUID createInsecureUUID(RandomSource pRandom) {
      long i = pRandom.nextLong() & -61441L | 16384L;
      long j = pRandom.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
      return new UUID(i, j);
   }

   /**
    * Generates a random UUID using the shared random
    */
   public static UUID createInsecureUUID() {
      return createInsecureUUID(RANDOM);
   }

   public static double inverseLerp(double pDelta, double pStart, double pEnd) {
      return (pDelta - pStart) / (pEnd - pStart);
   }

   public static float inverseLerp(float pDelta, float pStart, float pEnd) {
      return (pDelta - pStart) / (pEnd - pStart);
   }

   public static boolean rayIntersectsAABB(Vec3 pStart, Vec3 pEnd, AABB pBoundingBox) {
      double d0 = (pBoundingBox.minX + pBoundingBox.maxX) * 0.5D;
      double d1 = (pBoundingBox.maxX - pBoundingBox.minX) * 0.5D;
      double d2 = pStart.x - d0;
      if (Math.abs(d2) > d1 && d2 * pEnd.x >= 0.0D) {
         return false;
      } else {
         double d3 = (pBoundingBox.minY + pBoundingBox.maxY) * 0.5D;
         double d4 = (pBoundingBox.maxY - pBoundingBox.minY) * 0.5D;
         double d5 = pStart.y - d3;
         if (Math.abs(d5) > d4 && d5 * pEnd.y >= 0.0D) {
            return false;
         } else {
            double d6 = (pBoundingBox.minZ + pBoundingBox.maxZ) * 0.5D;
            double d7 = (pBoundingBox.maxZ - pBoundingBox.minZ) * 0.5D;
            double d8 = pStart.z - d6;
            if (Math.abs(d8) > d7 && d8 * pEnd.z >= 0.0D) {
               return false;
            } else {
               double d9 = Math.abs(pEnd.x);
               double d10 = Math.abs(pEnd.y);
               double d11 = Math.abs(pEnd.z);
               double d12 = pEnd.y * d8 - pEnd.z * d5;
               if (Math.abs(d12) > d4 * d11 + d7 * d10) {
                  return false;
               } else {
                  d12 = pEnd.z * d2 - pEnd.x * d8;
                  if (Math.abs(d12) > d1 * d11 + d7 * d9) {
                     return false;
                  } else {
                     d12 = pEnd.x * d5 - pEnd.y * d2;
                     return Math.abs(d12) < d1 * d10 + d4 * d9;
                  }
               }
            }
         }
      }
   }

   public static double atan2(double pY, double pX) {
      double d0 = pX * pX + pY * pY;
      if (Double.isNaN(d0)) {
         return Double.NaN;
      } else {
         boolean flag = pY < 0.0D;
         if (flag) {
            pY = -pY;
         }

         boolean flag1 = pX < 0.0D;
         if (flag1) {
            pX = -pX;
         }

         boolean flag2 = pY > pX;
         if (flag2) {
            double d1 = pX;
            pX = pY;
            pY = d1;
         }

         double d9 = fastInvSqrt(d0);
         pX *= d9;
         pY *= d9;
         double d2 = FRAC_BIAS + pY;
         int i = (int)Double.doubleToRawLongBits(d2);
         double d3 = ASIN_TAB[i];
         double d4 = COS_TAB[i];
         double d5 = d2 - FRAC_BIAS;
         double d6 = pY * d4 - pX * d5;
         double d7 = (6.0D + d6 * d6) * d6 * 0.16666666666666666D;
         double d8 = d3 + d7;
         if (flag2) {
            d8 = (Math.PI / 2D) - d8;
         }

         if (flag1) {
            d8 = Math.PI - d8;
         }

         if (flag) {
            d8 = -d8;
         }

         return d8;
      }
   }

   public static float invSqrt(float pNumber) {
      return org.joml.Math.invsqrt(pNumber);
   }

   public static double invSqrt(double pNumber) {
      return org.joml.Math.invsqrt(pNumber);
   }

   /** @deprecated */
   /**
    * Computes 1/sqrt(n) using <a href="https://en.wikipedia.org/wiki/Fast_inverse_square_root">the fast inverse square
    * root</a> with a constant of 0x5FE6EB50C7B537AA.
    */
   @Deprecated
   public static double fastInvSqrt(double pNumber) {
      double d0 = 0.5D * pNumber;
      long i = Double.doubleToRawLongBits(pNumber);
      i = 6910469410427058090L - (i >> 1);
      pNumber = Double.longBitsToDouble(i);
      return pNumber * (1.5D - d0 * pNumber * pNumber);
   }

   public static float fastInvCubeRoot(float pNumber) {
      int i = Float.floatToIntBits(pNumber);
      i = 1419967116 - i / 3;
      float f = Float.intBitsToFloat(i);
      f = 0.6666667F * f + 1.0F / (3.0F * f * f * pNumber);
      return 0.6666667F * f + 1.0F / (3.0F * f * f * pNumber);
   }

   public static int hsvToRgb(float pHue, float pSaturation, float pValue) {
      int i = (int)(pHue * 6.0F) % 6;
      float f = pHue * 6.0F - (float)i;
      float f1 = pValue * (1.0F - pSaturation);
      float f2 = pValue * (1.0F - f * pSaturation);
      float f3 = pValue * (1.0F - (1.0F - f) * pSaturation);
      float f4;
      float f5;
      float f6;
      switch (i) {
         case 0:
            f4 = pValue;
            f5 = f3;
            f6 = f1;
            break;
         case 1:
            f4 = f2;
            f5 = pValue;
            f6 = f1;
            break;
         case 2:
            f4 = f1;
            f5 = pValue;
            f6 = f3;
            break;
         case 3:
            f4 = f1;
            f5 = f2;
            f6 = pValue;
            break;
         case 4:
            f4 = f3;
            f5 = f1;
            f6 = pValue;
            break;
         case 5:
            f4 = pValue;
            f5 = f1;
            f6 = f2;
            break;
         default:
            throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + pHue + ", " + pSaturation + ", " + pValue);
      }

      return FastColor.ARGB32.color(0, clamp((int)(f4 * 255.0F), 0, 255), clamp((int)(f5 * 255.0F), 0, 255), clamp((int)(f6 * 255.0F), 0, 255));
   }

   public static int murmurHash3Mixer(int pInput) {
      pInput ^= pInput >>> 16;
      pInput *= -2048144789;
      pInput ^= pInput >>> 13;
      pInput *= -1028477387;
      return pInput ^ pInput >>> 16;
   }

   public static int binarySearch(int pMin, int pMax, IntPredicate pIsTargetBeforeOrAt) {
      int i = pMax - pMin;

      while(i > 0) {
         int j = i / 2;
         int k = pMin + j;
         if (pIsTargetBeforeOrAt.test(k)) {
            i = j;
         } else {
            pMin = k + 1;
            i -= j + 1;
         }
      }

      return pMin;
   }

   public static int lerpInt(float pDelta, int pStart, int pEnd) {
      return pStart + floor(pDelta * (float)(pEnd - pStart));
   }

   /**
    * Method for linear interpolation of floats
    * @param pDelta A value usually between 0 and 1 that indicates the percentage of the lerp. (0 will give the start
    * value and 1 will give the end value)
    * @param pStart Start value for the lerp
    * @param pEnd End value for the lerp
    */
   public static float lerp(float pDelta, float pStart, float pEnd) {
      return pStart + pDelta * (pEnd - pStart);
   }

   /**
    * Method for linear interpolation of doubles
    * @param pDelta A value usually between 0 and 1 that indicates the percentage of the lerp. (0 will give the start
    * value and 1 will give the end value)
    * @param pStart Start value for the lerp
    * @param pEnd End value for the lerp
    */
   public static double lerp(double pDelta, double pStart, double pEnd) {
      return pStart + pDelta * (pEnd - pStart);
   }

   public static double lerp2(double pDelta1, double pDelta2, double pStart1, double pEnd1, double pStart2, double pEnd2) {
      return lerp(pDelta2, lerp(pDelta1, pStart1, pEnd1), lerp(pDelta1, pStart2, pEnd2));
   }

   public static double lerp3(double pDelta1, double pDelta2, double pDelta3, double pStart1, double pEnd1, double pStart2, double pEnd2, double pStart3, double pEnd3, double pStart4, double pEnd4) {
      return lerp(pDelta3, lerp2(pDelta1, pDelta2, pStart1, pEnd1, pStart2, pEnd2), lerp2(pDelta1, pDelta2, pStart3, pEnd3, pStart4, pEnd4));
   }

   public static float catmullrom(float pDelta, float pControlPoint1, float pControlPoint2, float pControlPoint3, float pControlPoint4) {
      return 0.5F * (2.0F * pControlPoint2 + (pControlPoint3 - pControlPoint1) * pDelta + (2.0F * pControlPoint1 - 5.0F * pControlPoint2 + 4.0F * pControlPoint3 - pControlPoint4) * pDelta * pDelta + (3.0F * pControlPoint2 - pControlPoint1 - 3.0F * pControlPoint3 + pControlPoint4) * pDelta * pDelta * pDelta);
   }

   public static double smoothstep(double pInput) {
      return pInput * pInput * pInput * (pInput * (pInput * 6.0D - 15.0D) + 10.0D);
   }

   public static double smoothstepDerivative(double pInput) {
      return 30.0D * pInput * pInput * (pInput - 1.0D) * (pInput - 1.0D);
   }

   public static int sign(double pX) {
      if (pX == 0.0D) {
         return 0;
      } else {
         return pX > 0.0D ? 1 : -1;
      }
   }

   /**
    * Linearly interpolates an angle between the start between the start and end values given as degrees.
    * @param pDelta A value between 0 and 1 that indicates the percentage of the lerp. (0 will give the start value and
    * 1 will give the end value)
    */
   public static float rotLerp(float pDelta, float pStart, float pEnd) {
      return pStart + pDelta * wrapDegrees(pEnd - pStart);
   }

   public static float triangleWave(float pInput, float pPeriod) {
      return (Math.abs(pInput % pPeriod - pPeriod * 0.5F) - pPeriod * 0.25F) / (pPeriod * 0.25F);
   }

   public static float square(float pValue) {
      return pValue * pValue;
   }

   public static double square(double pValue) {
      return pValue * pValue;
   }

   public static int square(int pValue) {
      return pValue * pValue;
   }

   public static long square(long pValue) {
      return pValue * pValue;
   }

   public static double clampedMap(double pInput, double pInputMin, double pInputMax, double pOuputMin, double pOutputMax) {
      return clampedLerp(pOuputMin, pOutputMax, inverseLerp(pInput, pInputMin, pInputMax));
   }

   public static float clampedMap(float pInput, float pInputMin, float pInputMax, float pOutputMin, float pOutputMax) {
      return clampedLerp(pOutputMin, pOutputMax, inverseLerp(pInput, pInputMin, pInputMax));
   }

   public static double map(double pInput, double pInputMin, double pInputMax, double pOutputMin, double pOutputMax) {
      return lerp(inverseLerp(pInput, pInputMin, pInputMax), pOutputMin, pOutputMax);
   }

   public static float map(float pInput, float pInputMin, float pInputMax, float pOutputMin, float pOutputMax) {
      return lerp(inverseLerp(pInput, pInputMin, pInputMax), pOutputMin, pOutputMax);
   }

   public static double wobble(double pInput) {
      return pInput + (2.0D * RandomSource.create((long)floor(pInput * 3000.0D)).nextDouble() - 1.0D) * 1.0E-7D / 2.0D;
   }

   /**
    * Rounds the given value up to a multiple of factor.
    * @return The smallest integer multiple of factor that is greater than or equal to the value
    */
   public static int roundToward(int pValue, int pFactor) {
      return positiveCeilDiv(pValue, pFactor) * pFactor;
   }

   /**
    * Returns the smallest (closest to negative infinity) int value that is greater than or equal to the algebraic
    * quotient.
    * @see java.lang.Math#floorDiv(int, int)
    */
   public static int positiveCeilDiv(int pX, int pY) {
      return -Math.floorDiv(-pX, pY);
   }

   public static int randomBetweenInclusive(RandomSource pRandom, int pMinInclusive, int pMaxInclusive) {
      return pRandom.nextInt(pMaxInclusive - pMinInclusive + 1) + pMinInclusive;
   }

   public static float randomBetween(RandomSource pRandom, float pMinInclusive, float pMaxExclusive) {
      return pRandom.nextFloat() * (pMaxExclusive - pMinInclusive) + pMinInclusive;
   }

   /**
    * Generates a value from a normal distribution with the given mean and deviation.
    */
   public static float normal(RandomSource pRandom, float pMean, float pDeviation) {
      return pMean + (float)pRandom.nextGaussian() * pDeviation;
   }

   public static double lengthSquared(double pXDistance, double pYDistance) {
      return pXDistance * pXDistance + pYDistance * pYDistance;
   }

   public static double length(double pXDistance, double pYDistance) {
      return Math.sqrt(lengthSquared(pXDistance, pYDistance));
   }

   public static double lengthSquared(double pXDistance, double pYDistance, double pZDistance) {
      return pXDistance * pXDistance + pYDistance * pYDistance + pZDistance * pZDistance;
   }

   public static double length(double pXDistance, double pYDistance, double pZDistance) {
      return Math.sqrt(lengthSquared(pXDistance, pYDistance, pZDistance));
   }

   /**
    * Gets the value closest to zero that is not closer to zero than the given value and is a multiple of the factor.
    */
   public static int quantize(double pValue, int pFactor) {
      return floor(pValue / (double)pFactor) * pFactor;
   }

   public static IntStream outFromOrigin(int pInput, int pLowerBound, int pUpperBound) {
      return outFromOrigin(pInput, pLowerBound, pUpperBound, 1);
   }

   public static IntStream outFromOrigin(int pInput, int pLowerBound, int pUpperBound, int pSteps) {
      if (pLowerBound > pUpperBound) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "upperbound %d expected to be > lowerBound %d", pUpperBound, pLowerBound));
      } else if (pSteps < 1) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "steps expected to be >= 1, was %d", pSteps));
      } else {
         return pInput >= pLowerBound && pInput <= pUpperBound ? IntStream.iterate(pInput, (p_216282_) -> {
            int i = Math.abs(pInput - p_216282_);
            return pInput - i >= pLowerBound || pInput + i <= pUpperBound;
         }, (p_216260_) -> {
            boolean flag = p_216260_ <= pInput;
            int i = Math.abs(pInput - p_216260_);
            boolean flag1 = pInput + i + pSteps <= pUpperBound;
            if (!flag || !flag1) {
               int j = pInput - i - (flag ? pSteps : 0);
               if (j >= pLowerBound) {
                  return j;
               }
            }

            return pInput + i + pSteps;
         }) : IntStream.empty();
      }
   }

   static {
      for(int i = 0; i < 257; ++i) {
         double d0 = (double)i / 256.0D;
         double d1 = Math.asin(d0);
         COS_TAB[i] = Math.cos(d1);
         ASIN_TAB[i] = d1;
      }

   }
}