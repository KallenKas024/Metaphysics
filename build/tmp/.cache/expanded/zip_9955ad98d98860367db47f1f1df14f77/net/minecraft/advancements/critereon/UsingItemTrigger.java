package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class UsingItemTrigger extends SimpleCriterionTrigger<UsingItemTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("using_item");

   public ResourceLocation getId() {
      return ID;
   }

   public UsingItemTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new UsingItemTrigger.TriggerInstance(pPredicate, itempredicate);
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pItem) {
      this.trigger(pPlayer, (p_163870_) -> {
         return p_163870_.matches(pItem);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate pPlayer, ItemPredicate pItem) {
         super(UsingItemTrigger.ID, pPlayer);
         this.item = pItem;
      }

      public static UsingItemTrigger.TriggerInstance lookingAt(EntityPredicate.Builder pEntityPredicateBuilder, ItemPredicate.Builder pItemPredicateBuilder) {
         return new UsingItemTrigger.TriggerInstance(EntityPredicate.wrap(pEntityPredicateBuilder.build()), pItemPredicateBuilder.build());
      }

      public boolean matches(ItemStack pItem) {
         return this.item.matches(pItem);
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}