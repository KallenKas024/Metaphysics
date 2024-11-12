package net.minecraft.world.entity.animal;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Pufferfish extends AbstractFish {
   private static final EntityDataAccessor<Integer> PUFF_STATE = SynchedEntityData.defineId(Pufferfish.class, EntityDataSerializers.INT);
   int inflateCounter;
   int deflateTimer;
   private static final Predicate<LivingEntity> SCARY_MOB = (p_289442_) -> {
      if (p_289442_ instanceof Player && ((Player)p_289442_).isCreative()) {
         return false;
      } else {
         return p_289442_.getType() == EntityType.AXOLOTL || p_289442_.getMobType() != MobType.WATER;
      }
   };
   static final TargetingConditions targetingConditions = TargetingConditions.forNonCombat().ignoreInvisibilityTesting().ignoreLineOfSight().selector(SCARY_MOB);
   public static final int STATE_SMALL = 0;
   public static final int STATE_MID = 1;
   public static final int STATE_FULL = 2;

   public Pufferfish(EntityType<? extends Pufferfish> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.refreshDimensions();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(PUFF_STATE, 0);
   }

   public int getPuffState() {
      return this.entityData.get(PUFF_STATE);
   }

   public void setPuffState(int pPuffState) {
      this.entityData.set(PUFF_STATE, pPuffState);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      if (PUFF_STATE.equals(pKey)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(pKey);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("PuffState", this.getPuffState());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setPuffState(Math.min(pCompound.getInt("PuffState"), 2));
   }

   public ItemStack getBucketItemStack() {
      return new ItemStack(Items.PUFFERFISH_BUCKET);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new Pufferfish.PufferfishPuffGoal(this));
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
         if (this.inflateCounter > 0) {
            if (this.getPuffState() == 0) {
               this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getVoicePitch());
               this.setPuffState(1);
            } else if (this.inflateCounter > 40 && this.getPuffState() == 1) {
               this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getVoicePitch());
               this.setPuffState(2);
            }

            ++this.inflateCounter;
         } else if (this.getPuffState() != 0) {
            if (this.deflateTimer > 60 && this.getPuffState() == 2) {
               this.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getVoicePitch());
               this.setPuffState(1);
            } else if (this.deflateTimer > 100 && this.getPuffState() == 1) {
               this.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getVoicePitch());
               this.setPuffState(0);
            }

            ++this.deflateTimer;
         }
      }

      super.tick();
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.isAlive() && this.getPuffState() > 0) {
         for(Mob mob : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(0.3D), (p_149013_) -> {
            return targetingConditions.test(this, p_149013_);
         })) {
            if (mob.isAlive()) {
               this.touch(mob);
            }
         }
      }

   }

   private void touch(Mob pMob) {
      int i = this.getPuffState();
      if (pMob.hurt(this.damageSources().mobAttack(this), (float)(1 + i))) {
         pMob.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * i, 0), this);
         this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
      }

   }

   /**
    * Called by a player entity when they collide with an entity
    */
   public void playerTouch(Player pEntity) {
      int i = this.getPuffState();
      if (pEntity instanceof ServerPlayer && i > 0 && pEntity.hurt(this.damageSources().mobAttack(this), (float)(1 + i))) {
         if (!this.isSilent()) {
            ((ServerPlayer)pEntity).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
         }

         pEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * i, 0), this);
      }

   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.PUFFER_FISH_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PUFFER_FISH_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.PUFFER_FISH_HURT;
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.PUFFER_FISH_FLOP;
   }

   public EntityDimensions getDimensions(Pose pPose) {
      return super.getDimensions(pPose).scale(getScale(this.getPuffState()));
   }

   private static float getScale(int pPuffState) {
      switch (pPuffState) {
         case 0:
            return 0.5F;
         case 1:
            return 0.7F;
         default:
            return 1.0F;
      }
   }

   static class PufferfishPuffGoal extends Goal {
      private final Pufferfish fish;

      public PufferfishPuffGoal(Pufferfish pFish) {
         this.fish = pFish;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         List<LivingEntity> list = this.fish.level().getEntitiesOfClass(LivingEntity.class, this.fish.getBoundingBox().inflate(2.0D), (p_149015_) -> {
            return Pufferfish.targetingConditions.test(this.fish, p_149015_);
         });
         return !list.isEmpty();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.fish.inflateCounter = 1;
         this.fish.deflateTimer = 0;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.fish.inflateCounter = 0;
      }
   }
}