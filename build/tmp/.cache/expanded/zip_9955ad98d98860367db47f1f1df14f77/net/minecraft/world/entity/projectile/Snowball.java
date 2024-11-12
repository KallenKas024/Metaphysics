package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class Snowball extends ThrowableItemProjectile {
   public Snowball(EntityType<? extends Snowball> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public Snowball(Level pLevel, LivingEntity pShooter) {
      super(EntityType.SNOWBALL, pShooter, pLevel);
   }

   public Snowball(Level pLevel, double pX, double pY, double pZ) {
      super(EntityType.SNOWBALL, pX, pY, pZ, pLevel);
   }

   protected Item getDefaultItem() {
      return Items.SNOWBALL;
   }

   private ParticleOptions getParticle() {
      ItemStack itemstack = this.getItemRaw();
      return (ParticleOptions)(itemstack.isEmpty() ? ParticleTypes.ITEM_SNOWBALL : new ItemParticleOption(ParticleTypes.ITEM, itemstack));
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 3) {
         ParticleOptions particleoptions = this.getParticle();

         for(int i = 0; i < 8; ++i) {
            this.level().addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
         }
      }

   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityHitResult pResult) {
      super.onHitEntity(pResult);
      Entity entity = pResult.getEntity();
      int i = entity instanceof Blaze ? 3 : 0;
      entity.hurt(this.damageSources().thrown(this, this.getOwner()), (float)i);
   }

   /**
    * Called when this EntityFireball hits a block or entity.
    */
   protected void onHit(HitResult pResult) {
      super.onHit(pResult);
      if (!this.level().isClientSide) {
         this.level().broadcastEntityEvent(this, (byte)3);
         this.discard();
      }

   }
}