package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientRecipeBook extends RecipeBook {
   private static final Logger LOGGER = LogUtils.getLogger();
   private Map<RecipeBookCategories, List<RecipeCollection>> collectionsByTab = ImmutableMap.of();
   private List<RecipeCollection> allCollections = ImmutableList.of();

   public void setupCollections(Iterable<Recipe<?>> pRecipes, RegistryAccess pRegistryAccess) {
      Map<RecipeBookCategories, List<List<Recipe<?>>>> map = categorizeAndGroupRecipes(pRecipes);
      Map<RecipeBookCategories, List<RecipeCollection>> map1 = Maps.newHashMap();
      ImmutableList.Builder<RecipeCollection> builder = ImmutableList.builder();
      map.forEach((p_266602_, p_266603_) -> {
         map1.put(p_266602_, p_266603_.stream().map((p_266605_) -> {
            return new RecipeCollection(pRegistryAccess, p_266605_);
         }).peek(builder::add).collect(ImmutableList.toImmutableList()));
      });
      RecipeBookCategories.AGGREGATE_CATEGORIES.forEach((p_90637_, p_90638_) -> {
         map1.put(p_90637_, p_90638_.stream().flatMap((p_167706_) -> {
            return map1.getOrDefault(p_167706_, ImmutableList.of()).stream();
         }).collect(ImmutableList.toImmutableList()));
      });
      this.collectionsByTab = ImmutableMap.copyOf(map1);
      this.allCollections = builder.build();
   }

   private static Map<RecipeBookCategories, List<List<Recipe<?>>>> categorizeAndGroupRecipes(Iterable<Recipe<?>> pRecipes) {
      Map<RecipeBookCategories, List<List<Recipe<?>>>> map = Maps.newHashMap();
      Table<RecipeBookCategories, String, List<Recipe<?>>> table = HashBasedTable.create();

      for(Recipe<?> recipe : pRecipes) {
         if (!recipe.isSpecial() && !recipe.isIncomplete()) {
            RecipeBookCategories recipebookcategories = getCategory(recipe);
            String s = recipe.getGroup().isEmpty() ? recipe.getId().toString() : recipe.getGroup(); // FORGE: Group value defaults to the recipe's ID if the recipe's explicit group is empty.
            if (s.isEmpty()) {
               map.computeIfAbsent(recipebookcategories, (p_90645_) -> {
                  return Lists.newArrayList();
               }).add(ImmutableList.of(recipe));
            } else {
               List<Recipe<?>> list = table.get(recipebookcategories, s);
               if (list == null) {
                  list = Lists.newArrayList();
                  table.put(recipebookcategories, s, list);
                  map.computeIfAbsent(recipebookcategories, (p_90641_) -> {
                     return Lists.newArrayList();
                  }).add(list);
               }

               list.add(recipe);
            }
         }
      }

      return map;
   }

   private static RecipeBookCategories getCategory(Recipe<?> pRecipe) {
      if (pRecipe instanceof CraftingRecipe) {
         CraftingRecipe craftingrecipe = (CraftingRecipe)pRecipe;
         RecipeBookCategories recipebookcategories;
         switch (craftingrecipe.category()) {
            case BUILDING:
               recipebookcategories = RecipeBookCategories.CRAFTING_BUILDING_BLOCKS;
               break;
            case EQUIPMENT:
               recipebookcategories = RecipeBookCategories.CRAFTING_EQUIPMENT;
               break;
            case REDSTONE:
               recipebookcategories = RecipeBookCategories.CRAFTING_REDSTONE;
               break;
            case MISC:
               recipebookcategories = RecipeBookCategories.CRAFTING_MISC;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return recipebookcategories;
      } else {
         RecipeType<?> recipetype = pRecipe.getType();
         if (pRecipe instanceof AbstractCookingRecipe) {
            AbstractCookingRecipe abstractcookingrecipe = (AbstractCookingRecipe)pRecipe;
            CookingBookCategory cookingbookcategory = abstractcookingrecipe.category();
            if (recipetype == RecipeType.SMELTING) {
               RecipeBookCategories recipebookcategories1;
               switch (cookingbookcategory) {
                  case BLOCKS:
                     recipebookcategories1 = RecipeBookCategories.FURNACE_BLOCKS;
                     break;
                  case FOOD:
                     recipebookcategories1 = RecipeBookCategories.FURNACE_FOOD;
                     break;
                  case MISC:
                     recipebookcategories1 = RecipeBookCategories.FURNACE_MISC;
                     break;
                  default:
                     throw new IncompatibleClassChangeError();
               }

               return recipebookcategories1;
            }

            if (recipetype == RecipeType.BLASTING) {
               return cookingbookcategory == CookingBookCategory.BLOCKS ? RecipeBookCategories.BLAST_FURNACE_BLOCKS : RecipeBookCategories.BLAST_FURNACE_MISC;
            }

            if (recipetype == RecipeType.SMOKING) {
               return RecipeBookCategories.SMOKER_FOOD;
            }

            if (recipetype == RecipeType.CAMPFIRE_COOKING) {
               return RecipeBookCategories.CAMPFIRE;
            }
         }

         if (recipetype == RecipeType.STONECUTTING) {
            return RecipeBookCategories.STONECUTTER;
         } else if (recipetype == RecipeType.SMITHING) {
            return RecipeBookCategories.SMITHING;
         } else {
            RecipeBookCategories categories = net.minecraftforge.client.RecipeBookManager.findCategories((RecipeType) recipetype, pRecipe);
            if (categories != null) return categories;
            LOGGER.warn("Unknown recipe category: {}/{}", LogUtils.defer(() -> {
               return BuiltInRegistries.RECIPE_TYPE.getKey(pRecipe.getType());
            }), LogUtils.defer(pRecipe::getId));
            return RecipeBookCategories.UNKNOWN;
         }
      }
   }

   public List<RecipeCollection> getCollections() {
      return this.allCollections;
   }

   public List<RecipeCollection> getCollection(RecipeBookCategories pCategories) {
      return this.collectionsByTab.getOrDefault(pCategories, Collections.emptyList());
   }
}
