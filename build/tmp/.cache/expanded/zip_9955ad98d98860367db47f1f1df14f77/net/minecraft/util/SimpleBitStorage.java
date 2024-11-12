package net.minecraft.util;

import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

public class SimpleBitStorage implements BitStorage {
   private static final int[] MAGIC = new int[]{-1, -1, 0, Integer.MIN_VALUE, 0, 0, 1431655765, 1431655765, 0, Integer.MIN_VALUE, 0, 1, 858993459, 858993459, 0, 715827882, 715827882, 0, 613566756, 613566756, 0, Integer.MIN_VALUE, 0, 2, 477218588, 477218588, 0, 429496729, 429496729, 0, 390451572, 390451572, 0, 357913941, 357913941, 0, 330382099, 330382099, 0, 306783378, 306783378, 0, 286331153, 286331153, 0, Integer.MIN_VALUE, 0, 3, 252645135, 252645135, 0, 238609294, 238609294, 0, 226050910, 226050910, 0, 214748364, 214748364, 0, 204522252, 204522252, 0, 195225786, 195225786, 0, 186737708, 186737708, 0, 178956970, 178956970, 0, 171798691, 171798691, 0, 165191049, 165191049, 0, 159072862, 159072862, 0, 153391689, 153391689, 0, 148102320, 148102320, 0, 143165576, 143165576, 0, 138547332, 138547332, 0, Integer.MIN_VALUE, 0, 4, 130150524, 130150524, 0, 126322567, 126322567, 0, 122713351, 122713351, 0, 119304647, 119304647, 0, 116080197, 116080197, 0, 113025455, 113025455, 0, 110127366, 110127366, 0, 107374182, 107374182, 0, 104755299, 104755299, 0, 102261126, 102261126, 0, 99882960, 99882960, 0, 97612893, 97612893, 0, 95443717, 95443717, 0, 93368854, 93368854, 0, 91382282, 91382282, 0, 89478485, 89478485, 0, 87652393, 87652393, 0, 85899345, 85899345, 0, 84215045, 84215045, 0, 82595524, 82595524, 0, 81037118, 81037118, 0, 79536431, 79536431, 0, 78090314, 78090314, 0, 76695844, 76695844, 0, 75350303, 75350303, 0, 74051160, 74051160, 0, 72796055, 72796055, 0, 71582788, 71582788, 0, 70409299, 70409299, 0, 69273666, 69273666, 0, 68174084, 68174084, 0, Integer.MIN_VALUE, 0, 5};
   private final long[] data;
   private final int bits;
   private final long mask;
   private final int size;
   private final int valuesPerLong;
   private final int divideMul;
   private final int divideAdd;
   private final int divideShift;

   public SimpleBitStorage(int pBits, int pSize, int[] p_198166_) {
      this(pBits, pSize);
      int j = 0;

      int i;
      for(i = 0; i <= pSize - this.valuesPerLong; i += this.valuesPerLong) {
         long k = 0L;

         for(int i1 = this.valuesPerLong - 1; i1 >= 0; --i1) {
            k <<= pBits;
            k |= (long)p_198166_[i + i1] & this.mask;
         }

         this.data[j++] = k;
      }

      int k1 = pSize - i;
      if (k1 > 0) {
         long l = 0L;

         for(int j1 = k1 - 1; j1 >= 0; --j1) {
            l <<= pBits;
            l |= (long)p_198166_[i + j1] & this.mask;
         }

         this.data[j] = l;
      }

   }

   public SimpleBitStorage(int pBits, int pSize) {
      this(pBits, pSize, (long[])null);
   }

   public SimpleBitStorage(int pBits, int pSize, @Nullable long[] pData) {
      Validate.inclusiveBetween(1L, 32L, (long)pBits);
      this.size = pSize;
      this.bits = pBits;
      this.mask = (1L << pBits) - 1L;
      this.valuesPerLong = (char)(64 / pBits);
      int i = 3 * (this.valuesPerLong - 1);
      this.divideMul = MAGIC[i + 0];
      this.divideAdd = MAGIC[i + 1];
      this.divideShift = MAGIC[i + 2];
      int j = (pSize + this.valuesPerLong - 1) / this.valuesPerLong;
      if (pData != null) {
         if (pData.length != j) {
            throw new SimpleBitStorage.InitializationException("Invalid length given for storage, got: " + pData.length + " but expected: " + j);
         }

         this.data = pData;
      } else {
         this.data = new long[j];
      }

   }

   private int cellIndex(int p_184740_) {
      long i = Integer.toUnsignedLong(this.divideMul);
      long j = Integer.toUnsignedLong(this.divideAdd);
      return (int)((long)p_184740_ * i + j >> 32 >> this.divideShift);
   }

   public int getAndSet(int pIndex, int pValue) {
      Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)pIndex);
      Validate.inclusiveBetween(0L, this.mask, (long)pValue);
      int i = this.cellIndex(pIndex);
      long j = this.data[i];
      int k = (pIndex - i * this.valuesPerLong) * this.bits;
      int l = (int)(j >> k & this.mask);
      this.data[i] = j & ~(this.mask << k) | ((long)pValue & this.mask) << k;
      return l;
   }

   /**
    * Sets the entry at the given location to the given value
    */
   public void set(int pIndex, int pValue) {
      Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)pIndex);
      Validate.inclusiveBetween(0L, this.mask, (long)pValue);
      int i = this.cellIndex(pIndex);
      long j = this.data[i];
      int k = (pIndex - i * this.valuesPerLong) * this.bits;
      this.data[i] = j & ~(this.mask << k) | ((long)pValue & this.mask) << k;
   }

   /**
    * Gets the entry at the given index
    */
   public int get(int pIndex) {
      Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)pIndex);
      int i = this.cellIndex(pIndex);
      long j = this.data[i];
      int k = (pIndex - i * this.valuesPerLong) * this.bits;
      return (int)(j >> k & this.mask);
   }

   /**
    * Gets the long array that is used to store the data in this BitArray. This is useful for sending packet data.
    */
   public long[] getRaw() {
      return this.data;
   }

   public int getSize() {
      return this.size;
   }

   public int getBits() {
      return this.bits;
   }

   public void getAll(IntConsumer pConsumer) {
      int i = 0;

      for(long j : this.data) {
         for(int k = 0; k < this.valuesPerLong; ++k) {
            pConsumer.accept((int)(j & this.mask));
            j >>= this.bits;
            ++i;
            if (i >= this.size) {
               return;
            }
         }
      }

   }

   public void unpack(int[] pArray) {
      int i = this.data.length;
      int j = 0;

      for(int k = 0; k < i - 1; ++k) {
         long l = this.data[k];

         for(int i1 = 0; i1 < this.valuesPerLong; ++i1) {
            pArray[j + i1] = (int)(l & this.mask);
            l >>= this.bits;
         }

         j += this.valuesPerLong;
      }

      int j1 = this.size - j;
      if (j1 > 0) {
         long k1 = this.data[i - 1];

         for(int l1 = 0; l1 < j1; ++l1) {
            pArray[j + l1] = (int)(k1 & this.mask);
            k1 >>= this.bits;
         }
      }

   }

   public BitStorage copy() {
      return new SimpleBitStorage(this.bits, this.size, (long[])this.data.clone());
   }

   public static class InitializationException extends RuntimeException {
      InitializationException(String pMessage) {
         super(pMessage);
      }
   }
}