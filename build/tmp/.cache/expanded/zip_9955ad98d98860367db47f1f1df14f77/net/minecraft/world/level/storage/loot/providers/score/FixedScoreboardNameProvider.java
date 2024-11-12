package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * A {@link ScoreboardNameProvider} that always provides a fixed name.
 */
public class FixedScoreboardNameProvider implements ScoreboardNameProvider {
   final String name;

   FixedScoreboardNameProvider(String pName) {
      this.name = pName;
   }

   public static ScoreboardNameProvider forName(String pName) {
      return new FixedScoreboardNameProvider(pName);
   }

   public LootScoreProviderType getType() {
      return ScoreboardNameProviders.FIXED;
   }

   public String getName() {
      return this.name;
   }

   /**
    * Get the scoreboard name based on the given loot context.
    */
   @Nullable
   public String getScoreboardName(LootContext pLootContext) {
      return this.name;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of();
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<FixedScoreboardNameProvider> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_165855_, FixedScoreboardNameProvider p_165856_, JsonSerializationContext p_165857_) {
         p_165855_.addProperty("name", p_165856_.name);
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public FixedScoreboardNameProvider deserialize(JsonObject p_165863_, JsonDeserializationContext p_165864_) {
         String s = GsonHelper.getAsString(p_165863_, "name");
         return new FixedScoreboardNameProvider(s);
      }
   }
}