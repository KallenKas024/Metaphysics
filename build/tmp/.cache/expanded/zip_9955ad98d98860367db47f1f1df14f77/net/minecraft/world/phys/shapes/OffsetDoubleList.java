package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class OffsetDoubleList extends AbstractDoubleList {
   private final DoubleList delegate;
   private final double offset;

   public OffsetDoubleList(DoubleList pDelegate, double pOffset) {
      this.delegate = pDelegate;
      this.offset = pOffset;
   }

   public double getDouble(int pValue) {
      return this.delegate.getDouble(pValue) + this.offset;
   }

   public int size() {
      return this.delegate.size();
   }
}