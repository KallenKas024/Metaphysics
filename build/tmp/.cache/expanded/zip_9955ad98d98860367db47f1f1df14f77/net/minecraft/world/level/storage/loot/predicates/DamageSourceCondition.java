package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * A LootItemCondition which checks {@link LootContextParams#ORIGIN} and {@link LootContextParams#DAMAGE_SOURCE} against
 * a {@link DamageSourcePredicate}.
 */
public class DamageSourceCondition implements LootItemCondition {
   final DamageSourcePredicate predicate;

   DamageSourceCondition(DamageSourcePredicate pDamageSourcePredicate) {
      this.predicate = pDamageSourcePredicate;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.DAMAGE_SOURCE_PROPERTIES;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.ORIGIN, LootContextParams.DAMAGE_SOURCE);
   }

   public boolean test(LootContext pContext) {
      DamageSource damagesource = pContext.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
      Vec3 vec3 = pContext.getParamOrNull(LootContextParams.ORIGIN);
      return vec3 != null && damagesource != null && this.predicate.matches(pContext.getLevel(), vec3, damagesource);
   }

   public static LootItemCondition.Builder hasDamageSource(DamageSourcePredicate.Builder pBuilder) {
      return () -> {
         return new DamageSourceCondition(pBuilder.build());
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<DamageSourceCondition> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_81605_, DamageSourceCondition p_81606_, JsonSerializationContext p_81607_) {
         p_81605_.add("predicate", p_81606_.predicate.serializeToJson());
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public DamageSourceCondition deserialize(JsonObject p_81613_, JsonDeserializationContext p_81614_) {
         DamageSourcePredicate damagesourcepredicate = DamageSourcePredicate.fromJson(p_81613_.get("predicate"));
         return new DamageSourceCondition(damagesourcepredicate);
      }
   }
}