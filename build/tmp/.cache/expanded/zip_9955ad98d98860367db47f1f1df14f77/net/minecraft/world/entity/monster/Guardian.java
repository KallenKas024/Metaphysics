package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Guardian extends Monster {
   protected static final int ATTACK_TIME = 80;
   private static final EntityDataAccessor<Boolean> DATA_ID_MOVING = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData.defineId(Guardian.class, EntityDataSerializers.INT);
   private float clientSideTailAnimation;
   private float clientSideTailAnimationO;
   private float clientSideTailAnimationSpeed;
   private float clientSideSpikesAnimation;
   private float clientSideSpikesAnimationO;
   @Nullable
   private LivingEntity clientSideCachedAttackTarget;
   private int clientSideAttackTime;
   private boolean clientSideTouchedGround;
   @Nullable
   protected RandomStrollGoal randomStrollGoal;

   public Guardian(EntityType<? extends Guardian> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.xpReward = 10;
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
      this.moveControl = new Guardian.GuardianMoveControl(this);
      this.clientSideTailAnimation = this.random.nextFloat();
      this.clientSideTailAnimationO = this.clientSideTailAnimation;
   }

   protected void registerGoals() {
      MoveTowardsRestrictionGoal movetowardsrestrictiongoal = new MoveTowardsRestrictionGoal(this, 1.0D);
      this.randomStrollGoal = new RandomStrollGoal(this, 1.0D, 80);
      this.goalSelector.addGoal(4, new Guardian.GuardianAttackGoal(this));
      this.goalSelector.addGoal(5, movetowardsrestrictiongoal);
      this.goalSelector.addGoal(7, this.randomStrollGoal);
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Guardian.class, 12.0F, 0.01F));
      this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
      this.randomStrollGoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      movetowardsrestrictiongoal.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, new Guardian.GuardianAttackSelector(this)));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.MOVEMENT_SPEED, 0.5D).add(Attributes.FOLLOW_RANGE, 16.0D).add(Attributes.MAX_HEALTH, 30.0D);
   }

   protected PathNavigation createNavigation(Level pLevel) {
      return new WaterBoundPathNavigation(this, pLevel);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_MOVING, false);
      this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   public MobType getMobType() {
      return MobType.WATER;
   }

   public boolean isMoving() {
      return this.entityData.get(DATA_ID_MOVING);
   }

   void setMoving(boolean pMoving) {
      this.entityData.set(DATA_ID_MOVING, pMoving);
   }

   public int getAttackDuration() {
      return 80;
   }

   void setActiveAttackTarget(int pActiveAttackTargetId) {
      this.entityData.set(DATA_ID_ATTACK_TARGET, pActiveAttackTargetId);
   }

   public boolean hasActiveAttackTarget() {
      return this.entityData.get(DATA_ID_ATTACK_TARGET) != 0;
   }

   @Nullable
   public LivingEntity getActiveAttackTarget() {
      if (!this.hasActiveAttackTarget()) {
         return null;
      } else if (this.level().isClientSide) {
         if (this.clientSideCachedAttackTarget != null) {
            return this.clientSideCachedAttackTarget;
         } else {
            Entity entity = this.level().getEntity(this.entityData.get(DATA_ID_ATTACK_TARGET));
            if (entity instanceof LivingEntity) {
               this.clientSideCachedAttackTarget = (LivingEntity)entity;
               return this.clientSideCachedAttackTarget;
            } else {
               return null;
            }
         }
      } else {
         return this.getTarget();
      }
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (DATA_ID_ATTACK_TARGET.equals(pKey)) {
         this.clientSideAttackTime = 0;
         this.clientSideCachedAttackTarget = null;
      }

   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 160;
   }

   protected SoundEvent getAmbientSound() {
      return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_AMBIENT : SoundEvents.GUARDIAN_AMBIENT_LAND;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_HURT : SoundEvents.GUARDIAN_HURT_LAND;
   }

   protected SoundEvent getDeathSound() {
      return this.isInWaterOrBubble() ? SoundEvents.GUARDIAN_DEATH : SoundEvents.GUARDIAN_DEATH_LAND;
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.EVENTS;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return pSize.height * 0.5F;
   }

   public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
      return pLevel.getFluidState(pPos).is(FluidTags.WATER) ? 10.0F + pLevel.getPathfindingCostFromLightLevels(pPos) : super.getWalkTargetValue(pPos, pLevel);
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.isAlive()) {
         if (this.level().isClientSide) {
            this.clientSideTailAnimationO = this.clientSideTailAnimation;
            if (!this.isInWater()) {
               this.clientSideTailAnimationSpeed = 2.0F;
               Vec3 vec3 = this.getDeltaMovement();
               if (vec3.y > 0.0D && this.clientSideTouchedGround && !this.isSilent()) {
                  this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getFlopSound(), this.getSoundSource(), 1.0F, 1.0F, false);
               }

               this.clientSideTouchedGround = vec3.y < 0.0D && this.level().loadedAndEntityCanStandOn(this.blockPosition().below(), this);
            } else if (this.isMoving()) {
               if (this.clientSideTailAnimationSpeed < 0.5F) {
                  this.clientSideTailAnimationSpeed = 4.0F;
               } else {
                  this.clientSideTailAnimationSpeed += (0.5F - this.clientSideTailAnimationSpeed) * 0.1F;
               }
            } else {
               this.clientSideTailAnimationSpeed += (0.125F - this.clientSideTailAnimationSpeed) * 0.2F;
            }

            this.clientSideTailAnimation += this.clientSideTailAnimationSpeed;
            this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
            if (!this.isInWaterOrBubble()) {
               this.clientSideSpikesAnimation = this.random.nextFloat();
            } else if (this.isMoving()) {
               this.clientSideSpikesAnimation += (0.0F - this.clientSideSpikesAnimation) * 0.25F;
            } else {
               this.clientSideSpikesAnimation += (1.0F - this.clientSideSpikesAnimation) * 0.06F;
            }

            if (this.isMoving() && this.isInWater()) {
               Vec3 vec31 = this.getViewVector(0.0F);

               for(int i = 0; i < 2; ++i) {
                  this.level().addParticle(ParticleTypes.BUBBLE, this.getRandomX(0.5D) - vec31.x * 1.5D, this.getRandomY() - vec31.y * 1.5D, this.getRandomZ(0.5D) - vec31.z * 1.5D, 0.0D, 0.0D, 0.0D);
               }
            }

            if (this.hasActiveAttackTarget()) {
               if (this.clientSideAttackTime < this.getAttackDuration()) {
                  ++this.clientSideAttackTime;
               }

               LivingEntity livingentity = this.getActiveAttackTarget();
               if (livingentity != null) {
                  this.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
                  this.getLookControl().tick();
                  double d5 = (double)this.getAttackAnimationScale(0.0F);
                  double d0 = livingentity.getX() - this.getX();
                  double d1 = livingentity.getY(0.5D) - this.getEyeY();
                  double d2 = livingentity.getZ() - this.getZ();
                  double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                  d0 /= d3;
                  d1 /= d3;
                  d2 /= d3;
                  double d4 = this.random.nextDouble();

                  while(d4 < d3) {
                     d4 += 1.8D - d5 + this.random.nextDouble() * (1.7D - d5);
                     this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + d0 * d4, this.getEyeY() + d1 * d4, this.getZ() + d2 * d4, 0.0D, 0.0D, 0.0D);
                  }
               }
            }
         }

         if (this.isInWaterOrBubble()) {
            this.setAirSupply(300);
         } else if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F), 0.5D, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.4F)));
            this.setYRot(this.random.nextFloat() * 360.0F);
            this.setOnGround(false);
            this.hasImpulse = true;
         }

         if (this.hasActiveAttackTarget()) {
            this.setYRot(this.yHeadRot);
         }
      }

      super.aiStep();
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.GUARDIAN_FLOP;
   }

   public float getTailAnimation(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.clientSideTailAnimationO, this.clientSideTailAnimation);
   }

   public float getSpikesAnimation(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.clientSideSpikesAnimationO, this.clientSideSpikesAnimation);
   }

   public float getAttackAnimationScale(float pPartialTick) {
      return ((float)this.clientSideAttackTime + pPartialTick) / (float)this.getAttackDuration();
   }

   public float getClientSideAttackTime() {
      return (float)this.clientSideAttackTime;
   }

   public boolean checkSpawnObstruction(LevelReader pLevel) {
      return pLevel.isUnobstructed(this);
   }

   public static boolean checkGuardianSpawnRules(EntityType<? extends Guardian> pGuardian, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return (pRandom.nextInt(20) == 0 || !pLevel.canSeeSkyFromBelowWater(pPos)) && pLevel.getDifficulty() != Difficulty.PEACEFUL && (pSpawnType == MobSpawnType.SPAWNER || pLevel.getFluidState(pPos).is(FluidTags.WATER)) && pLevel.getFluidState(pPos.below()).is(FluidTags.WATER);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.level().isClientSide) {
         return false;
      } else {
         if (!this.isMoving() && !pSource.is(DamageTypeTags.AVOIDS_GUARDIAN_THORNS) && !pSource.is(DamageTypes.THORNS)) {
            Entity entity = pSource.getDirectEntity();
            if (entity instanceof LivingEntity) {
               LivingEntity livingentity = (LivingEntity)entity;
               livingentity.hurt(this.damageSources().thorns(this), 2.0F);
            }
         }

         if (this.randomStrollGoal != null) {
            this.randomStrollGoal.trigger();
         }

         return super.hurt(pSource, pAmount);
      }
   }

   /**
    * The speed it takes to move the entity's head rotation through the faceEntity method.
    */
   public int getMaxHeadXRot() {
      return 180;
   }

   public void travel(Vec3 pTravelVector) {
      if (this.isControlledByLocalInstance() && this.isInWater()) {
         this.moveRelative(0.1F, pTravelVector);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
         if (!this.isMoving() && this.getTarget() == null) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
         }
      } else {
         super.travel(pTravelVector);
      }

   }

   static class GuardianAttackGoal extends Goal {
      private final Guardian guardian;
      private int attackTime;
      private final boolean elder;

      public GuardianAttackGoal(Guardian pGuardian) {
         this.guardian = pGuardian;
         this.elder = pGuardian instanceof ElderGuardian;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         LivingEntity livingentity = this.guardian.getTarget();
         return livingentity != null && livingentity.isAlive();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return super.canContinueToUse() && (this.elder || this.guardian.getTarget() != null && this.guardian.distanceToSqr(this.guardian.getTarget()) > 9.0D);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.attackTime = -10;
         this.guardian.getNavigation().stop();
         LivingEntity livingentity = this.guardian.getTarget();
         if (livingentity != null) {
            this.guardian.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
         }

         this.guardian.hasImpulse = true;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.guardian.setActiveAttackTarget(0);
         this.guardian.setTarget((LivingEntity)null);
         this.guardian.randomStrollGoal.trigger();
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         LivingEntity livingentity = this.guardian.getTarget();
         if (livingentity != null) {
            this.guardian.getNavigation().stop();
            this.guardian.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
            if (!this.guardian.hasLineOfSight(livingentity)) {
               this.guardian.setTarget((LivingEntity)null);
            } else {
               ++this.attackTime;
               if (this.attackTime == 0) {
                  this.guardian.setActiveAttackTarget(livingentity.getId());
                  if (!this.guardian.isSilent()) {
                     this.guardian.level().broadcastEntityEvent(this.guardian, (byte)21);
                  }
               } else if (this.attackTime >= this.guardian.getAttackDuration()) {
                  float f = 1.0F;
                  if (this.guardian.level().getDifficulty() == Difficulty.HARD) {
                     f += 2.0F;
                  }

                  if (this.elder) {
                     f += 2.0F;
                  }

                  livingentity.hurt(this.guardian.damageSources().indirectMagic(this.guardian, this.guardian), f);
                  livingentity.hurt(this.guardian.damageSources().mobAttack(this.guardian), (float)this.guardian.getAttributeValue(Attributes.ATTACK_DAMAGE));
                  this.guardian.setTarget((LivingEntity)null);
               }

               super.tick();
            }
         }
      }
   }

   static class GuardianAttackSelector implements Predicate<LivingEntity> {
      private final Guardian guardian;

      public GuardianAttackSelector(Guardian pGuardian) {
         this.guardian = pGuardian;
      }

      public boolean test(@Nullable LivingEntity pEntity) {
         return (pEntity instanceof Player || pEntity instanceof Squid || pEntity instanceof Axolotl) && pEntity.distanceToSqr(this.guardian) > 9.0D;
      }
   }

   static class GuardianMoveControl extends MoveControl {
      private final Guardian guardian;

      public GuardianMoveControl(Guardian pGuardian) {
         super(pGuardian);
         this.guardian = pGuardian;
      }

      public void tick() {
         if (this.operation == MoveControl.Operation.MOVE_TO && !this.guardian.getNavigation().isDone()) {
            Vec3 vec3 = new Vec3(this.wantedX - this.guardian.getX(), this.wantedY - this.guardian.getY(), this.wantedZ - this.guardian.getZ());
            double d0 = vec3.length();
            double d1 = vec3.x / d0;
            double d2 = vec3.y / d0;
            double d3 = vec3.z / d0;
            float f = (float)(Mth.atan2(vec3.z, vec3.x) * (double)(180F / (float)Math.PI)) - 90.0F;
            this.guardian.setYRot(this.rotlerp(this.guardian.getYRot(), f, 90.0F));
            this.guardian.yBodyRot = this.guardian.getYRot();
            float f1 = (float)(this.speedModifier * this.guardian.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float f2 = Mth.lerp(0.125F, this.guardian.getSpeed(), f1);
            this.guardian.setSpeed(f2);
            double d4 = Math.sin((double)(this.guardian.tickCount + this.guardian.getId()) * 0.5D) * 0.05D;
            double d5 = Math.cos((double)(this.guardian.getYRot() * ((float)Math.PI / 180F)));
            double d6 = Math.sin((double)(this.guardian.getYRot() * ((float)Math.PI / 180F)));
            double d7 = Math.sin((double)(this.guardian.tickCount + this.guardian.getId()) * 0.75D) * 0.05D;
            this.guardian.setDeltaMovement(this.guardian.getDeltaMovement().add(d4 * d5, d7 * (d6 + d5) * 0.25D + (double)f2 * d2 * 0.1D, d4 * d6));
            LookControl lookcontrol = this.guardian.getLookControl();
            double d8 = this.guardian.getX() + d1 * 2.0D;
            double d9 = this.guardian.getEyeY() + d2 / d0;
            double d10 = this.guardian.getZ() + d3 * 2.0D;
            double d11 = lookcontrol.getWantedX();
            double d12 = lookcontrol.getWantedY();
            double d13 = lookcontrol.getWantedZ();
            if (!lookcontrol.isLookingAtTarget()) {
               d11 = d8;
               d12 = d9;
               d13 = d10;
            }

            this.guardian.getLookControl().setLookAt(Mth.lerp(0.125D, d11, d8), Mth.lerp(0.125D, d12, d9), Mth.lerp(0.125D, d13, d10), 10.0F, 40.0F);
            this.guardian.setMoving(true);
         } else {
            this.guardian.setSpeed(0.0F);
            this.guardian.setMoving(false);
         }
      }
   }
}