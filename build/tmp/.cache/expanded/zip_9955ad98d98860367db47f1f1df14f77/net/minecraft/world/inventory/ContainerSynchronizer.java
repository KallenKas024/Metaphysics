package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface ContainerSynchronizer {
   void sendInitialData(AbstractContainerMenu pContainer, NonNullList<ItemStack> pItems, ItemStack pCarriedItem, int[] pInitialData);

   void sendSlotChange(AbstractContainerMenu pContainer, int pSlot, ItemStack pItemStack);

   void sendCarriedChange(AbstractContainerMenu pContainerMenu, ItemStack pStack);

   void sendDataChange(AbstractContainerMenu pContainer, int pId, int pValue);
}