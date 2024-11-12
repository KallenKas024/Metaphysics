package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

public class NonTameRandomTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
   private final TamableAnimal tamableMob;

   public NonTameRandomTargetGoal(TamableAnimal pTamableMob, Class<T> pTargetType, boolean pMustSee, @Nullable Predicate<LivingEntity> pTargetPredicate) {
      super(pTamableMob, pTargetType, 10, pMustSee, false, pTargetPredicate);
      this.tamableMob = pTamableMob;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return !this.tamableMob.isTame() && super.canUse();
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.targetConditions != null ? this.targetConditions.test(this.mob, this.target) : super.canContinueToUse();
   }
}