package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

public class DragonSittingAttackingPhase extends AbstractDragonSittingPhase {
   private static final int ROAR_DURATION = 40;
   private int attackingTicks;

   public DragonSittingAttackingPhase(EnderDragon pDragon) {
      super(pDragon);
   }

   /**
    * Generates particle effects appropriate to the phase (or sometimes sounds).
    * Called by dragon's onLivingUpdate. Only used when worldObj.isRemote.
    */
   public void doClientTick() {
      this.dragon.level().playLocalSound(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.dragon.getSoundSource(), 2.5F, 0.8F + this.dragon.getRandom().nextFloat() * 0.3F, false);
   }

   /**
    * Gives the phase a chance to update its status.
    * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
    */
   public void doServerTick() {
      if (this.attackingTicks++ >= 40) {
         this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_FLAMING);
      }

   }

   /**
    * Called when this phase is set to active
    */
   public void begin() {
      this.attackingTicks = 0;
   }

   public EnderDragonPhase<DragonSittingAttackingPhase> getPhase() {
      return EnderDragonPhase.SITTING_ATTACKING;
   }
}