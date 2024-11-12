package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class NonOverlappingMerger extends AbstractDoubleList implements IndexMerger {
   private final DoubleList lower;
   private final DoubleList upper;
   private final boolean swap;

   protected NonOverlappingMerger(DoubleList pLower, DoubleList pUpper, boolean pSwap) {
      this.lower = pLower;
      this.upper = pUpper;
      this.swap = pSwap;
   }

   public int size() {
      return this.lower.size() + this.upper.size();
   }

   public boolean forMergedIndexes(IndexMerger.IndexConsumer pConsumer) {
      return this.swap ? this.forNonSwappedIndexes((p_83020_, p_83021_, p_83022_) -> {
         return pConsumer.merge(p_83021_, p_83020_, p_83022_);
      }) : this.forNonSwappedIndexes(pConsumer);
   }

   private boolean forNonSwappedIndexes(IndexMerger.IndexConsumer pConsumer) {
      int i = this.lower.size();

      for(int j = 0; j < i; ++j) {
         if (!pConsumer.merge(j, -1, j)) {
            return false;
         }
      }

      int l = this.upper.size() - 1;

      for(int k = 0; k < l; ++k) {
         if (!pConsumer.merge(i - 1, k, i + k)) {
            return false;
         }
      }

      return true;
   }

   public double getDouble(int pIndex) {
      return pIndex < this.lower.size() ? this.lower.getDouble(pIndex) : this.upper.getDouble(pIndex - this.lower.size());
   }

   public DoubleList getList() {
      return this;
   }
}