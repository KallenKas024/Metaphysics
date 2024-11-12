package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EyeOfEnder extends Entity implements ItemSupplier {
   private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(EyeOfEnder.class, EntityDataSerializers.ITEM_STACK);
   private double tx;
   private double ty;
   private double tz;
   private int life;
   private boolean surviveAfterDeath;

   public EyeOfEnder(EntityType<? extends EyeOfEnder> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public EyeOfEnder(Level pLevel, double pX, double pY, double pZ) {
      this(EntityType.EYE_OF_ENDER, pLevel);
      this.setPos(pX, pY, pZ);
   }

   public void setItem(ItemStack pStack) {
      if (!pStack.is(Items.ENDER_EYE) || pStack.hasTag()) {
         this.getEntityData().set(DATA_ITEM_STACK, pStack.copyWithCount(1));
      }

   }

   private ItemStack getItemRaw() {
      return this.getEntityData().get(DATA_ITEM_STACK);
   }

   public ItemStack getItem() {
      ItemStack itemstack = this.getItemRaw();
      return itemstack.isEmpty() ? new ItemStack(Items.ENDER_EYE) : itemstack;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
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

   public void signalTo(BlockPos pPos) {
      double d0 = (double)pPos.getX();
      int i = pPos.getY();
      double d1 = (double)pPos.getZ();
      double d2 = d0 - this.getX();
      double d3 = d1 - this.getZ();
      double d4 = Math.sqrt(d2 * d2 + d3 * d3);
      if (d4 > 12.0D) {
         this.tx = this.getX() + d2 / d4 * 12.0D;
         this.tz = this.getZ() + d3 / d4 * 12.0D;
         this.ty = this.getY() + 8.0D;
      } else {
         this.tx = d0;
         this.ty = (double)i;
         this.tz = d1;
      }

      this.life = 0;
      this.surviveAfterDeath = this.random.nextInt(5) > 0;
   }

   /**
    * Updates the entity motion clientside, called by packets from the server
    */
   public void lerpMotion(double pX, double pY, double pZ) {
      this.setDeltaMovement(pX, pY, pZ);
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         double d0 = Math.sqrt(pX * pX + pZ * pZ);
         this.setYRot((float)(Mth.atan2(pX, pZ) * (double)(180F / (float)Math.PI)));
         this.setXRot((float)(Mth.atan2(pY, d0) * (double)(180F / (float)Math.PI)));
         this.yRotO = this.getYRot();
         this.xRotO = this.getXRot();
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      Vec3 vec3 = this.getDeltaMovement();
      double d0 = this.getX() + vec3.x;
      double d1 = this.getY() + vec3.y;
      double d2 = this.getZ() + vec3.z;
      double d3 = vec3.horizontalDistance();
      this.setXRot(Projectile.lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, d3) * (double)(180F / (float)Math.PI))));
      this.setYRot(Projectile.lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI))));
      if (!this.level().isClientSide) {
         double d4 = this.tx - d0;
         double d5 = this.tz - d2;
         float f = (float)Math.sqrt(d4 * d4 + d5 * d5);
         float f1 = (float)Mth.atan2(d5, d4);
         double d6 = Mth.lerp(0.0025D, d3, (double)f);
         double d7 = vec3.y;
         if (f < 1.0F) {
            d6 *= 0.8D;
            d7 *= 0.8D;
         }

         int j = this.getY() < this.ty ? 1 : -1;
         vec3 = new Vec3(Math.cos((double)f1) * d6, d7 + ((double)j - d7) * (double)0.015F, Math.sin((double)f1) * d6);
         this.setDeltaMovement(vec3);
      }

      float f2 = 0.25F;
      if (this.isInWater()) {
         for(int i = 0; i < 4; ++i) {
            this.level().addParticle(ParticleTypes.BUBBLE, d0 - vec3.x * 0.25D, d1 - vec3.y * 0.25D, d2 - vec3.z * 0.25D, vec3.x, vec3.y, vec3.z);
         }
      } else {
         this.level().addParticle(ParticleTypes.PORTAL, d0 - vec3.x * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, d1 - vec3.y * 0.25D - 0.5D, d2 - vec3.z * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, vec3.x, vec3.y, vec3.z);
      }

      if (!this.level().isClientSide) {
         this.setPos(d0, d1, d2);
         ++this.life;
         if (this.life > 80 && !this.level().isClientSide) {
            this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
            this.discard();
            if (this.surviveAfterDeath) {
               this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.getItem()));
            } else {
               this.level().levelEvent(2003, this.blockPosition(), 0);
            }
         }
      } else {
         this.setPosRaw(d0, d1, d2);
      }

   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      ItemStack itemstack = this.getItemRaw();
      if (!itemstack.isEmpty()) {
         pCompound.put("Item", itemstack.save(new CompoundTag()));
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      ItemStack itemstack = ItemStack.of(pCompound.getCompound("Item"));
      this.setItem(itemstack);
   }

   public float getLightLevelDependentMagicValue() {
      return 1.0F;
   }

   /**
    * Returns {@code true} if it's possible to attack this entity with an item.
    */
   public boolean isAttackable() {
      return false;
   }
}