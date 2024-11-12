package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

public abstract class AbstractIllager extends Raider {
   protected AbstractIllager(EntityType<? extends AbstractIllager> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected void registerGoals() {
      super.registerGoals();
   }

   public MobType getMobType() {
      return MobType.ILLAGER;
   }

   public AbstractIllager.IllagerArmPose getArmPose() {
      return AbstractIllager.IllagerArmPose.CROSSED;
   }

   public boolean canAttack(LivingEntity pTarget) {
      return pTarget instanceof AbstractVillager && pTarget.isBaby() ? false : super.canAttack(pTarget);
   }

   public static enum IllagerArmPose {
      CROSSED,
      ATTACKING,
      SPELLCASTING,
      BOW_AND_ARROW,
      CROSSBOW_HOLD,
      CROSSBOW_CHARGE,
      CELEBRATING,
      NEUTRAL;
   }

   protected class RaiderOpenDoorGoal extends OpenDoorGoal {
      public RaiderOpenDoorGoal(Raider pRaider) {
         super(pRaider, false);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && AbstractIllager.this.hasActiveRaid();
      }
   }
}