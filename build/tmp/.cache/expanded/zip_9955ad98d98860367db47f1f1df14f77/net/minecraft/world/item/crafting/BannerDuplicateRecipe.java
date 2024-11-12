package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

public class BannerDuplicateRecipe extends CustomRecipe {
   public BannerDuplicateRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, pCategory);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      DyeColor dyecolor = null;
      ItemStack itemstack = null;
      ItemStack itemstack1 = null;

      for(int i = 0; i < pInv.getContainerSize(); ++i) {
         ItemStack itemstack2 = pInv.getItem(i);
         if (!itemstack2.isEmpty()) {
            Item item = itemstack2.getItem();
            if (!(item instanceof BannerItem)) {
               return false;
            }

            BannerItem banneritem = (BannerItem)item;
            if (dyecolor == null) {
               dyecolor = banneritem.getColor();
            } else if (dyecolor != banneritem.getColor()) {
               return false;
            }

            int j = BannerBlockEntity.getPatternCount(itemstack2);
            if (j > 6) {
               return false;
            }

            if (j > 0) {
               if (itemstack != null) {
                  return false;
               }

               itemstack = itemstack2;
            } else {
               if (itemstack1 != null) {
                  return false;
               }

               itemstack1 = itemstack2;
            }
         }
      }

      return itemstack != null && itemstack1 != null;
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      for(int i = 0; i < pContainer.getContainerSize(); ++i) {
         ItemStack itemstack = pContainer.getItem(i);
         if (!itemstack.isEmpty()) {
            int j = BannerBlockEntity.getPatternCount(itemstack);
            if (j > 0 && j <= 6) {
               return itemstack.copyWithCount(1);
            }
         }
      }

      return ItemStack.EMPTY;
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer pInv) {
      NonNullList<ItemStack> nonnulllist = NonNullList.withSize(pInv.getContainerSize(), ItemStack.EMPTY);

      for(int i = 0; i < nonnulllist.size(); ++i) {
         ItemStack itemstack = pInv.getItem(i);
         if (!itemstack.isEmpty()) {
            if (itemstack.hasCraftingRemainingItem()) {
               nonnulllist.set(i, itemstack.getCraftingRemainingItem());
            } else if (itemstack.hasTag() && BannerBlockEntity.getPatternCount(itemstack) > 0) {
               nonnulllist.set(i, itemstack.copyWithCount(1));
            }
         }
      }

      return nonnulllist;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.BANNER_DUPLICATE;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth * pHeight >= 2;
   }
}
