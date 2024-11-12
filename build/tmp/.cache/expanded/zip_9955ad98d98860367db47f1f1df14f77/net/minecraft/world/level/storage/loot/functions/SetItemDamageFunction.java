package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.slf4j.Logger;

/**
 * LootItemFunction that sets the stack's damage based on a {@link NumberProvider}, optionally adding to any existing
 * damage.
 */
public class SetItemDamageFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final NumberProvider damage;
   final boolean add;

   SetItemDamageFunction(LootItemCondition[] pConditions, NumberProvider pDamageValue, boolean pAdd) {
      super(pConditions);
      this.damage = pDamageValue;
      this.add = pAdd;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_DAMAGE;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.damage.getReferencedContextParams();
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.isDamageableItem()) {
         int i = pStack.getMaxDamage();
         float f = this.add ? 1.0F - (float)pStack.getDamageValue() / (float)i : 0.0F;
         float f1 = 1.0F - Mth.clamp(this.damage.getFloat(pContext) + f, 0.0F, 1.0F);
         pStack.setDamageValue(Mth.floor(f1 * (float)i));
      } else {
         LOGGER.warn("Couldn't set damage of loot item {}", (Object)pStack);
      }

      return pStack;
   }

   public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider pDamageValue) {
      return simpleBuilder((p_165441_) -> {
         return new SetItemDamageFunction(p_165441_, pDamageValue, false);
      });
   }

   public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider pDamageValue, boolean pAdd) {
      return simpleBuilder((p_165438_) -> {
         return new SetItemDamageFunction(p_165438_, pDamageValue, pAdd);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemDamageFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetItemDamageFunction pSetItemDamageFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pSetItemDamageFunction, pSerializationContext);
         pJson.add("damage", pSerializationContext.serialize(pSetItemDamageFunction.damage));
         pJson.addProperty("add", pSetItemDamageFunction.add);
      }

      public SetItemDamageFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         NumberProvider numberprovider = GsonHelper.getAsObject(pObject, "damage", pDeserializationContext, NumberProvider.class);
         boolean flag = GsonHelper.getAsBoolean(pObject, "add", false);
         return new SetItemDamageFunction(pConditions, numberprovider, flag);
      }
   }
}