package net.minecraft.world.inventory;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ItemCombinerMenu extends AbstractContainerMenu {
   private static final int INVENTORY_SLOTS_PER_ROW = 9;
   private static final int INVENTORY_SLOTS_PER_COLUMN = 3;
   protected final ContainerLevelAccess access;
   protected final Player player;
   protected final Container inputSlots;
   private final List<Integer> inputSlotIndexes;
   protected final ResultContainer resultSlots = new ResultContainer();
   private final int resultSlotIndex;

   protected abstract boolean mayPickup(Player pPlayer, boolean pHasStack);

   protected abstract void onTake(Player pPlayer, ItemStack pStack);

   protected abstract boolean isValidBlock(BlockState pState);

   public ItemCombinerMenu(@Nullable MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
      super(pType, pContainerId);
      this.access = pAccess;
      this.player = pPlayerInventory.player;
      ItemCombinerMenuSlotDefinition itemcombinermenuslotdefinition = this.createInputSlotDefinitions();
      this.inputSlots = this.createContainer(itemcombinermenuslotdefinition.getNumOfInputSlots());
      this.inputSlotIndexes = itemcombinermenuslotdefinition.getInputSlotIndexes();
      this.resultSlotIndex = itemcombinermenuslotdefinition.getResultSlotIndex();
      this.createInputSlots(itemcombinermenuslotdefinition);
      this.createResultSlot(itemcombinermenuslotdefinition);
      this.createInventorySlots(pPlayerInventory);
   }

   private void createInputSlots(ItemCombinerMenuSlotDefinition pSlotDefinition) {
      for(final ItemCombinerMenuSlotDefinition.SlotDefinition itemcombinermenuslotdefinition$slotdefinition : pSlotDefinition.getSlots()) {
         this.addSlot(new Slot(this.inputSlots, itemcombinermenuslotdefinition$slotdefinition.slotIndex(), itemcombinermenuslotdefinition$slotdefinition.x(), itemcombinermenuslotdefinition$slotdefinition.y()) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            public boolean mayPlace(ItemStack p_267156_) {
               return itemcombinermenuslotdefinition$slotdefinition.mayPlace().test(p_267156_);
            }
         });
      }

   }

   private void createResultSlot(ItemCombinerMenuSlotDefinition pSlotDefinition) {
      this.addSlot(new Slot(this.resultSlots, pSlotDefinition.getResultSlot().slotIndex(), pSlotDefinition.getResultSlot().x(), pSlotDefinition.getResultSlot().y()) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack p_39818_) {
            return false;
         }

         /**
          * Return whether this slot's stack can be taken from this slot.
          */
         public boolean mayPickup(Player p_39813_) {
            return ItemCombinerMenu.this.mayPickup(p_39813_, this.hasItem());
         }

         public void onTake(Player p_150604_, ItemStack p_150605_) {
            ItemCombinerMenu.this.onTake(p_150604_, p_150605_);
         }
      });
   }

   private void createInventorySlots(Inventory pInventory) {
      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pInventory, k, 8 + k * 18, 142));
      }

   }

   /**
    * Called when the Anvil Input Slot changes, calculates the new result and puts it in the output slot.
    */
   public abstract void createResult();

   protected abstract ItemCombinerMenuSlotDefinition createInputSlotDefinitions();

   private SimpleContainer createContainer(int pSize) {
      return new SimpleContainer(pSize) {
         /**
          * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't
          * think it hasn't changed and skip it.
          */
         public void setChanged() {
            super.setChanged();
            ItemCombinerMenu.this.slotsChanged(this);
         }
      };
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(Container pInventory) {
      super.slotsChanged(pInventory);
      if (pInventory == this.inputSlots) {
         this.createResult();
      }

   }

   /**
    * Called when the container is closed.
    */
   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.access.execute((p_39796_, p_39797_) -> {
         this.clearContainer(pPlayer, this.inputSlots);
      });
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(Player pPlayer) {
      return this.access.evaluate((p_39785_, p_39786_) -> {
         return !this.isValidBlock(p_39785_.getBlockState(p_39786_)) ? false : pPlayer.distanceToSqr((double)p_39786_.getX() + 0.5D, (double)p_39786_.getY() + 0.5D, (double)p_39786_.getZ() + 0.5D) <= 64.0D;
      }, true);
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
         int i = this.getInventorySlotStart();
         int j = this.getUseRowEnd();
         if (pIndex == this.getResultSlot()) {
            if (!this.moveItemStackTo(itemstack1, i, j, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (this.inputSlotIndexes.contains(pIndex)) {
            if (!this.moveItemStackTo(itemstack1, i, j, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.canMoveIntoInputSlots(itemstack1) && pIndex >= this.getInventorySlotStart() && pIndex < this.getUseRowEnd()) {
            int k = this.getSlotToQuickMoveTo(itemstack);
            if (!this.moveItemStackTo(itemstack1, k, this.getResultSlot(), false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= this.getInventorySlotStart() && pIndex < this.getInventorySlotEnd()) {
            if (!this.moveItemStackTo(itemstack1, this.getUseRowStart(), this.getUseRowEnd(), false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= this.getUseRowStart() && pIndex < this.getUseRowEnd() && !this.moveItemStackTo(itemstack1, this.getInventorySlotStart(), this.getInventorySlotEnd(), false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, itemstack1);
      }

      return itemstack;
   }

   protected boolean canMoveIntoInputSlots(ItemStack pStack) {
      return true;
   }

   public int getSlotToQuickMoveTo(ItemStack pStack) {
      return this.inputSlots.isEmpty() ? 0 : this.inputSlotIndexes.get(0);
   }

   public int getResultSlot() {
      return this.resultSlotIndex;
   }

   private int getInventorySlotStart() {
      return this.getResultSlot() + 1;
   }

   private int getInventorySlotEnd() {
      return this.getInventorySlotStart() + 27;
   }

   private int getUseRowStart() {
      return this.getInventorySlotEnd();
   }

   private int getUseRowEnd() {
      return this.getUseRowStart() + 9;
   }
}