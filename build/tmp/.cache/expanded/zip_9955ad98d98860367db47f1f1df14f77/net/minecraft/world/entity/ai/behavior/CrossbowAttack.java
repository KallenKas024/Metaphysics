package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CrossbowAttack<E extends Mob & CrossbowAttackMob, T extends LivingEntity> extends Behavior<E> {
   private static final int TIMEOUT = 1200;
   private int attackDelay;
   private CrossbowAttack.CrossbowState crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;

   public CrossbowAttack() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200);
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
      LivingEntity livingentity = getAttackTarget(pOwner);
      return pOwner.isHolding(is -> is.getItem() instanceof CrossbowItem) && BehaviorUtils.canSee(pOwner, livingentity) && BehaviorUtils.isWithinAttackRange(pOwner, livingentity, 0);
   }

   protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
      return pEntity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions(pLevel, pEntity);
   }

   protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
      LivingEntity livingentity = getAttackTarget(pOwner);
      this.lookAtTarget(pOwner, livingentity);
      this.crossbowAttack(pOwner, livingentity);
   }

   protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
      if (pEntity.isUsingItem()) {
         pEntity.stopUsingItem();
      }

      if (pEntity.isHolding(is -> is.getItem() instanceof CrossbowItem)) {
         pEntity.setChargingCrossbow(false);
         CrossbowItem.setCharged(pEntity.getUseItem(), false);
      }

   }

   private void crossbowAttack(E pShooter, LivingEntity pTarget) {
      if (this.crossbowState == CrossbowAttack.CrossbowState.UNCHARGED) {
         pShooter.startUsingItem(ProjectileUtil.getWeaponHoldingHand(pShooter, item -> item instanceof CrossbowItem));
         this.crossbowState = CrossbowAttack.CrossbowState.CHARGING;
         pShooter.setChargingCrossbow(true);
      } else if (this.crossbowState == CrossbowAttack.CrossbowState.CHARGING) {
         if (!pShooter.isUsingItem()) {
            this.crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;
         }

         int i = pShooter.getTicksUsingItem();
         ItemStack itemstack = pShooter.getUseItem();
         if (i >= CrossbowItem.getChargeDuration(itemstack)) {
            pShooter.releaseUsingItem();
            this.crossbowState = CrossbowAttack.CrossbowState.CHARGED;
            this.attackDelay = 20 + pShooter.getRandom().nextInt(20);
            pShooter.setChargingCrossbow(false);
         }
      } else if (this.crossbowState == CrossbowAttack.CrossbowState.CHARGED) {
         --this.attackDelay;
         if (this.attackDelay == 0) {
            this.crossbowState = CrossbowAttack.CrossbowState.READY_TO_ATTACK;
         }
      } else if (this.crossbowState == CrossbowAttack.CrossbowState.READY_TO_ATTACK) {
         pShooter.performRangedAttack(pTarget, 1.0F);
         ItemStack itemstack1 = pShooter.getItemInHand(ProjectileUtil.getWeaponHoldingHand(pShooter, item -> item instanceof CrossbowItem));
         CrossbowItem.setCharged(itemstack1, false);
         this.crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;
      }

   }

   private void lookAtTarget(Mob pShooter, LivingEntity pTarget) {
      pShooter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(pTarget, true));
   }

   private static LivingEntity getAttackTarget(LivingEntity pShooter) {
      return pShooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
   }

   static enum CrossbowState {
      UNCHARGED,
      CHARGING,
      CHARGED,
      READY_TO_ATTACK;
   }
}
