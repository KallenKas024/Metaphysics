package net.minecraft.world.entity.ai.goal.target;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.scores.Team;

public abstract class TargetGoal extends Goal {
   private static final int EMPTY_REACH_CACHE = 0;
   private static final int CAN_REACH_CACHE = 1;
   private static final int CANT_REACH_CACHE = 2;
   /** The entity that this goal belongs to */
   protected final Mob mob;
   protected final boolean mustSee;
   private final boolean mustReach;
   private int reachCache;
   private int reachCacheTime;
   private int unseenTicks;
   @Nullable
   protected LivingEntity targetMob;
   protected int unseenMemoryTicks = 60;

   public TargetGoal(Mob pMob, boolean pMustSee) {
      this(pMob, pMustSee, false);
   }

   public TargetGoal(Mob pMob, boolean pMustSee, boolean pMustReach) {
      this.mob = pMob;
      this.mustSee = pMustSee;
      this.mustReach = pMustReach;
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      LivingEntity livingentity = this.mob.getTarget();
      if (livingentity == null) {
         livingentity = this.targetMob;
      }

      if (livingentity == null) {
         return false;
      } else if (!this.mob.canAttack(livingentity)) {
         return false;
      } else {
         Team team = this.mob.getTeam();
         Team team1 = livingentity.getTeam();
         if (team != null && team1 == team) {
            return false;
         } else {
            double d0 = this.getFollowDistance();
            if (this.mob.distanceToSqr(livingentity) > d0 * d0) {
               return false;
            } else {
               if (this.mustSee) {
                  if (this.mob.getSensing().hasLineOfSight(livingentity)) {
                     this.unseenTicks = 0;
                  } else if (++this.unseenTicks > reducedTickDelay(this.unseenMemoryTicks)) {
                     return false;
                  }
               }

               this.mob.setTarget(livingentity);
               return true;
            }
         }
      }
   }

   protected double getFollowDistance() {
      return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.reachCache = 0;
      this.reachCacheTime = 0;
      this.unseenTicks = 0;
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.mob.setTarget((LivingEntity)null);
      this.targetMob = null;
   }

   /**
    * Checks if this is a suitable target.
    */
   protected boolean canAttack(@Nullable LivingEntity pPotentialTarget, TargetingConditions pTargetPredicate) {
      if (pPotentialTarget == null) {
         return false;
      } else if (!pTargetPredicate.test(this.mob, pPotentialTarget)) {
         return false;
      } else if (!this.mob.isWithinRestriction(pPotentialTarget.blockPosition())) {
         return false;
      } else {
         if (this.mustReach) {
            if (--this.reachCacheTime <= 0) {
               this.reachCache = 0;
            }

            if (this.reachCache == 0) {
               this.reachCache = this.canReach(pPotentialTarget) ? 1 : 2;
            }

            if (this.reachCache == 2) {
               return false;
            }
         }

         return true;
      }
   }

   /**
    * Checks to see if this entity can find a short path to the given target.
    */
   private boolean canReach(LivingEntity pTarget) {
      this.reachCacheTime = reducedTickDelay(10 + this.mob.getRandom().nextInt(5));
      Path path = this.mob.getNavigation().createPath(pTarget, 0);
      if (path == null) {
         return false;
      } else {
         Node node = path.getEndNode();
         if (node == null) {
            return false;
         } else {
            int i = node.x - pTarget.getBlockX();
            int j = node.z - pTarget.getBlockZ();
            return (double)(i * i + j * j) <= 2.25D;
         }
      }
   }

   public TargetGoal setUnseenMemoryTicks(int pUnseenMemoryTicks) {
      this.unseenMemoryTicks = pUnseenMemoryTicks;
      return this;
   }
}