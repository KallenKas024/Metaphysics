package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public class Donkey extends AbstractChestedHorse {
   public Donkey(EntityType<? extends Donkey> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.DONKEY_AMBIENT;
   }

   protected SoundEvent getAngrySound() {
      return SoundEvents.DONKEY_ANGRY;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.DONKEY_DEATH;
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return SoundEvents.DONKEY_EAT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.DONKEY_HURT;
   }

   /**
    * Returns {@code true} if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(Animal pOtherAnimal) {
      if (pOtherAnimal == this) {
         return false;
      } else if (!(pOtherAnimal instanceof Donkey) && !(pOtherAnimal instanceof Horse)) {
         return false;
      } else {
         return this.canParent() && ((AbstractHorse)pOtherAnimal).canParent();
      }
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      EntityType<? extends AbstractHorse> entitytype = pOtherParent instanceof Horse ? EntityType.MULE : EntityType.DONKEY;
      AbstractHorse abstracthorse = entitytype.create(pLevel);
      if (abstracthorse != null) {
         this.setOffspringAttributes(pOtherParent, abstracthorse);
      }

      return abstracthorse;
   }
}