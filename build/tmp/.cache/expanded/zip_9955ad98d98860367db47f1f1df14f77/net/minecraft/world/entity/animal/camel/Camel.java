package net.minecraft.world.entity.animal.camel;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RiderShieldingMount;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Camel extends AbstractHorse implements PlayerRideableJumping, RiderShieldingMount, Saddleable {
   public static final Ingredient TEMPTATION_ITEM = Ingredient.of(Items.CACTUS);
   public static final int DASH_COOLDOWN_TICKS = 55;
   public static final int MAX_HEAD_Y_ROT = 30;
   private static final float RUNNING_SPEED_BONUS = 0.1F;
   private static final float DASH_VERTICAL_MOMENTUM = 1.4285F;
   private static final float DASH_HORIZONTAL_MOMENTUM = 22.2222F;
   private static final int DASH_MINIMUM_DURATION_TICKS = 5;
   private static final int SITDOWN_DURATION_TICKS = 40;
   private static final int STANDUP_DURATION_TICKS = 52;
   private static final int IDLE_MINIMAL_DURATION_TICKS = 80;
   private static final float SITTING_HEIGHT_DIFFERENCE = 1.43F;
   public static final EntityDataAccessor<Boolean> DASH = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Long> LAST_POSE_CHANGE_TICK = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.LONG);
   public final AnimationState sitAnimationState = new AnimationState();
   public final AnimationState sitPoseAnimationState = new AnimationState();
   public final AnimationState sitUpAnimationState = new AnimationState();
   public final AnimationState idleAnimationState = new AnimationState();
   public final AnimationState dashAnimationState = new AnimationState();
   private static final EntityDimensions SITTING_DIMENSIONS = EntityDimensions.scalable(EntityType.CAMEL.getWidth(), EntityType.CAMEL.getHeight() - 1.43F);
   private int dashCooldown = 0;
   private int idleAnimationTimeout = 0;

   public Camel(EntityType<? extends Camel> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setMaxUpStep(1.5F);
      this.moveControl = new Camel.CamelMoveControl();
      GroundPathNavigation groundpathnavigation = (GroundPathNavigation)this.getNavigation();
      groundpathnavigation.setCanFloat(true);
      groundpathnavigation.setCanWalkOverFences(true);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putLong("LastPoseTick", this.entityData.get(LAST_POSE_CHANGE_TICK));
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      long i = pCompound.getLong("LastPoseTick");
      if (i < 0L) {
         this.setPose(Pose.SITTING);
      }

      this.resetLastPoseChangeTick(i);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 32.0D).add(Attributes.MOVEMENT_SPEED, (double)0.09F).add(Attributes.JUMP_STRENGTH, (double)0.42F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DASH, false);
      this.entityData.define(LAST_POSE_CHANGE_TICK, 0L);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      CamelAi.initMemories(this, pLevel.getRandom());
      this.resetLastPoseChangeTickToFullStand(pLevel.getLevel().getGameTime());
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   protected Brain.Provider<Camel> brainProvider() {
      return CamelAi.brainProvider();
   }

   protected void registerGoals() {
   }

   protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
      return CamelAi.makeBrain(this.brainProvider().makeBrain(pDynamic));
   }

   public EntityDimensions getDimensions(Pose pPose) {
      return pPose == Pose.SITTING ? SITTING_DIMENSIONS.scale(this.getScale()) : super.getDimensions(pPose);
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return pSize.height - 0.1F;
   }

   public double getRiderShieldingHeight() {
      return 0.5D;
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("camelBrain");
      Brain<?> brain = this.getBrain();
      ((Brain<Camel>)brain).tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      this.level().getProfiler().push("camelActivityUpdate");
      CamelAi.updateActivity(this);
      this.level().getProfiler().pop();
      super.customServerAiStep();
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.isDashing() && this.dashCooldown < 50 && (this.onGround() || this.isInWater() || this.isPassenger())) {
         this.setDashing(false);
      }

      if (this.dashCooldown > 0) {
         --this.dashCooldown;
         if (this.dashCooldown == 0) {
            this.level().playSound((Player)null, this.blockPosition(), SoundEvents.CAMEL_DASH_READY, SoundSource.NEUTRAL, 1.0F, 1.0F);
         }
      }

      if (this.level().isClientSide()) {
         this.setupAnimationStates();
      }

      if (this.refuseToMove()) {
         this.clampHeadRotationToBody(this, 30.0F);
      }

      if (this.isCamelSitting() && this.isInWater()) {
         this.standUpInstantly();
      }

   }

   private void setupAnimationStates() {
      if (this.idleAnimationTimeout <= 0) {
         this.idleAnimationTimeout = this.random.nextInt(40) + 80;
         this.idleAnimationState.start(this.tickCount);
      } else {
         --this.idleAnimationTimeout;
      }

      if (this.isCamelVisuallySitting()) {
         this.sitUpAnimationState.stop();
         this.dashAnimationState.stop();
         if (this.isVisuallySittingDown()) {
            this.sitAnimationState.startIfStopped(this.tickCount);
            this.sitPoseAnimationState.stop();
         } else {
            this.sitAnimationState.stop();
            this.sitPoseAnimationState.startIfStopped(this.tickCount);
         }
      } else {
         this.sitAnimationState.stop();
         this.sitPoseAnimationState.stop();
         this.dashAnimationState.animateWhen(this.isDashing(), this.tickCount);
         this.sitUpAnimationState.animateWhen(this.isInPoseTransition() && this.getPoseTime() >= 0L, this.tickCount);
      }

   }

   protected void updateWalkAnimation(float pPartialTick) {
      float f;
      if (this.getPose() == Pose.STANDING && !this.dashAnimationState.isStarted()) {
         f = Math.min(pPartialTick * 6.0F, 1.0F);
      } else {
         f = 0.0F;
      }

      this.walkAnimation.update(f, 0.2F);
   }

   public void travel(Vec3 pTravelVector) {
      if (this.refuseToMove() && this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
         pTravelVector = pTravelVector.multiply(0.0D, 1.0D, 0.0D);
      }

      super.travel(pTravelVector);
   }

   protected void tickRidden(Player pPlayer, Vec3 pTravelVector) {
      super.tickRidden(pPlayer, pTravelVector);
      if (pPlayer.zza > 0.0F && this.isCamelSitting() && !this.isInPoseTransition()) {
         this.standUp();
      }

   }

   public boolean refuseToMove() {
      return this.isCamelSitting() || this.isInPoseTransition();
   }

   protected float getRiddenSpeed(Player pPlayer) {
      float f = pPlayer.isSprinting() && this.getJumpCooldown() == 0 ? 0.1F : 0.0F;
      return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) + f;
   }

   protected Vec2 getRiddenRotation(LivingEntity pEntity) {
      return this.refuseToMove() ? new Vec2(this.getXRot(), this.getYRot()) : super.getRiddenRotation(pEntity);
   }

   protected Vec3 getRiddenInput(Player pPlayer, Vec3 pTravelVector) {
      return this.refuseToMove() ? Vec3.ZERO : super.getRiddenInput(pPlayer, pTravelVector);
   }

   public boolean canJump() {
      return !this.refuseToMove() && super.canJump();
   }

   public void onPlayerJump(int pJumpPower) {
      if (this.isSaddled() && this.dashCooldown <= 0 && this.onGround()) {
         super.onPlayerJump(pJumpPower);
      }
   }

   public boolean canSprint() {
      return true;
   }

   protected void executeRidersJump(float pPlayerJumpPendingScale, Vec3 pTravelVector) {
      double d0 = this.getAttributeValue(Attributes.JUMP_STRENGTH) * (double)this.getBlockJumpFactor() + (double)this.getJumpBoostPower();
      this.addDeltaMovement(this.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)(22.2222F * pPlayerJumpPendingScale) * this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)this.getBlockSpeedFactor()).add(0.0D, (double)(1.4285F * pPlayerJumpPendingScale) * d0, 0.0D));
      this.dashCooldown = 55;
      this.setDashing(true);
      this.hasImpulse = true;
   }

   public boolean isDashing() {
      return this.entityData.get(DASH);
   }

   public void setDashing(boolean pDashing) {
      this.entityData.set(DASH, pDashing);
   }

   public boolean isPanicking() {
      return this.getBrain().checkMemory(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_PRESENT);
   }

   public void handleStartJump(int pJumpPower) {
      this.playSound(SoundEvents.CAMEL_DASH, 1.0F, 1.0F);
      this.setDashing(true);
   }

   public void handleStopJump() {
   }

   public int getJumpCooldown() {
      return this.dashCooldown;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.CAMEL_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.CAMEL_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.CAMEL_HURT;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      if (pBlock.getSoundType(level(), pPos, this) == SoundType.SAND) {
         this.playSound(SoundEvents.CAMEL_STEP_SAND, 1.0F, 1.0F);
      } else {
         this.playSound(SoundEvents.CAMEL_STEP, 1.0F, 1.0F);
      }

   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return TEMPTATION_ITEM.test(pStack);
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (pPlayer.isSecondaryUseActive() && !this.isBaby()) {
         this.openCustomInventoryScreen(pPlayer);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         InteractionResult interactionresult = itemstack.interactLivingEntity(pPlayer, this, pHand);
         if (interactionresult.consumesAction()) {
            return interactionresult;
         } else if (this.isFood(itemstack)) {
            return this.fedFood(pPlayer, itemstack);
         } else {
            if (this.getPassengers().size() < 2 && !this.isBaby()) {
               this.doPlayerRide(pPlayer);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
      }
   }

   protected void onLeashDistance(float pDistance) {
      if (pDistance > 6.0F && this.isCamelSitting() && !this.isInPoseTransition()) {
         this.standUp();
      }

   }

   protected boolean handleEating(Player pPlayer, ItemStack pStack) {
      if (!this.isFood(pStack)) {
         return false;
      } else {
         boolean flag = this.getHealth() < this.getMaxHealth();
         if (flag) {
            this.heal(2.0F);
         }

         boolean flag1 = this.isTamed() && this.getAge() == 0 && this.canFallInLove();
         if (flag1) {
            this.setInLove(pPlayer);
         }

         boolean flag2 = this.isBaby();
         if (flag2) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
            if (!this.level().isClientSide) {
               this.ageUp(10);
            }
         }

         if (!flag && !flag1 && !flag2) {
            return false;
         } else {
            if (!this.isSilent()) {
               SoundEvent soundevent = this.getEatingSound();
               if (soundevent != null) {
                  this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), soundevent, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
               }
            }

            return true;
         }
      }
   }

   protected boolean canPerformRearing() {
      return false;
   }

   /**
    * Returns {@code true} if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(Animal pOtherAnimal) {
      if (pOtherAnimal != this && pOtherAnimal instanceof Camel camel) {
         if (this.canParent() && camel.canParent()) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public Camel getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      return EntityType.CAMEL.create(pLevel);
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return SoundEvents.CAMEL_EAT;
   }

   /**
    * Deals damage to the entity. This will take the armor of the entity into consideration before damaging the health
    * bar.
    */
   protected void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
      this.standUpInstantly();
      super.actuallyHurt(pDamageSource, pDamageAmount);
   }

   protected void positionRider(Entity pPassenger, Entity.MoveFunction pCallback) {
      int i = this.getPassengers().indexOf(pPassenger);
      if (i >= 0) {
         boolean flag = i == 0;
         float f = 0.5F;
         float f1 = (float)(this.isRemoved() ? (double)0.01F : this.getBodyAnchorAnimationYOffset(flag, 0.0F) + pPassenger.getMyRidingOffset());
         if (this.getPassengers().size() > 1) {
            if (!flag) {
               f = -0.7F;
            }

            if (pPassenger instanceof Animal) {
               f += 0.2F;
            }
         }

         Vec3 vec3 = (new Vec3(0.0D, 0.0D, (double)f)).yRot(-this.yBodyRot * ((float)Math.PI / 180F));
         pCallback.accept(pPassenger, this.getX() + vec3.x, this.getY() + (double)f1, this.getZ() + vec3.z);
         this.clampRotation(pPassenger);
      }
   }

   private double getBodyAnchorAnimationYOffset(boolean pSitting, float pPartialTick) {
      double d0 = this.getPassengersRidingOffset();
      float f = this.getScale() * 1.43F;
      float f1 = f - this.getScale() * 0.2F;
      float f2 = f - f1;
      boolean flag = this.isInPoseTransition();
      boolean flag1 = this.isCamelSitting();
      if (flag) {
         int i = flag1 ? 40 : 52;
         int j;
         float f3;
         if (flag1) {
            j = 28;
            f3 = pSitting ? 0.5F : 0.1F;
         } else {
            j = pSitting ? 24 : 32;
            f3 = pSitting ? 0.6F : 0.35F;
         }

         float f4 = Mth.clamp((float)this.getPoseTime() + pPartialTick, 0.0F, (float)i);
         boolean flag2 = f4 < (float)j;
         float f5 = flag2 ? f4 / (float)j : (f4 - (float)j) / (float)(i - j);
         float f6 = f - f3 * f1;
         d0 += flag1 ? (double)Mth.lerp(f5, flag2 ? f : f6, flag2 ? f6 : f2) : (double)Mth.lerp(f5, flag2 ? f2 - f : f2 - f6, flag2 ? f2 - f6 : 0.0F);
      }

      if (flag1 && !flag) {
         d0 += (double)f2;
      }

      return d0;
   }

   public Vec3 getLeashOffset(float pPartialTick) {
      return new Vec3(0.0D, this.getBodyAnchorAnimationYOffset(true, pPartialTick) - (double)(0.2F * this.getScale()), (double)(this.getBbWidth() * 0.56F));
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return (double)(this.getDimensions(this.isCamelSitting() ? Pose.SITTING : Pose.STANDING).height - (this.isBaby() ? 0.35F : 0.6F));
   }

   /**
    * Applies this entity's orientation to another entity. Used to update passenger orientation.
    */
   public void onPassengerTurned(Entity pEntityToUpdate) {
      if (this.getControllingPassenger() != pEntityToUpdate) {
         this.clampRotation(pEntityToUpdate);
      }

   }

   private void clampRotation(Entity pEntity) {
      pEntity.setYBodyRot(this.getYRot());
      float f = pEntity.getYRot();
      float f1 = Mth.wrapDegrees(f - this.getYRot());
      float f2 = Mth.clamp(f1, -160.0F, 160.0F);
      pEntity.yRotO += f2 - f1;
      float f3 = f + f2 - f1;
      pEntity.setYRot(f3);
      pEntity.setYHeadRot(f3);
   }

   private void clampHeadRotationToBody(Entity pEntity, float p_265541_) {
      float f = pEntity.getYHeadRot();
      float f1 = Mth.wrapDegrees(this.yBodyRot - f);
      float f2 = Mth.clamp(Mth.wrapDegrees(this.yBodyRot - f), -p_265541_, p_265541_);
      float f3 = f + f1 - f2;
      pEntity.setYHeadRot(f3);
   }

   public int getMaxHeadYRot() {
      return 30;
   }

   protected boolean canAddPassenger(Entity pPassenger) {
      return this.getPassengers().size() <= 2;
   }

   /**
    * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
    * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
    */
   @Nullable
   public LivingEntity getControllingPassenger() {
      if (!this.getPassengers().isEmpty() && this.isSaddled()) {
         Entity entity = this.getPassengers().get(0);
         if (entity instanceof LivingEntity) {
            return (LivingEntity)entity;
         }
      }

      return null;
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   public boolean isCamelSitting() {
      return this.entityData.get(LAST_POSE_CHANGE_TICK) < 0L;
   }

   public boolean isCamelVisuallySitting() {
      return this.getPoseTime() < 0L != this.isCamelSitting();
   }

   public boolean isInPoseTransition() {
      long i = this.getPoseTime();
      return i < (long)(this.isCamelSitting() ? 40 : 52);
   }

   private boolean isVisuallySittingDown() {
      return this.isCamelSitting() && this.getPoseTime() < 40L && this.getPoseTime() >= 0L;
   }

   public void sitDown() {
      if (!this.isCamelSitting()) {
         this.playSound(SoundEvents.CAMEL_SIT, 1.0F, 1.0F);
         this.setPose(Pose.SITTING);
         this.resetLastPoseChangeTick(-this.level().getGameTime());
      }
   }

   public void standUp() {
      if (this.isCamelSitting()) {
         this.playSound(SoundEvents.CAMEL_STAND, 1.0F, 1.0F);
         this.setPose(Pose.STANDING);
         this.resetLastPoseChangeTick(this.level().getGameTime());
      }
   }

   public void standUpInstantly() {
      this.setPose(Pose.STANDING);
      this.resetLastPoseChangeTickToFullStand(this.level().getGameTime());
   }

   @VisibleForTesting
   public void resetLastPoseChangeTick(long pLastPoseChangeTick) {
      this.entityData.set(LAST_POSE_CHANGE_TICK, pLastPoseChangeTick);
   }

   private void resetLastPoseChangeTickToFullStand(long pLastPoseChangedTick) {
      this.resetLastPoseChangeTick(Math.max(0L, pLastPoseChangedTick - 52L - 1L));
   }

   public long getPoseTime() {
      return this.level().getGameTime() - Math.abs(this.entityData.get(LAST_POSE_CHANGE_TICK));
   }

   public SoundEvent getSaddleSoundEvent() {
      return SoundEvents.CAMEL_SADDLE;
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      if (!this.firstTick && DASH.equals(pKey)) {
         this.dashCooldown = this.dashCooldown == 0 ? 55 : this.dashCooldown;
      }

      super.onSyncedDataUpdated(pKey);
   }

   protected BodyRotationControl createBodyControl() {
      return new Camel.CamelBodyRotationControl(this);
   }

   public boolean isTamed() {
      return true;
   }

   public void openCustomInventoryScreen(Player pPlayer) {
      if (!this.level().isClientSide) {
         pPlayer.openHorseInventory(this, this.inventory);
      }

   }

   class CamelBodyRotationControl extends BodyRotationControl {
      public CamelBodyRotationControl(Camel pCamel) {
         super(pCamel);
      }

      /**
       * Update the Head and Body rendering angles
       */
      public void clientTick() {
         if (!Camel.this.refuseToMove()) {
            super.clientTick();
         }

      }
   }

   class CamelMoveControl extends MoveControl {
      public CamelMoveControl() {
         super(Camel.this);
      }

      public void tick() {
         if (this.operation == MoveControl.Operation.MOVE_TO && !Camel.this.isLeashed() && Camel.this.isCamelSitting() && !Camel.this.isInPoseTransition()) {
            Camel.this.standUp();
         }

         super.tick();
      }
   }
}
