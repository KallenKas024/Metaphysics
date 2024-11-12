package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ShieldDecorationRecipe extends CustomRecipe {
   public ShieldDecorationRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, pCategory);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      ItemStack itemstack = ItemStack.EMPTY;
      ItemStack itemstack1 = ItemStack.EMPTY;

      for(int i = 0; i < pInv.getContainerSize(); ++i) {
         ItemStack itemstack2 = pInv.getItem(i);
         if (!itemstack2.isEmpty()) {
            if (itemstack2.getItem() instanceof BannerItem) {
               if (!itemstack1.isEmpty()) {
                  return false;
               }

               itemstack1 = itemstack2;
            } else {
               if (!itemstack2.is(Items.SHIELD)) {
                  return false;
               }

               if (!itemstack.isEmpty()) {
                  return false;
               }

               if (BlockItem.getBlockEntityData(itemstack2) != null) {
                  return false;
               }

               itemstack = itemstack2;
            }
         }
      }

      return !itemstack.isEmpty() && !itemstack1.isEmpty();
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      ItemStack itemstack = ItemStack.EMPTY;
      ItemStack itemstack1 = ItemStack.EMPTY;

      for(int i = 0; i < pContainer.getContainerSize(); ++i) {
         ItemStack itemstack2 = pContainer.getItem(i);
         if (!itemstack2.isEmpty()) {
            if (itemstack2.getItem() instanceof BannerItem) {
               itemstack = itemstack2;
            } else if (itemstack2.is(Items.SHIELD)) {
               itemstack1 = itemstack2.copy();
            }
         }
      }

      if (itemstack1.isEmpty()) {
         return itemstack1;
      } else {
         CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
         CompoundTag compoundtag1 = compoundtag == null ? new CompoundTag() : compoundtag.copy();
         compoundtag1.putInt("Base", ((BannerItem)itemstack.getItem()).getColor().getId());
         BlockItem.setBlockEntityData(itemstack1, BlockEntityType.BANNER, compoundtag1);
         return itemstack1;
      }
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth * pHeight >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SHIELD_DECORATION;
   }
}