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

public class SmithingTransformRecipeBuilder {
   private final Ingredient template;
   private final Ingredient base;
   private final Ingredient addition;
   private final RecipeCategory category;
   private final Item result;
   private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
   private final RecipeSerializer<?> type;

   public SmithingTransformRecipeBuilder(RecipeSerializer<?> pType, Ingredient pTemplate, Ingredient pBase, Ingredient pAddition, RecipeCategory pCategory, Item pResult) {
      this.category = pCategory;
      this.type = pType;
      this.template = pTemplate;
      this.base = pBase;
      this.addition = pAddition;
      this.result = pResult;
   }

   public static SmithingTransformRecipeBuilder smithing(Ingredient pTemplate, Ingredient pBase, Ingredient pAddition, RecipeCategory pCategory, Item pResult) {
      return new SmithingTransformRecipeBuilder(RecipeSerializer.SMITHING_TRANSFORM, pTemplate, pBase, pAddition, pCategory, pResult);
   }

   public SmithingTransformRecipeBuilder unlocks(String pKey, CriterionTriggerInstance pCriterion) {
      this.advancement.addCriterion(pKey, pCriterion);
      return this;
   }

   public void save(Consumer<FinishedRecipe> pRecipeConsumer, String pLocation) {
      this.save(pRecipeConsumer, new ResourceLocation(pLocation));
   }

   public void save(Consumer<FinishedRecipe> pRecipeConsumer, ResourceLocation pLocation) {
      this.ensureValid(pLocation);
      this.advancement.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pLocation)).rewards(AdvancementRewards.Builder.recipe(pLocation)).requirements(RequirementsStrategy.OR);
      pRecipeConsumer.accept(new SmithingTransformRecipeBuilder.Result(pLocation, this.type, this.template, this.base, this.addition, this.result, this.advancement, pLocation.withPrefix("recipes/" + this.category.getFolderName() + "/")));
   }

   private void ensureValid(ResourceLocation pLocation) {
      if (this.advancement.getCriteria().isEmpty()) {
         throw new IllegalStateException("No way of obtaining recipe " + pLocation);
      }
   }

   public static record Result(ResourceLocation id, RecipeSerializer<?> type, Ingredient template, Ingredient base, Ingredient addition, Item result, Advancement.Builder advancement, ResourceLocation advancementId) implements FinishedRecipe {
      public void serializeRecipeData(JsonObject p_266713_) {
         p_266713_.add("template", this.template.toJson());
         p_266713_.add("base", this.base.toJson());
         p_266713_.add("addition", this.addition.toJson());
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
         p_266713_.add("result", jsonobject);
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