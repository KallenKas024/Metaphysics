package net.minecraft.world.entity.animal.frog;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Tadpole extends AbstractFish {
   @VisibleForTesting
   public static int ticksToBeFrog = Math.abs(-24000);
   public static float HITBOX_WIDTH = 0.4F;
   public static float HITBOX_HEIGHT = 0.3F;
   private int age;
   protected static final ImmutableList<SensorType<? extends Sensor<? super Tadpole>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.FROG_TEMPTATIONS);
   protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.BREED_TARGET, MemoryModuleType.IS_PANICKING);

   public Tadpole(EntityType<? extends AbstractFish> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
      this.lookControl = new SmoothSwimmingLookControl(this, 10);
   }

   protected PathNavigation createNavigation(Level pLevel) {
      return new WaterBoundPathNavigation(this, pLevel);
   }

   protected Brain.Provider<Tadpole> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
      return TadpoleAi.makeBrain(this.brainProvider().makeBrain(pDynamic));
   }

   public Brain<Tadpole> getBrain() {
      return (Brain<Tadpole>)super.getBrain();
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.TADPOLE_FLOP;
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("tadpoleBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      this.level().getProfiler().push("tadpoleActivityUpdate");
      TadpoleAi.updateActivity(this);
      this.level().getProfiler().pop();
      super.customServerAiStep();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 1.0D).add(Attributes.MAX_HEALTH, 6.0D);
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         this.setAge(this.age + 1);
      }

   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Age", this.age);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setAge(pCompound.getInt("Age"));
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.TADPOLE_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.TADPOLE_DEATH;
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (this.isFood(itemstack)) {
         this.feed(pPlayer, itemstack);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         return Bucketable.bucketMobPickup(pPlayer, pHand, this).orElse(super.mobInteract(pPlayer, pHand));
      }
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   public boolean fromBucket() {
      return true;
   }

   public void setFromBucket(boolean pFromBucket) {
   }

   public void saveToBucketTag(ItemStack pStack) {
      Bucketable.saveDefaultDataToBucketTag(this, pStack);
      CompoundTag compoundtag = pStack.getOrCreateTag();
      compoundtag.putInt("Age", this.getAge());
   }

   public void loadFromBucketTag(CompoundTag pTag) {
      Bucketable.loadDefaultDataFromBucketTag(this, pTag);
      if (pTag.contains("Age")) {
         this.setAge(pTag.getInt("Age"));
      }

   }

   public ItemStack getBucketItemStack() {
      return new ItemStack(Items.TADPOLE_BUCKET);
   }

   public SoundEvent getPickupSound() {
      return SoundEvents.BUCKET_FILL_TADPOLE;
   }

   private boolean isFood(ItemStack pStack) {
      return Frog.TEMPTATION_ITEM.test(pStack);
   }

   private void feed(Player pPlayer, ItemStack pStack) {
      this.usePlayerItem(pPlayer, pStack);
      this.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(this.getTicksLeftUntilAdult()));
      this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
   }

   private void usePlayerItem(Player pPlayer, ItemStack pStack) {
      if (!pPlayer.getAbilities().instabuild) {
         pStack.shrink(1);
      }

   }

   private int getAge() {
      return this.age;
   }

   private void ageUp(int pOffset) {
      this.setAge(this.age + pOffset * 20);
   }

   private void setAge(int pAge) {
      this.age = pAge;
      if (this.age >= ticksToBeFrog) {
         this.ageUp();
      }

   }

   private void ageUp() {
      Level $$1 = this.level();
      if ($$1 instanceof ServerLevel serverlevel) {
         Frog frog = EntityType.FROG.create(this.level());
         if (frog != null) {
            frog.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            frog.finalizeSpawn(serverlevel, this.level().getCurrentDifficultyAt(frog.blockPosition()), MobSpawnType.CONVERSION, (SpawnGroupData)null, (CompoundTag)null);
            frog.setNoAi(this.isNoAi());
            if (this.hasCustomName()) {
               frog.setCustomName(this.getCustomName());
               frog.setCustomNameVisible(this.isCustomNameVisible());
            }

            frog.setPersistenceRequired();
            this.playSound(SoundEvents.TADPOLE_GROW_UP, 0.15F, 1.0F);
            serverlevel.addFreshEntityWithPassengers(frog);
            this.discard();
         }
      }

   }

   private int getTicksLeftUntilAdult() {
      return Math.max(0, ticksToBeFrog - this.age);
   }

   /**
    * Entity won't drop experience points if this returns false
    */
   public boolean shouldDropExperience() {
      return false;
   }
}