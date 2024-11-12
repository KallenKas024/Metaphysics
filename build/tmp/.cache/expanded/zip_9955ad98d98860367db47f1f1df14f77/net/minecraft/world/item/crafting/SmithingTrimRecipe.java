package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.Level;

public class SmithingTrimRecipe implements SmithingRecipe {
   private final ResourceLocation id;
   final Ingredient template;
   final Ingredient base;
   final Ingredient addition;

   public SmithingTrimRecipe(ResourceLocation pId, Ingredient pTemplate, Ingredient pBase, Ingredient pAddition) {
      this.id = pId;
      this.template = pTemplate;
      this.base = pBase;
      this.addition = pAddition;
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(Container pContainer, Level pLevel) {
      return this.template.test(pContainer.getItem(0)) && this.base.test(pContainer.getItem(1)) && this.addition.test(pContainer.getItem(2));
   }

   public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
      ItemStack itemstack = pContainer.getItem(1);
      if (this.base.test(itemstack)) {
         Optional<Holder.Reference<TrimMaterial>> optional = TrimMaterials.getFromIngredient(pRegistryAccess, pContainer.getItem(2));
         Optional<Holder.Reference<TrimPattern>> optional1 = TrimPatterns.getFromTemplate(pRegistryAccess, pContainer.getItem(0));
         if (optional.isPresent() && optional1.isPresent()) {
            Optional<ArmorTrim> optional2 = ArmorTrim.getTrim(pRegistryAccess, itemstack);
            if (optional2.isPresent() && optional2.get().hasPatternAndMaterial(optional1.get(), optional.get())) {
               return ItemStack.EMPTY;
            }

            ItemStack itemstack1 = itemstack.copy();
            itemstack1.setCount(1);
            ArmorTrim armortrim = new ArmorTrim(optional.get(), optional1.get());
            if (ArmorTrim.setTrim(pRegistryAccess, itemstack1, armortrim)) {
               return itemstack1;
            }
         }
      }

      return ItemStack.EMPTY;
   }

   public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
      ItemStack itemstack = new ItemStack(Items.IRON_CHESTPLATE);
      Optional<Holder.Reference<TrimPattern>> optional = pRegistryAccess.registryOrThrow(Registries.TRIM_PATTERN).holders().findFirst();
      if (optional.isPresent()) {
         Optional<Holder.Reference<TrimMaterial>> optional1 = pRegistryAccess.registryOrThrow(Registries.TRIM_MATERIAL).getHolder(TrimMaterials.REDSTONE);
         if (optional1.isPresent()) {
            ArmorTrim armortrim = new ArmorTrim(optional1.get(), optional.get());
            ArmorTrim.setTrim(pRegistryAccess, itemstack, armortrim);
         }
      }

      return itemstack;
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
      return RecipeSerializer.SMITHING_TRIM;
   }

   public boolean isIncomplete() {
      return Stream.of(this.template, this.base, this.addition).anyMatch(net.minecraftforge.common.ForgeHooks::hasNoElements);
   }

   public static class Serializer implements RecipeSerializer<SmithingTrimRecipe> {
      public SmithingTrimRecipe fromJson(ResourceLocation p_267037_, JsonObject p_267004_) {
         Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(p_267004_, "template"));
         Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getNonNull(p_267004_, "base"));
         Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getNonNull(p_267004_, "addition"));
         return new SmithingTrimRecipe(p_267037_, ingredient, ingredient1, ingredient2);
      }

      public SmithingTrimRecipe fromNetwork(ResourceLocation p_267169_, FriendlyByteBuf p_267251_) {
         Ingredient ingredient = Ingredient.fromNetwork(p_267251_);
         Ingredient ingredient1 = Ingredient.fromNetwork(p_267251_);
         Ingredient ingredient2 = Ingredient.fromNetwork(p_267251_);
         return new SmithingTrimRecipe(p_267169_, ingredient, ingredient1, ingredient2);
      }

      public void toNetwork(FriendlyByteBuf p_266901_, SmithingTrimRecipe p_266893_) {
         p_266893_.template.toNetwork(p_266901_);
         p_266893_.base.toNetwork(p_266901_);
         p_266893_.addition.toNetwork(p_266901_);
      }
   }
}
