package net.minecraft.world.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;

public class EvokerFangs extends Entity implements TraceableEntity {
   public static final int ATTACK_DURATION = 20;
   public static final int LIFE_OFFSET = 2;
   public static final int ATTACK_TRIGGER_TICKS = 14;
   private int warmupDelayTicks;
   private boolean sentSpikeEvent;
   private int lifeTicks = 22;
   private boolean clientSideAttackStarted;
   @Nullable
   private LivingEntity owner;
   @Nullable
   private UUID ownerUUID;

   public EvokerFangs(EntityType<? extends EvokerFangs> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public EvokerFangs(Level pLevel, double pX, double pY, double pZ, float pYRot, int pWarmupDelay, LivingEntity pOwner) {
      this(EntityType.EVOKER_FANGS, pLevel);
      this.warmupDelayTicks = pWarmupDelay;
      this.setOwner(pOwner);
      this.setYRot(pYRot * (180F / (float)Math.PI));
      this.setPos(pX, pY, pZ);
   }

   protected void defineSynchedData() {
   }

   public void setOwner(@Nullable LivingEntity pOwner) {
      this.owner = pOwner;
      this.ownerUUID = pOwner == null ? null : pOwner.getUUID();
   }

   /**
    * Returns null or the entityliving it was ignited by
    */
   @Nullable
   public LivingEntity getOwner() {
      if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel) {
         Entity entity = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
         if (entity instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
         }
      }

      return this.owner;
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundTag pCompound) {
      this.warmupDelayTicks = pCompound.getInt("Warmup");
      if (pCompound.hasUUID("Owner")) {
         this.ownerUUID = pCompound.getUUID("Owner");
      }

   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      pCompound.putInt("Warmup", this.warmupDelayTicks);
      if (this.ownerUUID != null) {
         pCompound.putUUID("Owner", this.ownerUUID);
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         if (this.clientSideAttackStarted) {
            --this.lifeTicks;
            if (this.lifeTicks == 14) {
               for(int i = 0; i < 12; ++i) {
                  double d0 = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                  double d1 = this.getY() + 0.05D + this.random.nextDouble();
                  double d2 = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                  double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                  double d4 = 0.3D + this.random.nextDouble() * 0.3D;
                  double d5 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                  this.level().addParticle(ParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
               }
            }
         }
      } else if (--this.warmupDelayTicks < 0) {
         if (this.warmupDelayTicks == -8) {
            for(LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2D, 0.0D, 0.2D))) {
               this.dealDamageTo(livingentity);
            }
         }

         if (!this.sentSpikeEvent) {
            this.level().broadcastEntityEvent(this, (byte)4);
            this.sentSpikeEvent = true;
         }

         if (--this.lifeTicks < 0) {
            this.discard();
         }
      }

   }

   private void dealDamageTo(LivingEntity pTarget) {
      LivingEntity livingentity = this.getOwner();
      if (pTarget.isAlive() && !pTarget.isInvulnerable() && pTarget != livingentity) {
         if (livingentity == null) {
            pTarget.hurt(this.damageSources().magic(), 6.0F);
         } else {
            if (livingentity.isAlliedTo(pTarget)) {
               return;
            }

            pTarget.hurt(this.damageSources().indirectMagic(this, livingentity), 6.0F);
         }

      }
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      super.handleEntityEvent(pId);
      if (pId == 4) {
         this.clientSideAttackStarted = true;
         if (!this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_FANGS_ATTACK, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.2F + 0.85F, false);
         }
      }

   }

   public float getAnimationProgress(float pPartialTicks) {
      if (!this.clientSideAttackStarted) {
         return 0.0F;
      } else {
         int i = this.lifeTicks - 2;
         return i <= 0 ? 1.0F : 1.0F - ((float)i - pPartialTicks) / 20.0F;
      }
   }
}