package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public abstract class AbstractGolem extends PathfinderMob {
   protected AbstractGolem(EntityType<? extends AbstractGolem> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return null;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return null;
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 120;
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return false;
   }
}