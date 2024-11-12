package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxSlot extends Slot {
   public ShulkerBoxSlot(Container pContainer, int pSlot, int pX, int pY) {
      super(pContainer, pSlot, pX, pY);
   }

   /**
    * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
    */
   public boolean mayPlace(ItemStack pStack) {
      return pStack.getItem().canFitInsideContainerItems();
   }
}