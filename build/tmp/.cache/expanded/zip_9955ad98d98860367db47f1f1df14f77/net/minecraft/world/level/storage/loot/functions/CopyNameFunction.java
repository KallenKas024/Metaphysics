package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that sets the stack's name by copying it from somewhere else, such as the killing player.
 */
public class CopyNameFunction extends LootItemConditionalFunction {
   final CopyNameFunction.NameSource source;

   CopyNameFunction(LootItemCondition[] pConditions, CopyNameFunction.NameSource pNameSource) {
      super(pConditions);
      this.source = pNameSource;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.COPY_NAME;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(this.source.param);
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      Object object = pContext.getParamOrNull(this.source.param);
      if (object instanceof Nameable nameable) {
         if (nameable.hasCustomName()) {
            pStack.setHoverName(nameable.getDisplayName());
         }
      }

      return pStack;
   }

   public static LootItemConditionalFunction.Builder<?> copyName(CopyNameFunction.NameSource pSource) {
      return simpleBuilder((p_80191_) -> {
         return new CopyNameFunction(p_80191_, pSource);
      });
   }

   public static enum NameSource {
      THIS("this", LootContextParams.THIS_ENTITY),
      KILLER("killer", LootContextParams.KILLER_ENTITY),
      KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER),
      BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY);

      public final String name;
      public final LootContextParam<?> param;

      private NameSource(String pName, LootContextParam<?> pParam) {
         this.name = pName;
         this.param = pParam;
      }

      public static CopyNameFunction.NameSource getByName(String pName) {
         for(CopyNameFunction.NameSource copynamefunction$namesource : values()) {
            if (copynamefunction$namesource.name.equals(pName)) {
               return copynamefunction$namesource;
            }
         }

         throw new IllegalArgumentException("Invalid name source " + pName);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNameFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, CopyNameFunction pCopyNameFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pCopyNameFunction, pSerializationContext);
         pJson.addProperty("source", pCopyNameFunction.source.name);
      }

      public CopyNameFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         CopyNameFunction.NameSource copynamefunction$namesource = CopyNameFunction.NameSource.getByName(GsonHelper.getAsString(pObject, "source"));
         return new CopyNameFunction(pConditions, copynamefunction$namesource);
      }
   }
}