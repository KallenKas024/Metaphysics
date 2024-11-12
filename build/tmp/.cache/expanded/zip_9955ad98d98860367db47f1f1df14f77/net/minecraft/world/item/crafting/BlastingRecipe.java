package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class BlastingRecipe extends AbstractCookingRecipe {
   public BlastingRecipe(ResourceLocation pId, String pGroup, CookingBookCategory pCategory, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime) {
      super(RecipeType.BLASTING, pId, pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime);
   }

   public ItemStack getToastSymbol() {
      return new ItemStack(Blocks.BLAST_FURNACE);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.BLASTING_RECIPE;
   }
}