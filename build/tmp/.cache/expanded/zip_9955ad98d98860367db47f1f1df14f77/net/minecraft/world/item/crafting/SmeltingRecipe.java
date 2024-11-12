package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class SmeltingRecipe extends AbstractCookingRecipe {
   public SmeltingRecipe(ResourceLocation pId, String pGroup, CookingBookCategory pCategory, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime) {
      super(RecipeType.SMELTING, pId, pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime);
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.FURNACE);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SMELTING_RECIPE;
   }
}