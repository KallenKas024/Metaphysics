package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.Level;

public class BookCloningRecipe extends CustomRecipe {
   public BookCloningRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
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
            if (itemstack1.is(Items.WRITTEN_BOOK)) {
               if (!itemstack.isEmpty()) {
                  return false;
               }

               itemstack = itemstack1;
            } else {
               if (!itemstack1.is(Items.WRITABLE_BOOK)) {
                  return false;
               }

               ++i;
            }
         }
      }

      return !itemstack.isEmpty() && itemstack.hasTag() && i > 0;
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      int i = 0;
      ItemStack itemstack = ItemStack.EMPTY;

      for(int j = 0; j < pContainer.getContainerSize(); ++j) {
         ItemStack itemstack1 = pContainer.getItem(j);
         if (!itemstack1.isEmpty()) {
            if (itemstack1.is(Items.WRITTEN_BOOK)) {
               if (!itemstack.isEmpty()) {
                  return ItemStack.EMPTY;
               }

               itemstack = itemstack1;
            } else {
               if (!itemstack1.is(Items.WRITABLE_BOOK)) {
                  return ItemStack.EMPTY;
               }

               ++i;
            }
         }
      }

      if (!itemstack.isEmpty() && itemstack.hasTag() && i >= 1 && WrittenBookItem.getGeneration(itemstack) < 2) {
         ItemStack itemstack2 = new ItemStack(Items.WRITTEN_BOOK, i);
         CompoundTag compoundtag = itemstack.getTag().copy();
         compoundtag.putInt("generation", WrittenBookItem.getGeneration(itemstack) + 1);
         itemstack2.setTag(compoundtag);
         return itemstack2;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer pInv) {
      NonNullList<ItemStack> nonnulllist = NonNullList.withSize(pInv.getContainerSize(), ItemStack.EMPTY);

      for(int i = 0; i < nonnulllist.size(); ++i) {
         ItemStack itemstack = pInv.getItem(i);
         if (itemstack.hasCraftingRemainingItem()) {
            nonnulllist.set(i, itemstack.getCraftingRemainingItem());
         } else if (itemstack.getItem() instanceof WrittenBookItem) {
            nonnulllist.set(i, itemstack.copyWithCount(1));
            break;
         }
      }

      return nonnulllist;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.BOOK_CLONING;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth >= 3 && pHeight >= 3;
   }
}
