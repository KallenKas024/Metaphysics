package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.LivingEntity;

public interface RangedAttackMob {
   /**
    * Attack the specified entity using a ranged attack.
    */
   void performRangedAttack(LivingEntity pTarget, float pVelocity);
}