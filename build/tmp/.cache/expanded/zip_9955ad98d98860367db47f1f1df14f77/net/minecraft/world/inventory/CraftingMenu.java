package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CraftingMenu extends RecipeBookMenu<CraftingContainer> {
   public static final int RESULT_SLOT = 0;
   private static final int CRAFT_SLOT_START = 1;
   private static final int CRAFT_SLOT_END = 10;
   private static final int INV_SLOT_START = 10;
   private static final int INV_SLOT_END = 37;
   private static final int USE_ROW_SLOT_START = 37;
   private static final int USE_ROW_SLOT_END = 46;
   private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
   private final ResultContainer resultSlots = new ResultContainer();
   private final ContainerLevelAccess access;
   private final Player player;

   public CraftingMenu(int pContainerId, Inventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
   }

   public CraftingMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
      super(MenuType.CRAFTING, pContainerId);
      this.access = pAccess;
      this.player = pPlayerInventory.player;
      this.addSlot(new ResultSlot(pPlayerInventory.player, this.craftSlots, this.resultSlots, 0, 124, 35));

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            this.addSlot(new Slot(this.craftSlots, j + i * 3, 30 + j * 18, 17 + i * 18));
         }
      }

      for(int k = 0; k < 3; ++k) {
         for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(pPlayerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
         }
      }

      for(int l = 0; l < 9; ++l) {
         this.addSlot(new Slot(pPlayerInventory, l, 8 + l * 18, 142));
      }

   }

   protected static void slotChangedCraftingGrid(AbstractContainerMenu pMenu, Level pLevel, Player pPlayer, CraftingContainer pContainer, ResultContainer pResult) {
      if (!pLevel.isClientSide) {
         ServerPlayer serverplayer = (ServerPlayer)pPlayer;
         ItemStack itemstack = ItemStack.EMPTY;
         Optional<CraftingRecipe> optional = pLevel.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, pContainer, pLevel);
         if (optional.isPresent()) {
            CraftingRecipe craftingrecipe = optional.get();
            if (pResult.setRecipeUsed(pLevel, serverplayer, craftingrecipe)) {
               ItemStack itemstack1 = craftingrecipe.assemble(pContainer, pLevel.registryAccess());
               if (itemstack1.isItemEnabled(pLevel.enabledFeatures())) {
                  itemstack = itemstack1;
               }
            }
         }

         pResult.setItem(0, itemstack);
         pMenu.setRemoteSlot(0, itemstack);
         serverplayer.connection.send(new ClientboundContainerSetSlotPacket(pMenu.containerId, pMenu.incrementStateId(), 0, itemstack));
      }
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(Container pInventory) {
      this.access.execute((p_39386_, p_39387_) -> {
         slotChangedCraftingGrid(this, p_39386_, this.player, this.craftSlots, this.resultSlots);
      });
   }

   public void fillCraftSlotsStackedContents(StackedContents pItemHelper) {
      this.craftSlots.fillStackedContents(pItemHelper);
   }

   public void clearCraftingContent() {
      this.craftSlots.clearContent();
      this.resultSlots.clearContent();
   }

   public boolean recipeMatches(Recipe<? super CraftingContainer> pRecipe) {
      return pRecipe.matches(this.craftSlots, this.player.level());
   }

   /**
    * Called when the container is closed.
    */
   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.access.execute((p_39371_, p_39372_) -> {
         this.clearContainer(pPlayer, this.craftSlots);
      });
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(Player pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.CRAFTING_TABLE);
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
         if (pIndex == 0) {
            this.access.execute((p_39378_, p_39379_) -> {
               itemstack1.getItem().onCraftedBy(itemstack1, p_39378_, pPlayer);
            });
            if (!this.moveItemStackTo(itemstack1, 10, 46, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex >= 10 && pIndex < 46) {
            if (!this.moveItemStackTo(itemstack1, 1, 10, false)) {
               if (pIndex < 37) {
                  if (!this.moveItemStackTo(itemstack1, 37, 46, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.moveItemStackTo(itemstack1, 10, 37, false)) {
                  return ItemStack.EMPTY;
               }
            }
         } else if (!this.moveItemStackTo(itemstack1, 10, 46, false)) {
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
      return 10;
   }

   public RecipeBookType getRecipeBookType() {
      return RecipeBookType.CRAFTING;
   }

   public boolean shouldMoveToInventory(int pSlotIndex) {
      return pSlotIndex != this.getResultSlotIndex();
   }
}