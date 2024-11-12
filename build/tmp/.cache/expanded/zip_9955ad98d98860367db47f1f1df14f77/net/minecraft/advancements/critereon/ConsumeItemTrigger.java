package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.ItemLike;

public class ConsumeItemTrigger extends SimpleCriterionTrigger<ConsumeItemTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("consume_item");

   public ResourceLocation getId() {
      return ID;
   }

   public ConsumeItemTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      return new ConsumeItemTrigger.TriggerInstance(pPredicate, ItemPredicate.fromJson(pJson.get("item")));
   }

   public void trigger(ServerPlayer pPlayer, ItemStack pItem) {
      this.trigger(pPlayer, (p_23687_) -> {
         return p_23687_.matches(pItem);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate pPlayer, ItemPredicate pItem) {
         super(ConsumeItemTrigger.ID, pPlayer);
         this.item = pItem;
      }

      public static ConsumeItemTrigger.TriggerInstance usedItem() {
         return new ConsumeItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY);
      }

      public static ConsumeItemTrigger.TriggerInstance usedItem(ItemPredicate pItem) {
         return new ConsumeItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, pItem);
      }

      public static ConsumeItemTrigger.TriggerInstance usedItem(ItemLike pItem) {
         return new ConsumeItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, new ItemPredicate((TagKey<Item>)null, ImmutableSet.of(pItem.asItem()), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, (Potion)null, NbtPredicate.ANY));
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