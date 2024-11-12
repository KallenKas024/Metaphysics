package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * Applies a random enchantment to the stack.
 * 
 * @see EnchantmentHelper#enchantItem
 */
public class EnchantWithLevelsFunction extends LootItemConditionalFunction {
   final NumberProvider levels;
   final boolean treasure;

   EnchantWithLevelsFunction(LootItemCondition[] pConditions, NumberProvider pLevels, boolean pTreasure) {
      super(pConditions);
      this.levels = pLevels;
      this.treasure = pTreasure;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.ENCHANT_WITH_LEVELS;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.levels.getReferencedContextParams();
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      RandomSource randomsource = pContext.getRandom();
      return EnchantmentHelper.enchantItem(randomsource, pStack, this.levels.getInt(pContext), this.treasure);
   }

   public static EnchantWithLevelsFunction.Builder enchantWithLevels(NumberProvider pLevels) {
      return new EnchantWithLevelsFunction.Builder(pLevels);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<EnchantWithLevelsFunction.Builder> {
      private final NumberProvider levels;
      private boolean treasure;

      public Builder(NumberProvider pLevels) {
         this.levels = pLevels;
      }

      protected EnchantWithLevelsFunction.Builder getThis() {
         return this;
      }

      public EnchantWithLevelsFunction.Builder allowTreasure() {
         this.treasure = true;
         return this;
      }

      public LootItemFunction build() {
         return new EnchantWithLevelsFunction(this.getConditions(), this.levels, this.treasure);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantWithLevelsFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, EnchantWithLevelsFunction pEnchantWithLevelsFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pEnchantWithLevelsFunction, pSerializationContext);
         pJson.add("levels", pSerializationContext.serialize(pEnchantWithLevelsFunction.levels));
         pJson.addProperty("treasure", pEnchantWithLevelsFunction.treasure);
      }

      public EnchantWithLevelsFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         NumberProvider numberprovider = GsonHelper.getAsObject(pObject, "levels", pDeserializationContext, NumberProvider.class);
         boolean flag = GsonHelper.getAsBoolean(pObject, "treasure", false);
         return new EnchantWithLevelsFunction(pConditions, numberprovider, flag);
      }
   }
}