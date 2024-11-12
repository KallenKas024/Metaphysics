package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SmithingTransformRecipe implements SmithingRecipe {
   private final ResourceLocation id;
   final Ingredient template;
   final Ingredient base;
   final Ingredient addition;
   final ItemStack result;

   public SmithingTransformRecipe(ResourceLocation pId, Ingredient pTemplate, Ingredient pBase, Ingredient pAddition, ItemStack pResult) {
      this.id = pId;
      this.template = pTemplate;
      this.base = pBase;
      this.addition = pAddition;
      this.result = pResult;
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(Container pContainer, Level pLevel) {
      return this.template.test(pContainer.getItem(0)) && this.base.test(pContainer.getItem(1)) && this.addition.test(pContainer.getItem(2));
   }

   public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
      ItemStack itemstack = this.result.copy();
      CompoundTag compoundtag = pContainer.getItem(1).getTag();
      if (compoundtag != null) {
         itemstack.setTag(compoundtag.copy());
      }

      return itemstack;
   }

   public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
      return this.result;
   }

   public boolean isTemplateIngredient(ItemStack pStack) {
      return this.template.test(pStack);
   }

   public boolean isBaseIngredient(ItemStack pStack) {
      return this.base.test(pStack);
   }

   public boolean isAdditionIngredient(ItemStack pStack) {
      return this.addition.test(pStack);
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SMITHING_TRANSFORM;
   }

   public boolean isIncomplete() {
      return Stream.of(this.template, this.base, this.addition).anyMatch(net.minecraftforge.common.ForgeHooks::hasNoElements);
   }

   public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
      public SmithingTransformRecipe fromJson(ResourceLocation p_266953_, JsonObject p_266720_) {
         Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(p_266720_, "template"));
         Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getNonNull(p_266720_, "base"));
         Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getNonNull(p_266720_, "addition"));
         ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(p_266720_, "result"));
         return new SmithingTransformRecipe(p_266953_, ingredient, ingredient1, ingredient2, itemstack);
      }

      public SmithingTransformRecipe fromNetwork(ResourceLocation p_267117_, FriendlyByteBuf p_267316_) {
         Ingredient ingredient = Ingredient.fromNetwork(p_267316_);
         Ingredient ingredient1 = Ingredient.fromNetwork(p_267316_);
         Ingredient ingredient2 = Ingredient.fromNetwork(p_267316_);
         ItemStack itemstack = p_267316_.readItem();
         return new SmithingTransformRecipe(p_267117_, ingredient, ingredient1, ingredient2, itemstack);
      }

      public void toNetwork(FriendlyByteBuf p_266746_, SmithingTransformRecipe p_266927_) {
         p_266927_.template.toNetwork(p_266746_);
         p_266927_.base.toNetwork(p_266746_);
         p_266927_.addition.toNetwork(p_266746_);
         p_266746_.writeItem(p_266927_.result);
      }
   }
}
