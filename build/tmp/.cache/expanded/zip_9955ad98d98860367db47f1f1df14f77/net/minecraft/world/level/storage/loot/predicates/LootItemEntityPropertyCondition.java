package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * A LootItemCondition that checks a given {@link EntityPredicate} against a given {@link LootContext.EntityTarget}.
 */
public class LootItemEntityPropertyCondition implements LootItemCondition {
   final EntityPredicate predicate;
   final LootContext.EntityTarget entityTarget;

   LootItemEntityPropertyCondition(EntityPredicate pEntityPredicate, LootContext.EntityTarget pEntityTarget) {
      this.predicate = pEntityPredicate;
      this.entityTarget = pEntityTarget;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.ENTITY_PROPERTIES;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.ORIGIN, this.entityTarget.getParam());
   }

   public boolean test(LootContext pContext) {
      Entity entity = pContext.getParamOrNull(this.entityTarget.getParam());
      Vec3 vec3 = pContext.getParamOrNull(LootContextParams.ORIGIN);
      return this.predicate.matches(pContext.getLevel(), vec3, entity);
   }

   public static LootItemCondition.Builder entityPresent(LootContext.EntityTarget pTarget) {
      return hasProperties(pTarget, EntityPredicate.Builder.entity());
   }

   public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget pTarget, EntityPredicate.Builder pPredicateBuilder) {
      return () -> {
         return new LootItemEntityPropertyCondition(pPredicateBuilder.build(), pTarget);
      };
   }

   public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget pTarget, EntityPredicate pEntityPredicate) {
      return () -> {
         return new LootItemEntityPropertyCondition(pEntityPredicate, pTarget);
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemEntityPropertyCondition> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_81884_, LootItemEntityPropertyCondition p_81885_, JsonSerializationContext p_81886_) {
         p_81884_.add("predicate", p_81885_.predicate.serializeToJson());
         p_81884_.add("entity", p_81886_.serialize(p_81885_.entityTarget));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public LootItemEntityPropertyCondition deserialize(JsonObject p_81892_, JsonDeserializationContext p_81893_) {
         EntityPredicate entitypredicate = EntityPredicate.fromJson(p_81892_.get("predicate"));
         return new LootItemEntityPropertyCondition(entitypredicate, GsonHelper.getAsObject(p_81892_, "entity", p_81893_, LootContext.EntityTarget.class));
      }
   }
}