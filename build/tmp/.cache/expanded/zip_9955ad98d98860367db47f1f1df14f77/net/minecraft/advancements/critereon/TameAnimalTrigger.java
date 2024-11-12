package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("tame_animal");

   public ResourceLocation getId() {
      return ID;
   }

   public TameAnimalTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "entity", pDeserializationContext);
      return new TameAnimalTrigger.TriggerInstance(pPredicate, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, Animal pEntity) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_68838_) -> {
         return p_68838_.matches(lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate pPlayer, ContextAwarePredicate pEntity) {
         super(TameAnimalTrigger.ID, pPlayer);
         this.entity = pEntity;
      }

      public static TameAnimalTrigger.TriggerInstance tamedAnimal() {
         return new TameAnimalTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
      }

      public static TameAnimalTrigger.TriggerInstance tamedAnimal(EntityPredicate pEntityPredicate) {
         return new TameAnimalTrigger.TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(pEntityPredicate));
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