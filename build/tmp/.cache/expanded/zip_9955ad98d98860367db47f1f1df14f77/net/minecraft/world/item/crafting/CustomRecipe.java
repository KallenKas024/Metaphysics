package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class CustomRecipe implements CraftingRecipe {
   private final ResourceLocation id;
   private final CraftingBookCategory category;

   public CustomRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
      this.id = pId;
      this.category = pCategory;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   /**
    * If true, this recipe does not appear in the recipe book and does not respect recipe unlocking (and the
    * doLimitedCrafting gamerule)
    */
   public boolean isSpecial() {
      return true;
   }

   public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
      return ItemStack.EMPTY;
   }

   public CraftingBookCategory category() {
      return this.category;
   }
}