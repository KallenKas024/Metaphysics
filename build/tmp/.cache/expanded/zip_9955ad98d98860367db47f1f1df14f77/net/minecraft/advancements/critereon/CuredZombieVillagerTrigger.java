package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

   public ResourceLocation getId() {
      return ID;
   }

   public CuredZombieVillagerTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(pJson, "zombie", pDeserializationContext);
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(pJson, "villager", pDeserializationContext);
      return new CuredZombieVillagerTrigger.TriggerInstance(pPredicate, contextawarepredicate, contextawarepredicate1);
   }

   public void trigger(ServerPlayer pPlayer, Zombie pZombie, Villager pVillager) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pZombie);
      LootContext lootcontext1 = EntityPredicate.createContext(pPlayer, pVillager);
      this.trigger(pPlayer, (p_24285_) -> {
         return p_24285_.matches(lootcontext, lootcontext1);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate zombie;
      private final ContextAwarePredicate villager;

      public TriggerInstance(ContextAwarePredicate pPlayer, ContextAwarePredicate pZombie, ContextAwarePredicate pVillager) {
         super(CuredZombieVillagerTrigger.ID, pPlayer);
         this.zombie = pZombie;
         this.villager = pVillager;
      }

      public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
         return new CuredZombieVillagerTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
      }

      public boolean matches(LootContext pZombie, LootContext pVillager) {
         if (!this.zombie.matches(pZombie)) {
            return false;
         } else {
            return this.villager.matches(pVillager);
         }
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("zombie", this.zombie.toJson(pConditions));
         jsonobject.add("villager", this.villager.toJson(pConditions));
         return jsonobject;
      }
   }
}