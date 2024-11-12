package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * A LootItemCondition that checks the {@linkplain LootContextParams#TOOL tool} against an {@link ItemPredicate}.
 */
public class MatchTool implements LootItemCondition {
   final ItemPredicate predicate;

   public MatchTool(ItemPredicate pToolPredicate) {
      this.predicate = pToolPredicate;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.MATCH_TOOL;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.TOOL);
   }

   public boolean test(LootContext pContext) {
      ItemStack itemstack = pContext.getParamOrNull(LootContextParams.TOOL);
      return itemstack != null && this.predicate.matches(itemstack);
   }

   public static LootItemCondition.Builder toolMatches(ItemPredicate.Builder pToolPredicateBuilder) {
      return () -> {
         return new MatchTool(pToolPredicateBuilder.build());
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<MatchTool> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_82013_, MatchTool p_82014_, JsonSerializationContext p_82015_) {
         p_82013_.add("predicate", p_82014_.predicate.serializeToJson());
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public MatchTool deserialize(JsonObject p_82021_, JsonDeserializationContext p_82022_) {
         ItemPredicate itempredicate = ItemPredicate.fromJson(p_82021_.get("predicate"));
         return new MatchTool(itempredicate);
      }
   }
}