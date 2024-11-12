package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CampfireCookingRecipe extends AbstractCookingRecipe {
   public CampfireCookingRecipe(ResourceLocation pId, String pGroup, CookingBookCategory pCategory, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime) {
      super(RecipeType.CAMPFIRE_COOKING, pId, pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime);
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.CAMPFIRE);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
   }
}