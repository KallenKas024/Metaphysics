package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractDragonPhaseInstance implements DragonPhaseInstance {
   protected final EnderDragon dragon;

   public AbstractDragonPhaseInstance(EnderDragon pDragon) {
      this.dragon = pDragon;
   }

   public boolean isSitting() {
      return false;
   }

   /**
    * Generates particle effects appropriate to the phase (or sometimes sounds).
    * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
    */
   public void doClientTick() {
   }

   /**
    * Gives the phase a chance to update its status.
    * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
    */
   public void doServerTick() {
   }

   public void onCrystalDestroyed(EndCrystal pCrystal, BlockPos pPos, DamageSource pDmgSrc, @Nullable Player pPlyr) {
   }

   /**
    * Called when this phase is set to active
    */
   public void begin() {
   }

   public void end() {
   }

   /**
    * Returns the maximum amount dragon may rise or fall during this phase
    */
   public float getFlySpeed() {
      return 0.6F;
   }

   /**
    * Returns the location the dragon is flying toward
    */
   @Nullable
   public Vec3 getFlyTargetLocation() {
      return null;
   }

   public float onHurt(DamageSource pDamageSource, float pAmount) {
      return pAmount;
   }

   public float getTurnSpeed() {
      float f = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0F;
      float f1 = Math.min(f, 40.0F);
      return 0.7F / f1 / f;
   }
}