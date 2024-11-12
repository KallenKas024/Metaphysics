package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public class BreedGoal extends Goal {
   private static final TargetingConditions PARTNER_TARGETING = TargetingConditions.forNonCombat().range(8.0D).ignoreLineOfSight();
   protected final Animal animal;
   private final Class<? extends Animal> partnerClass;
   protected final Level level;
   @Nullable
   protected Animal partner;
   private int loveTime;
   private final double speedModifier;

   public BreedGoal(Animal pAnimal, double pSpeedModifier) {
      this(pAnimal, pSpeedModifier, pAnimal.getClass());
   }

   public BreedGoal(Animal pAnimal, double pSpeedModifier, Class<? extends Animal> pPartnerClass) {
      this.animal = pAnimal;
      this.level = pAnimal.level();
      this.partnerClass = pPartnerClass;
      this.speedModifier = pSpeedModifier;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (!this.animal.isInLove()) {
         return false;
      } else {
         this.partner = this.getFreePartner();
         return this.partner != null;
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.partner.isAlive() && this.partner.isInLove() && this.loveTime < 60;
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.partner = null;
      this.loveTime = 0;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      this.animal.getLookControl().setLookAt(this.partner, 10.0F, (float)this.animal.getMaxHeadXRot());
      this.animal.getNavigation().moveTo(this.partner, this.speedModifier);
      ++this.loveTime;
      if (this.loveTime >= this.adjustedTickDelay(60) && this.animal.distanceToSqr(this.partner) < 9.0D) {
         this.breed();
      }

   }

   /**
    * Loops through nearby animals and finds another animal of the same type that can be mated with. Returns the first
    * valid mate found.
    */
   @Nullable
   private Animal getFreePartner() {
      List<? extends Animal> list = this.level.getNearbyEntities(this.partnerClass, PARTNER_TARGETING, this.animal, this.animal.getBoundingBox().inflate(8.0D));
      double d0 = Double.MAX_VALUE;
      Animal animal = null;

      for(Animal animal1 : list) {
         if (this.animal.canMate(animal1) && this.animal.distanceToSqr(animal1) < d0) {
            animal = animal1;
            d0 = this.animal.distanceToSqr(animal1);
         }
      }

      return animal;
   }

   /**
    * Spawns a baby animal of the same type.
    */
   protected void breed() {
      this.animal.spawnChildFromBreeding((ServerLevel)this.level, this.partner);
   }
}