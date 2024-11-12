package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;

public class ServerPlaceRecipe<C extends Container> implements PlaceRecipe<Integer> {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final StackedContents stackedContents = new StackedContents();
   protected Inventory inventory;
   protected RecipeBookMenu<C> menu;

   public ServerPlaceRecipe(RecipeBookMenu<C> pMenu) {
      this.menu = pMenu;
   }

   public void recipeClicked(ServerPlayer pPlayer, @Nullable Recipe<C> pRecipe, boolean pPlaceAll) {
      if (pRecipe != null && pPlayer.getRecipeBook().contains(pRecipe)) {
         this.inventory = pPlayer.getInventory();
         if (this.testClearGrid() || pPlayer.isCreative()) {
            this.stackedContents.clear();
            pPlayer.getInventory().fillStackedContents(this.stackedContents);
            this.menu.fillCraftSlotsStackedContents(this.stackedContents);
            if (this.stackedContents.canCraft(pRecipe, (IntList)null)) {
               this.handleRecipeClicked(pRecipe, pPlaceAll);
            } else {
               this.clearGrid();
               pPlayer.connection.send(new ClientboundPlaceGhostRecipePacket(pPlayer.containerMenu.containerId, pRecipe));
            }

            pPlayer.getInventory().setChanged();
         }
      }
   }

   protected void clearGrid() {
      for(int i = 0; i < this.menu.getSize(); ++i) {
         if (this.menu.shouldMoveToInventory(i)) {
            ItemStack itemstack = this.menu.getSlot(i).getItem().copy();
            this.inventory.placeItemBackInInventory(itemstack, false);
            this.menu.getSlot(i).set(itemstack);
         }
      }

      this.menu.clearCraftingContent();
   }

   protected void handleRecipeClicked(Recipe<C> pRecipe, boolean pPlaceAll) {
      boolean flag = this.menu.recipeMatches(pRecipe);
      int i = this.stackedContents.getBiggestCraftableStack(pRecipe, (IntList)null);
      if (flag) {
         for(int j = 0; j < this.menu.getGridHeight() * this.menu.getGridWidth() + 1; ++j) {
            if (j != this.menu.getResultSlotIndex()) {
               ItemStack itemstack = this.menu.getSlot(j).getItem();
               if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
                  return;
               }
            }
         }
      }

      int j1 = this.getStackSize(pPlaceAll, i, flag);
      IntList intlist = new IntArrayList();
      if (this.stackedContents.canCraft(pRecipe, intlist, j1)) {
         int k = j1;

         for(int l : intlist) {
            int i1 = StackedContents.fromStackingIndex(l).getMaxStackSize();
            if (i1 < k) {
               k = i1;
            }
         }

         if (this.stackedContents.canCraft(pRecipe, intlist, k)) {
            this.clearGrid();
            this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), pRecipe, intlist.iterator(), k);
         }
      }

   }

   public void addItemToSlot(Iterator<Integer> pIngredients, int pSlot, int pMaxAmount, int pY, int pX) {
      Slot slot = this.menu.getSlot(pSlot);
      ItemStack itemstack = StackedContents.fromStackingIndex(pIngredients.next());
      if (!itemstack.isEmpty()) {
         for(int i = 0; i < pMaxAmount; ++i) {
            this.moveItemToGrid(slot, itemstack);
         }
      }

   }

   protected int getStackSize(boolean pPlaceAll, int pMaxPossible, boolean pRecipeMatches) {
      int i = 1;
      if (pPlaceAll) {
         i = pMaxPossible;
      } else if (pRecipeMatches) {
         i = 64;

         for(int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++j) {
            if (j != this.menu.getResultSlotIndex()) {
               ItemStack itemstack = this.menu.getSlot(j).getItem();
               if (!itemstack.isEmpty() && i > itemstack.getCount()) {
                  i = itemstack.getCount();
               }
            }
         }

         if (i < 64) {
            ++i;
         }
      }

      return i;
   }

   protected void moveItemToGrid(Slot pSlotToFill, ItemStack pIngredient) {
      int i = this.inventory.findSlotMatchingUnusedItem(pIngredient);
      if (i != -1) {
         ItemStack itemstack = this.inventory.getItem(i);
         if (!itemstack.isEmpty()) {
            if (itemstack.getCount() > 1) {
               this.inventory.removeItem(i, 1);
            } else {
               this.inventory.removeItemNoUpdate(i);
            }

            if (pSlotToFill.getItem().isEmpty()) {
               pSlotToFill.set(itemstack.copyWithCount(1));
            } else {
               pSlotToFill.getItem().grow(1);
            }

         }
      }
   }

   /**
    * Places the output of the recipe into the player's inventory.
    */
   private boolean testClearGrid() {
      List<ItemStack> list = Lists.newArrayList();
      int i = this.getAmountOfFreeSlotsInInventory();

      for(int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++j) {
         if (j != this.menu.getResultSlotIndex()) {
            ItemStack itemstack = this.menu.getSlot(j).getItem().copy();
            if (!itemstack.isEmpty()) {
               int k = this.inventory.getSlotWithRemainingSpace(itemstack);
               if (k == -1 && list.size() <= i) {
                  for(ItemStack itemstack1 : list) {
                     if (ItemStack.isSameItem(itemstack1, itemstack) && itemstack1.getCount() != itemstack1.getMaxStackSize() && itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
                        itemstack1.grow(itemstack.getCount());
                        itemstack.setCount(0);
                        break;
                     }
                  }

                  if (!itemstack.isEmpty()) {
                     if (list.size() >= i) {
                        return false;
                     }

                     list.add(itemstack);
                  }
               } else if (k == -1) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private int getAmountOfFreeSlotsInInventory() {
      int i = 0;

      for(ItemStack itemstack : this.inventory.items) {
         if (itemstack.isEmpty()) {
            ++i;
         }
      }

      return i;
   }
}