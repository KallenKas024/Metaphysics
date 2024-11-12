package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BegGoal extends Goal {
   private final Wolf wolf;
   @Nullable
   private Player player;
   private final Level level;
   private final float lookDistance;
   private int lookTime;
   private final TargetingConditions begTargeting;

   public BegGoal(Wolf pWolf, float pLookDistance) {
      this.wolf = pWolf;
      this.level = pWolf.level();
      this.lookDistance = pLookDistance;
      this.begTargeting = TargetingConditions.forNonCombat().range((double)pLookDistance);
      this.setFlags(EnumSet.of(Goal.Flag.LOOK));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      this.player = this.level.getNearestPlayer(this.begTargeting, this.wolf);
      return this.player == null ? false : this.playerHoldingInteresting(this.player);
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      if (!this.player.isAlive()) {
         return false;
      } else if (this.wolf.distanceToSqr(this.player) > (double)(this.lookDistance * this.lookDistance)) {
         return false;
      } else {
         return this.lookTime > 0 && this.playerHoldingInteresting(this.player);
      }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.wolf.setIsInterested(true);
      this.lookTime = this.adjustedTickDelay(40 + this.wolf.getRandom().nextInt(40));
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.wolf.setIsInterested(false);
      this.player = null;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      this.wolf.getLookControl().setLookAt(this.player.getX(), this.player.getEyeY(), this.player.getZ(), 10.0F, (float)this.wolf.getMaxHeadXRot());
      --this.lookTime;
   }

   /**
    * Gets if the Player has the Bone in the hand.
    */
   private boolean playerHoldingInteresting(Player pPlayer) {
      for(InteractionHand interactionhand : InteractionHand.values()) {
         ItemStack itemstack = pPlayer.getItemInHand(interactionhand);
         if (this.wolf.isTame() && itemstack.is(Items.BONE)) {
            return true;
         }

         if (this.wolf.isFood(itemstack)) {
            return true;
         }
      }

      return false;
   }
}