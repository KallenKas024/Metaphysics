package net.minecraft.world;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CompoundContainer implements Container {
   private final Container container1;
   private final Container container2;

   public CompoundContainer(Container pContainer1, Container pContainer2) {
      this.container1 = pContainer1;
      this.container2 = pContainer2;
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return this.container1.getContainerSize() + this.container2.getContainerSize();
   }

   public boolean isEmpty() {
      return this.container1.isEmpty() && this.container2.isEmpty();
   }

   /**
    * Return whether the given inventory is part of this large chest.
    */
   public boolean contains(Container pInventory) {
      return this.container1 == pInventory || this.container2 == pInventory;
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pIndex) {
      return pIndex >= this.container1.getContainerSize() ? this.container2.getItem(pIndex - this.container1.getContainerSize()) : this.container1.getItem(pIndex);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      return pIndex >= this.container1.getContainerSize() ? this.container2.removeItem(pIndex - this.container1.getContainerSize(), pCount) : this.container1.removeItem(pIndex, pCount);
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pIndex) {
      return pIndex >= this.container1.getContainerSize() ? this.container2.removeItemNoUpdate(pIndex - this.container1.getContainerSize()) : this.container1.removeItemNoUpdate(pIndex);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      if (pIndex >= this.container1.getContainerSize()) {
         this.container2.setItem(pIndex - this.container1.getContainerSize(), pStack);
      } else {
         this.container1.setItem(pIndex, pStack);
      }

   }

   /**
    * Returns the maximum stack size for an inventory slot. Seems to always be 64, possibly will be extended.
    */
   public int getMaxStackSize() {
      return this.container1.getMaxStackSize();
   }

   /**
    * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void setChanged() {
      this.container1.setChanged();
      this.container2.setChanged();
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(Player pPlayer) {
      return this.container1.stillValid(pPlayer) && this.container2.stillValid(pPlayer);
   }

   public void startOpen(Player pPlayer) {
      this.container1.startOpen(pPlayer);
      this.container2.startOpen(pPlayer);
   }

   public void stopOpen(Player pPlayer) {
      this.container1.stopOpen(pPlayer);
      this.container2.stopOpen(pPlayer);
   }

   /**
    * Returns {@code true} if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
    * For guis use Slot.isItemValid
    */
   public boolean canPlaceItem(int pIndex, ItemStack pStack) {
      return pIndex >= this.container1.getContainerSize() ? this.container2.canPlaceItem(pIndex - this.container1.getContainerSize(), pStack) : this.container1.canPlaceItem(pIndex, pStack);
   }

   public void clearContent() {
      this.container1.clearContent();
      this.container2.clearContent();
   }
}