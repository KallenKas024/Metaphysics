package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PickedUpItemTrigger extends SimpleCriterionTrigger<PickedUpItemTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public PickedUpItemTrigger(ResourceLocation pId) {
      this.id = pId;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   protected PickedUpItemTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "entity", pDeserializationContext);
      return new PickedUpItemTrigger.TriggerInstance(this.id, pPredicate, itempredicate, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pStack, @Nullable Entity pEntity) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_221306_) -> {
         return p_221306_.matches(pPlayer, pStack, lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;
      private final ContextAwarePredicate entity;

      public TriggerInstance(ResourceLocation pCriterion, ContextAwarePredicate pPlayer, ItemPredicate pItem, ContextAwarePredicate pEntity) {
         super(pCriterion, pPlayer);
         this.item = pItem;
         this.entity = pEntity;
      }

      public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByEntity(ContextAwarePredicate pPlayer, ItemPredicate pItem, ContextAwarePredicate pEntity) {
         return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.getId(), pPlayer, pItem, pEntity);
      }

      public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByPlayer(ContextAwarePredicate pPlayer, ItemPredicate pItem, ContextAwarePredicate pEntity) {
         return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.getId(), pPlayer, pItem, pEntity);
      }

      public boolean matches(ServerPlayer pPlayer, ItemStack pStack, LootContext pContext) {
         if (!this.item.matches(pStack)) {
            return false;
         } else {
            return this.entity.matches(pContext);
         }
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(pConditions));
         return jsonobject;
      }
   }
}