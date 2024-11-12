package net.minecraft.util.datafix;

import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;

public class PackedBitStorage {
   private static final int BIT_TO_LONG_SHIFT = 6;
   private final long[] data;
   private final int bits;
   private final long mask;
   private final int size;

   public PackedBitStorage(int pBits, int pSize) {
      this(pBits, pSize, new long[Mth.roundToward(pSize * pBits, 64) / 64]);
   }

   public PackedBitStorage(int pBits, int pSize, long[] pData) {
      Validate.inclusiveBetween(1L, 32L, (long)pBits);
      this.size = pSize;
      this.bits = pBits;
      this.data = pData;
      this.mask = (1L << pBits) - 1L;
      int i = Mth.roundToward(pSize * pBits, 64) / 64;
      if (pData.length != i) {
         throw new IllegalArgumentException("Invalid length given for storage, got: " + pData.length + " but expected: " + i);
      }
   }

   public void set(int pIndex, int pValue) {
      Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)pIndex);
      Validate.inclusiveBetween(0L, this.mask, (long)pValue);
      int i = pIndex * this.bits;
      int j = i >> 6;
      int k = (pIndex + 1) * this.bits - 1 >> 6;
      int l = i ^ j << 6;
      this.data[j] = this.data[j] & ~(this.mask << l) | ((long)pValue & this.mask) << l;
      if (j != k) {
         int i1 = 64 - l;
         int j1 = this.bits - i1;
         this.data[k] = this.data[k] >>> j1 << j1 | ((long)pValue & this.mask) >> i1;
      }

   }

   public int get(int pIndex) {
      Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)pIndex);
      int i = pIndex * this.bits;
      int j = i >> 6;
      int k = (pIndex + 1) * this.bits - 1 >> 6;
      int l = i ^ j << 6;
      if (j == k) {
         return (int)(this.data[j] >>> l & this.mask);
      } else {
         int i1 = 64 - l;
         return (int)((this.data[j] >>> l | this.data[k] << i1) & this.mask);
      }
   }

   public long[] getRaw() {
      return this.data;
   }

   public int getBits() {
      return this.bits;
   }
}