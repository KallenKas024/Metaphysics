package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {
   public static final ContextAwarePredicate ANY = new ContextAwarePredicate(new LootItemCondition[0]);
   private final LootItemCondition[] conditions;
   private final Predicate<LootContext> compositePredicates;

   ContextAwarePredicate(LootItemCondition[] pConditons) {
      this.conditions = pConditons;
      this.compositePredicates = LootItemConditions.andConditions(pConditons);
   }

   public static ContextAwarePredicate create(LootItemCondition... pConditions) {
      return new ContextAwarePredicate(pConditions);
   }

   @Nullable
   public static ContextAwarePredicate fromElement(String pId, DeserializationContext pContext, @Nullable JsonElement pElement, LootContextParamSet pParamSet) {
      if (pElement != null && pElement.isJsonArray()) {
         LootItemCondition[] alootitemcondition = pContext.deserializeConditions(pElement.getAsJsonArray(), pContext.getAdvancementId() + "/" + pId, pParamSet);
         return new ContextAwarePredicate(alootitemcondition);
      } else {
         return null;
      }
   }

   public boolean matches(LootContext pContext) {
      return this.compositePredicates.test(pContext);
   }

   public JsonElement toJson(SerializationContext pContext) {
      return (JsonElement)(this.conditions.length == 0 ? JsonNull.INSTANCE : pContext.serializeConditions(this.conditions));
   }

   public static JsonElement toJson(ContextAwarePredicate[] pPredicates, SerializationContext pContext) {
      if (pPredicates.length == 0) {
         return JsonNull.INSTANCE;
      } else {
         JsonArray jsonarray = new JsonArray();

         for(ContextAwarePredicate contextawarepredicate : pPredicates) {
            jsonarray.add(contextawarepredicate.toJson(pContext));
         }

         return jsonarray;
      }
   }
}