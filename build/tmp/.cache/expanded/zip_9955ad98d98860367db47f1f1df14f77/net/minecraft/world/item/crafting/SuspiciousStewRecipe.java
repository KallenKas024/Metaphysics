package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class SuspiciousStewRecipe extends CustomRecipe {
   public SuspiciousStewRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, pCategory);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      boolean flag = false;
      boolean flag1 = false;
      boolean flag2 = false;
      boolean flag3 = false;

      for(int i = 0; i < pInv.getContainerSize(); ++i) {
         ItemStack itemstack = pInv.getItem(i);
         if (!itemstack.isEmpty()) {
            if (itemstack.is(Blocks.BROWN_MUSHROOM.asItem()) && !flag2) {
               flag2 = true;
            } else if (itemstack.is(Blocks.RED_MUSHROOM.asItem()) && !flag1) {
               flag1 = true;
            } else if (itemstack.is(ItemTags.SMALL_FLOWERS) && !flag) {
               flag = true;
            } else {
               if (!itemstack.is(Items.BOWL) || flag3) {
                  return false;
               }

               flag3 = true;
            }
         }
      }

      return flag && flag2 && flag1 && flag3;
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      ItemStack itemstack = new ItemStack(Items.SUSPICIOUS_STEW, 1);

      for(int i = 0; i < pContainer.getContainerSize(); ++i) {
         ItemStack itemstack1 = pContainer.getItem(i);
         if (!itemstack1.isEmpty()) {
            SuspiciousEffectHolder suspiciouseffectholder = SuspiciousEffectHolder.tryGet(itemstack1.getItem());
            if (suspiciouseffectholder != null) {
               SuspiciousStewItem.saveMobEffect(itemstack, suspiciouseffectholder.getSuspiciousEffect(), suspiciouseffectholder.getEffectDuration());
               break;
            }
         }
      }

      return itemstack;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth >= 2 && pHeight >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SUSPICIOUS_STEW;
   }
}