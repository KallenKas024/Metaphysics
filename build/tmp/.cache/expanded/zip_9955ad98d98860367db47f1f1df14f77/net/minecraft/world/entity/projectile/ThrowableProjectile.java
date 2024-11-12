package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ThrowableProjectile extends Projectile {
   protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> pEntityType, double pX, double pY, double pZ, Level pLevel) {
      this(pEntityType, pLevel);
      this.setPos(pX, pY, pZ);
   }

   protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> pEntityType, LivingEntity pShooter, Level pLevel) {
      this(pEntityType, pShooter.getX(), pShooter.getEyeY() - (double)0.1F, pShooter.getZ(), pLevel);
      this.setOwner(pShooter);
   }

   /**
    * Checks if the entity is in range to render.
    */
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      double d0 = this.getBoundingBox().getSize() * 4.0D;
      if (Double.isNaN(d0)) {
         d0 = 4.0D;
      }

      d0 *= 64.0D;
      return pDistance < d0 * d0;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      boolean flag = false;
      if (hitresult.getType() == HitResult.Type.BLOCK) {
         BlockPos blockpos = ((BlockHitResult)hitresult).getBlockPos();
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (blockstate.is(Blocks.NETHER_PORTAL)) {
            this.handleInsidePortal(blockpos);
            flag = true;
         } else if (blockstate.is(Blocks.END_GATEWAY)) {
            BlockEntity blockentity = this.level().getBlockEntity(blockpos);
            if (blockentity instanceof TheEndGatewayBlockEntity && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
               TheEndGatewayBlockEntity.teleportEntity(this.level(), blockpos, blockstate, this, (TheEndGatewayBlockEntity)blockentity);
            }

            flag = true;
         }
      }

      if (hitresult.getType() != HitResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
         this.onHit(hitresult);
      }

      this.checkInsideBlocks();
      Vec3 vec3 = this.getDeltaMovement();
      double d2 = this.getX() + vec3.x;
      double d0 = this.getY() + vec3.y;
      double d1 = this.getZ() + vec3.z;
      this.updateRotation();
      float f;
      if (this.isInWater()) {
         for(int i = 0; i < 4; ++i) {
            float f1 = 0.25F;
            this.level().addParticle(ParticleTypes.BUBBLE, d2 - vec3.x * 0.25D, d0 - vec3.y * 0.25D, d1 - vec3.z * 0.25D, vec3.x, vec3.y, vec3.z);
         }

         f = 0.8F;
      } else {
         f = 0.99F;
      }

      this.setDeltaMovement(vec3.scale((double)f));
      if (!this.isNoGravity()) {
         Vec3 vec31 = this.getDeltaMovement();
         this.setDeltaMovement(vec31.x, vec31.y - (double)this.getGravity(), vec31.z);
      }

      this.setPos(d2, d0, d1);
   }

   /**
    * Gets the amount of gravity to apply to the thrown entity with each tick.
    */
   protected float getGravity() {
      return 0.03F;
   }
}
