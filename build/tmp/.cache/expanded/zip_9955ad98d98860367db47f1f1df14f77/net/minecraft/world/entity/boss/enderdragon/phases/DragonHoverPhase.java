package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;

public class DragonHoverPhase extends AbstractDragonPhaseInstance {
   @Nullable
   private Vec3 targetLocation;

   public DragonHoverPhase(EnderDragon pDragon) {
      super(pDragon);
   }

   /**
    * Gives the phase a chance to update its status.
    * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
    */
   public void doServerTick() {
      if (this.targetLocation == null) {
         this.targetLocation = this.dragon.position();
      }

   }

   public boolean isSitting() {
      return true;
   }

   /**
    * Called when this phase is set to active
    */
   public void begin() {
      this.targetLocation = null;
   }

   /**
    * Returns the maximum amount dragon may rise or fall during this phase
    */
   public float getFlySpeed() {
      return 1.0F;
   }

   /**
    * Returns the location the dragon is flying toward
    */
   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   public EnderDragonPhase<DragonHoverPhase> getPhase() {
      return EnderDragonPhase.HOVERING;
   }
}