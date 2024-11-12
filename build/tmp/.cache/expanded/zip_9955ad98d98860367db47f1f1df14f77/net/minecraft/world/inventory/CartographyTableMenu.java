package net.minecraft.world.inventory;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CartographyTableMenu extends AbstractContainerMenu {
   public static final int MAP_SLOT = 0;
   public static final int ADDITIONAL_SLOT = 1;
   public static final int RESULT_SLOT = 2;
   private static final int INV_SLOT_START = 3;
   private static final int INV_SLOT_END = 30;
   private static final int USE_ROW_SLOT_START = 30;
   private static final int USE_ROW_SLOT_END = 39;
   private final ContainerLevelAccess access;
   long lastSoundTime;
   public final Container container = new SimpleContainer(2) {
      /**
       * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         CartographyTableMenu.this.slotsChanged(this);
         super.setChanged();
      }
   };
   private final ResultContainer resultContainer = new ResultContainer() {
      /**
       * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         CartographyTableMenu.this.slotsChanged(this);
         super.setChanged();
      }
   };

   public CartographyTableMenu(int pContainerId, Inventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
   }

   public CartographyTableMenu(int pContainerId, Inventory pPlayerInventory, final ContainerLevelAccess pAccess) {
      super(MenuType.CARTOGRAPHY_TABLE, pContainerId);
      this.access = pAccess;
      this.addSlot(new Slot(this.container, 0, 15, 15) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack p_39194_) {
            return p_39194_.is(Items.FILLED_MAP);
         }
      });
      this.addSlot(new Slot(this.container, 1, 15, 52) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack p_39203_) {
            return p_39203_.is(Items.PAPER) || p_39203_.is(Items.MAP) || p_39203_.is(Items.GLASS_PANE);
         }
      });
      this.addSlot(new Slot(this.resultContainer, 2, 145, 39) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack p_39217_) {
            return false;
         }

         public void onTake(Player p_150509_, ItemStack p_150510_) {
            CartographyTableMenu.this.slots.get(0).remove(1);
            CartographyTableMenu.this.slots.get(1).remove(1);
            p_150510_.getItem().onCraftedBy(p_150510_, p_150509_.level(), p_150509_);
            pAccess.execute((p_39219_, p_39220_) -> {
               long l = p_39219_.getGameTime();
               if (CartographyTableMenu.this.lastSoundTime != l) {
                  p_39219_.playSound((Player)null, p_39220_, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  CartographyTableMenu.this.lastSoundTime = l;
               }

            });
            super.onTake(p_150509_, p_150510_);
         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
      }

   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(Player pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.CARTOGRAPHY_TABLE);
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(Container pInventory) {
      ItemStack itemstack = this.container.getItem(0);
      ItemStack itemstack1 = this.container.getItem(1);
      ItemStack itemstack2 = this.resultContainer.getItem(2);
      if (itemstack2.isEmpty() || !itemstack.isEmpty() && !itemstack1.isEmpty()) {
         if (!itemstack.isEmpty() && !itemstack1.isEmpty()) {
            this.setupResultSlot(itemstack, itemstack1, itemstack2);
         }
      } else {
         this.resultContainer.removeItemNoUpdate(2);
      }

   }

   private void setupResultSlot(ItemStack pMap, ItemStack pFirstSlotStack, ItemStack pResultOutput) {
      this.access.execute((p_279039_, p_279040_) -> {
         MapItemSavedData mapitemsaveddata = MapItem.getSavedData(pMap, p_279039_);
         if (mapitemsaveddata != null) {
            ItemStack itemstack;
            if (pFirstSlotStack.is(Items.PAPER) && !mapitemsaveddata.locked && mapitemsaveddata.scale < 4) {
               itemstack = pMap.copyWithCount(1);
               itemstack.getOrCreateTag().putInt("map_scale_direction", 1);
               this.broadcastChanges();
            } else if (pFirstSlotStack.is(Items.GLASS_PANE) && !mapitemsaveddata.locked) {
               itemstack = pMap.copyWithCount(1);
               itemstack.getOrCreateTag().putBoolean("map_to_lock", true);
               this.broadcastChanges();
            } else {
               if (!pFirstSlotStack.is(Items.MAP)) {
                  this.resultContainer.removeItemNoUpdate(2);
                  this.broadcastChanges();
                  return;
               }

               itemstack = pMap.copyWithCount(2);
               this.broadcastChanges();
            }

            if (!ItemStack.matches(itemstack, pResultOutput)) {
               this.resultContainer.setItem(2, itemstack);
               this.broadcastChanges();
            }

         }
      });
   }

   /**
    * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
    * null for the initial slot that was double-clicked.
    */
   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return pSlot.container != this.resultContainer && super.canTakeItemForPickAll(pStack, pSlot);
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
         if (pIndex == 2) {
            itemstack1.getItem().onCraftedBy(itemstack1, pPlayer.level(), pPlayer);
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex != 1 && pIndex != 0) {
            if (itemstack1.is(Items.FILLED_MAP)) {
               if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!itemstack1.is(Items.PAPER) && !itemstack1.is(Items.MAP) && !itemstack1.is(Items.GLASS_PANE)) {
               if (pIndex >= 3 && pIndex < 30) {
                  if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (pIndex >= 30 && pIndex < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         }

         slot.setChanged();
         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, itemstack1);
         this.broadcastChanges();
      }

      return itemstack;
   }

   /**
    * Called when the container is closed.
    */
   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.resultContainer.removeItemNoUpdate(2);
      this.access.execute((p_39152_, p_39153_) -> {
         this.clearContainer(pPlayer, this.container);
      });
   }
}