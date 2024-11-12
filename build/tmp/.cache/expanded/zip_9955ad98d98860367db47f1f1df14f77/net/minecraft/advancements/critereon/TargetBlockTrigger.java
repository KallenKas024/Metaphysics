package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("target_hit");

   public ResourceLocation getId() {
      return ID;
   }

   public TargetBlockTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("signal_strength"));
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "projectile", pDeserializationContext);
      return new TargetBlockTrigger.TriggerInstance(pPredicate, minmaxbounds$ints, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, Entity pProjectile, Vec3 pVector, int pSignalStrength) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pProjectile);
      this.trigger(pPlayer, (p_70224_) -> {
         return p_70224_.matches(lootcontext, pVector, pSignalStrength);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MinMaxBounds.Ints signalStrength;
      private final ContextAwarePredicate projectile;

      public TriggerInstance(ContextAwarePredicate pPlayer, MinMaxBounds.Ints pSignalStrength, ContextAwarePredicate pProjectile) {
         super(TargetBlockTrigger.ID, pPlayer);
         this.signalStrength = pSignalStrength;
         this.projectile = pProjectile;
      }

      public static TargetBlockTrigger.TriggerInstance targetHit(MinMaxBounds.Ints pSignalStrength, ContextAwarePredicate pProjectile) {
         return new TargetBlockTrigger.TriggerInstance(ContextAwarePredicate.ANY, pSignalStrength, pProjectile);
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("signal_strength", this.signalStrength.serializeToJson());
         jsonobject.add("projectile", this.projectile.toJson(pConditions));
         return jsonobject;
      }

      public boolean matches(LootContext pContext, Vec3 pVector, int pSignalStrength) {
         if (!this.signalStrength.matches(pSignalStrength)) {
            return false;
         } else {
            return this.projectile.matches(pContext);
         }
      }
   }
}