package net.minecraft.world.level.storage.loot.providers.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/**
 * A {@link ScoreboardNameProvider} that provides the scoreboard name for an entity selected by a {@link
 * LootContext.EntityTarget}.
 */
public class ContextScoreboardNameProvider implements ScoreboardNameProvider {
   final LootContext.EntityTarget target;

   ContextScoreboardNameProvider(LootContext.EntityTarget pTarget) {
      this.target = pTarget;
   }

   public static ScoreboardNameProvider forTarget(LootContext.EntityTarget pTarget) {
      return new ContextScoreboardNameProvider(pTarget);
   }

   public LootScoreProviderType getType() {
      return ScoreboardNameProviders.CONTEXT;
   }

   /**
    * Get the scoreboard name based on the given loot context.
    */
   @Nullable
   public String getScoreboardName(LootContext pLootContext) {
      Entity entity = pLootContext.getParamOrNull(this.target.getParam());
      return entity != null ? entity.getScoreboardName() : null;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(this.target.getParam());
   }

   public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ContextScoreboardNameProvider> {
      public JsonElement serialize(ContextScoreboardNameProvider p_165817_, JsonSerializationContext p_165818_) {
         return p_165818_.serialize(p_165817_.target);
      }

      public ContextScoreboardNameProvider deserialize(JsonElement p_165823_, JsonDeserializationContext p_165824_) {
         LootContext.EntityTarget lootcontext$entitytarget = p_165824_.deserialize(p_165823_, LootContext.EntityTarget.class);
         return new ContextScoreboardNameProvider(lootcontext$entitytarget);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ContextScoreboardNameProvider> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_165830_, ContextScoreboardNameProvider p_165831_, JsonSerializationContext p_165832_) {
         p_165830_.addProperty("target", p_165831_.target.name());
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public ContextScoreboardNameProvider deserialize(JsonObject p_165838_, JsonDeserializationContext p_165839_) {
         LootContext.EntityTarget lootcontext$entitytarget = GsonHelper.getAsObject(p_165838_, "target", p_165839_, LootContext.EntityTarget.class);
         return new ContextScoreboardNameProvider(lootcontext$entitytarget);
      }
   }
}