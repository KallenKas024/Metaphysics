package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.slf4j.Logger;

/**
 * A LootItemCondition that refers to another LootItemCondition by its ID.
 */
public class ConditionReference implements LootItemCondition {
   private static final Logger LOGGER = LogUtils.getLogger();
   final ResourceLocation name;

   ConditionReference(ResourceLocation pName) {
      this.name = pName;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.REFERENCE;
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationContext pContext) {
      LootDataId<LootItemCondition> lootdataid = new LootDataId<>(LootDataType.PREDICATE, this.name);
      if (pContext.hasVisitedElement(lootdataid)) {
         pContext.reportProblem("Condition " + this.name + " is recursively called");
      } else {
         LootItemCondition.super.validate(pContext);
         pContext.resolver().getElementOptional(lootdataid).ifPresentOrElse((p_279085_) -> {
            p_279085_.validate(pContext.enterElement(".{" + this.name + "}", lootdataid));
         }, () -> {
            pContext.reportProblem("Unknown condition table called " + this.name);
         });
      }
   }

   public boolean test(LootContext pContext) {
      LootItemCondition lootitemcondition = pContext.getResolver().getElement(LootDataType.PREDICATE, this.name);
      if (lootitemcondition == null) {
         LOGGER.warn("Tried using unknown condition table called {}", (Object)this.name);
         return false;
      } else {
         LootContext.VisitedEntry<?> visitedentry = LootContext.createVisitedEntry(lootitemcondition);
         if (pContext.pushVisitedElement(visitedentry)) {
            boolean flag;
            try {
               flag = lootitemcondition.test(pContext);
            } finally {
               pContext.popVisitedElement(visitedentry);
            }

            return flag;
         } else {
            LOGGER.warn("Detected infinite loop in loot tables");
            return false;
         }
      }
   }

   public static LootItemCondition.Builder conditionReference(ResourceLocation pReferencedCondition) {
      return () -> {
         return new ConditionReference(pReferencedCondition);
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConditionReference> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_81571_, ConditionReference p_81572_, JsonSerializationContext p_81573_) {
         p_81571_.addProperty("name", p_81572_.name.toString());
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public ConditionReference deserialize(JsonObject p_81579_, JsonDeserializationContext p_81580_) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_81579_, "name"));
         return new ConditionReference(resourcelocation);
      }
   }
}