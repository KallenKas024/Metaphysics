package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SingleItemRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final Item result;
   private final Ingredient ingredient;
   private final int count;
   private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
   @Nullable
   private String group;
   private final RecipeSerializer<?> type;

   public SingleItemRecipeBuilder(RecipeCategory pCategory, RecipeSerializer<?> pType, Ingredient pIngredient, ItemLike pResult, int pCount) {
      this.category = pCategory;
      this.type = pType;
      this.result = pResult.asItem();
      this.ingredient = pIngredient;
      this.count = pCount;
   }

   public static SingleItemRecipeBuilder stonecutting(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult) {
      return new SingleItemRecipeBuilder(pCategory, RecipeSerializer.STONECUTTER, pIngredient, pResult, 1);
   }

   public static SingleItemRecipeBuilder stonecutting(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, int pCount) {
      return new SingleItemRecipeBuilder(pCategory, RecipeSerializer.STONECUTTER, pIngredient, pResult, pCount);
   }

   public SingleItemRecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
      this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
      return this;
   }

   public SingleItemRecipeBuilder group(@Nullable String pGroupName) {
      this.group = pGroupName;
      return this;
   }

   public Item getResult() {
      return this.result;
   }

   public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
      this.ensureValid(pRecipeId);
      this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId)).rewards(AdvancementRewards.Builder.recipe(pRecipeId)).requirements(RequirementsStrategy.OR);
      pFinishedRecipeConsumer.accept(new SingleItemRecipeBuilder.Result(pRecipeId, this.type, this.group == null ? "" : this.group, this.ingredient, this.result, this.count, this.advancement, pRecipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceLocation pId) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + pId);
      }
   }

   public static class Result implements FinishedRecipe {
      private final ResourceLocation id;
      private final String group;
      private final Ingredient ingredient;
      private final Item result;
      private final int count;
      private final Advancement.Builder advancement;
      private final ResourceLocation advancementId;
      private final RecipeSerializer<?> type;

      public Result(ResourceLocation pId, RecipeSerializer<?> pType, String pGroup, Ingredient pIngredient, Item pResult, int pCount, Advancement.Builder pAdvancement, ResourceLocation pAdvancementId) {
         this.id = pId;
         this.type = pType;
         this.group = pGroup;
         this.ingredient = pIngredient;
         this.result = pResult;
         this.count = pCount;
         this.advancement = pAdvancement;
         this.advancementId = pAdvancementId;
      }

      public void serializeRecipeData(JsonObject pJson) {
         if (!this.group.isEmpty()) {
            pJson.addProperty("group", this.group);
         }

         pJson.add("ingredient", this.ingredient.toJson());
         pJson.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
         pJson.addProperty("count", this.count);
      }

      /**
       * Gets the ID for the recipe.
       */
      public ResourceLocation getId() {
         return this.id;
      }

      public RecipeSerializer<?> getType() {
         return this.type;
      }

      /**
       * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
       */
      @Nullable
      public JsonObject serializeAdvancement() {
         return this.advancement.serializeToJson();
      }

      /**
       * Gets the ID for the advancement associated with this recipe. Should not be null if {@link #getAdvancementJson}
       * is non-null.
       */
      @Nullable
      public ResourceLocation getAdvancementId() {
         return this.advancementId;
      }
   }
}