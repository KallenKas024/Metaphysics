package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByCrossbowTrigger extends SimpleCriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("killed_by_crossbow");

   public ResourceLocation getId() {
      return ID;
   }

   public KilledByCrossbowTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ContextAwarePredicate[] acontextawarepredicate = EntityPredicate.fromJsonArray(pJson, "victims", pDeserializationContext);
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("unique_entity_types"));
      return new KilledByCrossbowTrigger.TriggerInstance(pPredicate, acontextawarepredicate, minmaxbounds$ints);
   }

   public void trigger(ServerPlayer pPlayer, Collection<Entity> pEntities) {
      List<LootContext> list = Lists.newArrayList();
      Set<EntityType<?>> set = Sets.newHashSet();

      for(Entity entity : pEntities) {
         set.add(entity.getType());
         list.add(EntityPredicate.createContext(pPlayer, entity));
      }

      this.trigger(pPlayer, (p_46881_) -> {
         return p_46881_.matches(list, set.size());
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate[] victims;
      private final MinMaxBounds.Ints uniqueEntityTypes;

      public TriggerInstance(ContextAwarePredicate pPlayer, ContextAwarePredicate[] pVictims, MinMaxBounds.Ints pUniqueEntityTypes) {
         super(KilledByCrossbowTrigger.ID, pPlayer);
         this.victims = pVictims;
         this.uniqueEntityTypes = pUniqueEntityTypes;
      }

      public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(EntityPredicate.Builder... pBuilders) {
         ContextAwarePredicate[] acontextawarepredicate = new ContextAwarePredicate[pBuilders.length];

         for(int i = 0; i < pBuilders.length; ++i) {
            EntityPredicate.Builder entitypredicate$builder = pBuilders[i];
            acontextawarepredicate[i] = EntityPredicate.wrap(entitypredicate$builder.build());
         }

         return new KilledByCrossbowTrigger.TriggerInstance(ContextAwarePredicate.ANY, acontextawarepredicate, MinMaxBounds.Ints.ANY);
      }

      public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(MinMaxBounds.Ints pBounds) {
         ContextAwarePredicate[] acontextawarepredicate = new ContextAwarePredicate[0];
         return new KilledByCrossbowTrigger.TriggerInstance(ContextAwarePredicate.ANY, acontextawarepredicate, pBounds);
      }

      public boolean matches(Collection<LootContext> pContexts, int pBounds) {
         if (this.victims.length > 0) {
            List<LootContext> list = Lists.newArrayList(pContexts);

            for(ContextAwarePredicate contextawarepredicate : this.victims) {
               boolean flag = false;
               Iterator<LootContext> iterator = list.iterator();

               while(iterator.hasNext()) {
                  LootContext lootcontext = iterator.next();
                  if (contextawarepredicate.matches(lootcontext)) {
                     iterator.remove();
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }
         }

         return this.uniqueEntityTypes.matches(pBounds);
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("victims", ContextAwarePredicate.toJson(this.victims, pConditions));
         jsonobject.add("unique_entity_types", this.uniqueEntityTypes.serializeToJson());
         return jsonobject;
      }
   }
}