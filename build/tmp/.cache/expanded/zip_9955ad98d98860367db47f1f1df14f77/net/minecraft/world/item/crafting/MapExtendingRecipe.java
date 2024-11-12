package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapExtendingRecipe extends ShapedRecipe {
   public MapExtendingRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, "", pCategory, 3, 3, NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.FILLED_MAP), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER)), new ItemStack(Items.MAP));
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      if (!super.matches(pInv, pLevel)) {
         return false;
      } else {
         ItemStack itemstack = findFilledMap(pInv);
         if (itemstack.isEmpty()) {
            return false;
         } else {
            MapItemSavedData mapitemsaveddata = MapItem.getSavedData(itemstack, pLevel);
            if (mapitemsaveddata == null) {
               return false;
            } else if (mapitemsaveddata.isExplorationMap()) {
               return false;
            } else {
               return mapitemsaveddata.scale < 4;
            }
         }
      }
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      ItemStack itemstack = findFilledMap(pContainer).copyWithCount(1);
      itemstack.getOrCreateTag().putInt("map_scale_direction", 1);
      return itemstack;
   }

   private static ItemStack findFilledMap(CraftingContainer pContainer) {
      for(int i = 0; i < pContainer.getContainerSize(); ++i) {
         ItemStack itemstack = pContainer.getItem(i);
         if (itemstack.is(Items.FILLED_MAP)) {
            return itemstack;
         }
      }

      return ItemStack.EMPTY;
   }

   /**
    * If true, this recipe does not appear in the recipe book and does not respect recipe unlocking (and the
    * doLimitedCrafting gamerule)
    */
   public boolean isSpecial() {
      return true;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.MAP_EXTENDING;
   }
}