package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StonecutterRecipe extends SingleItemRecipe {
   public StonecutterRecipe(ResourceLocation pId, String pGroup, Ingredient pIngredient, ItemStack pResult) {
      super(RecipeType.STONECUTTING, RecipeSerializer.STONECUTTER, pId, pGroup, pIngredient, pResult);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(Container pInv, Level pLevel) {
      return this.ingredient.test(pInv.getItem(0));
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.STONECUTTER);
   }
}