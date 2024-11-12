package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger extends SimpleCriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("item_durability_changed");

   public ResourceLocation getId() {
      return ID;
   }

   public ItemDurabilityTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("durability"));
      MinMaxBounds.Ints minmaxbounds$ints1 = MinMaxBounds.Ints.fromJson(pJson.get("delta"));
      return new ItemDurabilityTrigger.TriggerInstance(pPredicate, itempredicate, minmaxbounds$ints, minmaxbounds$ints1);
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pItem, int pNewDurability) {
      this.trigger(pPlayer, (p_43676_) -> {
         return p_43676_.matches(pItem, pNewDurability);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;
      private final MinMaxBounds.Ints durability;
      private final MinMaxBounds.Ints delta;

      public TriggerInstance(ContextAwarePredicate pPlayer, ItemPredicate pItem, MinMaxBounds.Ints pDurability, MinMaxBounds.Ints pDelta) {
         super(ItemDurabilityTrigger.ID, pPlayer);
         this.item = pItem;
         this.durability = pDurability;
         this.delta = pDelta;
      }

      public static ItemDurabilityTrigger.TriggerInstance changedDurability(ItemPredicate pItem, MinMaxBounds.Ints pDurability) {
         return changedDurability(ContextAwarePredicate.ANY, pItem, pDurability);
      }

      public static ItemDurabilityTrigger.TriggerInstance changedDurability(ContextAwarePredicate pPlayer, ItemPredicate pItem, MinMaxBounds.Ints pDurability) {
         return new ItemDurabilityTrigger.TriggerInstance(pPlayer, pItem, pDurability, MinMaxBounds.Ints.ANY);
      }

      public boolean matches(ItemStack pItem, int pDurability) {
         if (!this.item.matches(pItem)) {
            return false;
         } else if (!this.durability.matches(pItem.getMaxDamage() - pDurability)) {
            return false;
         } else {
            return this.delta.matches(pItem.getDamageValue() - pDurability);
         }
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("durability", this.durability.serializeToJson());
         jsonobject.add("delta", this.delta.serializeToJson());
         return jsonobject;
      }
   }
}