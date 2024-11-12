package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe> implements RecipeSerializer<T> {
   private final SimpleCraftingRecipeSerializer.Factory<T> constructor;

   public SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.Factory<T> pConstructor) {
      this.constructor = pConstructor;
   }

   public T fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
      CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(pSerializedRecipe, "category", (String)null), CraftingBookCategory.MISC);
      return this.constructor.create(pRecipeId, craftingbookcategory);
   }

   public T fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
      CraftingBookCategory craftingbookcategory = pBuffer.readEnum(CraftingBookCategory.class);
      return this.constructor.create(pRecipeId, craftingbookcategory);
   }

   public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {
      pBuffer.writeEnum(pRecipe.category());
   }

   @FunctionalInterface
   public interface Factory<T extends CraftingRecipe> {
      T create(ResourceLocation pId, CraftingBookCategory pCategory);
   }
}