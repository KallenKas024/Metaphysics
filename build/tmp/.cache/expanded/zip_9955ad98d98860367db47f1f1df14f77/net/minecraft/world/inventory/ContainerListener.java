package net.minecraft.world.inventory;

import net.minecraft.world.item.ItemStack;

public interface ContainerListener {
   /**
    * Sends the contents of an inventory slot to the client-side Container. This doesn't have to match the actual
    * contents of that slot.
    */
   void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack);

   void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue);
}