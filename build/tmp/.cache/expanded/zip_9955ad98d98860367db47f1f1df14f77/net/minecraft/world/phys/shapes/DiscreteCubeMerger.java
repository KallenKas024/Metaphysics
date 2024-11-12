package net.minecraft.world.phys.shapes;

import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public final class DiscreteCubeMerger implements IndexMerger {
   private final CubePointRange result;
   private final int firstDiv;
   private final int secondDiv;

   DiscreteCubeMerger(int pAa, int pBb) {
      this.result = new CubePointRange((int)Shapes.lcm(pAa, pBb));
      int i = IntMath.gcd(pAa, pBb);
      this.firstDiv = pAa / i;
      this.secondDiv = pBb / i;
   }

   public boolean forMergedIndexes(IndexMerger.IndexConsumer pConsumer) {
      int i = this.result.size() - 1;

      for(int j = 0; j < i; ++j) {
         if (!pConsumer.merge(j / this.secondDiv, j / this.firstDiv, j)) {
            return false;
         }
      }

      return true;
   }

   public int size() {
      return this.result.size();
   }

   public DoubleList getList() {
      return this.result;
   }
}