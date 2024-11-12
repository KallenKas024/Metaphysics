package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LlamaSpit extends Projectile {
   public LlamaSpit(EntityType<? extends LlamaSpit> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public LlamaSpit(Level pLevel, Llama pSpitter) {
      this(EntityType.LLAMA_SPIT, pLevel);
      this.setOwner(pSpitter);
      this.setPos(pSpitter.getX() - (double)(pSpitter.getBbWidth() + 1.0F) * 0.5D * (double)Mth.sin(pSpitter.yBodyRot * ((float)Math.PI / 180F)), pSpitter.getEyeY() - (double)0.1F, pSpitter.getZ() + (double)(pSpitter.getBbWidth() + 1.0F) * 0.5D * (double)Mth.cos(pSpitter.yBodyRot * ((float)Math.PI / 180F)));
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      Vec3 vec3 = this.getDeltaMovement();
      HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      if (hitresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult))
      this.onHit(hitresult);
      double d0 = this.getX() + vec3.x;
      double d1 = this.getY() + vec3.y;
      double d2 = this.getZ() + vec3.z;
      this.updateRotation();
      float f = 0.99F;
      float f1 = 0.06F;
      if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(BlockBehaviour.BlockStateBase::isAir)) {
         this.discard();
      } else if (this.isInWaterOrBubble()) {
         this.discard();
      } else {
         this.setDeltaMovement(vec3.scale((double)0.99F));
         if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)-0.06F, 0.0D));
         }

         this.setPos(d0, d1, d2);
      }
   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityHitResult pResult) {
      super.onHitEntity(pResult);
      Entity entity = this.getOwner();
      if (entity instanceof LivingEntity livingentity) {
         pResult.getEntity().hurt(this.damageSources().mobProjectile(this, livingentity), 1.0F);
      }

   }

   protected void onHitBlock(BlockHitResult pResult) {
      super.onHitBlock(pResult);
      if (!this.level().isClientSide) {
         this.discard();
      }

   }

   protected void defineSynchedData() {
   }

   public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
      super.recreateFromPacket(pPacket);
      double d0 = pPacket.getXa();
      double d1 = pPacket.getYa();
      double d2 = pPacket.getZa();

      for(int i = 0; i < 7; ++i) {
         double d3 = 0.4D + 0.1D * (double)i;
         this.level().addParticle(ParticleTypes.SPIT, this.getX(), this.getY(), this.getZ(), d0 * d3, d1, d2 * d3);
      }

      this.setDeltaMovement(d0, d1, d2);
   }
}
