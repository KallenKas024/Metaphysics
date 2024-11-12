package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface FinishedRecipe {
   void serializeRecipeData(JsonObject pJson);

   /**
    * Gets the JSON for the recipe.
    */
   default JsonObject serializeRecipe() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("type", BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.getType()).toString());
      this.serializeRecipeData(jsonobject);
      return jsonobject;
   }

   /**
    * Gets the ID for the recipe.
    */
   ResourceLocation getId();

   RecipeSerializer<?> getType();

   /**
    * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
    */
   @Nullable
   JsonObject serializeAdvancement();

   /**
    * Gets the ID for the advancement associated with this recipe. Should not be null if {@link #getAdvancementJson} is
    * non-null.
    */
   @Nullable
   ResourceLocation getAdvancementId();
}