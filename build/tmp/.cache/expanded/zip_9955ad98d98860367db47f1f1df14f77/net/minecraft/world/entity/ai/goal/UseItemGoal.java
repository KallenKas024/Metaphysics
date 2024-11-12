package net.minecraft.world.entity.ai.goal;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public class UseItemGoal<T extends Mob> extends Goal {
   private final T mob;
   private final ItemStack item;
   private final Predicate<? super T> canUseSelector;
   @Nullable
   private final SoundEvent finishUsingSound;

   public UseItemGoal(T pMob, ItemStack pItem, @Nullable SoundEvent pFinishUsingSound, Predicate<? super T> pCanUseSelector) {
      this.mob = pMob;
      this.item = pItem;
      this.finishUsingSound = pFinishUsingSound;
      this.canUseSelector = pCanUseSelector;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.canUseSelector.test(this.mob);
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.mob.isUsingItem();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.setItemSlot(EquipmentSlot.MAINHAND, this.item.copy());
      this.mob.startUsingItem(InteractionHand.MAIN_HAND);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      if (this.finishUsingSound != null) {
         this.mob.playSound(this.finishUsingSound, 1.0F, this.mob.getRandom().nextFloat() * 0.2F + 0.9F);
      }

   }
}