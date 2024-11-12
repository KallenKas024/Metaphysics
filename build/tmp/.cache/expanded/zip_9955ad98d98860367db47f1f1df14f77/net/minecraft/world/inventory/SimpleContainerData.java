package net.minecraft.world.inventory;

public class SimpleContainerData implements ContainerData {
   private final int[] ints;

   public SimpleContainerData(int pSize) {
      this.ints = new int[pSize];
   }

   public int get(int pIndex) {
      return this.ints[pIndex];
   }

   public void set(int pIndex, int pValue) {
      this.ints[pIndex] = pValue;
   }

   public int getCount() {
      return this.ints.length;
   }
}