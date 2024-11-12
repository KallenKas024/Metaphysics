package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public interface Recipe<C extends Container> {
   /**
    * Used to check if a recipe matches current crafting inventory
    */
   boolean matches(C pContainer, Level pLevel);

   ItemStack assemble(C pContainer, RegistryAccess pRegistryAccess);

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   boolean canCraftInDimensions(int pWidth, int pHeight);

   ItemStack getResultItem(RegistryAccess pRegistryAccess);

   default NonNullList<ItemStack> getRemainingItems(C pContainer) {
      NonNullList<ItemStack> nonnulllist = NonNullList.withSize(pContainer.getContainerSize(), ItemStack.EMPTY);

      for(int i = 0; i < nonnulllist.size(); ++i) {
         ItemStack item = pContainer.getItem(i);
         if (item.hasCraftingRemainingItem()) {
            nonnulllist.set(i, item.getCraftingRemainingItem());
         }
      }

      return nonnulllist;
   }

   default NonNullList<Ingredient> getIngredients() {
      return NonNullList.create();
   }

   /**
    * If true, this recipe does not appear in the recipe book and does not respect recipe unlocking (and the
    * doLimitedCrafting gamerule)
    */
   default boolean isSpecial() {
      return false;
   }

   default boolean showNotification() {
      return true;
   }

   /**
    * Recipes with equal group are combined into one button in the recipe book
    */
   default String getGroup() {
      return "";
   }

   default ItemStack getToastSymbol() {
      return new ItemStack(Blocks.CRAFTING_TABLE);
   }

   ResourceLocation getId();

   RecipeSerializer<?> getSerializer();

   RecipeType<?> getType();

   default boolean isIncomplete() {
      NonNullList<Ingredient> nonnulllist = this.getIngredients();
      return nonnulllist.isEmpty() || nonnulllist.stream().anyMatch((p_151268_) -> {
         return net.minecraftforge.common.ForgeHooks.hasNoElements(p_151268_);
      });
   }
}
