package net.minecraft.world.level.chunk.storage;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.BitSet;

/**
 * Keeps track of which parts of a region file are used and which parts are free.
 */
public class RegionBitmap {
   private final BitSet used = new BitSet();

   /**
    * Marks a range of 4 KiB sectors relative to the region file as used.
    * @param pSectorOffset The first sector in the range.
    * @param pSectorCount The amount of sectors in the range.
    */
   public void force(int pSectorOffset, int pSectorCount) {
      this.used.set(pSectorOffset, pSectorOffset + pSectorCount);
   }

   /**
    * Marks a range of 4 KiB sectors relative to the region file as not used.
    * @param pSectorOffset The first sector in the range.
    * @param pSectorCount The amount of sectors in the range.
    */
   public void free(int pSectorOffset, int pSectorCount) {
      this.used.clear(pSectorOffset, pSectorOffset + pSectorCount);
   }

   /**
    * Gets a valid offset inside the region file with enough space to store the given amount of sectors and marks that
    * space as used.
    */
   public int allocate(int pSectorCount) {
      int i = 0;

      while(true) {
         int j = this.used.nextClearBit(i);
         int k = this.used.nextSetBit(j);
         if (k == -1 || k - j >= pSectorCount) {
            this.force(j, pSectorCount);
            return j;
         }

         i = k;
      }
   }

   @VisibleForTesting
   public IntSet getUsed() {
      return this.used.stream().collect(IntArraySet::new, IntCollection::add, IntCollection::addAll);
   }
}