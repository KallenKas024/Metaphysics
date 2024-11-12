package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownEnderpearl extends ThrowableItemProjectile {
   public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ThrownEnderpearl(Level pLevel, LivingEntity pShooter) {
      super(EntityType.ENDER_PEARL, pShooter, pLevel);
   }

   protected Item getDefaultItem() {
      return Items.ENDER_PEARL;
   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityHitResult pResult) {
      super.onHitEntity(pResult);
      pResult.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
   }

   /**
    * Called when this EntityFireball hits a block or entity.
    */
   protected void onHit(HitResult pResult) {
      super.onHit(pResult);

      for(int i = 0; i < 32; ++i) {
         this.level().addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0D, this.getZ(), this.random.nextGaussian(), 0.0D, this.random.nextGaussian());
      }

      if (!this.level().isClientSide && !this.isRemoved()) {
         Entity entity = this.getOwner();
         if (entity instanceof ServerPlayer) {
            ServerPlayer serverplayer = (ServerPlayer)entity;
            if (serverplayer.connection.isAcceptingMessages() && serverplayer.level() == this.level() && !serverplayer.isSleeping()) {
               net.minecraftforge.event.entity.EntityTeleportEvent.EnderPearl event = net.minecraftforge.event.ForgeEventFactory.onEnderPearlLand(serverplayer, this.getX(), this.getY(), this.getZ(), this, 5.0F, pResult);
               if (!event.isCanceled()) { // Don't indent to lower patch size
               if (this.random.nextFloat() < 0.05F && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                  Endermite endermite = EntityType.ENDERMITE.create(this.level());
                  if (endermite != null) {
                     endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                     this.level().addFreshEntity(endermite);
                  }
               }

               if (entity.isPassenger()) {
                  serverplayer.dismountTo(this.getX(), this.getY(), this.getZ());
               } else {
                  entity.teleportTo(this.getX(), this.getY(), this.getZ());
               }

               entity.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
               entity.resetFallDistance();
               entity.hurt(this.damageSources().fall(), event.getAttackDamage());
               } //Forge: End
            }
         } else if (entity != null) {
            entity.teleportTo(this.getX(), this.getY(), this.getZ());
            entity.resetFallDistance();
         }

         this.discard();
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      Entity entity = this.getOwner();
      if (entity instanceof Player && !entity.isAlive()) {
         this.discard();
      } else {
         super.tick();
      }

   }

   @Nullable
   public Entity changeDimension(ServerLevel pServer, net.minecraftforge.common.util.ITeleporter teleporter) {
      Entity entity = this.getOwner();
      if (entity != null && entity.level().dimension() != pServer.dimension()) {
         this.setOwner((Entity)null);
      }

      return super.changeDimension(pServer, teleporter);
   }
}
