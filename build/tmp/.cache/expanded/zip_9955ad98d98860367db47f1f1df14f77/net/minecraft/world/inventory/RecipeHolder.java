package net.minecraft.world.inventory;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface RecipeHolder {
   void setRecipeUsed(@Nullable Recipe<?> pRecipe);

   @Nullable
   Recipe<?> getRecipeUsed();

   default void awardUsedRecipes(Player pPlayer, List<ItemStack> pItems) {
      Recipe<?> recipe = this.getRecipeUsed();
      if (recipe != null) {
         pPlayer.triggerRecipeCrafted(recipe, pItems);
         if (!recipe.isSpecial()) {
            pPlayer.awardRecipes(Collections.singleton(recipe));
            this.setRecipeUsed((Recipe<?>)null);
         }
      }

   }

   default boolean setRecipeUsed(Level pLevel, ServerPlayer pPlayer, Recipe<?> pRecipe) {
      if (!pRecipe.isSpecial() && pLevel.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) && !pPlayer.getRecipeBook().contains(pRecipe)) {
         return false;
      } else {
         this.setRecipeUsed(pRecipe);
         return true;
      }
   }
}