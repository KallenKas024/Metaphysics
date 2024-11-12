package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RecipeManager extends SimpleJsonResourceReloadListener {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private static final Logger LOGGER = LogUtils.getLogger();
   private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = ImmutableMap.of();
   private Map<ResourceLocation, Recipe<?>> byName = ImmutableMap.of();
   private boolean hasErrors;
   private final net.minecraftforge.common.crafting.conditions.ICondition.IContext context; //Forge: add context

   /** @deprecated Forge: use {@linkplain RecipeManager#RecipeManager(net.minecraftforge.common.crafting.conditions.ICondition.IContext) constructor with context}. */
   @Deprecated
   public RecipeManager() {
      this(net.minecraftforge.common.crafting.conditions.ICondition.IContext.EMPTY);
   }

   public RecipeManager(net.minecraftforge.common.crafting.conditions.ICondition.IContext context) {
      super(GSON, "recipes");
      this.context = context;
   }

   /**
    * Applies the prepared sound event registrations and caches to the sound manager.
    * @param pObject The prepared sound event registrations and caches
    * @param pResourceManager The resource manager
    * @param pProfiler The profiler
    */
   protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      this.hasErrors = false;
      Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> map = Maps.newHashMap();
      ImmutableMap.Builder<ResourceLocation, Recipe<?>> builder = ImmutableMap.builder();

      for(Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
         ResourceLocation resourcelocation = entry.getKey();
         if (resourcelocation.getPath().startsWith("_")) continue; //Forge: filter anything beginning with "_" as it's used for metadata.

         try {
            if (entry.getValue().isJsonObject() && !net.minecraftforge.common.crafting.CraftingHelper.processConditions(entry.getValue().getAsJsonObject(), "conditions", this.context)) {
               LOGGER.debug("Skipping loading recipe {} as it's conditions were not met", resourcelocation);
               continue;
            }
            Recipe<?> recipe = fromJson(resourcelocation, GsonHelper.convertToJsonObject(entry.getValue(), "top element"), this.context);
            if (recipe == null) {
               LOGGER.info("Skipping loading recipe {} as it's serializer returned null", resourcelocation);
               continue;
            }
            map.computeIfAbsent(recipe.getType(), (p_44075_) -> {
               return ImmutableMap.builder();
            }).put(resourcelocation, recipe);
            builder.put(resourcelocation, recipe);
         } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
            LOGGER.error("Parsing error loading recipe {}", resourcelocation, jsonparseexception);
         }
      }

      this.recipes = map.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (p_44033_) -> {
         return p_44033_.getValue().build();
      }));
      this.byName = builder.build();
      LOGGER.info("Loaded {} recipes", (int)map.size());
   }

   public boolean hadErrorsLoading() {
      return this.hasErrors;
   }

   public <C extends Container, T extends Recipe<C>> Optional<T> getRecipeFor(RecipeType<T> pRecipeType, C pInventory, Level pLevel) {
      return this.byType(pRecipeType).values().stream().filter((p_220266_) -> {
         return p_220266_.matches(pInventory, pLevel);
      }).findFirst();
   }

   public <C extends Container, T extends Recipe<C>> Optional<Pair<ResourceLocation, T>> getRecipeFor(RecipeType<T> pRecipeType, C pInventory, Level pLevel, @Nullable ResourceLocation pLastRecipe) {
      Map<ResourceLocation, T> map = this.byType(pRecipeType);
      if (pLastRecipe != null) {
         T t = map.get(pLastRecipe);
         if (t != null && t.matches(pInventory, pLevel)) {
            return Optional.of(Pair.of(pLastRecipe, t));
         }
      }

      return map.entrySet().stream().filter((p_220245_) -> {
         return p_220245_.getValue().matches(pInventory, pLevel);
      }).findFirst().map((p_220256_) -> {
         return Pair.of(p_220256_.getKey(), p_220256_.getValue());
      });
   }

   public <C extends Container, T extends Recipe<C>> List<T> getAllRecipesFor(RecipeType<T> pRecipeType) {
      return List.copyOf(this.byType(pRecipeType).values());
   }

   public <C extends Container, T extends Recipe<C>> List<T> getRecipesFor(RecipeType<T> pRecipeType, C pInventory, Level pLevel) {
      return this.byType(pRecipeType).values().stream().filter((p_220241_) -> {
         return p_220241_.matches(pInventory, pLevel);
      }).sorted(Comparator.comparing((p_270043_) -> {
         return p_270043_.getResultItem(pLevel.registryAccess()).getDescriptionId();
      })).collect(Collectors.toList());
   }

   private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> byType(RecipeType<T> pRecipeType) {
      return (Map<ResourceLocation, T>)this.recipes.getOrDefault(pRecipeType, Collections.emptyMap());
   }

   public <C extends Container, T extends Recipe<C>> NonNullList<ItemStack> getRemainingItemsFor(RecipeType<T> pRecipeType, C pInventory, Level pLevel) {
      Optional<T> optional = this.getRecipeFor(pRecipeType, pInventory, pLevel);
      if (optional.isPresent()) {
         return optional.get().getRemainingItems(pInventory);
      } else {
         NonNullList<ItemStack> nonnulllist = NonNullList.withSize(pInventory.getContainerSize(), ItemStack.EMPTY);

         for(int i = 0; i < nonnulllist.size(); ++i) {
            nonnulllist.set(i, pInventory.getItem(i));
         }

         return nonnulllist;
      }
   }

   public Optional<? extends Recipe<?>> byKey(ResourceLocation pRecipeId) {
      return Optional.ofNullable(this.byName.get(pRecipeId));
   }

   public Collection<Recipe<?>> getRecipes() {
      return this.recipes.values().stream().flatMap((p_220270_) -> {
         return p_220270_.values().stream();
      }).collect(Collectors.toSet());
   }

   public Stream<ResourceLocation> getRecipeIds() {
      return this.recipes.values().stream().flatMap((p_220258_) -> {
         return p_220258_.keySet().stream();
      });
   }

   /** @deprecated Forge: use {@linkplain #fromJson(ResourceLocation, JsonObject, net.minecraftforge.common.crafting.conditions.ICondition.IContext) overload with context}. */
   /**
    * Deserializes a recipe object from json data.
    */
   @Deprecated
   public static Recipe<?> fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
      return fromJson(pRecipeId, pJson, net.minecraftforge.common.crafting.conditions.ICondition.IContext.EMPTY);
   }

   public static Recipe<?> fromJson(ResourceLocation pRecipeId, JsonObject pJson, net.minecraftforge.common.crafting.conditions.ICondition.IContext context) {
      String s = GsonHelper.getAsString(pJson, "type");
      return BuiltInRegistries.RECIPE_SERIALIZER.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
         return new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'");
      }).fromJson(pRecipeId, pJson, context);
   }

   public void replaceRecipes(Iterable<Recipe<?>> pRecipes) {
      this.hasErrors = false;
      Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> map = Maps.newHashMap();
      ImmutableMap.Builder<ResourceLocation, Recipe<?>> builder = ImmutableMap.builder();
      pRecipes.forEach((p_220262_) -> {
         Map<ResourceLocation, Recipe<?>> map1 = map.computeIfAbsent(p_220262_.getType(), (p_220272_) -> {
            return Maps.newHashMap();
         });
         ResourceLocation resourcelocation = p_220262_.getId();
         Recipe<?> recipe = map1.put(resourcelocation, p_220262_);
         builder.put(resourcelocation, p_220262_);
         if (recipe != null) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + resourcelocation);
         }
      });
      this.recipes = ImmutableMap.copyOf(map);
      this.byName = builder.build();
   }

   public static <C extends Container, T extends Recipe<C>> RecipeManager.CachedCheck<C, T> createCheck(final RecipeType<T> pRecipeType) {
      return new RecipeManager.CachedCheck<C, T>() {
         @Nullable
         private ResourceLocation lastRecipe;

         public Optional<T> getRecipeFor(C p_220278_, Level p_220279_) {
            RecipeManager recipemanager = p_220279_.getRecipeManager();
            Optional<Pair<ResourceLocation, T>> optional = recipemanager.getRecipeFor(pRecipeType, p_220278_, p_220279_, this.lastRecipe);
            if (optional.isPresent()) {
               Pair<ResourceLocation, T> pair = optional.get();
               this.lastRecipe = pair.getFirst();
               return Optional.of(pair.getSecond());
            } else {
               return Optional.empty();
            }
         }
      };
   }

   public interface CachedCheck<C extends Container, T extends Recipe<C>> {
      Optional<T> getRecipeFor(C pContainer, Level pLevel);
   }
}
