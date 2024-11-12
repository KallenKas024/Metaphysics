package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("changed_dimension");

   public ResourceLocation getId() {
      return ID;
   }

   public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ResourceKey<Level> resourcekey = pJson.has("from") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(pJson, "from"))) : null;
      ResourceKey<Level> resourcekey1 = pJson.has("to") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(pJson, "to"))) : null;
      return new ChangeDimensionTrigger.TriggerInstance(pPredicate, resourcekey, resourcekey1);
   }

   public void trigger(ServerPlayer pPlayer, ResourceKey<Level> pFromLevel, ResourceKey<Level> pToLevel) {
      this.trigger(pPlayer, (p_19768_) -> {
         return p_19768_.matches(pFromLevel, pToLevel);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      @Nullable
      private final ResourceKey<Level> from;
      @Nullable
      private final ResourceKey<Level> to;

      public TriggerInstance(ContextAwarePredicate pPlayer, @Nullable ResourceKey<Level> pFrom, @Nullable ResourceKey<Level> pTo) {
         super(ChangeDimensionTrigger.ID, pPlayer);
         this.from = pFrom;
         this.to = pTo;
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimension() {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.ANY, (ResourceKey<Level>)null, (ResourceKey<Level>)null);
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimension(ResourceKey<Level> pFrom, ResourceKey<Level> pTo) {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.ANY, pFrom, pTo);
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(ResourceKey<Level> pTo) {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.ANY, (ResourceKey<Level>)null, pTo);
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimensionFrom(ResourceKey<Level> pFrom) {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.ANY, pFrom, (ResourceKey<Level>)null);
      }

      public boolean matches(ResourceKey<Level> pFromLevel, ResourceKey<Level> pToLevel) {
         if (this.from != null && this.from != pFromLevel) {
            return false;
         } else {
            return this.to == null || this.to == pToLevel;
         }
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         if (this.from != null) {
            jsonobject.addProperty("from", this.from.location().toString());
         }

         if (this.to != null) {
            jsonobject.addProperty("to", this.to.location().toString());
         }

         return jsonobject;
      }
   }
}