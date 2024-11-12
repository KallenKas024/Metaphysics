package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class SingleItemRecipe implements Recipe<Container> {
   protected final Ingredient ingredient;
   protected final ItemStack result;
   private final RecipeType<?> type;
   private final RecipeSerializer<?> serializer;
   protected final ResourceLocation id;
   protected final String group;

   public SingleItemRecipe(RecipeType<?> pType, RecipeSerializer<?> pSerializer, ResourceLocation pId, String pGroup, Ingredient pIngredient, ItemStack pResult) {
      this.type = pType;
      this.serializer = pSerializer;
      this.id = pId;
      this.group = pGroup;
      this.ingredient = pIngredient;
      this.result = pResult;
   }

   public RecipeType<?> getType() {
      return this.type;
   }

   public RecipeSerializer<?> getSerializer() {
      return this.serializer;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   /**
    * Recipes with equal group are combined into one button in the recipe book
    */
   public String getGroup() {
      return this.group;
   }

   public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      NonNullList<Ingredient> nonnulllist = NonNullList.create();
      nonnulllist.add(this.ingredient);
      return nonnulllist;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return true;
   }

   public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
      return this.result.copy();
   }

   public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
      final SingleItemRecipe.Serializer.SingleItemMaker<T> factory;

      protected Serializer(SingleItemRecipe.Serializer.SingleItemMaker<T> pFactory) {
         this.factory = pFactory;
      }

      public T fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
         String s = GsonHelper.getAsString(pJson, "group", "");
         Ingredient ingredient;
         if (GsonHelper.isArrayNode(pJson, "ingredient")) {
            ingredient = Ingredient.fromJson(GsonHelper.getAsJsonArray(pJson, "ingredient"), false);
         } else {
            ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "ingredient"), false);
         }

         String s1 = GsonHelper.getAsString(pJson, "result");
         int i = GsonHelper.getAsInt(pJson, "count");
         ItemStack itemstack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(s1)), i);
         return this.factory.create(pRecipeId, s, ingredient, itemstack);
      }

      public T fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
         String s = pBuffer.readUtf();
         Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
         ItemStack itemstack = pBuffer.readItem();
         return this.factory.create(pRecipeId, s, ingredient, itemstack);
      }

      public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe) {
         pBuffer.writeUtf(pRecipe.group);
         pRecipe.ingredient.toNetwork(pBuffer);
         pBuffer.writeItem(pRecipe.result);
      }

      interface SingleItemMaker<T extends SingleItemRecipe> {
         T create(ResourceLocation pId, String pGroup, Ingredient pIngredient, ItemStack pResult);
      }
   }
}