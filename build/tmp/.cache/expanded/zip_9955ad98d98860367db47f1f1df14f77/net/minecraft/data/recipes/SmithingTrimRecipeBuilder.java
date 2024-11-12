package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SmithingTrimRecipeBuilder {
   private final RecipeCategory category;
   private final Ingredient template;
   private final Ingredient base;
   private final Ingredient addition;
   private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
   private final RecipeSerializer<?> type;

   public SmithingTrimRecipeBuilder(RecipeSerializer<?> pType, RecipeCategory pCategory, Ingredient pTemplate, Ingredient pBase, Ingredient pAddition) {
      this.category = pCategory;
      this.type = pType;
      this.template = pTemplate;
      this.base = pBase;
      this.addition = pAddition;
   }

   public static SmithingTrimRecipeBuilder smithingTrim(Ingredient pTemplate, Ingredient pBase, Ingredient pAddition, RecipeCategory pCategory) {
      return new SmithingTrimRecipeBuilder(RecipeSerializer.SMITHING_TRIM, pCategory, pTemplate, pBase, pAddition);
   }

   public SmithingTrimRecipeBuilder unlocks(String pKey, CriterionTriggerInstance pCriterion) {
      this.advancement.addCriterion(pKey, pCriterion);
      return this;
   }

   public void save(Consumer<FinishedRecipe> pRecipeConsumer, ResourceLocation pLocation) {
      this.ensureValid(pLocation);
      this.advancement.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pLocation)).rewards(AdvancementRewards.Builder.recipe(pLocation)).requirements(RequirementsStrategy.OR);
      pRecipeConsumer.accept(new SmithingTrimRecipeBuilder.Result(pLocation, this.type, this.template, this.base, this.addition, this.advancement, pLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceLocation pLocation) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + pLocation);
      }
   }

   public static record Result(ResourceLocation id, RecipeSerializer<?> type, Ingredient template, Ingredient base, Ingredient addition, Advancement.Builder advancement, ResourceLocation advancementId) implements FinishedRecipe {
      public void serializeRecipeData(JsonObject p_267008_) {
         p_267008_.add("template", this.template.toJson());
         p_267008_.add("base", this.base.toJson());
         p_267008_.add("addition", this.addition.toJson());
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