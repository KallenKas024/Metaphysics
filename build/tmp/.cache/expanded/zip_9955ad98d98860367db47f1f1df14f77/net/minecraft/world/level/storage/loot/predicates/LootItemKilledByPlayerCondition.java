package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * A LootItemCondition that matches if the last damage to an entity was done by a player.
 * 
 * @see LootContextParams#LAST_DAMAGE_PLAYER
 */
public class LootItemKilledByPlayerCondition implements LootItemCondition {
   static final LootItemKilledByPlayerCondition INSTANCE = new LootItemKilledByPlayerCondition();

   private LootItemKilledByPlayerCondition() {
   }

   public LootItemConditionType getType() {
      return LootItemConditions.KILLED_BY_PLAYER;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.LAST_DAMAGE_PLAYER);
   }

   public boolean test(LootContext pContext) {
      return pContext.hasParam(LootContextParams.LAST_DAMAGE_PLAYER);
   }

   public static LootItemCondition.Builder killedByPlayer() {
      return () -> {
         return INSTANCE;
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemKilledByPlayerCondition> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_81911_, LootItemKilledByPlayerCondition p_81912_, JsonSerializationContext p_81913_) {
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public LootItemKilledByPlayerCondition deserialize(JsonObject p_81919_, JsonDeserializationContext p_81920_) {
         return LootItemKilledByPlayerCondition.INSTANCE;
      }
   }
}