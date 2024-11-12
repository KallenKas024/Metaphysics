package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class InventoryMenu extends RecipeBookMenu<CraftingContainer> {
   public static final int CONTAINER_ID = 0;
   public static final int RESULT_SLOT = 0;
   public static final int CRAFT_SLOT_START = 1;
   public static final int CRAFT_SLOT_END = 5;
   public static final int ARMOR_SLOT_START = 5;
   public static final int ARMOR_SLOT_END = 9;
   public static final int INV_SLOT_START = 9;
   public static final int INV_SLOT_END = 36;
   public static final int USE_ROW_SLOT_START = 36;
   public static final int USE_ROW_SLOT_END = 45;
   public static final int SHIELD_SLOT = 45;
   public static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");
   static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET};
   private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 2, 2);
   private final ResultContainer resultSlots = new ResultContainer();
   public final boolean active;
   private final Player owner;

   public InventoryMenu(Inventory pPlayerInventory, boolean pActive, final Player pOwner) {
      super((MenuType<?>)null, 0);
      this.active = pActive;
      this.owner = pOwner;
      this.addSlot(new ResultSlot(pPlayerInventory.player, this.craftSlots, this.resultSlots, 0, 154, 28));

      for(int i = 0; i < 2; ++i) {
         for(int j = 0; j < 2; ++j) {
            this.addSlot(new Slot(this.craftSlots, j + i * 2, 98 + j * 18, 18 + i * 18));
         }
      }

      for(int k = 0; k < 4; ++k) {
         final EquipmentSlot equipmentslot = SLOT_IDS[k];
         this.addSlot(new Slot(pPlayerInventory, 39 - k, 8, 8 + k * 18) {
            public void setByPlayer(ItemStack p_270969_) {
               InventoryMenu.onEquipItem(pOwner, equipmentslot, p_270969_, this.getItem());
               super.setByPlayer(p_270969_);
            }

            /**
             * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in
             * the case of armor slots)
             */
            public int getMaxStackSize() {
               return 1;
            }

            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            public boolean mayPlace(ItemStack p_39746_) {
               return p_39746_.canEquip(equipmentslot, owner);
            }

            /**
             * Return whether this slot's stack can be taken from this slot.
             */
            public boolean mayPickup(Player p_39744_) {
               ItemStack itemstack = this.getItem();
               return !itemstack.isEmpty() && !p_39744_.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.mayPickup(p_39744_);
            }

            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
               return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.TEXTURE_EMPTY_SLOTS[equipmentslot.getIndex()]);
            }
         });
      }

      for(int l = 0; l < 3; ++l) {
         for(int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(pPlayerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
         }
      }

      for(int i1 = 0; i1 < 9; ++i1) {
         this.addSlot(new Slot(pPlayerInventory, i1, 8 + i1 * 18, 142));
      }

      this.addSlot(new Slot(pPlayerInventory, 40, 77, 62) {
         public void setByPlayer(ItemStack p_270479_) {
            InventoryMenu.onEquipItem(pOwner, EquipmentSlot.OFFHAND, p_270479_, this.getItem());
            super.setByPlayer(p_270479_);
         }

         public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
         }
      });
   }

   static void onEquipItem(Player pPlayer, EquipmentSlot pSlot, ItemStack pNewItem, ItemStack pOldItem) {
      Equipable equipable = Equipable.get(pNewItem);
      if (equipable != null) {
         pPlayer.onEquipItem(pSlot, pOldItem, pNewItem);
      }

   }

   public static boolean isHotbarSlot(int pIndex) {
      return pIndex >= 36 && pIndex < 45 || pIndex == 45;
   }

   public void fillCraftSlotsStackedContents(StackedContents pItemHelper) {
      this.craftSlots.fillStackedContents(pItemHelper);
   }

   public void clearCraftingContent() {
      this.resultSlots.clearContent();
      this.craftSlots.clearContent();
   }

   public boolean recipeMatches(Recipe<? super CraftingContainer> pRecipe) {
      return pRecipe.matches(this.craftSlots, this.owner.level());
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(Container pInventory) {
      CraftingMenu.slotChangedCraftingGrid(this, this.owner.level(), this.owner, this.craftSlots, this.resultSlots);
   }

   /**
    * Called when the container is closed.
    */
   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.resultSlots.clearContent();
      if (!pPlayer.level().isClientSide) {
         this.clearContainer(pPlayer, this.craftSlots);
      }
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(Player pPlayer) {
      return true;
   }

   /**
    * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
    * inventory and the other inventory(s).
    */
   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
         if (pIndex == 0) {
            if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex >= 1 && pIndex < 5) {
            if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 5 && pIndex < 9) {
            if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(8 - equipmentslot.getIndex()).hasItem()) {
            int i = 8 - equipmentslot.getIndex();
            if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (equipmentslot == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem()) {
            if (!this.moveItemStackTo(itemstack1, 45, 46, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 9 && pIndex < 36) {
            if (!this.moveItemStackTo(itemstack1, 36, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 36 && pIndex < 45) {
            if (!this.moveItemStackTo(itemstack1, 9, 36, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
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
         if (pIndex == 0) {
            pPlayer.drop(itemstack1, false);
         }
      }

      return itemstack;
   }

   /**
    * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
    * null for the initial slot that was double-clicked.
    */
   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return pSlot.container != this.resultSlots && super.canTakeItemForPickAll(pStack, pSlot);
   }

   public int getResultSlotIndex() {
      return 0;
   }

   public int getGridWidth() {
      return this.craftSlots.getWidth();
   }

   public int getGridHeight() {
      return this.craftSlots.getHeight();
   }

   public int getSize() {
      return 5;
   }

   public CraftingContainer getCraftSlots() {
      return this.craftSlots;
   }

   public RecipeBookType getRecipeBookType() {
      return RecipeBookType.CRAFTING;
   }

   public boolean shouldMoveToInventory(int pSlotIndex) {
      return pSlotIndex != this.getResultSlotIndex();
   }
}
