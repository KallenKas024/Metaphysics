package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

public class LookAtPlayerGoal extends Goal {
   public static final float DEFAULT_PROBABILITY = 0.02F;
   protected final Mob mob;
   @Nullable
   protected Entity lookAt;
   protected final float lookDistance;
   private int lookTime;
   protected final float probability;
   private final boolean onlyHorizontal;
   protected final Class<? extends LivingEntity> lookAtType;
   protected final TargetingConditions lookAtContext;

   public LookAtPlayerGoal(Mob pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance) {
      this(pMob, pLookAtType, pLookDistance, 0.02F);
   }

   public LookAtPlayerGoal(Mob pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance, float pProbability) {
      this(pMob, pLookAtType, pLookDistance, pProbability, false);
   }

   public LookAtPlayerGoal(Mob pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance, float pProbability, boolean pOnlyHorizontal) {
      this.mob = pMob;
      this.lookAtType = pLookAtType;
      this.lookDistance = pLookDistance;
      this.probability = pProbability;
      this.onlyHorizontal = pOnlyHorizontal;
      this.setFlags(EnumSet.of(Goal.Flag.LOOK));
      if (pLookAtType == Player.class) {
         this.lookAtContext = TargetingConditions.forNonCombat().range((double)pLookDistance).selector((p_25531_) -> {
            return EntitySelector.notRiding(pMob).test(p_25531_);
         });
      } else {
         this.lookAtContext = TargetingConditions.forNonCombat().range((double)pLookDistance);
      }

   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.mob.getRandom().nextFloat() >= this.probability) {
         return false;
      } else {
         if (this.mob.getTarget() != null) {
            this.lookAt = this.mob.getTarget();
         }

         if (this.lookAtType == Player.class) {
            this.lookAt = this.mob.level().getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
         } else {
            this.lookAt = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0D, (double)this.lookDistance), (p_148124_) -> {
               return true;
            }), this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
         }

         return this.lookAt != null;
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      if (!this.lookAt.isAlive()) {
         return false;
      } else if (this.mob.distanceToSqr(this.lookAt) > (double)(this.lookDistance * this.lookDistance)) {
         return false;
      } else {
         return this.lookTime > 0;
      }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.lookAt = null;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      if (this.lookAt.isAlive()) {
         double d0 = this.onlyHorizontal ? this.mob.getEyeY() : this.lookAt.getEyeY();
         this.mob.getLookControl().setLookAt(this.lookAt.getX(), d0, this.lookAt.getZ());
         --this.lookTime;
      }
   }
}