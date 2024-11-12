package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TradeWithVillager extends Behavior<Villager> {
   private static final int INTERACT_DIST_SQR = 5;
   private static final float SPEED_MODIFIER = 0.5F;
   private Set<Item> trades = ImmutableSet.of();

   public TradeWithVillager() {
      super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
      return BehaviorUtils.targetIsValid(pOwner.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
   }

   protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      return this.checkExtraStartConditions(pLevel, pEntity);
   }

   protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      Villager villager = (Villager)pEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      BehaviorUtils.lockGazeAndWalkToEachOther(pEntity, villager, 0.5F);
      this.trades = figureOutWhatIAmWillingToTrade(pEntity, villager);
   }

   protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
      Villager villager = (Villager)pOwner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      if (!(pOwner.distanceToSqr(villager) > 5.0D)) {
         BehaviorUtils.lockGazeAndWalkToEachOther(pOwner, villager, 0.5F);
         pOwner.gossip(pLevel, villager, pGameTime);
         if (pOwner.hasExcessFood() && (pOwner.getVillagerData().getProfession() == VillagerProfession.FARMER || villager.wantsMoreFood())) {
            throwHalfStack(pOwner, Villager.FOOD_POINTS.keySet(), villager);
         }

         if (villager.getVillagerData().getProfession() == VillagerProfession.FARMER && pOwner.getInventory().countItem(Items.WHEAT) > Items.WHEAT.getMaxStackSize() / 2) {
            throwHalfStack(pOwner, ImmutableSet.of(Items.WHEAT), villager);
         }

         if (!this.trades.isEmpty() && pOwner.getInventory().hasAnyOf(this.trades)) {
            throwHalfStack(pOwner, this.trades, villager);
         }

      }
   }

   protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
   }

   private static Set<Item> figureOutWhatIAmWillingToTrade(Villager pVillager, Villager pOther) {
      ImmutableSet<Item> immutableset = pOther.getVillagerData().getProfession().requestedItems();
      ImmutableSet<Item> immutableset1 = pVillager.getVillagerData().getProfession().requestedItems();
      return immutableset.stream().filter((p_24431_) -> {
         return !immutableset1.contains(p_24431_);
      }).collect(Collectors.toSet());
   }

   private static void throwHalfStack(Villager pVillager, Set<Item> pStack, LivingEntity pEntity) {
      SimpleContainer simplecontainer = pVillager.getInventory();
      ItemStack itemstack = ItemStack.EMPTY;
      int i = 0;

      while(i < simplecontainer.getContainerSize()) {
         ItemStack itemstack1;
         Item item;
         int j;
         label28: {
            itemstack1 = simplecontainer.getItem(i);
            if (!itemstack1.isEmpty()) {
               item = itemstack1.getItem();
               if (pStack.contains(item)) {
                  if (itemstack1.getCount() > itemstack1.getMaxStackSize() / 2) {
                     j = itemstack1.getCount() / 2;
                     break label28;
                  }

                  if (itemstack1.getCount() > 24) {
                     j = itemstack1.getCount() - 24;
                     break label28;
                  }
               }
            }

            ++i;
            continue;
         }

         itemstack1.shrink(j);
         itemstack = new ItemStack(item, j);
         break;
      }

      if (!itemstack.isEmpty()) {
         BehaviorUtils.throwItem(pVillager, itemstack, pEntity.position());
      }

   }
}