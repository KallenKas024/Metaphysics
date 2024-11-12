package net.minecraft.world.level.storage.loot.providers.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

/**
 * Provides a number by reading the score of a scoreboard member whose name is provided by a {@link
 * ScoreboardNameProvider}.
 * Additionally a scale can be provided, which will be multiplied with the score.
 */
public class ScoreboardValue implements NumberProvider {
   final ScoreboardNameProvider target;
   final String score;
   final float scale;

   ScoreboardValue(ScoreboardNameProvider pTarget, String pScore, float pScale) {
      this.target = pTarget;
      this.score = pScore;
      this.scale = pScale;
   }

   public LootNumberProviderType getType() {
      return NumberProviders.SCORE;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.target.getReferencedContextParams();
   }

   public static ScoreboardValue fromScoreboard(LootContext.EntityTarget pEntityTarget, String pScore) {
      return fromScoreboard(pEntityTarget, pScore, 1.0F);
   }

   public static ScoreboardValue fromScoreboard(LootContext.EntityTarget pEntityTarget, String pScore, float pScale) {
      return new ScoreboardValue(ContextScoreboardNameProvider.forTarget(pEntityTarget), pScore, pScale);
   }

   public float getFloat(LootContext pLootContext) {
      String s = this.target.getScoreboardName(pLootContext);
      if (s == null) {
         return 0.0F;
      } else {
         Scoreboard scoreboard = pLootContext.getLevel().getScoreboard();
         Objective objective = scoreboard.getObjective(this.score);
         if (objective == null) {
            return 0.0F;
         } else {
            return !scoreboard.hasPlayerScore(s, objective) ? 0.0F : (float)scoreboard.getOrCreatePlayerScore(s, objective).getScore() * this.scale;
         }
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ScoreboardValue> {
      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public ScoreboardValue deserialize(JsonObject p_165772_, JsonDeserializationContext p_165773_) {
         String s = GsonHelper.getAsString(p_165772_, "score");
         float f = GsonHelper.getAsFloat(p_165772_, "scale", 1.0F);
         ScoreboardNameProvider scoreboardnameprovider = GsonHelper.getAsObject(p_165772_, "target", p_165773_, ScoreboardNameProvider.class);
         return new ScoreboardValue(scoreboardnameprovider, s, f);
      }

      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_165764_, ScoreboardValue p_165765_, JsonSerializationContext p_165766_) {
         p_165764_.addProperty("score", p_165765_.score);
         p_165764_.add("target", p_165766_.serialize(p_165765_.target));
         p_165764_.addProperty("scale", p_165765_.scale);
      }
   }
}