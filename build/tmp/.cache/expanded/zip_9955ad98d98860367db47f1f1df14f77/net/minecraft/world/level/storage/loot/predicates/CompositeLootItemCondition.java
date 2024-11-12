package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class CompositeLootItemCondition implements LootItemCondition {
   final LootItemCondition[] terms;
   private final Predicate<LootContext> composedPredicate;

   protected CompositeLootItemCondition(LootItemCondition[] pTerms, Predicate<LootContext> pComposedPredicate) {
      this.terms = pTerms;
      this.composedPredicate = pComposedPredicate;
   }

   public final boolean test(LootContext pContext) {
      return this.composedPredicate.test(pContext);
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationContext pContext) {
      LootItemCondition.super.validate(pContext);

      for(int i = 0; i < this.terms.length; ++i) {
         this.terms[i].validate(pContext.forChild(".term[" + i + "]"));
      }

   }

   public abstract static class Builder implements LootItemCondition.Builder {
      private final List<LootItemCondition> terms = new ArrayList<>();

      public Builder(LootItemCondition.Builder... pConditions) {
         for(LootItemCondition.Builder lootitemcondition$builder : pConditions) {
            this.terms.add(lootitemcondition$builder.build());
         }

      }

      public void addTerm(LootItemCondition.Builder pCondition) {
         this.terms.add(pCondition.build());
      }

      public LootItemCondition build() {
         LootItemCondition[] alootitemcondition = this.terms.toArray((p_286455_) -> {
            return new LootItemCondition[p_286455_];
         });
         return this.create(alootitemcondition);
      }

      protected abstract LootItemCondition create(LootItemCondition[] pConditions);
   }

   public abstract static class Serializer<T extends CompositeLootItemCondition> implements net.minecraft.world.level.storage.loot.Serializer<T> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, CompositeLootItemCondition pValue, JsonSerializationContext pSerializationContext) {
         pJson.add("terms", pSerializationContext.serialize(pValue.terms));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public T deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         LootItemCondition[] alootitemcondition = GsonHelper.getAsObject(pJson, "terms", pSerializationContext, LootItemCondition[].class);
         return this.create(alootitemcondition);
      }

      protected abstract T create(LootItemCondition[] pConditions);
   }
}