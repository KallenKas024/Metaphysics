package net.minecraft.world.entity.ambient;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Bat extends AmbientCreature {
   public static final float FLAP_DEGREES_PER_TICK = 74.48451F;
   public static final int TICKS_PER_FLAP = Mth.ceil(2.4166098F);
   private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(Bat.class, EntityDataSerializers.BYTE);
   private static final int FLAG_RESTING = 1;
   private static final TargetingConditions BAT_RESTING_TARGETING = TargetingConditions.forNonCombat().range(4.0D);
   @Nullable
   private BlockPos targetPosition;

   public Bat(EntityType<? extends Bat> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      if (!pLevel.isClientSide) {
         this.setResting(true);
      }

   }

   public boolean isFlapping() {
      return !this.isResting() && this.tickCount % TICKS_PER_FLAP == 0;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_FLAGS, (byte)0);
   }

   /**
    * Returns the volume for the sounds this mob makes.
    */
   protected float getSoundVolume() {
      return 0.1F;
   }

   /**
    * Gets the pitch of living sounds in living entities.
    */
   public float getVoicePitch() {
      return super.getVoicePitch() * 0.95F;
   }

   @Nullable
   public SoundEvent getAmbientSound() {
      return this.isResting() && this.random.nextInt(4) != 0 ? null : SoundEvents.BAT_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.BAT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.BAT_DEATH;
   }

   /**
    * Returns {@code true} if this entity should push and be pushed by other entities when colliding.
    */
   public boolean isPushable() {
      return false;
   }

   protected void doPush(Entity pEntity) {
   }

   protected void pushEntities() {
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0D);
   }

   public boolean isResting() {
      return (this.entityData.get(DATA_ID_FLAGS) & 1) != 0;
   }

   public void setResting(boolean pIsResting) {
      byte b0 = this.entityData.get(DATA_ID_FLAGS);
      if (pIsResting) {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 | 1));
      } else {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 & -2));
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.isResting()) {
         this.setDeltaMovement(Vec3.ZERO);
         this.setPosRaw(this.getX(), (double)Mth.floor(this.getY()) + 1.0D - (double)this.getBbHeight(), this.getZ());
      } else {
         this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
      }

   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      BlockPos blockpos = this.blockPosition();
      BlockPos blockpos1 = blockpos.above();
      if (this.isResting()) {
         boolean flag = this.isSilent();
         if (this.level().getBlockState(blockpos1).isRedstoneConductor(this.level(), blockpos)) {
            if (this.random.nextInt(200) == 0) {
               this.yHeadRot = (float)this.random.nextInt(360);
            }

            if (this.level().getNearestPlayer(BAT_RESTING_TARGETING, this) != null) {
               this.setResting(false);
               if (!flag) {
                  this.level().levelEvent((Player)null, 1025, blockpos, 0);
               }
            }
         } else {
            this.setResting(false);
            if (!flag) {
               this.level().levelEvent((Player)null, 1025, blockpos, 0);
            }
         }
      } else {
         if (this.targetPosition != null && (!this.level().isEmptyBlock(this.targetPosition) || this.targetPosition.getY() <= this.level().getMinBuildHeight())) {
            this.targetPosition = null;
         }

         if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerToCenterThan(this.position(), 2.0D)) {
            this.targetPosition = BlockPos.containing(this.getX() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7), this.getY() + (double)this.random.nextInt(6) - 2.0D, this.getZ() + (double)this.random.nextInt(7) - (double)this.random.nextInt(7));
         }

         double d2 = (double)this.targetPosition.getX() + 0.5D - this.getX();
         double d0 = (double)this.targetPosition.getY() + 0.1D - this.getY();
         double d1 = (double)this.targetPosition.getZ() + 0.5D - this.getZ();
         Vec3 vec3 = this.getDeltaMovement();
         Vec3 vec31 = vec3.add((Math.signum(d2) * 0.5D - vec3.x) * (double)0.1F, (Math.signum(d0) * (double)0.7F - vec3.y) * (double)0.1F, (Math.signum(d1) * 0.5D - vec3.z) * (double)0.1F);
         this.setDeltaMovement(vec31);
         float f = (float)(Mth.atan2(vec31.z, vec31.x) * (double)(180F / (float)Math.PI)) - 90.0F;
         float f1 = Mth.wrapDegrees(f - this.getYRot());
         this.zza = 0.5F;
         this.setYRot(this.getYRot() + f1);
         if (this.random.nextInt(100) == 0 && this.level().getBlockState(blockpos1).isRedstoneConductor(this.level(), blockpos1)) {
            this.setResting(true);
         }
      }

   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.EVENTS;
   }

   protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
   }

   /**
    * Return whether this entity should NOT trigger a pressure plate or a tripwire.
    */
   public boolean isIgnoringBlockTriggers() {
      return true;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         if (!this.level().isClientSide && this.isResting()) {
            this.setResting(false);
         }

         return super.hurt(pSource, pAmount);
      }
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.entityData.set(DATA_ID_FLAGS, pCompound.getByte("BatFlags"));
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putByte("BatFlags", this.entityData.get(DATA_ID_FLAGS));
   }

   public static boolean checkBatSpawnRules(EntityType<Bat> pBat, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      if (pPos.getY() >= pLevel.getSeaLevel()) {
         return false;
      } else {
         int i = pLevel.getMaxLocalRawBrightness(pPos);
         int j = 4;
         if (isHalloween()) {
            j = 7;
         } else if (pRandom.nextBoolean()) {
            return false;
         }

         return i > pRandom.nextInt(j) ? false : checkMobSpawnRules(pBat, pLevel, pSpawnType, pPos, pRandom);
      }
   }

   private static boolean isHalloween() {
      LocalDate localdate = LocalDate.now();
      int i = localdate.get(ChronoField.DAY_OF_MONTH);
      int j = localdate.get(ChronoField.MONTH_OF_YEAR);
      return j == 10 && i >= 20 || j == 11 && i <= 3;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return pSize.height / 2.0F;
   }
}