package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public interface PlaceRecipe<T> {
   default void placeRecipe(int pWidth, int pHeight, int pOutputSlot, Recipe<?> pRecipe, Iterator<T> pIngredients, int pMaxAmount) {
      int i = pWidth;
      int j = pHeight;
      if (pRecipe instanceof net.minecraftforge.common.crafting.IShapedRecipe) {
         net.minecraftforge.common.crafting.IShapedRecipe shapedrecipe = (net.minecraftforge.common.crafting.IShapedRecipe)pRecipe;
         i = shapedrecipe.getRecipeWidth();
         j = shapedrecipe.getRecipeHeight();
      }

      int k1 = 0;

      for(int k = 0; k < pHeight; ++k) {
         if (k1 == pOutputSlot) {
            ++k1;
         }

         boolean flag = (float)j < (float)pHeight / 2.0F;
         int l = Mth.floor((float)pHeight / 2.0F - (float)j / 2.0F);
         if (flag && l > k) {
            k1 += pWidth;
            ++k;
         }

         for(int i1 = 0; i1 < pWidth; ++i1) {
            if (!pIngredients.hasNext()) {
               return;
            }

            flag = (float)i < (float)pWidth / 2.0F;
            l = Mth.floor((float)pWidth / 2.0F - (float)i / 2.0F);
            int j1 = i;
            boolean flag1 = i1 < i;
            if (flag) {
               j1 = l + i;
               flag1 = l <= i1 && i1 < l + i;
            }

            if (flag1) {
               this.addItemToSlot(pIngredients, k1, pMaxAmount, k, i1);
            } else if (j1 == i1) {
               k1 += pWidth - i1;
               break;
            }

            ++k1;
         }
      }

   }

   void addItemToSlot(Iterator<T> pIngredients, int pSlot, int pMaxAmount, int pY, int pX);
}
