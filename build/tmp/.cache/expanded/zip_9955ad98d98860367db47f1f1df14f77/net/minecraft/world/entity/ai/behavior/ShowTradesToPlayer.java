package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public class ShowTradesToPlayer extends Behavior<Villager> {
   private static final int MAX_LOOK_TIME = 900;
   private static final int STARTING_LOOK_TIME = 40;
   @Nullable
   private ItemStack playerItemStack;
   private final List<ItemStack> displayItems = Lists.newArrayList();
   private int cycleCounter;
   private int displayIndex;
   private int lookTime;

   public ShowTradesToPlayer(int pMinDuration, int pMaxDuration) {
      super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT), pMinDuration, pMaxDuration);
   }

   public boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
      Brain<?> brain = pOwner.getBrain();
      if (!brain.getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent()) {
         return false;
      } else {
         LivingEntity livingentity = brain.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
         return livingentity.getType() == EntityType.PLAYER && pOwner.isAlive() && livingentity.isAlive() && !pOwner.isBaby() && pOwner.distanceToSqr(livingentity) <= 17.0D;
      }
   }

   public boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      return this.checkExtraStartConditions(pLevel, pEntity) && this.lookTime > 0 && pEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
   }

   public void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      super.start(pLevel, pEntity, pGameTime);
      this.lookAtTarget(pEntity);
      this.cycleCounter = 0;
      this.displayIndex = 0;
      this.lookTime = 40;
   }

   public void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
      LivingEntity livingentity = this.lookAtTarget(pOwner);
      this.findItemsToDisplay(livingentity, pOwner);
      if (!this.displayItems.isEmpty()) {
         this.displayCyclingItems(pOwner);
      } else {
         clearHeldItem(pOwner);
         this.lookTime = Math.min(this.lookTime, 40);
      }

      --this.lookTime;
   }

   public void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      super.stop(pLevel, pEntity, pGameTime);
      pEntity.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
      clearHeldItem(pEntity);
      this.playerItemStack = null;
   }

   private void findItemsToDisplay(LivingEntity pEntity, Villager pVillager) {
      boolean flag = false;
      ItemStack itemstack = pEntity.getMainHandItem();
      if (this.playerItemStack == null || !ItemStack.isSameItem(this.playerItemStack, itemstack)) {
         this.playerItemStack = itemstack;
         flag = true;
         this.displayItems.clear();
      }

      if (flag && !this.playerItemStack.isEmpty()) {
         this.updateDisplayItems(pVillager);
         if (!this.displayItems.isEmpty()) {
            this.lookTime = 900;
            this.displayFirstItem(pVillager);
         }
      }

   }

   private void displayFirstItem(Villager pVillager) {
      displayAsHeldItem(pVillager, this.displayItems.get(0));
   }

   private void updateDisplayItems(Villager pVillager) {
      for(MerchantOffer merchantoffer : pVillager.getOffers()) {
         if (!merchantoffer.isOutOfStock() && this.playerItemStackMatchesCostOfOffer(merchantoffer)) {
            this.displayItems.add(merchantoffer.getResult());
         }
      }

   }

   private boolean playerItemStackMatchesCostOfOffer(MerchantOffer pOffer) {
      return ItemStack.isSameItem(this.playerItemStack, pOffer.getCostA()) || ItemStack.isSameItem(this.playerItemStack, pOffer.getCostB());
   }

   private static void clearHeldItem(Villager pVillager) {
      pVillager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      pVillager.setDropChance(EquipmentSlot.MAINHAND, 0.085F);
   }

   private static void displayAsHeldItem(Villager pVillager, ItemStack pItem) {
      pVillager.setItemSlot(EquipmentSlot.MAINHAND, pItem);
      pVillager.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
   }

   private LivingEntity lookAtTarget(Villager pVillager) {
      Brain<?> brain = pVillager.getBrain();
      LivingEntity livingentity = brain.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingentity, true));
      return livingentity;
   }

   private void displayCyclingItems(Villager pVillager) {
      if (this.displayItems.size() >= 2 && ++this.cycleCounter >= 40) {
         ++this.displayIndex;
         this.cycleCounter = 0;
         if (this.displayIndex > this.displayItems.size() - 1) {
            this.displayIndex = 0;
         }

         displayAsHeldItem(pVillager, this.displayItems.get(this.displayIndex));
      }

   }
}