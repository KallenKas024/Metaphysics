package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.RegistryAccess;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeCollection {
   private final RegistryAccess registryAccess;
   private final List<Recipe<?>> recipes;
   private final boolean singleResultItem;
   private final Set<Recipe<?>> craftable = Sets.newHashSet();
   private final Set<Recipe<?>> fitsDimensions = Sets.newHashSet();
   private final Set<Recipe<?>> known = Sets.newHashSet();

   public RecipeCollection(RegistryAccess pRegistryAccess, List<Recipe<?>> pRecipes) {
      this.registryAccess = pRegistryAccess;
      this.recipes = ImmutableList.copyOf(pRecipes);
      if (pRecipes.size() <= 1) {
         this.singleResultItem = true;
      } else {
         this.singleResultItem = allRecipesHaveSameResult(pRegistryAccess, pRecipes);
      }

   }

   private static boolean allRecipesHaveSameResult(RegistryAccess pRegistryAccess, List<Recipe<?>> pRecipes) {
      int i = pRecipes.size();
      ItemStack itemstack = pRecipes.get(0).getResultItem(pRegistryAccess);

      for(int j = 1; j < i; ++j) {
         ItemStack itemstack1 = pRecipes.get(j).getResultItem(pRegistryAccess);
         if (!ItemStack.isSameItemSameTags(itemstack, itemstack1)) {
            return false;
         }
      }

      return true;
   }

   public RegistryAccess registryAccess() {
      return this.registryAccess;
   }

   /**
    * Checks if recipebook is not empty
    */
   public boolean hasKnownRecipes() {
      return !this.known.isEmpty();
   }

   public void updateKnownRecipes(RecipeBook pBook) {
      for(Recipe<?> recipe : this.recipes) {
         if (pBook.contains(recipe)) {
            this.known.add(recipe);
         }
      }

   }

   public void canCraft(StackedContents pHandler, int pWidth, int pHeight, RecipeBook pBook) {
      for(Recipe<?> recipe : this.recipes) {
         boolean flag = recipe.canCraftInDimensions(pWidth, pHeight) && pBook.contains(recipe);
         if (flag) {
            this.fitsDimensions.add(recipe);
         } else {
            this.fitsDimensions.remove(recipe);
         }

         if (flag && pHandler.canCraft(recipe, (IntList)null)) {
            this.craftable.add(recipe);
         } else {
            this.craftable.remove(recipe);
         }
      }

   }

   public boolean isCraftable(Recipe<?> pRecipe) {
      return this.craftable.contains(pRecipe);
   }

   public boolean hasCraftable() {
      return !this.craftable.isEmpty();
   }

   public boolean hasFitting() {
      return !this.fitsDimensions.isEmpty();
   }

   public List<Recipe<?>> getRecipes() {
      return this.recipes;
   }

   public List<Recipe<?>> getRecipes(boolean pOnlyCraftable) {
      List<Recipe<?>> list = Lists.newArrayList();
      Set<Recipe<?>> set = pOnlyCraftable ? this.craftable : this.fitsDimensions;

      for(Recipe<?> recipe : this.recipes) {
         if (set.contains(recipe)) {
            list.add(recipe);
         }
      }

      return list;
   }

   /**
    * 
    * @param pCraftable If true, this method will only return craftable recipes. If false, this method will only return
    * uncraftable recipes.
    */
   public List<Recipe<?>> getDisplayRecipes(boolean pCraftable) {
      List<Recipe<?>> list = Lists.newArrayList();

      for(Recipe<?> recipe : this.recipes) {
         if (this.fitsDimensions.contains(recipe) && this.craftable.contains(recipe) == pCraftable) {
            list.add(recipe);
         }
      }

      return list;
   }

   public boolean hasSingleResultItem() {
      return this.singleResultItem;
   }
}