package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class AvoidEntityGoal<T extends LivingEntity> extends Goal {
   protected final PathfinderMob mob;
   private final double walkSpeedModifier;
   private final double sprintSpeedModifier;
   @Nullable
   protected T toAvoid;
   protected final float maxDist;
   @Nullable
   protected Path path;
   protected final PathNavigation pathNav;
   /** Class of entity this behavior seeks to avoid */
   protected final Class<T> avoidClass;
   protected final Predicate<LivingEntity> avoidPredicate;
   protected final Predicate<LivingEntity> predicateOnAvoidEntity;
   private final TargetingConditions avoidEntityTargeting;

   /**
    * Goal that helps mobs avoid mobs of a specific class
    */
   public AvoidEntityGoal(PathfinderMob pMob, Class<T> pEntityClassToAvoid, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier) {
      this(pMob, pEntityClassToAvoid, (p_25052_) -> {
         return true;
      }, pMaxDistance, pWalkSpeedModifier, pSprintSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
   }

   /**
    * Goal that helps mobs avoid mobs of a specific class
    */
   public AvoidEntityGoal(PathfinderMob pMob, Class<T> pEntityClassToAvoid, Predicate<LivingEntity> pAvoidPredicate, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier, Predicate<LivingEntity> pPredicateOnAvoidEntity) {
      this.mob = pMob;
      this.avoidClass = pEntityClassToAvoid;
      this.avoidPredicate = pAvoidPredicate;
      this.maxDist = pMaxDistance;
      this.walkSpeedModifier = pWalkSpeedModifier;
      this.sprintSpeedModifier = pSprintSpeedModifier;
      this.predicateOnAvoidEntity = pPredicateOnAvoidEntity;
      this.pathNav = pMob.getNavigation();
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      this.avoidEntityTargeting = TargetingConditions.forCombat().range((double)pMaxDistance).selector(pPredicateOnAvoidEntity.and(pAvoidPredicate));
   }

   /**
    * Goal that helps mobs avoid mobs of a specific class
    */
   public AvoidEntityGoal(PathfinderMob pMob, Class<T> pEntityClassToAvoid, float pMaxDistance, double pWalkSpeedModifier, double pSprintSpeedModifier, Predicate<LivingEntity> pPredicateOnAvoidEntity) {
      this(pMob, pEntityClassToAvoid, (p_25049_) -> {
         return true;
      }, pMaxDistance, pWalkSpeedModifier, pSprintSpeedModifier, pPredicateOnAvoidEntity);
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      this.toAvoid = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.avoidClass, this.mob.getBoundingBox().inflate((double)this.maxDist, 3.0D, (double)this.maxDist), (p_148078_) -> {
         return true;
      }), this.avoidEntityTargeting, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
      if (this.toAvoid == null) {
         return false;
      } else {
         Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.position());
         if (vec3 == null) {
            return false;
         } else if (this.toAvoid.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.distanceToSqr(this.mob)) {
            return false;
         } else {
            this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
            return this.path != null;
         }
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return !this.pathNav.isDone();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.pathNav.moveTo(this.path, this.walkSpeedModifier);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.toAvoid = null;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      if (this.mob.distanceToSqr(this.toAvoid) < 49.0D) {
         this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
      } else {
         this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
      }

   }
}