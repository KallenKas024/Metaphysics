package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

public class HurtByTargetGoal extends TargetGoal {
   private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
   private static final int ALERT_RANGE_Y = 10;
   private boolean alertSameType;
   /** Store the previous revengeTimer value */
   private int timestamp;
   private final Class<?>[] toIgnoreDamage;
   @Nullable
   private Class<?>[] toIgnoreAlert;

   public HurtByTargetGoal(PathfinderMob pMob, Class<?>... pToIgnoreDamage) {
      super(pMob, true);
      this.toIgnoreDamage = pToIgnoreDamage;
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      int i = this.mob.getLastHurtByMobTimestamp();
      LivingEntity livingentity = this.mob.getLastHurtByMob();
      if (i != this.timestamp && livingentity != null) {
         if (livingentity.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            return false;
         } else {
            for(Class<?> oclass : this.toIgnoreDamage) {
               if (oclass.isAssignableFrom(livingentity.getClass())) {
                  return false;
               }
            }

            return this.canAttack(livingentity, HURT_BY_TARGETING);
         }
      } else {
         return false;
      }
   }

   public HurtByTargetGoal setAlertOthers(Class<?>... pReinforcementTypes) {
      this.alertSameType = true;
      this.toIgnoreAlert = pReinforcementTypes;
      return this;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.setTarget(this.mob.getLastHurtByMob());
      this.targetMob = this.mob.getTarget();
      this.timestamp = this.mob.getLastHurtByMobTimestamp();
      this.unseenMemoryTicks = 300;
      if (this.alertSameType) {
         this.alertOthers();
      }

      super.start();
   }

   protected void alertOthers() {
      double d0 = this.getFollowDistance();
      AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, 10.0D, d0);
      List<? extends Mob> list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), aabb, EntitySelector.NO_SPECTATORS);
      Iterator iterator = list.iterator();

      while(true) {
         Mob mob;
         while(true) {
            if (!iterator.hasNext()) {
               return;
            }

            mob = (Mob)iterator.next();
            if (this.mob != mob && mob.getTarget() == null && (!(this.mob instanceof TamableAnimal) || ((TamableAnimal)this.mob).getOwner() == ((TamableAnimal)mob).getOwner()) && !mob.isAlliedTo(this.mob.getLastHurtByMob())) {
               if (this.toIgnoreAlert == null) {
                  break;
               }

               boolean flag = false;

               for(Class<?> oclass : this.toIgnoreAlert) {
                  if (mob.getClass() == oclass) {
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  break;
               }
            }
         }

         this.alertOther(mob, this.mob.getLastHurtByMob());
      }
   }

   protected void alertOther(Mob pMob, LivingEntity pTarget) {
      pMob.setTarget(pTarget);
   }
}