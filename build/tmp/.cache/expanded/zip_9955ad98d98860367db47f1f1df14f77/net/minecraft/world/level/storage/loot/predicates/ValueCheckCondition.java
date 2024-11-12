package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * LootItemCondition that checks if a number provided by a {@link NumberProvider} is within an {@link IntRange}.
 */
public class ValueCheckCondition implements LootItemCondition {
   final NumberProvider provider;
   final IntRange range;

   ValueCheckCondition(NumberProvider pNumberProvider, IntRange pRange) {
      this.provider = pNumberProvider;
      this.range = pRange;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.VALUE_CHECK;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Sets.union(this.provider.getReferencedContextParams(), this.range.getReferencedContextParams());
   }

   public boolean test(LootContext pContext) {
      return this.range.test(pContext, this.provider.getInt(pContext));
   }

   public static LootItemCondition.Builder hasValue(NumberProvider pProvider, IntRange pRange) {
      return () -> {
         return new ValueCheckCondition(pProvider, pRange);
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ValueCheckCondition> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_165542_, ValueCheckCondition p_165543_, JsonSerializationContext p_165544_) {
         p_165542_.add("value", p_165544_.serialize(p_165543_.provider));
         p_165542_.add("range", p_165544_.serialize(p_165543_.range));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public ValueCheckCondition deserialize(JsonObject p_165550_, JsonDeserializationContext p_165551_) {
         NumberProvider numberprovider = GsonHelper.getAsObject(p_165550_, "value", p_165551_, NumberProvider.class);
         IntRange intrange = GsonHelper.getAsObject(p_165550_, "range", p_165551_, IntRange.class);
         return new ValueCheckCondition(numberprovider, intrange);
      }
   }
}