package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("player_generates_container_loot");

   public ResourceLocation getId() {
      return ID;
   }

   protected LootTableTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "loot_table"));
      return new LootTableTrigger.TriggerInstance(pPredicate, resourcelocation);
   }

   public void trigger(ServerPlayer pPlayer, ResourceLocation pLootTable) {
      this.trigger(pPlayer, (p_54606_) -> {
         return p_54606_.matches(pLootTable);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ResourceLocation lootTable;

      public TriggerInstance(ContextAwarePredicate pPlayer, ResourceLocation pLootTable) {
         super(LootTableTrigger.ID, pPlayer);
         this.lootTable = pLootTable;
      }

      public static LootTableTrigger.TriggerInstance lootTableUsed(ResourceLocation pLootTable) {
         return new LootTableTrigger.TriggerInstance(ContextAwarePredicate.ANY, pLootTable);
      }

      public boolean matches(ResourceLocation pLootTable) {
         return this.lootTable.equals(pLootTable);
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.addProperty("loot_table", this.lootTable.toString());
         return jsonobject;
      }
   }
}