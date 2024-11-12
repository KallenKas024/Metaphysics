package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * A LootItemCondition that succeeds with a given probability.
 */
public class LootItemRandomChanceCondition implements LootItemCondition {
   final float probability;

   LootItemRandomChanceCondition(float pProbability) {
      this.probability = pProbability;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.RANDOM_CHANCE;
   }

   public boolean test(LootContext pContext) {
      return pContext.getRandom().nextFloat() < this.probability;
   }

   public static LootItemCondition.Builder randomChance(float pProbability) {
      return () -> {
         return new LootItemRandomChanceCondition(pProbability);
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemRandomChanceCondition> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_81943_, LootItemRandomChanceCondition p_81944_, JsonSerializationContext p_81945_) {
         p_81943_.addProperty("chance", p_81944_.probability);
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public LootItemRandomChanceCondition deserialize(JsonObject p_81951_, JsonDeserializationContext p_81952_) {
         return new LootItemRandomChanceCondition(GsonHelper.getAsFloat(p_81951_, "chance"));
      }
   }
}