package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
   final ResourceLocation id;

   public PlayerTrigger(ResourceLocation pId) {
      this.id = pId;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public PlayerTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      return new PlayerTrigger.TriggerInstance(this.id, pPredicate);
   }

   public void trigger(ServerPlayer pPlayer) {
      this.trigger(pPlayer, (p_222625_) -> {
         return true;
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      public TriggerInstance(ResourceLocation pCriterion, ContextAwarePredicate pPlayer) {
         super(pCriterion, pPlayer);
      }

      public static PlayerTrigger.TriggerInstance located(LocationPredicate pLocation) {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.wrap(EntityPredicate.Builder.entity().located(pLocation).build()));
      }

      public static PlayerTrigger.TriggerInstance located(EntityPredicate pLocation) {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.wrap(pLocation));
      }

      public static PlayerTrigger.TriggerInstance sleptInBed() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, ContextAwarePredicate.ANY);
      }

      public static PlayerTrigger.TriggerInstance raidWon() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, ContextAwarePredicate.ANY);
      }

      public static PlayerTrigger.TriggerInstance avoidVibration() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.AVOID_VIBRATION.id, ContextAwarePredicate.ANY);
      }

      public static PlayerTrigger.TriggerInstance tick() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.id, ContextAwarePredicate.ANY);
      }

      public static PlayerTrigger.TriggerInstance walkOnBlockWithEquipment(Block pBlock, Item pEquipment) {
         return located(EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(pEquipment).build()).build()).steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(pBlock).build()).build()).build());
      }
   }
}