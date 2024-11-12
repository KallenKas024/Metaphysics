package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction extends LootItemConditionalFunction {
   final Potion potion;

   SetPotionFunction(LootItemCondition[] pConditions, Potion pPotion) {
      super(pConditions);
      this.potion = pPotion;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_POTION;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      PotionUtils.setPotion(pStack, this.potion);
      return pStack;
   }

   public static LootItemConditionalFunction.Builder<?> setPotion(Potion pPotion) {
      return simpleBuilder((p_193079_) -> {
         return new SetPotionFunction(p_193079_, pPotion);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetPotionFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetPotionFunction pSetPotionFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pSetPotionFunction, pSerializationContext);
         pJson.addProperty("id", BuiltInRegistries.POTION.getKey(pSetPotionFunction.potion).toString());
      }

      public SetPotionFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         String s = GsonHelper.getAsString(pObject, "id");
         Potion potion = BuiltInRegistries.POTION.getOptional(ResourceLocation.tryParse(s)).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown potion '" + s + "'");
         });
         return new SetPotionFunction(pConditions, potion);
      }
   }
}