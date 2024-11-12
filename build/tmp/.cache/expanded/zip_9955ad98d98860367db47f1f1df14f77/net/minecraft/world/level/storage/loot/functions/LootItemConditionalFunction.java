package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A LootItemFunction that only modifies the stacks if a list of {@linkplain LootItemCondition predicates} passes.
 */
public abstract class LootItemConditionalFunction implements LootItemFunction {
   protected final LootItemCondition[] predicates;
   private final Predicate<LootContext> compositePredicates;

   protected LootItemConditionalFunction(LootItemCondition[] pPredicates) {
      this.predicates = pPredicates;
      this.compositePredicates = LootItemConditions.andConditions(pPredicates);
   }

   public final ItemStack apply(ItemStack pStack, LootContext pContext) {
      return this.compositePredicates.test(pContext) ? this.run(pStack, pContext) : pStack;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   protected abstract ItemStack run(ItemStack pStack, LootContext pContext);

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationContext pContext) {
      LootItemFunction.super.validate(pContext);

      for(int i = 0; i < this.predicates.length; ++i) {
         this.predicates[i].validate(pContext.forChild(".conditions[" + i + "]"));
      }

   }

   protected static LootItemConditionalFunction.Builder<?> simpleBuilder(Function<LootItemCondition[], LootItemFunction> pConstructor) {
      return new LootItemConditionalFunction.DummyBuilder(pConstructor);
   }

   public abstract static class Builder<T extends LootItemConditionalFunction.Builder<T>> implements LootItemFunction.Builder, ConditionUserBuilder<T> {
      private final List<LootItemCondition> conditions = Lists.newArrayList();

      public T when(LootItemCondition.Builder p_80694_) {
         this.conditions.add(p_80694_.build());
         return this.getThis();
      }

      public final T unwrap() {
         return this.getThis();
      }

      protected abstract T getThis();

      protected LootItemCondition[] getConditions() {
         return this.conditions.toArray(new LootItemCondition[0]);
      }
   }

   static final class DummyBuilder extends LootItemConditionalFunction.Builder<LootItemConditionalFunction.DummyBuilder> {
      private final Function<LootItemCondition[], LootItemFunction> constructor;

      public DummyBuilder(Function<LootItemCondition[], LootItemFunction> pConstructor) {
         this.constructor = pConstructor;
      }

      protected LootItemConditionalFunction.DummyBuilder getThis() {
         return this;
      }

      public LootItemFunction build() {
         return this.constructor.apply(this.getConditions());
      }
   }

   public abstract static class Serializer<T extends LootItemConditionalFunction> implements net.minecraft.world.level.storage.loot.Serializer<T> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, T pLootItemConditionalFunction, JsonSerializationContext pSerializationContext) {
         if (!ArrayUtils.isEmpty((Object[])pLootItemConditionalFunction.predicates)) {
            pJson.add("conditions", pSerializationContext.serialize(pLootItemConditionalFunction.predicates));
         }

      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public final T deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         LootItemCondition[] alootitemcondition = GsonHelper.getAsObject(pJson, "conditions", new LootItemCondition[0], pSerializationContext, LootItemCondition[].class);
         return this.deserialize(pJson, pSerializationContext, alootitemcondition);
      }

      public abstract T deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions);
   }
}