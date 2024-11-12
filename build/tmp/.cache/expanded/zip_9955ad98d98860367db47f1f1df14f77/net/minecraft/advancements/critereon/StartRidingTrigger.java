package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class StartRidingTrigger extends SimpleCriterionTrigger<StartRidingTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("started_riding");

   public ResourceLocation getId() {
      return ID;
   }

   public StartRidingTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      return new StartRidingTrigger.TriggerInstance(pPredicate);
   }

   public void trigger(ServerPlayer pPlayer) {
      this.trigger(pPlayer, (p_160394_) -> {
         return true;
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      public TriggerInstance(ContextAwarePredicate pPlayer) {
         super(StartRidingTrigger.ID, pPlayer);
      }

      public static StartRidingTrigger.TriggerInstance playerStartsRiding(EntityPredicate.Builder pPlayer) {
         return new StartRidingTrigger.TriggerInstance(EntityPredicate.wrap(pPlayer.build()));
      }
   }
}