package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class ItemUsedOnLocationTrigger extends SimpleCriterionTrigger<ItemUsedOnLocationTrigger.TriggerInstance> {
   final ResourceLocation id;

   public ItemUsedOnLocationTrigger(ResourceLocation pId) {
      this.id = pId;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public ItemUsedOnLocationTrigger.TriggerInstance createInstance(JsonObject pJson, ContextAwarePredicate pPredicate, DeserializationContext pDeserializationContext) {
      ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.fromElement("location", pDeserializationContext, pJson.get("location"), LootContextParamSets.ADVANCEMENT_LOCATION);
      if (contextawarepredicate == null) {
         throw new JsonParseException("Failed to parse 'location' field");
      } else {
         return new ItemUsedOnLocationTrigger.TriggerInstance(this.id, pPredicate, contextawarepredicate);
      }
   }

   public void trigger(ServerPlayer pPlayer, BlockPos pPos, ItemStack pStack) {
      ServerLevel serverlevel = pPlayer.serverLevel();
      BlockState blockstate = serverlevel.getBlockState(pPos);
      LootParams lootparams = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, pPos.getCenter()).withParameter(LootContextParams.THIS_ENTITY, pPlayer).withParameter(LootContextParams.BLOCK_STATE, blockstate).withParameter(LootContextParams.TOOL, pStack).create(LootContextParamSets.ADVANCEMENT_LOCATION);
      LootContext lootcontext = (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
      this.trigger(pPlayer, (p_286596_) -> {
         return p_286596_.matches(lootcontext);
      });
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate location;

      public TriggerInstance(ResourceLocation pCriterion, ContextAwarePredicate pPlayer, ContextAwarePredicate pLocation) {
         super(pCriterion, pPlayer);
         this.location = pLocation;
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance placedBlock(Block pBlock) {
         ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pBlock).build());
         return new ItemUsedOnLocationTrigger.TriggerInstance(CriteriaTriggers.PLACED_BLOCK.id, ContextAwarePredicate.ANY, contextawarepredicate);
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance placedBlock(LootItemCondition.Builder... pConditions) {
         ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(Arrays.stream(pConditions).map(LootItemCondition.Builder::build).toArray((p_286827_) -> {
            return new LootItemCondition[p_286827_];
         }));
         return new ItemUsedOnLocationTrigger.TriggerInstance(CriteriaTriggers.PLACED_BLOCK.id, ContextAwarePredicate.ANY, contextawarepredicate);
      }

      private static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnLocation(LocationPredicate.Builder pLocationPredicate, ItemPredicate.Builder pItemPredicate, ResourceLocation pCriterion) {
         ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LocationCheck.checkLocation(pLocationPredicate).build(), MatchTool.toolMatches(pItemPredicate).build());
         return new ItemUsedOnLocationTrigger.TriggerInstance(pCriterion, ContextAwarePredicate.ANY, contextawarepredicate);
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnBlock(LocationPredicate.Builder pLocationPredicate, ItemPredicate.Builder pItemPredicate) {
         return itemUsedOnLocation(pLocationPredicate, pItemPredicate, CriteriaTriggers.ITEM_USED_ON_BLOCK.id);
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance allayDropItemOnBlock(LocationPredicate.Builder pLocationPredicate, ItemPredicate.Builder pItemPredicate) {
         return itemUsedOnLocation(pLocationPredicate, pItemPredicate, CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.id);
      }

      public boolean matches(LootContext pContext) {
         return this.location.matches(pContext);
      }

      public JsonObject serializeToJson(SerializationContext pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("location", this.location.toJson(pConditions));
         return jsonobject;
      }
   }
}