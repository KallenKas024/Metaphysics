package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");

   public ResourceLocation getId() {
      return ID;
   }

   public PlayerHurtEntityTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      DamagePredicate damagepredicate = DamagePredicate.fromJson(pJson.get("damage"));
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "entity", pDeserializationContext);
      return new PlayerHurtEntityTrigger.TriggerInstance(pPredicate, damagepredicate, contextawarepredicate);
   }

   public void trigger(ServerPlayer pPlayer, Entity pEntity, DamageSource pSource, float pAmountDealt, float pAmountTaken, boolean pBlocked) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
      this.trigger(pPlayer, (p_60126_) -> {
         return p_60126_.matches(pPlayer, lootcontext, pSource, pAmountDealt, pAmountTaken, pBlocked);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final DamagePredicate damage;
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate pPlayer, DamagePredicate pDamage, ContextAwarePredicate pEntity) {
         super(PlayerHurtEntityTrigger.ID, pPlayer);
         this.damage = pDamage;
         this.entity = pEntity;
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity() {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, DamagePredicate.ANY, ContextAwarePredicate.ANY);
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate pDamage) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, pDamage, ContextAwarePredicate.ANY);
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder pDamageBuilder) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, pDamageBuilder.build(), ContextAwarePredicate.ANY);
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(EntityPredicate pEntity) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, DamagePredicate.ANY, EntityPredicate.wrap(pEntity));
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate pDamage, EntityPredicate pEntity) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, pDamage, EntityPredicate.wrap(pEntity));
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder pDamageBuilder, EntityPredicate pEntity) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, pDamageBuilder.build(), EntityPredicate.wrap(pEntity));
      }

      public boolean matches(ServerPlayer pPlayer, LootContext pContext, DamageSource pDamage, float pDealt, float pTaken, boolean pBlocked) {
         if (!this.damage.matches(pPlayer, pDamage, pDealt, pTaken, pBlocked)) {
            return false;
         } else {
            return this.entity.matches(pContext);
         }
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("damage", this.damage.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(pConditions));
         return jsonobject;
      }
   }
}