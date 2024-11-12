package net.minecraft.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
   private final int defaultCookingTime;
   private final SimpleCookingSerializer.CookieBaker<T> factory;

   public SimpleCookingSerializer(SimpleCookingSerializer.CookieBaker<T> pFactory, int pDefaultCookingTime) {
      this.defaultCookingTime = pDefaultCookingTime;
      this.factory = pFactory;
   }

   public T fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
      String s = GsonHelper.getAsString(pJson, "group", "");
      CookingBookCategory cookingbookcategory = CookingBookCategory.CODEC.byName(GsonHelper.getAsString(pJson, "category", (String)null), CookingBookCategory.MISC);
      JsonElement jsonelement = (JsonElement)(GsonHelper.isArrayNode(pJson, "ingredient") ? GsonHelper.getAsJsonArray(pJson, "ingredient") : GsonHelper.getAsJsonObject(pJson, "ingredient"));
      Ingredient ingredient = Ingredient.fromJson(jsonelement, false);
      //Forge: Check if primitive string to keep vanilla or a object which can contain a count field.
      if (!pJson.has("result")) throw new com.google.gson.JsonSyntaxException("Missing result, expected to find a string or object");
      ItemStack itemstack;
      if (pJson.get("result").isJsonObject()) itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
      else {
      String s1 = GsonHelper.getAsString(pJson, "result");
      ResourceLocation resourcelocation = new ResourceLocation(s1);
      itemstack = new ItemStack(BuiltInRegistries.ITEM.getOptional(resourcelocation).orElseThrow(() -> {
         return new IllegalStateException("Item: " + s1 + " does not exist");
      }));
      }
      float f = GsonHelper.getAsFloat(pJson, "experience", 0.0F);
      int i = GsonHelper.getAsInt(pJson, "cookingtime", this.defaultCookingTime);
      return this.factory.create(pRecipeId, s, cookingbookcategory, ingredient, itemstack, f, i);
   }

   public T fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
      String s = pBuffer.readUtf();
      CookingBookCategory cookingbookcategory = pBuffer.readEnum(CookingBookCategory.class);
      Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
      ItemStack itemstack = pBuffer.readItem();
      float f = pBuffer.readFloat();
      int i = pBuffer.readVarInt();
      return this.factory.create(pRecipeId, s, cookingbookcategory, ingredient, itemstack, f, i);
   }

   public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {
      pBuffer.writeUtf(pRecipe.group);
      pBuffer.writeEnum(pRecipe.category());
      pRecipe.ingredient.toNetwork(pBuffer);
      pBuffer.writeItem(pRecipe.result);
      pBuffer.writeFloat(pRecipe.experience);
      pBuffer.writeVarInt(pRecipe.cookingTime);
   }

   interface CookieBaker<T extends AbstractCookingRecipe> {
      T create(ResourceLocation pId, String pGroup, CookingBookCategory pCategory, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime);
   }
}
