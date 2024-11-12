package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ChestMenu extends AbstractContainerMenu {
   private static final int SLOTS_PER_ROW = 9;
   private final Container container;
   private final int containerRows;

   private ChestMenu(MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, int pRows) {
      this(pType, pContainerId, pPlayerInventory, new SimpleContainer(9 * pRows), pRows);
   }

   public static ChestMenu oneRow(int pContainerId, Inventory pPlayerInventory) {
      return new ChestMenu(MenuType.GENERIC_9x1, pContainerId, pPlayerInventory, 1);
   }

   public static ChestMenu twoRows(int pContainerId, Inventory pPlayerInventory) {
      return new ChestMenu(MenuType.GENERIC_9x2, pContainerId, pPlayerInventory, 2);
   }

   public static ChestMenu threeRows(int pContainerId, Inventory pPlayerInventory) {
      return new ChestMenu(MenuType.GENERIC_9x3, pContainerId, pPlayerInventory, 3);
   }

   public static ChestMenu fourRows(int pContainerId, Inventory pPlayerInventory) {
      return new ChestMenu(MenuType.GENERIC_9x4, pContainerId, pPlayerInventory, 4);
   }

   public static ChestMenu fiveRows(int pContainerId, Inventory pPlayerInventory) {
      return new ChestMenu(MenuType.GENERIC_9x5, pContainerId, pPlayerInventory, 5);
   }

   public static ChestMenu sixRows(int pContainerId, Inventory pPlayerInventory) {
      return new ChestMenu(MenuType.GENERIC_9x6, pContainerId, pPlayerInventory, 6);
   }

   public static ChestMenu threeRows(int pContainerId, Inventory pPlayerInventory, Container pContainer) {
      return new ChestMenu(MenuType.GENERIC_9x3, pContainerId, pPlayerInventory, pContainer, 3);
   }

   public static ChestMenu sixRows(int pContainerId, Inventory pPlayerInventory, Container pContainer) {
      return new ChestMenu(MenuType.GENERIC_9x6, pContainerId, pPlayerInventory, pContainer, 6);
   }

   public ChestMenu(MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, Container pContainer, int pRows) {
      super(pType, pContainerId);
      checkContainerSize(pContainer, pRows * 9);
      this.container = pContainer;
      this.containerRows = pRows;
      pContainer.startOpen(pPlayerInventory.player);
      int i = (this.containerRows - 4) * 18;

      for(int j = 0; j < this.containerRows; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(pContainer, k + j * 9, 8 + k * 18, 18 + j * 18));
         }
      }

      for(int l = 0; l < 3; ++l) {
         for(int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(pPlayerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
         }
      }

      for(int i1 = 0; i1 < 9; ++i1) {
         this.addSlot(new Slot(pPlayerInventory, i1, 8 + i1 * 18, 161 + i));
      }

   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(Player pPlayer) {
      return this.container.stillValid(pPlayer);
   }

   /**
    * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
    * inventory and the other inventory(s).
    */
   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (pIndex < this.containerRows * 9) {
            if (!this.moveItemStackTo(itemstack1, this.containerRows * 9, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, this.containerRows * 9, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return itemstack;
   }

   /**
    * Called when the container is closed.
    */
   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.container.stopOpen(pPlayer);
   }

   /**
    * Gets the inventory associated with this chest container.
    * 
    * @see #field_75155_e
    */
   public Container getContainer() {
      return this.container;
   }

   public int getRowCount() {
      return this.containerRows;
   }
}