package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * LootItemFunction that sets the stack's count based on a {@link NumberProvider}, optionally adding to any existing
 * count.
 */
public class SetItemCountFunction extends LootItemConditionalFunction {
   final NumberProvider value;
   final boolean add;

   SetItemCountFunction(LootItemCondition[] pConditions, NumberProvider pCountValue, boolean pAdd) {
      super(pConditions);
      this.value = pCountValue;
      this.add = pAdd;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_COUNT;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.value.getReferencedContextParams();
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      int i = this.add ? pStack.getCount() : 0;
      pStack.setCount(Mth.clamp(i + this.value.getInt(pContext), 0, pStack.getMaxStackSize()));
      return pStack;
   }

   public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider pCountValue) {
      return simpleBuilder((p_165423_) -> {
         return new SetItemCountFunction(p_165423_, pCountValue, false);
      });
   }

   public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider pCountValue, boolean pAdd) {
      return simpleBuilder((p_165420_) -> {
         return new SetItemCountFunction(p_165420_, pCountValue, pAdd);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemCountFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetItemCountFunction pSetItemCountFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pSetItemCountFunction, pSerializationContext);
         pJson.add("count", pSerializationContext.serialize(pSetItemCountFunction.value));
         pJson.addProperty("add", pSetItemCountFunction.add);
      }

      public SetItemCountFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         NumberProvider numberprovider = GsonHelper.getAsObject(pObject, "count", pDeserializationContext, NumberProvider.class);
         boolean flag = GsonHelper.getAsBoolean(pObject, "add", false);
         return new SetItemCountFunction(pConditions, numberprovider, flag);
      }
   }
}