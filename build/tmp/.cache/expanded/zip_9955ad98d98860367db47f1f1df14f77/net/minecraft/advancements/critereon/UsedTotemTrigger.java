package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger extends SimpleCriterionTrigger<UsedTotemTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("used_totem");

   public ResourceLocation getId() {
      return ID;
   }

   public UsedTotemTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new UsedTotemTrigger.TriggerInstance(pPredicate, itempredicate);
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pItem) {
      this.trigger(pPlayer, (p_74436_) -> {
         return p_74436_.matches(pItem);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate pPlayer, ItemPredicate pItem) {
         super(UsedTotemTrigger.ID, pPlayer);
         this.item = pItem;
      }

      public static UsedTotemTrigger.TriggerInstance usedTotem(ItemPredicate pItem) {
         return new UsedTotemTrigger.TriggerInstance(ContextAwarePredicate.ANY, pItem);
      }

      public static UsedTotemTrigger.TriggerInstance usedTotem(ItemLike pItem) {
         return new UsedTotemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(pItem).build());
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