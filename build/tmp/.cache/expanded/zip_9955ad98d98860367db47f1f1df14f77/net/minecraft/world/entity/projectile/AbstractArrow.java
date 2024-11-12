package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractArrow extends Projectile {
   private static final double ARROW_BASE_DAMAGE = 2.0D;
   private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
   private static final int FLAG_CRIT = 1;
   private static final int FLAG_NOPHYSICS = 2;
   private static final int FLAG_CROSSBOW = 4;
   @Nullable
   private BlockState lastState;
   protected boolean inGround;
   protected int inGroundTime;
   public AbstractArrow.Pickup pickup = AbstractArrow.Pickup.DISALLOWED;
   public int shakeTime;
   private int life;
   private double baseDamage = 2.0D;
   private int knockback;
   private SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
   @Nullable
   private IntOpenHashSet piercingIgnoreEntityIds;
   @Nullable
   private List<Entity> piercedAndKilledEntities;

   private final IntOpenHashSet ignoredEntities = new IntOpenHashSet();

   protected AbstractArrow(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected AbstractArrow(EntityType<? extends AbstractArrow> pEntityType, double pX, double pY, double pZ, Level pLevel) {
      this(pEntityType, pLevel);
      this.setPos(pX, pY, pZ);
   }

   protected AbstractArrow(EntityType<? extends AbstractArrow> pEntityType, LivingEntity pShooter, Level pLevel) {
      this(pEntityType, pShooter.getX(), pShooter.getEyeY() - (double)0.1F, pShooter.getZ(), pLevel);
      this.setOwner(pShooter);
      if (pShooter instanceof Player) {
         this.pickup = AbstractArrow.Pickup.ALLOWED;
      }

   }

   public void setSoundEvent(SoundEvent pSoundEvent) {
      this.soundEvent = pSoundEvent;
   }

   /**
    * Checks if the entity is in range to render.
    */
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      double d0 = this.getBoundingBox().getSize() * 10.0D;
      if (Double.isNaN(d0)) {
         d0 = 1.0D;
      }

      d0 *= 64.0D * getViewScale();
      return pDistance < d0 * d0;
   }

   protected void defineSynchedData() {
      this.entityData.define(ID_FLAGS, (byte)0);
      this.entityData.define(PIERCE_LEVEL, (byte)0);
   }

   /**
    * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
    */
   public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
      super.shoot(pX, pY, pZ, pVelocity, pInaccuracy);
      this.life = 0;
   }

   /**
    * Sets a target for the client to interpolate towards over the next few ticks
    */
   public void lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport) {
      this.setPos(pX, pY, pZ);
      this.setRot(pYaw, pPitch);
   }

   /**
    * Updates the entity motion clientside, called by packets from the server
    */
   public void lerpMotion(double pX, double pY, double pZ) {
      super.lerpMotion(pX, pY, pZ);
      this.life = 0;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      boolean flag = this.isNoPhysics();
      Vec3 vec3 = this.getDeltaMovement();
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         double d0 = vec3.horizontalDistance();
         this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
         this.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI)));
         this.yRotO = this.getYRot();
         this.xRotO = this.getXRot();
      }

      BlockPos blockpos = this.blockPosition();
      BlockState blockstate = this.level().getBlockState(blockpos);
      if (!blockstate.isAir() && !flag) {
         VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
         if (!voxelshape.isEmpty()) {
            Vec3 vec31 = this.position();

            for(AABB aabb : voxelshape.toAabbs()) {
               if (aabb.move(blockpos).contains(vec31)) {
                  this.inGround = true;
                  break;
               }
            }
         }
      }

      if (this.shakeTime > 0) {
         --this.shakeTime;
      }

      if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW) || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType))) {
         this.clearFire();
      }

      if (this.inGround && !flag) {
         if (this.lastState != blockstate && this.shouldFall()) {
            this.startFalling();
         } else if (!this.level().isClientSide) {
            this.tickDespawn();
         }

         ++this.inGroundTime;
      } else {
         this.inGroundTime = 0;
         Vec3 vec32 = this.position();
         Vec3 vec33 = vec32.add(vec3);
         HitResult hitresult = this.level().clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
         if (hitresult.getType() != HitResult.Type.MISS) {
            vec33 = hitresult.getLocation();
         }

         while(!this.isRemoved()) {
            EntityHitResult entityhitresult = this.findHitEntity(vec32, vec33);
            if (entityhitresult != null) {
               hitresult = entityhitresult;
            }

            if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY) {
               Entity entity = ((EntityHitResult)hitresult).getEntity();
               Entity entity1 = this.getOwner();
               if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                  hitresult = null;
                  entityhitresult = null;
               }
            }

            if (hitresult != null && hitresult.getType() != HitResult.Type.MISS && !flag) {
               switch (net.minecraftforge.event.ForgeEventFactory.onProjectileImpactResult(this, hitresult)) {
                  case SKIP_ENTITY:
                     if (hitresult.getType() != HitResult.Type.ENTITY) { // If there is no entity, we just return default behaviour
                        this.onHit(hitresult);
                        this.hasImpulse = true;
                        break;
                     }
                     ignoredEntities.add(entityhitresult.getEntity().getId());
                     entityhitresult = null; // Don't process any further
                     break;
                  case STOP_AT_CURRENT_NO_DAMAGE:
                     this.discard();
                     entityhitresult = null; // Don't process any further
                     break;
                  case STOP_AT_CURRENT:
                     this.setPierceLevel((byte) 0);
                  case DEFAULT:
                     this.onHit(hitresult);
                     this.hasImpulse = true;
                     break;
               }
            }

            if (entityhitresult == null || this.getPierceLevel() <= 0) {
               break;
            }

            hitresult = null;
         }

         if (this.isRemoved())
            return;

         vec3 = this.getDeltaMovement();
         double d5 = vec3.x;
         double d6 = vec3.y;
         double d1 = vec3.z;
         if (this.isCritArrow()) {
            for(int i = 0; i < 4; ++i) {
               this.level().addParticle(ParticleTypes.CRIT, this.getX() + d5 * (double)i / 4.0D, this.getY() + d6 * (double)i / 4.0D, this.getZ() + d1 * (double)i / 4.0D, -d5, -d6 + 0.2D, -d1);
            }
         }

         double d7 = this.getX() + d5;
         double d2 = this.getY() + d6;
         double d3 = this.getZ() + d1;
         double d4 = vec3.horizontalDistance();
         if (flag) {
            this.setYRot((float)(Mth.atan2(-d5, -d1) * (double)(180F / (float)Math.PI)));
         } else {
            this.setYRot((float)(Mth.atan2(d5, d1) * (double)(180F / (float)Math.PI)));
         }

         this.setXRot((float)(Mth.atan2(d6, d4) * (double)(180F / (float)Math.PI)));
         this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
         this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
         float f = 0.99F;
         float f1 = 0.05F;
         if (this.isInWater()) {
            for(int j = 0; j < 4; ++j) {
               float f2 = 0.25F;
               this.level().addParticle(ParticleTypes.BUBBLE, d7 - d5 * 0.25D, d2 - d6 * 0.25D, d3 - d1 * 0.25D, d5, d6, d1);
            }

            f = this.getWaterInertia();
         }

         this.setDeltaMovement(vec3.scale((double)f));
         if (!this.isNoGravity() && !flag) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - (double)0.05F, vec34.z);
         }

         this.setPos(d7, d2, d3);
         this.checkInsideBlocks();
      }
   }

   private boolean shouldFall() {
      return this.inGround && this.level().noCollision((new AABB(this.position(), this.position())).inflate(0.06D));
   }

   private void startFalling() {
      this.inGround = false;
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F)));
      this.life = 0;
   }

   public void move(MoverType pType, Vec3 pPos) {
      super.move(pType, pPos);
      if (pType != MoverType.SELF && this.shouldFall()) {
         this.startFalling();
      }

   }

   protected void tickDespawn() {
      ++this.life;
      if (this.life >= 1200) {
         this.discard();
      }

   }

   private void resetPiercedEntities() {
      if (this.piercedAndKilledEntities != null) {
         this.piercedAndKilledEntities.clear();
      }

      if (this.piercingIgnoreEntityIds != null) {
         this.piercingIgnoreEntityIds.clear();
      }

   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityHitResult pResult) {
      super.onHitEntity(pResult);
      Entity entity = pResult.getEntity();
      float f = (float)this.getDeltaMovement().length();
      int i = Mth.ceil(Mth.clamp((double)f * this.baseDamage, 0.0D, (double)Integer.MAX_VALUE));
      if (this.getPierceLevel() > 0) {
         if (this.piercingIgnoreEntityIds == null) {
            this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
         }

         if (this.piercedAndKilledEntities == null) {
            this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
         }

         if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
            this.discard();
            return;
         }

         this.piercingIgnoreEntityIds.add(entity.getId());
      }

      if (this.isCritArrow()) {
         long j = (long)this.random.nextInt(i / 2 + 2);
         i = (int)Math.min(j + (long)i, 2147483647L);
      }

      Entity entity1 = this.getOwner();
      DamageSource damagesource;
      if (entity1 == null) {
         damagesource = this.damageSources().arrow(this, this);
      } else {
         damagesource = this.damageSources().arrow(this, entity1);
         if (entity1 instanceof LivingEntity) {
            ((LivingEntity)entity1).setLastHurtMob(entity);
         }
      }

      boolean flag = entity.getType() == EntityType.ENDERMAN;
      int k = entity.getRemainingFireTicks();
      if (this.isOnFire() && !flag) {
         entity.setSecondsOnFire(5);
      }

      if (entity.hurt(damagesource, (float)i)) {
         if (flag) {
            return;
         }

         if (entity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity;
            if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
               livingentity.setArrowCount(livingentity.getArrowCount() + 1);
            }

            if (this.knockback > 0) {
               double d0 = Math.max(0.0D, 1.0D - livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
               Vec3 vec3 = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)this.knockback * 0.6D * d0);
               if (vec3.lengthSqr() > 0.0D) {
                  livingentity.push(vec3.x, 0.1D, vec3.z);
               }
            }

            if (!this.level().isClientSide && entity1 instanceof LivingEntity) {
               EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
               EnchantmentHelper.doPostDamageEffects((LivingEntity)entity1, livingentity);
            }

            this.doPostHurtEffects(livingentity);
            if (entity1 != null && livingentity != entity1 && livingentity instanceof Player && entity1 instanceof ServerPlayer && !this.isSilent()) {
               ((ServerPlayer)entity1).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
            }

            if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
               this.piercedAndKilledEntities.add(livingentity);
            }

            if (!this.level().isClientSide && entity1 instanceof ServerPlayer) {
               ServerPlayer serverplayer = (ServerPlayer)entity1;
               if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                  CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, this.piercedAndKilledEntities);
               } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                  CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, Arrays.asList(entity));
               }
            }
         }

         this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
         if (this.getPierceLevel() <= 0) {
            this.discard();
         }
      } else {
         entity.setRemainingFireTicks(k);
         this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
         this.setYRot(this.getYRot() + 180.0F);
         this.yRotO += 180.0F;
         if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
            if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
               this.spawnAtLocation(this.getPickupItem(), 0.1F);
            }

            this.discard();
         }
      }

   }

   protected void onHitBlock(BlockHitResult pResult) {
      this.lastState = this.level().getBlockState(pResult.getBlockPos());
      super.onHitBlock(pResult);
      Vec3 vec3 = pResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
      this.setDeltaMovement(vec3);
      Vec3 vec31 = vec3.normalize().scale((double)0.05F);
      this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
      this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
      this.inGround = true;
      this.shakeTime = 7;
      this.setCritArrow(false);
      this.setPierceLevel((byte)0);
      this.setSoundEvent(SoundEvents.ARROW_HIT);
      this.setShotFromCrossbow(false);
      this.resetPiercedEntities();
   }

   /**
    * The sound made when an entity is hit by this projectile
    */
   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.ARROW_HIT;
   }

   protected final SoundEvent getHitGroundSoundEvent() {
      return this.soundEvent;
   }

   protected void doPostHurtEffects(LivingEntity pTarget) {
   }

   /**
    * Gets the EntityHitResult representing the entity hit
    */
   @Nullable
   protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
      return ProjectileUtil.getEntityHitResult(this.level(), this, pStartVec, pEndVec, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
   }

   protected boolean canHitEntity(Entity p_36743_) {
      return super.canHitEntity(p_36743_) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(p_36743_.getId())) && !this.ignoredEntities.contains(p_36743_.getId());
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putShort("life", (short)this.life);
      if (this.lastState != null) {
         pCompound.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
      }

      pCompound.putByte("shake", (byte)this.shakeTime);
      pCompound.putBoolean("inGround", this.inGround);
      pCompound.putByte("pickup", (byte)this.pickup.ordinal());
      pCompound.putDouble("damage", this.baseDamage);
      pCompound.putBoolean("crit", this.isCritArrow());
      pCompound.putByte("PierceLevel", this.getPierceLevel());
      pCompound.putString("SoundEvent", BuiltInRegistries.SOUND_EVENT.getKey(this.soundEvent).toString());
      pCompound.putBoolean("ShotFromCrossbow", this.shotFromCrossbow());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.life = pCompound.getShort("life");
      if (pCompound.contains("inBlockState", 10)) {
         this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), pCompound.getCompound("inBlockState"));
      }

      this.shakeTime = pCompound.getByte("shake") & 255;
      this.inGround = pCompound.getBoolean("inGround");
      if (pCompound.contains("damage", 99)) {
         this.baseDamage = pCompound.getDouble("damage");
      }

      this.pickup = AbstractArrow.Pickup.byOrdinal(pCompound.getByte("pickup"));
      this.setCritArrow(pCompound.getBoolean("crit"));
      this.setPierceLevel(pCompound.getByte("PierceLevel"));
      if (pCompound.contains("SoundEvent", 8)) {
         this.soundEvent = BuiltInRegistries.SOUND_EVENT.getOptional(new ResourceLocation(pCompound.getString("SoundEvent"))).orElse(this.getDefaultHitGroundSoundEvent());
      }

      this.setShotFromCrossbow(pCompound.getBoolean("ShotFromCrossbow"));
   }

   public void setOwner(@Nullable Entity pEntity) {
      super.setOwner(pEntity);
      if (pEntity instanceof Player) {
         this.pickup = ((Player)pEntity).getAbilities().instabuild ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
      }

   }

   /**
    * Called by a player entity when they collide with an entity
    */
   public void playerTouch(Player pEntity) {
      if (!this.level().isClientSide && (this.inGround || this.isNoPhysics()) && this.shakeTime <= 0) {
         if (this.tryPickup(pEntity)) {
            pEntity.take(this, 1);
            this.discard();
         }

      }
   }

   protected boolean tryPickup(Player pPlayer) {
      switch (this.pickup) {
         case ALLOWED:
            return pPlayer.getInventory().add(this.getPickupItem());
         case CREATIVE_ONLY:
            return pPlayer.getAbilities().instabuild;
         default:
            return false;
      }
   }

   protected abstract ItemStack getPickupItem();

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   public void setBaseDamage(double pBaseDamage) {
      this.baseDamage = pBaseDamage;
   }

   public double getBaseDamage() {
      return this.baseDamage;
   }

   /**
    * Sets the amount of knockback the arrow applies when it hits a mob.
    */
   public void setKnockback(int pKnockback) {
      this.knockback = pKnockback;
   }

   public int getKnockback() {
      return this.knockback;
   }

   /**
    * Returns {@code true} if it's possible to attack this entity with an item.
    */
   public boolean isAttackable() {
      return false;
   }

   protected float getEyeHeight(Pose pPose, EntityDimensions pSize) {
      return 0.13F;
   }

   /**
    * Whether the arrow has a stream of critical hit particles flying behind it.
    */
   public void setCritArrow(boolean pCritArrow) {
      this.setFlag(1, pCritArrow);
   }

   public void setPierceLevel(byte pPierceLevel) {
      this.entityData.set(PIERCE_LEVEL, pPierceLevel);
   }

   private void setFlag(int pId, boolean pValue) {
      byte b0 = this.entityData.get(ID_FLAGS);
      if (pValue) {
         this.entityData.set(ID_FLAGS, (byte)(b0 | pId));
      } else {
         this.entityData.set(ID_FLAGS, (byte)(b0 & ~pId));
      }

   }

   /**
    * Whether the arrow has a stream of critical hit particles flying behind it.
    */
   public boolean isCritArrow() {
      byte b0 = this.entityData.get(ID_FLAGS);
      return (b0 & 1) != 0;
   }

   /**
    * Whether the arrow was shot from a crossbow.
    */
   public boolean shotFromCrossbow() {
      byte b0 = this.entityData.get(ID_FLAGS);
      return (b0 & 4) != 0;
   }

   public byte getPierceLevel() {
      return this.entityData.get(PIERCE_LEVEL);
   }

   public void setEnchantmentEffectsFromEntity(LivingEntity pShooter, float pVelocity) {
      int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER_ARROWS, pShooter);
      int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH_ARROWS, pShooter);
      this.setBaseDamage((double)(pVelocity * 2.0F) + this.random.triangle((double)this.level().getDifficulty().getId() * 0.11D, 0.57425D));
      if (i > 0) {
         this.setBaseDamage(this.getBaseDamage() + (double)i * 0.5D + 0.5D);
      }

      if (j > 0) {
         this.setKnockback(j);
      }

      if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAMING_ARROWS, pShooter) > 0) {
         this.setSecondsOnFire(100);
      }

   }

   protected float getWaterInertia() {
      return 0.6F;
   }

   /**
    * Sets if this arrow can noClip
    */
   public void setNoPhysics(boolean pNoPhysics) {
      this.noPhysics = pNoPhysics;
      this.setFlag(2, pNoPhysics);
   }

   /**
    * Whether the arrow can noClip
    */
   public boolean isNoPhysics() {
      if (!this.level().isClientSide) {
         return this.noPhysics;
      } else {
         return (this.entityData.get(ID_FLAGS) & 2) != 0;
      }
   }

   /**
    * Sets data about if this arrow entity was shot from a crossbow
    */
   public void setShotFromCrossbow(boolean pShotFromCrossbow) {
      this.setFlag(4, pShotFromCrossbow);
   }

   public static enum Pickup {
      DISALLOWED,
      ALLOWED,
      CREATIVE_ONLY;

      public static AbstractArrow.Pickup byOrdinal(int pOrdinal) {
         if (pOrdinal < 0 || pOrdinal > values().length) {
            pOrdinal = 0;
         }

         return values()[pOrdinal];
      }
   }
}
