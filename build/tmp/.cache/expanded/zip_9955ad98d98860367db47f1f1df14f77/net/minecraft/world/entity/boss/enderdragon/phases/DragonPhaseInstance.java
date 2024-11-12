package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface DragonPhaseInstance {
   boolean isSitting();

   /**
    * Generates particle effects appropriate to the phase (or sometimes sounds).
    * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
    */
   void doClientTick();

   /**
    * Gives the phase a chance to update its status.
    * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
    */
   void doServerTick();

   void onCrystalDestroyed(EndCrystal pCrystal, BlockPos pPos, DamageSource pDamageSource, @Nullable Player pPlayer);

   /**
    * Called when this phase is set to active
    */
   void begin();

   void end();

   /**
    * Returns the maximum amount dragon may rise or fall during this phase
    */
   float getFlySpeed();

   float getTurnSpeed();

   EnderDragonPhase<? extends DragonPhaseInstance> getPhase();

   /**
    * Returns the location the dragon is flying toward
    */
   @Nullable
   Vec3 getFlyTargetLocation();

   float onHurt(DamageSource pDamageSource, float pAmount);
}