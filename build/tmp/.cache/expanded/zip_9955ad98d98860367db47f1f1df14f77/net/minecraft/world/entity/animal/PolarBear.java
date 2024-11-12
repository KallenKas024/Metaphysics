package net.minecraft.world.entity.animal;

import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class PolarBear extends Animal implements NeutralMob {
   private static final EntityDataAccessor<Boolean> DATA_STANDING_ID = SynchedEntityData.defineId(PolarBear.class, EntityDataSerializers.BOOLEAN);
   private static final float STAND_ANIMATION_TICKS = 6.0F;
   private float clientSideStandAnimationO;
   private float clientSideStandAnimation;
   private int warningSoundTicks;
   private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
   private int remainingPersistentAngerTime;
   @Nullable
   private UUID persistentAngerTarget;

   public PolarBear(EntityType<? extends PolarBear> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      return EntityType.POLAR_BEAR.create(pLevel);
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return false;
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new PolarBear.PolarBearMeleeAttackGoal());
      this.goalSelector.addGoal(1, new PolarBear.PolarBearPanicGoal());
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25D));
      this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new PolarBear.PolarBearHurtByTargetGoal());
      this.targetSelector.addGoal(2, new PolarBear.PolarBearAttackPlayersGoal());
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Fox.class, 10, true, true, (Predicate<LivingEntity>)null));
      this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D).add(Attributes.FOLLOW_RANGE, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_DAMAGE, 6.0D);
   }

   public static boolean checkPolarBearSpawnRules(EntityType<PolarBear> pPolarBear, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      Holder<Biome> holder = pLevel.getBiome(pPos);
      if (!holder.is(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS)) {
         return checkAnimalSpawnRules(pPolarBear, pLevel, pSpawnType, pPos, pRandom);
      } else {
         return isBrightEnoughToSpawn(pLevel, pPos) && pLevel.getBlockState(pPos.below()).is(BlockTags.POLAR_BEARS_SPAWNABLE_ON_ALTERNATE);
      }
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.readPersistentAngerSaveData(this.level(), pCompound);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      this.addPersistentAngerSaveData(pCompound);
   }

   public void startPersistentAngerTimer() {
      this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
   }

   public void setRemainingPersistentAngerTime(int pTime) {
      this.remainingPersistentAngerTime = pTime;
   }

   public int getRemainingPersistentAngerTime() {
      return this.remainingPersistentAngerTime;
   }

   public void setPersistentAngerTarget(@Nullable UUID pTarget) {
      this.persistentAngerTarget = pTarget;
   }

   @Nullable
   public UUID getPersistentAngerTarget() {
      return this.persistentAngerTarget;
   }

   protected SoundEvent getAmbientSound() {
      return this.isBaby() ? SoundEvents.POLAR_BEAR_AMBIENT_BABY : SoundEvents.POLAR_BEAR_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.POLAR_BEAR_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.POLAR_BEAR_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.POLAR_BEAR_STEP, 0.15F, 1.0F);
   }

   protected void playWarningSound() {
      if (this.warningSoundTicks <= 0) {
         this.playSound(SoundEvents.POLAR_BEAR_WARNING, 1.0F, this.getVoicePitch());
         this.warningSoundTicks = 40;
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_STANDING_ID, false);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         if (this.clientSideStandAnimation != this.clientSideStandAnimationO) {
            this.refreshDimensions();
         }

         this.clientSideStandAnimationO = this.clientSideStandAnimation;
         if (this.isStanding()) {
            this.clientSideStandAnimation = Mth.clamp(this.clientSideStandAnimation + 1.0F, 0.0F, 6.0F);
         } else {
            this.clientSideStandAnimation = Mth.clamp(this.clientSideStandAnimation - 1.0F, 0.0F, 6.0F);
         }
      }

      if (this.warningSoundTicks > 0) {
         --this.warningSoundTicks;
      }

      if (!this.level().isClientSide) {
         this.updatePersistentAnger((ServerLevel)this.level(), true);
      }

   }

   public EntityDimensions getDimensions(Pose pPose) {
      if (this.clientSideStandAnimation > 0.0F) {
         float f = this.clientSideStandAnimation / 6.0F;
         float f1 = 1.0F + f;
         return super.getDimensions(pPose).scale(1.0F, f1);
      } else {
         return super.getDimensions(pPose);
      }
   }

   public boolean doHurtTarget(Entity pEntity) {
      boolean flag = pEntity.hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
      if (flag) {
         this.doEnchantDamageEffects(this, pEntity);
      }

      return flag;
   }

   public boolean isStanding() {
      return this.entityData.get(DATA_STANDING_ID);
   }

   public void setStanding(boolean pStanding) {
      this.entityData.set(DATA_STANDING_ID, pStanding);
   }

   public float getStandingAnimationScale(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.clientSideStandAnimationO, this.clientSideStandAnimation) / 6.0F;
   }

   protected float getWaterSlowDown() {
      return 0.98F;
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      if (pSpawnData == null) {
         pSpawnData = new AgeableMob.AgeableMobGroupData(1.0F);
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   class PolarBearAttackPlayersGoal extends NearestAttackableTargetGoal<Player> {
      public PolarBearAttackPlayersGoal() {
         super(PolarBear.this, Player.class, 20, true, true, (Predicate<LivingEntity>)null);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (PolarBear.this.isBaby()) {
            return false;
         } else {
            if (super.canUse()) {
               for(PolarBear polarbear : PolarBear.this.level().getEntitiesOfClass(PolarBear.class, PolarBear.this.getBoundingBox().inflate(8.0D, 4.0D, 8.0D))) {
                  if (polarbear.isBaby()) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      protected double getFollowDistance() {
         return super.getFollowDistance() * 0.5D;
      }
   }

   class PolarBearHurtByTargetGoal extends HurtByTargetGoal {
      public PolarBearHurtByTargetGoal() {
         super(PolarBear.this);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         if (PolarBear.this.isBaby()) {
            this.alertOthers();
            this.stop();
         }

      }

      protected void alertOther(Mob pMob, LivingEntity pTarget) {
         if (pMob instanceof PolarBear && !pMob.isBaby()) {
            super.alertOther(pMob, pTarget);
         }

      }
   }

   class PolarBearMeleeAttackGoal extends MeleeAttackGoal {
      public PolarBearMeleeAttackGoal() {
         super(PolarBear.this, 1.25D, true);
      }

      protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
         double d0 = this.getAttackReachSqr(pEnemy);
         if (pDistToEnemySqr <= d0 && this.isTimeToAttack()) {
            this.resetAttackCooldown();
            this.mob.doHurtTarget(pEnemy);
            PolarBear.this.setStanding(false);
         } else if (pDistToEnemySqr <= d0 * 2.0D) {
            if (this.isTimeToAttack()) {
               PolarBear.this.setStanding(false);
               this.resetAttackCooldown();
            }

            if (this.getTicksUntilNextAttack() <= 10) {
               PolarBear.this.setStanding(true);
               PolarBear.this.playWarningSound();
            }
         } else {
            this.resetAttackCooldown();
            PolarBear.this.setStanding(false);
         }

      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         PolarBear.this.setStanding(false);
         super.stop();
      }

      protected double getAttackReachSqr(LivingEntity pAttackTarget) {
         return (double)(4.0F + pAttackTarget.getBbWidth());
      }
   }

   class PolarBearPanicGoal extends PanicGoal {
      public PolarBearPanicGoal() {
         super(PolarBear.this, 2.0D);
      }

      protected boolean shouldPanic() {
         return this.mob.getLastHurtByMob() != null && this.mob.isBaby() || this.mob.isOnFire();
      }
   }
}