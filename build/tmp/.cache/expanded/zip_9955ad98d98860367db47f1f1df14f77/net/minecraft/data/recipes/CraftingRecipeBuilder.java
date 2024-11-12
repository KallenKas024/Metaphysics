package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.CraftingBookCategory;

public abstract class CraftingRecipeBuilder {
   protected static CraftingBookCategory determineBookCategory(RecipeCategory pCategory) {
      CraftingBookCategory craftingbookcategory;
      switch (pCategory) {
         case BUILDING_BLOCKS:
            craftingbookcategory = CraftingBookCategory.BUILDING;
            break;
         case TOOLS:
         case COMBAT:
            craftingbookcategory = CraftingBookCategory.EQUIPMENT;
            break;
         case REDSTONE:
            craftingbookcategory = CraftingBookCategory.REDSTONE;
            break;
         default:
            craftingbookcategory = CraftingBookCategory.MISC;
      }

      return craftingbookcategory;
   }

   protected abstract static class CraftingResult implements FinishedRecipe {
      private final CraftingBookCategory category;

      protected CraftingResult(CraftingBookCategory pCategory) {
         this.category = pCategory;
      }

      public void serializeRecipeData(JsonObject pJson) {
         pJson.addProperty("category", this.category.getSerializedName());
      }
   }
}