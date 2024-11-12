package net.minecraft.world.entity.monster;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Zoglin extends Monster implements Enemy, HoglinBase {
   private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zoglin.class, EntityDataSerializers.BOOLEAN);
   private static final int MAX_HEALTH = 40;
   private static final int ATTACK_KNOCKBACK = 1;
   private static final float KNOCKBACK_RESISTANCE = 0.6F;
   private static final int ATTACK_DAMAGE = 6;
   private static final float BABY_ATTACK_DAMAGE = 0.5F;
   private static final int ATTACK_INTERVAL = 40;
   private static final int BABY_ATTACK_INTERVAL = 15;
   private static final int ATTACK_DURATION = 200;
   private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.4F;
   private int attackAnimationRemainingTicks;
   protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Zoglin>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
   protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);

   public Zoglin(EntityType<? extends Zoglin> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.xpReward = 5;
   }

   protected Brain.Provider<Zoglin> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
      Brain<Zoglin> brain = this.brainProvider().makeBrain(pDynamic);
      initCoreActivity(brain);
      initIdleActivity(brain);
      initFightActivity(brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.useDefaultActivity();
      return brain;
   }

   private static void initCoreActivity(Brain<Zoglin> pBrain) {
      pBrain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink()));
   }

   private static void initIdleActivity(Brain<Zoglin> pBrain) {
      pBrain.addActivity(Activity.IDLE, 10, ImmutableList.of(StartAttacking.create(Zoglin::findNearestValidAttackTarget), SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)), new RunOne<>(ImmutableList.of(Pair.of(RandomStroll.stroll(0.4F), 2), Pair.of(SetWalkTargetFromLookTarget.create(0.4F, 3), 2), Pair.of(new DoNothing(30, 60), 1)))));
   }

   private static void initFightActivity(Brain<Zoglin> pBrain) {
      pBrain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F), BehaviorBuilder.triggerIf(Zoglin::isAdult, MeleeAttack.create(40)), BehaviorBuilder.triggerIf(Zoglin::isBaby, MeleeAttack.create(15)), StopAttackingIfTargetInvalid.create()), MemoryModuleType.ATTACK_TARGET);
   }

   private Optional<? extends LivingEntity> findNearestValidAttackTarget() {
      return this.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty()).findClosest(this::isTargetable);
   }

   private boolean isTargetable(LivingEntity p_34253_) {
      EntityType<?> entitytype = p_34253_.getType();
      return entitytype != EntityType.ZOGLIN && entitytype != EntityType.CREEPER && Sensor.isEntityAttackable(this, p_34253_);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_BABY_ID, false);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (DATA_BABY_ID.equals(pKey)) {
         this.refreshDimensions();
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.KNOCKBACK_RESISTANCE, (double)0.6F).add(Attributes.ATTACK_KNOCKBACK, 1.0D).add(Attributes.ATTACK_DAMAGE, 6.0D);
   }

   public boolean isAdult() {
      return !this.isBaby();
   }

   public boolean doHurtTarget(Entity pEntity) {
      if (!(pEntity instanceof LivingEntity)) {
         return false;
      } else {
         this.attackAnimationRemainingTicks = 10;
         this.level().broadcastEntityEvent(this, (byte)4);
         this.playSound(SoundEvents.ZOGLIN_ATTACK, 1.0F, this.getVoicePitch());
         return HoglinBase.hurtAndThrowTarget(this, (LivingEntity)pEntity);
      }
   }

   public boolean canBeLeashed(Player pPlayer) {
      return !this.isLeashed();
   }

   protected void blockedByShield(LivingEntity pEntity) {
      if (!this.isBaby()) {
         HoglinBase.throwTarget(this, pEntity);
      }

   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return (double)this.getBbHeight() - (this.isBaby() ? 0.2D : 0.15D);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      boolean flag = super.hurt(pSource, pAmount);
      if (this.level().isClientSide) {
         return false;
      } else if (flag && pSource.getEntity() instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)pSource.getEntity();
         if (this.canAttack(livingentity) && !BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(this, livingentity, 4.0D)) {
            this.setAttackTarget(livingentity);
         }

         return flag;
      } else {
         return flag;
      }
   }

   private void setAttackTarget(LivingEntity pTarget) {
      this.brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      this.brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, pTarget, 200L);
   }

   public Brain<Zoglin> getBrain() {
      return (Brain<Zoglin>) super.getBrain();
   }

   protected void updateActivity() {
      Activity activity = this.brain.getActiveNonCoreActivity().orElse((Activity)null);
      this.brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
      Activity activity1 = this.brain.getActiveNonCoreActivity().orElse((Activity)null);
      if (activity1 == Activity.FIGHT && activity != Activity.FIGHT) {
         this.playAngrySound();
      }

      this.setAggressive(this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("zoglinBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      this.updateActivity();
   }

   /**
    * Set whether this zombie is a child.
    */
   public void setBaby(boolean pChildZombie) {
      this.getEntityData().set(DATA_BABY_ID, pChildZombie);
      if (!this.level().isClientSide && pChildZombie) {
         this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.5D);
      }

   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return this.getEntityData().get(DATA_BABY_ID);
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.attackAnimationRemainingTicks > 0) {
         --this.attackAnimationRemainingTicks;
      }

      super.aiStep();
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 4) {
         this.attackAnimationRemainingTicks = 10;
         this.playSound(SoundEvents.ZOGLIN_ATTACK, 1.0F, this.getVoicePitch());
      } else {
         super.handleEntityEvent(pId);
      }

   }

   public int getAttackAnimationRemainingTicks() {
      return this.attackAnimationRemainingTicks;
   }

   protected SoundEvent getAmbientSound() {
      if (this.level().isClientSide) {
         return null;
      } else {
         return this.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ? SoundEvents.ZOGLIN_ANGRY : SoundEvents.ZOGLIN_AMBIENT;
      }
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.ZOGLIN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ZOGLIN_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.ZOGLIN_STEP, 0.15F, 1.0F);
   }

   protected void playAngrySound() {
      this.playSound(SoundEvents.ZOGLIN_ANGRY, 1.0F, this.getVoicePitch());
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   public MobType getMobType() {
      return MobType.UNDEAD;
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.isBaby()) {
         pCompound.putBoolean("IsBaby", true);
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.getBoolean("IsBaby")) {
         this.setBaby(true);
      }

   }
}