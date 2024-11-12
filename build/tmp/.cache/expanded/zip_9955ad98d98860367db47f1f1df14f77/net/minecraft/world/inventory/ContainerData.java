package net.minecraft.world.inventory;

public interface ContainerData {
   int get(int pIndex);

   void set(int pIndex, int pValue);

   int getCount();
}