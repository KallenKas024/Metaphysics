package net.minecraft.world.entity.ai.goal;

import com.mojang.datafixers.DataFixUtils;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;

public class FollowFlockLeaderGoal extends Goal {
   private static final int INTERVAL_TICKS = 200;
   private final AbstractSchoolingFish mob;
   private int timeToRecalcPath;
   private int nextStartTick;

   public FollowFlockLeaderGoal(AbstractSchoolingFish pFish) {
      this.mob = pFish;
      this.nextStartTick = this.nextStartTick(pFish);
   }

   protected int nextStartTick(AbstractSchoolingFish pTaskOwner) {
      return reducedTickDelay(200 + pTaskOwner.getRandom().nextInt(200) % 20);
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.mob.hasFollowers()) {
         return false;
      } else if (this.mob.isFollower()) {
         return true;
      } else if (this.nextStartTick > 0) {
         --this.nextStartTick;
         return false;
      } else {
         this.nextStartTick = this.nextStartTick(this.mob);
         Predicate<AbstractSchoolingFish> predicate = (p_25258_) -> {
            return p_25258_.canBeFollowed() || !p_25258_.isFollower();
         };
         List<? extends AbstractSchoolingFish> list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), predicate);
         AbstractSchoolingFish abstractschoolingfish = DataFixUtils.orElse(list.stream().filter(AbstractSchoolingFish::canBeFollowed).findAny(), this.mob);
         abstractschoolingfish.addFollowers(list.stream().filter((p_25255_) -> {
            return !p_25255_.isFollower();
         }));
         return this.mob.isFollower();
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.mob.isFollower() && this.mob.inRangeOfLeader();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.timeToRecalcPath = 0;
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.mob.stopFollowing();
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      if (--this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = this.adjustedTickDelay(10);
         this.mob.pathToLeader();
      }
   }
}