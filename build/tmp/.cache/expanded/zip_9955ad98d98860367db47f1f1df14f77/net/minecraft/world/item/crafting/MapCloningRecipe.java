package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class MapCloningRecipe extends CustomRecipe {
   public MapCloningRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, pCategory);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      int i = 0;
      ItemStack itemstack = ItemStack.EMPTY;

      for(int j = 0; j < pInv.getContainerSize(); ++j) {
         ItemStack itemstack1 = pInv.getItem(j);
         if (!itemstack1.isEmpty()) {
            if (itemstack1.is(Items.FILLED_MAP)) {
               if (!itemstack.isEmpty()) {
                  return false;
               }

               itemstack = itemstack1;
            } else {
               if (!itemstack1.is(Items.MAP)) {
                  return false;
               }

               ++i;
            }
         }
      }

      return !itemstack.isEmpty() && i > 0;
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      int i = 0;
      ItemStack itemstack = ItemStack.EMPTY;

      for(int j = 0; j < pContainer.getContainerSize(); ++j) {
         ItemStack itemstack1 = pContainer.getItem(j);
         if (!itemstack1.isEmpty()) {
            if (itemstack1.is(Items.FILLED_MAP)) {
               if (!itemstack.isEmpty()) {
                  return ItemStack.EMPTY;
               }

               itemstack = itemstack1;
            } else {
               if (!itemstack1.is(Items.MAP)) {
                  return ItemStack.EMPTY;
               }

               ++i;
            }
         }
      }

      return !itemstack.isEmpty() && i >= 1 ? itemstack.copyWithCount(i + 1) : ItemStack.EMPTY;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth >= 3 && pHeight >= 3;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.MAP_CLONING;
   }
}