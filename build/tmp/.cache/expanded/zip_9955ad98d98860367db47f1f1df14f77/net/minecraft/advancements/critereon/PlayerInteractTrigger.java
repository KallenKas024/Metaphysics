package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("player_interacted_with_entity");

   public ResourceLocation getId() {
      return ID;
   }

   protected PlayerInteractTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "entity", pDeserializationContext);
      return new PlayerInteractTrigger.TriggerInstance(pPredicate, itempredicate, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pItem, Entity pEntity) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_61501_) -> {
         return p_61501_.matches(pItem, lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate pPlayer, ItemPredicate pItem, ContextAwarePredicate pEntity) {
         super(PlayerInteractTrigger.ID, pPlayer);
         this.item = pItem;
         this.entity = pEntity;
      }

      public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(ContextAwarePredicate pPlayer, ItemPredicate.Builder pItem, ContextAwarePredicate pEntity) {
         return new PlayerInteractTrigger.TriggerInstance(pPlayer, pItem.build(), pEntity);
      }

      public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(ItemPredicate.Builder pItem, ContextAwarePredicate pEntity) {
         return itemUsedOnEntity(ContextAwarePredicate.ANY, pItem, pEntity);
      }

      public boolean matches(ItemStack pItem, LootContext pLootContext) {
         return !this.item.matches(pItem) ? false : this.entity.matches(pLootContext);
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(pConditions));
         return jsonobject;
      }
   }
}