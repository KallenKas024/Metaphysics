package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("effects_changed");

   public ResourceLocation getId() {
      return ID;
   }

   public EffectsChangedTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      MobEffectsPredicate mobeffectspredicate = MobEffectsPredicate.fromJson(pJson.get("effects"));
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "source", pDeserializationContext);
      return new EffectsChangedTrigger.TriggerInstance(pPredicate, mobeffectspredicate, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, @Nullable Entity pSource) {
      LootContext lootcontext = pSource != null ? EntityPredicate.createContext(pPlayer, pSource) : null;
      this.trigger(pPlayer, (p_149268_) -> {
         return p_149268_.matches(pPlayer, lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MobEffectsPredicate effects;
      private final ContextAwarePredicate source;

      public TriggerInstance(ContextAwarePredicate pPlayer, MobEffectsPredicate pEffects, ContextAwarePredicate pSource) {
         super(EffectsChangedTrigger.ID, pPlayer);
         this.effects = pEffects;
         this.source = pSource;
      }

      public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate pEffects) {
         return new EffectsChangedTrigger.TriggerInstance(ContextAwarePredicate.ANY, pEffects, ContextAwarePredicate.ANY);
      }

      public static EffectsChangedTrigger.TriggerInstance gotEffectsFrom(EntityPredicate pSource) {
         return new EffectsChangedTrigger.TriggerInstance(ContextAwarePredicate.ANY, MobEffectsPredicate.ANY, EntityPredicate.wrap(pSource));
      }

      public boolean matches(ServerPlayer pPlayer, @Nullable LootContext pLootContext) {
         if (!this.effects.matches(pPlayer)) {
            return false;
         } else {
            return this.source == ContextAwarePredicate.ANY || pLootContext != null && this.source.matches(pLootContext);
         }
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("effects", this.effects.serializeToJson());
         jsonobject.add("source", this.source.toJson(pConditions));
         return jsonobject;
      }
   }
}