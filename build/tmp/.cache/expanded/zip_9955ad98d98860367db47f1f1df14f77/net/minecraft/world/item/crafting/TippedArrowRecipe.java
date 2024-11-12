package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class TippedArrowRecipe extends CustomRecipe {
   public TippedArrowRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, pCategory);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      if (pInv.getWidth() == 3 && pInv.getHeight() == 3) {
         for(int i = 0; i < pInv.getWidth(); ++i) {
            for(int j = 0; j < pInv.getHeight(); ++j) {
               ItemStack itemstack = pInv.getItem(i + j * pInv.getWidth());
               if (itemstack.isEmpty()) {
                  return false;
               }

               if (i == 1 && j == 1) {
                  if (!itemstack.is(Items.LINGERING_POTION)) {
                     return false;
                  }
               } else if (!itemstack.is(Items.ARROW)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      ItemStack itemstack = pContainer.getItem(1 + pContainer.getWidth());
      if (!itemstack.is(Items.LINGERING_POTION)) {
         return ItemStack.EMPTY;
      } else {
         ItemStack itemstack1 = new ItemStack(Items.TIPPED_ARROW, 8);
         PotionUtils.setPotion(itemstack1, PotionUtils.getPotion(itemstack));
         PotionUtils.setCustomEffects(itemstack1, PotionUtils.getCustomEffects(itemstack));
         return itemstack1;
      }
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth >= 2 && pHeight >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.TIPPED_ARROW;
   }
}