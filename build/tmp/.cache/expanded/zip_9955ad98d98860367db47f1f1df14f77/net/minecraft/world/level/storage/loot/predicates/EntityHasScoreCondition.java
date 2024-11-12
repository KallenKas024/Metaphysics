package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

/**
 * A LootItemCondition that checks if an Entity selected by a {@link LootContext.EntityTarget} has a given set of
 * scores.
 * If one of the given objectives does not exist or the entity does not have a score for that objective, the condition
 * fails.
 */
public class EntityHasScoreCondition implements LootItemCondition {
   final Map<String, IntRange> scores;
   final LootContext.EntityTarget entityTarget;

   EntityHasScoreCondition(Map<String, IntRange> pScoreRanges, LootContext.EntityTarget pEntityTarget) {
      this.scores = ImmutableMap.copyOf(pScoreRanges);
      this.entityTarget = pEntityTarget;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.ENTITY_SCORES;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return Stream.concat(Stream.of(this.entityTarget.getParam()), this.scores.values().stream().flatMap((p_165487_) -> {
         return p_165487_.getReferencedContextParams().stream();
      })).collect(ImmutableSet.toImmutableSet());
   }

   public boolean test(LootContext pContext) {
      Entity entity = pContext.getParamOrNull(this.entityTarget.getParam());
      if (entity == null) {
         return false;
      } else {
         Scoreboard scoreboard = entity.level().getScoreboard();

         for(Map.Entry<String, IntRange> entry : this.scores.entrySet()) {
            if (!this.hasScore(pContext, entity, scoreboard, entry.getKey(), entry.getValue())) {
               return false;
            }
         }

         return true;
      }
   }

   protected boolean hasScore(LootContext pLootContext, Entity pTargetEntity, Scoreboard pScoreboard, String pObjectiveName, IntRange pScoreRange) {
      Objective objective = pScoreboard.getObjective(pObjectiveName);
      if (objective == null) {
         return false;
      } else {
         String s = pTargetEntity.getScoreboardName();
         return !pScoreboard.hasPlayerScore(s, objective) ? false : pScoreRange.test(pLootContext, pScoreboard.getOrCreatePlayerScore(s, objective).getScore());
      }
   }

   public static EntityHasScoreCondition.Builder hasScores(LootContext.EntityTarget pEntityTarget) {
      return new EntityHasScoreCondition.Builder(pEntityTarget);
   }

   public static class Builder implements LootItemCondition.Builder {
      private final Map<String, IntRange> scores = Maps.newHashMap();
      private final LootContext.EntityTarget entityTarget;

      public Builder(LootContext.EntityTarget pEntityTarget) {
         this.entityTarget = pEntityTarget;
      }

      /**
       * Add a check that the score for the given {@code objectiveName} is within {@code scoreRange}.
       */
      public EntityHasScoreCondition.Builder withScore(String pObjectiveName, IntRange pScoreRange) {
         this.scores.put(pObjectiveName, pScoreRange);
         return this;
      }

      public LootItemCondition build() {
         return new EntityHasScoreCondition(this.scores, this.entityTarget);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityHasScoreCondition> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_81644_, EntityHasScoreCondition p_81645_, JsonSerializationContext p_81646_) {
         JsonObject jsonobject = new JsonObject();

         for(Map.Entry<String, IntRange> entry : p_81645_.scores.entrySet()) {
            jsonobject.add(entry.getKey(), p_81646_.serialize(entry.getValue()));
         }

         p_81644_.add("scores", jsonobject);
         p_81644_.add("entity", p_81646_.serialize(p_81645_.entityTarget));
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public EntityHasScoreCondition deserialize(JsonObject p_81652_, JsonDeserializationContext p_81653_) {
         Set<Map.Entry<String, JsonElement>> set = GsonHelper.getAsJsonObject(p_81652_, "scores").entrySet();
         Map<String, IntRange> map = Maps.newLinkedHashMap();

         for(Map.Entry<String, JsonElement> entry : set) {
            map.put(entry.getKey(), GsonHelper.convertToObject(entry.getValue(), "score", p_81653_, IntRange.class));
         }

         return new EntityHasScoreCondition(map, GsonHelper.getAsObject(p_81652_, "entity", p_81653_, LootContext.EntityTarget.class));
      }
   }
}