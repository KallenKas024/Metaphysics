package net.minecraft.world.entity.animal;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.GolemRandomStrollInVillageGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveBackToVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class IronGolem extends AbstractGolem implements NeutralMob {
   protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(IronGolem.class, EntityDataSerializers.BYTE);
   private static final int IRON_INGOT_HEAL_AMOUNT = 25;
   private int attackAnimationTick;
   private int offerFlowerTick;
   private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
   private int remainingPersistentAngerTime;
   @Nullable
   private UUID persistentAngerTarget;

   public IronGolem(EntityType<? extends IronGolem> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setMaxUpStep(1.0F);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
      this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
      this.goalSelector.addGoal(2, new MoveBackToVillageGoal(this, 0.6D, false));
      this.goalSelector.addGoal(4, new GolemRandomStrollInVillageGoal(this, 0.6D));
      this.goalSelector.addGoal(5, new OfferFlowerGoal(this));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new DefendVillageTargetGoal(this));
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, false, false, (p_28879_) -> {
         return p_28879_ instanceof Enemy && !(p_28879_ instanceof Creeper);
      }));
      this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_FLAGS_ID, (byte)0);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 100.0D).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.KNOCKBACK_RESISTANCE, 1.0D).add(Attributes.ATTACK_DAMAGE, 15.0D);
   }

   /**
    * Decrements the entity's air supply when underwater
    */
   protected int decreaseAirSupply(int pAir) {
      return pAir;
   }

   protected void doPush(Entity pEntity) {
      if (pEntity instanceof Enemy && !(pEntity instanceof Creeper) && this.getRandom().nextInt(20) == 0) {
         this.setTarget((LivingEntity)pEntity);
      }

      super.doPush(pEntity);
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.attackAnimationTick > 0) {
         --this.attackAnimationTick;
      }

      if (this.offerFlowerTick > 0) {
         --this.offerFlowerTick;
      }

      if (!this.level().isClientSide) {
         this.updatePersistentAnger((ServerLevel)this.level(), true);
      }

   }

   public boolean canSpawnSprintParticle() {
      return this.getDeltaMovement().horizontalDistanceSqr() > (double)2.5000003E-7F && this.random.nextInt(5) == 0;
   }

   public boolean canAttackType(EntityType<?> pType) {
      if (this.isPlayerCreated() && pType == EntityType.PLAYER) {
         return false;
      } else {
         return pType == EntityType.CREEPER ? false : super.canAttackType(pType);
      }
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("PlayerCreated", this.isPlayerCreated());
      this.addPersistentAngerSaveData(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setPlayerCreated(pCompound.getBoolean("PlayerCreated"));
      this.readPersistentAngerSaveData(this.level(), pCompound);
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

   private float getAttackDamage() {
      return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
   }

   public boolean doHurtTarget(Entity pEntity) {
      this.attackAnimationTick = 10;
      this.level().broadcastEntityEvent(this, (byte)4);
      float f = this.getAttackDamage();
      float f1 = (int)f > 0 ? f / 2.0F + (float)this.random.nextInt((int)f) : f;
      boolean flag = pEntity.hurt(this.damageSources().mobAttack(this), f1);
      if (flag) {
         double d2;
         if (pEntity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)pEntity;
            d2 = livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
         } else {
            d2 = 0.0D;
         }

         double d0 = d2;
         double d1 = Math.max(0.0D, 1.0D - d0);
         pEntity.setDeltaMovement(pEntity.getDeltaMovement().add(0.0D, (double)0.4F * d1, 0.0D));
         this.doEnchantDamageEffects(this, pEntity);
      }

      this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      return flag;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      IronGolem.Crackiness irongolem$crackiness = this.getCrackiness();
      boolean flag = super.hurt(pSource, pAmount);
      if (flag && this.getCrackiness() != irongolem$crackiness) {
         this.playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
      }

      return flag;
   }

   public IronGolem.Crackiness getCrackiness() {
      return IronGolem.Crackiness.byFraction(this.getHealth() / this.getMaxHealth());
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 4) {
         this.attackAnimationTick = 10;
         this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
      } else if (pId == 11) {
         this.offerFlowerTick = 400;
      } else if (pId == 34) {
         this.offerFlowerTick = 0;
      } else {
         super.handleEntityEvent(pId);
      }

   }

   public int getAttackAnimationTick() {
      return this.attackAnimationTick;
   }

   public void offerFlower(boolean pOfferingFlower) {
      if (pOfferingFlower) {
         this.offerFlowerTick = 400;
         this.level().broadcastEntityEvent(this, (byte)11);
      } else {
         this.offerFlowerTick = 0;
         this.level().broadcastEntityEvent(this, (byte)34);
      }

   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.IRON_GOLEM_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.IRON_GOLEM_DEATH;
   }

   protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!itemstack.is(Items.IRON_INGOT)) {
         return InteractionResult.PASS;
      } else {
         float f = this.getHealth();
         this.heal(25.0F);
         if (this.getHealth() == f) {
            return InteractionResult.PASS;
         } else {
            float f1 = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
            this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, f1);
            if (!pPlayer.getAbilities().instabuild) {
               itemstack.shrink(1);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
      }
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
   }

   public int getOfferFlowerTick() {
      return this.offerFlowerTick;
   }

   public boolean isPlayerCreated() {
      return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
   }

   public void setPlayerCreated(boolean pPlayerCreated) {
      byte b0 = this.entityData.get(DATA_FLAGS_ID);
      if (pPlayerCreated) {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 1));
      } else {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -2));
      }

   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      super.die(pCause);
   }

   public boolean checkSpawnObstruction(LevelReader pLevel) {
      BlockPos blockpos = this.blockPosition();
      BlockPos blockpos1 = blockpos.below();
      BlockState blockstate = pLevel.getBlockState(blockpos1);
      if (!blockstate.entityCanStandOn(pLevel, blockpos1, this)) {
         return false;
      } else {
         for(int i = 1; i < 3; ++i) {
            BlockPos blockpos2 = blockpos.above(i);
            BlockState blockstate1 = pLevel.getBlockState(blockpos2);
            if (!NaturalSpawner.isValidEmptySpawnBlock(pLevel, blockpos2, blockstate1, blockstate1.getFluidState(), EntityType.IRON_GOLEM)) {
               return false;
            }
         }

         return NaturalSpawner.isValidEmptySpawnBlock(pLevel, blockpos, pLevel.getBlockState(blockpos), Fluids.EMPTY.defaultFluidState(), EntityType.IRON_GOLEM) && pLevel.isUnobstructed(this);
      }
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)(0.875F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }

   public static enum Crackiness {
      NONE(1.0F),
      LOW(0.75F),
      MEDIUM(0.5F),
      HIGH(0.25F);

      private static final List<IronGolem.Crackiness> BY_DAMAGE = Stream.of(values()).sorted(Comparator.comparingDouble((p_28904_) -> {
         return (double)p_28904_.fraction;
      })).collect(ImmutableList.toImmutableList());
      private final float fraction;

      private Crackiness(float pFraction) {
         this.fraction = pFraction;
      }

      public static IronGolem.Crackiness byFraction(float pFraction) {
         for(IronGolem.Crackiness irongolem$crackiness : BY_DAMAGE) {
            if (pFraction < irongolem$crackiness.fraction) {
               return irongolem$crackiness;
            }
         }

         return NONE;
      }
   }
}