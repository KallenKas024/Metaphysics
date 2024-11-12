package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * A LootItemCondition that inverts the output of another one.
 */
public class InvertedLootItemCondition implements LootItemCondition {
   final LootItemCondition term;

   InvertedLootItemCondition(LootItemCondition pTerm) {
      this.term = pTerm;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.INVERTED;
   }

   public final boolean test(LootContext pContext) {
      return !this.term.test(pContext);
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.term.getReferencedContextParams();
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationContext pContext) {
      LootItemCondition.super.validate(pContext);
      this.term.validate(pContext);
   }

   public static LootItemCondition.Builder invert(LootItemCondition.Builder pToInvert) {
      InvertedLootItemCondition invertedlootitemcondition = new InvertedLootItemCondition(pToInvert.build());
      return () -> {
         return invertedlootitemcondition;
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<InvertedLootItemCondition> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_81706_, InvertedLootItemCondition p_81707_, JsonSerializationContext p_81708_) {
         p_81706_.add("term", p_81708_.serialize(p_81707_.term));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public InvertedLootItemCondition deserialize(JsonObject p_81714_, JsonDeserializationContext p_81715_) {
         LootItemCondition lootitemcondition = GsonHelper.getAsObject(p_81714_, "term", p_81715_, LootItemCondition.class);
         return new InvertedLootItemCondition(lootitemcondition);
      }
   }
}