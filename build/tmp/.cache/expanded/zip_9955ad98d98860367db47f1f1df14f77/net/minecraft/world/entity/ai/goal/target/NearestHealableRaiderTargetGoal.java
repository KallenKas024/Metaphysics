package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;

public class NearestHealableRaiderTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
   private static final int DEFAULT_COOLDOWN = 200;
   private int cooldown = 0;

   public NearestHealableRaiderTargetGoal(Raider pMob, Class<T> pTargetType, boolean pMustSee, @Nullable Predicate<LivingEntity> pTargetPredicate) {
      super(pMob, pTargetType, 500, pMustSee, false, pTargetPredicate);
   }

   public int getCooldown() {
      return this.cooldown;
   }

   public void decrementCooldown() {
      --this.cooldown;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.cooldown <= 0 && this.mob.getRandom().nextBoolean()) {
         if (!((Raider)this.mob).hasActiveRaid()) {
            return false;
         } else {
            this.findTarget();
            return this.target != null;
         }
      } else {
         return false;
      }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.cooldown = reducedTickDelay(200);
      super.start();
   }
}