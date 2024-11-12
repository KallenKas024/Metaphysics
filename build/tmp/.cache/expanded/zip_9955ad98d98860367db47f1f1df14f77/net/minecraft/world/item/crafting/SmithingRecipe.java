package net.minecraft.world.item.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public interface SmithingRecipe extends Recipe<Container> {
   default RecipeType<?> getType() {
      return RecipeType.SMITHING;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   default boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth >= 3 && pHeight >= 1;
   }

   default ItemStack getToastSymbol() {
      return new ItemStack(Blocks.SMITHING_TABLE);
   }

   boolean isTemplateIngredient(ItemStack pStack);

   boolean isBaseIngredient(ItemStack pStack);

   boolean isAdditionIngredient(ItemStack pStack);
}