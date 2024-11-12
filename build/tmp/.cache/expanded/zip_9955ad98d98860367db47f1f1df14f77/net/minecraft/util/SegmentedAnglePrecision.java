package net.minecraft.util;

import net.minecraft.core.Direction;

public class SegmentedAnglePrecision {
   private final int mask;
   private final int precision;
   private final float degreeToAngle;
   private final float angleToDegree;

   public SegmentedAnglePrecision(int pPrecision) {
      if (pPrecision < 2) {
         throw new IllegalArgumentException("Precision cannot be less than 2 bits");
      } else if (pPrecision > 30) {
         throw new IllegalArgumentException("Precision cannot be greater than 30 bits");
      } else {
         int i = 1 << pPrecision;
         this.mask = i - 1;
         this.precision = pPrecision;
         this.degreeToAngle = (float)i / 360.0F;
         this.angleToDegree = 360.0F / (float)i;
      }
   }

   public boolean isSameAxis(int pFirst, int pSecond) {
      int i = this.getMask() >> 1;
      return (pFirst & i) == (pSecond & i);
   }

   public int fromDirection(Direction pDirection) {
      if (pDirection.getAxis().isVertical()) {
         return 0;
      } else {
         int i = pDirection.get2DDataValue();
         return i << this.precision - 2;
      }
   }

   public int fromDegreesWithTurns(float pDegreesWithTurns) {
      return Math.round(pDegreesWithTurns * this.degreeToAngle);
   }

   public int fromDegrees(float pDegrees) {
      return this.normalize(this.fromDegreesWithTurns(pDegrees));
   }

   public float toDegreesWithTurns(int pDegrees) {
      return (float)pDegrees * this.angleToDegree;
   }

   public float toDegrees(int pDegreesWithTurns) {
      float f = this.toDegreesWithTurns(this.normalize(pDegreesWithTurns));
      return f >= 180.0F ? f - 360.0F : f;
   }

   public int normalize(int pDegrees) {
      return pDegrees & this.mask;
   }

   public int getMask() {
      return this.mask;
   }
}