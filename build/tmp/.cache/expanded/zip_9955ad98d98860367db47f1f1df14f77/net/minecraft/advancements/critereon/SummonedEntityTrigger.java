package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class SummonedEntityTrigger extends SimpleCriterionTrigger<SummonedEntityTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("summoned_entity");

   public ResourceLocation getId() {
      return ID;
   }

   public SummonedEntityTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "entity", pDeserializationContext);
      return new SummonedEntityTrigger.TriggerInstance(pPredicate, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, Entity pEntity) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_68265_) -> {
         return p_68265_.matches(lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate pPlayer, ContextAwarePredicate pEntity) {
         super(SummonedEntityTrigger.ID, pPlayer);
         this.entity = pEntity;
      }

      public static SummonedEntityTrigger.TriggerInstance summonedEntity(EntityPredicate.Builder pEntityPredicateBuilder) {
         return new SummonedEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(pEntityPredicateBuilder.build()));
      }

      public boolean matches(LootContext pLootContext) {
         return this.entity.matches(pLootContext);
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("entity", this.entity.toJson(pConditions));
         return jsonobject;
      }
   }
}