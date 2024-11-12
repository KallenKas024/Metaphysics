package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that merges a given CompoundTag into the stack's NBT tag.
 */
public class SetNbtFunction extends LootItemConditionalFunction {
   final CompoundTag tag;

   SetNbtFunction(LootItemCondition[] pConditions, CompoundTag pTag) {
      super(pConditions);
      this.tag = pTag;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_NBT;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      pStack.getOrCreateTag().merge(this.tag);
      return pStack;
   }

   /** @deprecated */
   @Deprecated
   public static LootItemConditionalFunction.Builder<?> setTag(CompoundTag pTag) {
      return simpleBuilder((p_81191_) -> {
         return new SetNbtFunction(p_81191_, pTag);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetNbtFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetNbtFunction pSetNbtFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pSetNbtFunction, pSerializationContext);
         pJson.addProperty("tag", pSetNbtFunction.tag.toString());
      }

      public SetNbtFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         try {
            CompoundTag compoundtag = TagParser.parseTag(GsonHelper.getAsString(pObject, "tag"));
            return new SetNbtFunction(pConditions, compoundtag);
         } catch (CommandSyntaxException commandsyntaxexception) {
            throw new JsonSyntaxException(commandsyntaxexception.getMessage());
         }
      }
   }
}