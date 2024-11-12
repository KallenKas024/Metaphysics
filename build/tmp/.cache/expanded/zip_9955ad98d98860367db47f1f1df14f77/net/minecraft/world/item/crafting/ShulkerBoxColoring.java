package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public class ShulkerBoxColoring extends CustomRecipe {
   public ShulkerBoxColoring(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, pCategory);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      int i = 0;
      int j = 0;

      for(int k = 0; k < pInv.getContainerSize(); ++k) {
         ItemStack itemstack = pInv.getItem(k);
         if (!itemstack.isEmpty()) {
            if (Block.byItem(itemstack.getItem()) instanceof ShulkerBoxBlock) {
               ++i;
            } else {
               if (!itemstack.is(net.minecraftforge.common.Tags.Items.DYES)) {
                  return false;
               }

               ++j;
            }

            if (j > 1 || i > 1) {
               return false;
            }
         }
      }

      return i == 1 && j == 1;
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      ItemStack itemstack = ItemStack.EMPTY;
      net.minecraft.world.item.DyeColor dyecolor = net.minecraft.world.item.DyeColor.WHITE;

      for(int i = 0; i < pContainer.getContainerSize(); ++i) {
         ItemStack itemstack1 = pContainer.getItem(i);
         if (!itemstack1.isEmpty()) {
            Item item = itemstack1.getItem();
            if (Block.byItem(item) instanceof ShulkerBoxBlock) {
               itemstack = itemstack1;
            } else {
               net.minecraft.world.item.DyeColor tmp = net.minecraft.world.item.DyeColor.getColor(itemstack1);
               if (tmp != null) dyecolor = tmp;
            }
         }
      }

      ItemStack itemstack2 = ShulkerBoxBlock.getColoredItemStack(dyecolor);
      if (itemstack.hasTag()) {
         itemstack2.setTag(itemstack.getTag().copy());
      }

      return itemstack2;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth * pHeight >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SHULKER_BOX_COLORING;
   }
}
