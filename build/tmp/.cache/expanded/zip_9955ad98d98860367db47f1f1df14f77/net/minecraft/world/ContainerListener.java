package net.minecraft.world;

public interface ContainerListener {
   /**
    * Called by {@code InventoryBasic.onInventoryChanged()} on an array that is never filled.
    */
   void containerChanged(Container pContainer);
}