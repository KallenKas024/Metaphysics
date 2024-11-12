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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class SimpleCookingRecipeBuilder implements RecipeBuilder {
   private final RecipeCategory category;
   private final CookingBookCategory bookCategory;
   private final Item result;
   private final Ingredient ingredient;
   private final float experience;
   private final int cookingTime;
   private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
   @Nullable
   private String group;
   private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

   private SimpleCookingRecipeBuilder(RecipeCategory pCategory, CookingBookCategory pBookCategory, ItemLike pResult, Ingredient pIngredient, float pExperience, int pCookingTime, RecipeSerializer<? extends AbstractCookingRecipe> pSerializer) {
      this.category = pCategory;
      this.bookCategory = pBookCategory;
      this.result = pResult.asItem();
      this.ingredient = pIngredient;
      this.experience = pExperience;
      this.cookingTime = pCookingTime;
      this.serializer = pSerializer;
   }

   public static SimpleCookingRecipeBuilder generic(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, RecipeSerializer<? extends AbstractCookingRecipe> pSerializer) {
      return new SimpleCookingRecipeBuilder(pCategory, determineRecipeCategory(pSerializer, pResult), pResult, pIngredient, pExperience, pCookingTime, pSerializer);
   }

   public static SimpleCookingRecipeBuilder campfireCooking(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime) {
      return new SimpleCookingRecipeBuilder(pCategory, CookingBookCategory.FOOD, pResult, pIngredient, pExperience, pCookingTime, RecipeSerializer.CAMPFIRE_COOKING_RECIPE);
   }

   public static SimpleCookingRecipeBuilder blasting(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime) {
      return new SimpleCookingRecipeBuilder(pCategory, determineBlastingRecipeCategory(pResult), pResult, pIngredient, pExperience, pCookingTime, RecipeSerializer.BLASTING_RECIPE);
   }

   public static SimpleCookingRecipeBuilder smelting(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime) {
      return new SimpleCookingRecipeBuilder(pCategory, determineSmeltingRecipeCategory(pResult), pResult, pIngredient, pExperience, pCookingTime, RecipeSerializer.SMELTING_RECIPE);
   }

   public static SimpleCookingRecipeBuilder smoking(Ingredient pIngredient, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime) {
      return new SimpleCookingRecipeBuilder(pCategory, CookingBookCategory.FOOD, pResult, pIngredient, pExperience, pCookingTime, RecipeSerializer.SMOKING_RECIPE);
   }

   public SimpleCookingRecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
      this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
      return this;
   }

   public SimpleCookingRecipeBuilder group(@Nullable String pGroupName) {
      this.group = pGroupName;
      return this;
   }

   public Item getResult() {
      return this.result;
   }

   public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
      this.ensureValid(pRecipeId);
      this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId)).rewards(AdvancementRewards.Builder.recipe(pRecipeId)).requirements(RequirementsStrategy.OR);
      pFinishedRecipeConsumer.accept(new SimpleCookingRecipeBuilder.Result(pRecipeId, this.group == null ? "" : this.group, this.bookCategory, this.ingredient, this.result, this.experience, this.cookingTime, this.advancement, pRecipeId.withPrefix("recipes/" + this.category.getFolderName() + "/"), this.serializer));
   }

   private static CookingBookCategory determineSmeltingRecipeCategory(ItemLike pResult) {
      if (pResult.asItem().isEdible()) {
         return CookingBookCategory.FOOD;
      } else {
         return pResult.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
      }
   }

   private static CookingBookCategory determineBlastingRecipeCategory(ItemLike pResult) {
      return pResult.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
   }

   private static CookingBookCategory determineRecipeCategory(RecipeSerializer<? extends AbstractCookingRecipe> pSerializer, ItemLike pResult) {
      if (pSerializer == RecipeSerializer.SMELTING_RECIPE) {
         return determineSmeltingRecipeCategory(pResult);
      } else if (pSerializer == RecipeSerializer.BLASTING_RECIPE) {
         return determineBlastingRecipeCategory(pResult);
      } else if (pSerializer != RecipeSerializer.SMOKING_RECIPE && pSerializer != RecipeSerializer.CAMPFIRE_COOKING_RECIPE) {
         throw new IllegalStateException("Unknown cooking recipe type");
      } else {
         return CookingBookCategory.FOOD;
      }
   }

   /**
    * Makes sure that this obtainable.
    */
   private void ensureValid(ResourceLocation pId) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + pId);
      }
   }

   static class Result implements FinishedRecipe {
      private final ResourceLocation id;
      private final String group;
      private final CookingBookCategory category;
      private final Ingredient ingredient;
      private final Item result;
      private final float experience;
      private final int cookingTime;
      private final Advancement.Builder advancement;
      private final ResourceLocation advancementId;
      private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;

      public Result(ResourceLocation pId, String pGroup, CookingBookCategory pCategory, Ingredient pIngredient, Item pResult, float pExperience, int pCookingTime, Advancement.Builder pAdvancement, ResourceLocation pAdvancementId, RecipeSerializer<? extends AbstractCookingRecipe> pSerializer) {
         this.id = pId;
         this.group = pGroup;
         this.category = pCategory;
         this.ingredient = pIngredient;
         this.result = pResult;
         this.experience = pExperience;
         this.cookingTime = pCookingTime;
         this.advancement = pAdvancement;
         this.advancementId = pAdvancementId;
         this.serializer = pSerializer;
      }

      public void serializeRecipeData(JsonObject pJson) {
         if (!this.group.isEmpty()) {
            pJson.addProperty("group", this.group);
         }

         pJson.addProperty("category", this.category.getSerializedName());
         pJson.add("ingredient", this.ingredient.toJson());
         pJson.addProperty("result", BuiltInRegistries.ITEM.getKey(this.result).toString());
         pJson.addProperty("experience", this.experience);
         pJson.addProperty("cookingtime", this.cookingTime);
      }

      public RecipeSerializer<?> getType() {
         return this.serializer;
      }

      /**
       * Gets the ID for the recipe.
       */
      public ResourceLocation getId() {
         return this.id;
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