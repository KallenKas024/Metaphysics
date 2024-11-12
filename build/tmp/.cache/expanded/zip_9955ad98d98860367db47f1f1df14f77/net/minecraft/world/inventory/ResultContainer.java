package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class ResultContainer implements Container, RecipeHolder {
   private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);
   @Nullable
   private Recipe<?> recipeUsed;

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return 1;
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.itemStacks) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pIndex) {
      return this.itemStacks.get(0);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      return ContainerHelper.takeItem(this.itemStacks, 0);
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pIndex) {
      return ContainerHelper.takeItem(this.itemStacks, 0);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      this.itemStacks.set(0, pStack);
   }

   /**
    * For block entities, ensures the chunk containing the block entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void setChanged() {
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(Player pPlayer) {
      return true;
   }

   public void clearContent() {
      this.itemStacks.clear();
   }

   public void setRecipeUsed(@Nullable Recipe<?> pRecipe) {
      this.recipeUsed = pRecipe;
   }

   @Nullable
   public Recipe<?> getRecipeUsed() {
      return this.recipeUsed;
   }
}