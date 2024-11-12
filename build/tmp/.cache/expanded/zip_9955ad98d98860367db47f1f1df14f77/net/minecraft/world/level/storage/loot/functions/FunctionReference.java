package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final ResourceLocation name;

   FunctionReference(LootItemCondition[] pPredicates, ResourceLocation pName) {
      super(pPredicates);
      this.name = pName;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.REFERENCE;
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationContext pContext) {
      LootDataId<LootItemFunction> lootdataid = new LootDataId<>(LootDataType.MODIFIER, this.name);
      if (pContext.hasVisitedElement(lootdataid)) {
         pContext.reportProblem("Function " + this.name + " is recursively called");
      } else {
         super.validate(pContext);
         pContext.resolver().getElementOptional(lootdataid).ifPresentOrElse((p_279367_) -> {
            p_279367_.validate(pContext.enterElement(".{" + this.name + "}", lootdataid));
         }, () -> {
            pContext.reportProblem("Unknown function table called " + this.name);
         });
      }
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   protected ItemStack run(ItemStack pStack, LootContext pContext) {
      LootItemFunction lootitemfunction = pContext.getResolver().getElement(LootDataType.MODIFIER, this.name);
      if (lootitemfunction == null) {
         LOGGER.warn("Unknown function: {}", (Object)this.name);
         return pStack;
      } else {
         LootContext.VisitedEntry<?> visitedentry = LootContext.createVisitedEntry(lootitemfunction);
         if (pContext.pushVisitedElement(visitedentry)) {
            ItemStack itemstack;
            try {
               itemstack = lootitemfunction.apply(pStack, pContext);
            } finally {
               pContext.popVisitedElement(visitedentry);
            }

            return itemstack;
         } else {
            LOGGER.warn("Detected infinite loop in loot tables");
            return pStack;
         }
      }
   }

   public static LootItemConditionalFunction.Builder<?> functionReference(ResourceLocation pName) {
      return simpleBuilder((p_279452_) -> {
         return new FunctionReference(p_279452_, pName);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<FunctionReference> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_279239_, FunctionReference p_279287_, JsonSerializationContext p_279375_) {
         p_279239_.addProperty("name", p_279287_.name.toString());
      }

      public FunctionReference deserialize(JsonObject p_279189_, JsonDeserializationContext p_279307_, LootItemCondition[] p_279314_) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_279189_, "name"));
         return new FunctionReference(p_279314_, resourcelocation);
      }
   }
}