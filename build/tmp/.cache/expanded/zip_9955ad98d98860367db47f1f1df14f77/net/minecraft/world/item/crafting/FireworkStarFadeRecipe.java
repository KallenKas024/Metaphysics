package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FireworkStarFadeRecipe extends CustomRecipe {
   private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

   public FireworkStarFadeRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      super(pId, pCategory);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingContainer pInv, Level pLevel) {
      boolean flag = false;
      boolean flag1 = false;

      for(int i = 0; i < pInv.getContainerSize(); ++i) {
         ItemStack itemstack = pInv.getItem(i);
         if (!itemstack.isEmpty()) {
            if (itemstack.getItem() instanceof DyeItem) {
               flag = true;
            } else {
               if (!STAR_INGREDIENT.test(itemstack)) {
                  return false;
               }

               if (flag1) {
                  return false;
               }

               flag1 = true;
            }
         }
      }

      return flag1 && flag;
   }

   public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
      List<Integer> list = Lists.newArrayList();
      ItemStack itemstack = null;

      for(int i = 0; i < pContainer.getContainerSize(); ++i) {
         ItemStack itemstack1 = pContainer.getItem(i);
         Item item = itemstack1.getItem();
         if (item instanceof DyeItem) {
            list.add(((DyeItem)item).getDyeColor().getFireworkColor());
         } else if (STAR_INGREDIENT.test(itemstack1)) {
            itemstack = itemstack1.copyWithCount(1);
         }
      }

      if (itemstack != null && !list.isEmpty()) {
         itemstack.getOrCreateTagElement("Explosion").putIntArray("FadeColors", list);
         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth * pHeight >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.FIREWORK_STAR_FADE;
   }
}