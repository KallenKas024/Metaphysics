package net.minecraft.data.recipes;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
   ResourceLocation ROOT_RECIPE_ADVANCEMENT = new ResourceLocation("recipes/root");

   RecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger);

   RecipeBuilder group(@Nullable String pGroupName);

   Item getResult();

   void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId);

   default void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer) {
      this.save(pFinishedRecipeConsumer, getDefaultRecipeId(this.getResult()));
   }

   default void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, String pRecipeId) {
      ResourceLocation resourcelocation = getDefaultRecipeId(this.getResult());
      ResourceLocation resourcelocation1 = new ResourceLocation(pRecipeId);
      if (resourcelocation1.equals(resourcelocation)) {
         throw new IllegalStateException("Recipe " + pRecipeId + " should remove its 'save' argument as it is equal to default one");
      } else {
         this.save(pFinishedRecipeConsumer, resourcelocation1);
      }
   }

   static ResourceLocation getDefaultRecipeId(ItemLike pItemLike) {
      return BuiltInRegistries.ITEM.getKey(pItemLike.asItem());
   }
}