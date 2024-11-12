package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LerpingModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Axolotl extends Animal implements LerpingModel, VariantHolder<Axolotl.Variant>, Bucketable {
   public static final int TOTAL_PLAYDEAD_TIME = 200;
   protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Axolotl>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.AXOLOTL_ATTACKABLES, SensorType.AXOLOTL_TEMPTATIONS);
   protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.PLAY_DEAD_TICKS, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.HAS_HUNTING_COOLDOWN, MemoryModuleType.IS_PANICKING);
   private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_PLAYING_DEAD = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
   public static final double PLAYER_REGEN_DETECTION_RANGE = 20.0D;
   public static final int RARE_VARIANT_CHANCE = 1200;
   private static final int AXOLOTL_TOTAL_AIR_SUPPLY = 6000;
   public static final String VARIANT_TAG = "Variant";
   private static final int REHYDRATE_AIR_SUPPLY = 1800;
   private static final int REGEN_BUFF_MAX_DURATION = 2400;
   private final Map<String, Vector3f> modelRotationValues = Maps.newHashMap();
   private static final int REGEN_BUFF_BASE_DURATION = 100;

   public Axolotl(EntityType<? extends Axolotl> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
      this.moveControl = new Axolotl.AxolotlMoveControl(this);
      this.lookControl = new Axolotl.AxolotlLookControl(this, 20);
      this.setMaxUpStep(1.0F);
   }

   public Map<String, Vector3f> getModelRotationValues() {
      return this.modelRotationValues;
   }

   public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
      return 0.0F;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_VARIANT, 0);
      this.entityData.define(DATA_PLAYING_DEAD, false);
      this.entityData.define(FROM_BUCKET, false);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Variant", this.getVariant().getId());
      pCompound.putBoolean("FromBucket", this.fromBucket());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setVariant(Axolotl.Variant.byId(pCompound.getInt("Variant")));
      this.setFromBucket(pCompound.getBoolean("FromBucket"));
   }

   /**
    * Plays living's sound at its position
    */
   public void playAmbientSound() {
      if (!this.isPlayingDead()) {
         super.playAmbientSound();
      }
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      boolean flag = false;
      if (pReason == MobSpawnType.BUCKET) {
         return pSpawnData;
      } else {
         RandomSource randomsource = pLevel.getRandom();
         if (pSpawnData instanceof Axolotl.AxolotlGroupData) {
            if (((Axolotl.AxolotlGroupData)pSpawnData).getGroupSize() >= 2) {
               flag = true;
            }
         } else {
            pSpawnData = new Axolotl.AxolotlGroupData(Axolotl.Variant.getCommonSpawnVariant(randomsource), Axolotl.Variant.getCommonSpawnVariant(randomsource));
         }

         this.setVariant(((Axolotl.AxolotlGroupData)pSpawnData).getVariant(randomsource));
         if (flag) {
            this.setAge(-24000);
         }

         return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      }
   }

   /**
    * Gets called every tick from main Entity class
    */
   public void baseTick() {
      int i = this.getAirSupply();
      super.baseTick();
      if (!this.isNoAi()) {
         this.handleAirSupply(i);
      }

   }

   protected void handleAirSupply(int pAirSupply) {
      if (this.isAlive() && !this.isInWaterRainOrBubble()) {
         this.setAirSupply(pAirSupply - 1);
         if (this.getAirSupply() == -20) {
            this.setAirSupply(0);
            this.hurt(this.damageSources().dryOut(), 2.0F);
         }
      } else {
         this.setAirSupply(this.getMaxAirSupply());
      }

   }

   public void rehydrate() {
      int i = this.getAirSupply() + 1800;
      this.setAirSupply(Math.min(i, this.getMaxAirSupply()));
   }

   public int getMaxAirSupply() {
      return 6000;
   }

   public Axolotl.Variant getVariant() {
      return Axolotl.Variant.byId(this.entityData.get(DATA_VARIANT));
   }

   public void setVariant(Axolotl.Variant pVariant) {
      this.entityData.set(DATA_VARIANT, pVariant.getId());
   }

   private static boolean useRareVariant(RandomSource pRandom) {
      return pRandom.nextInt(1200) == 0;
   }

   public boolean checkSpawnObstruction(LevelReader pLevel) {
      return pLevel.isUnobstructed(this);
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   public boolean isPushedByFluid() {
      return false;
   }

   public MobType getMobType() {
      return MobType.WATER;
   }

   public void setPlayingDead(boolean pPlayingDead) {
      this.entityData.set(DATA_PLAYING_DEAD, pPlayingDead);
   }

   public boolean isPlayingDead() {
      return this.entityData.get(DATA_PLAYING_DEAD);
   }

   public boolean fromBucket() {
      return this.entityData.get(FROM_BUCKET);
   }

   public void setFromBucket(boolean pFromBucket) {
      this.entityData.set(FROM_BUCKET, pFromBucket);
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      Axolotl axolotl = EntityType.AXOLOTL.create(pLevel);
      if (axolotl != null) {
         Axolotl.Variant axolotl$variant;
         if (useRareVariant(this.random)) {
            axolotl$variant = Axolotl.Variant.getRareSpawnVariant(this.random);
         } else {
            axolotl$variant = this.random.nextBoolean() ? this.getVariant() : ((Axolotl)pOtherParent).getVariant();
         }

         axolotl.setVariant(axolotl$variant);
         axolotl.setPersistenceRequired();
      }

      return axolotl;
   }

   public double getMeleeAttackRangeSqr(LivingEntity pEntity) {
      return 1.5D + (double)pEntity.getBbWidth() * 2.0D;
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return pStack.is(ItemTags.AXOLOTL_TEMPT_ITEMS);
   }

   public boolean canBeLeashed(Player pPlayer) {
      return true;
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("axolotlBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      this.level().getProfiler().push("axolotlActivityUpdate");
      AxolotlAi.updateActivity(this);
      this.level().getProfiler().pop();
      if (!this.isNoAi()) {
         Optional<Integer> optional = this.getBrain().getMemory(MemoryModuleType.PLAY_DEAD_TICKS);
         this.setPlayingDead(optional.isPresent() && optional.get() > 0);
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 14.0D).add(Attributes.MOVEMENT_SPEED, 1.0D).add(Attributes.ATTACK_DAMAGE, 2.0D);
   }

   protected PathNavigation createNavigation(Level pLevel) {
      return new AmphibiousPathNavigation(this, pLevel);
   }

   public boolean doHurtTarget(Entity pEntity) {
      boolean flag = pEntity.hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
      if (flag) {
         this.doEnchantDamageEffects(this, pEntity);
         this.playSound(SoundEvents.AXOLOTL_ATTACK, 1.0F, 1.0F);
      }

      return flag;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      float f = this.getHealth();
      if (!this.level().isClientSide && !this.isNoAi() && this.level().random.nextInt(3) == 0 && ((float)this.level().random.nextInt(3) < pAmount || f / this.getMaxHealth() < 0.5F) && pAmount < f && this.isInWater() && (pSource.getEntity() != null || pSource.getDirectEntity() != null) && !this.isPlayingDead()) {
         this.brain.setMemory(MemoryModuleType.PLAY_DEAD_TICKS, 200);
      }

      return super.hurt(pSource, pAmount);
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
      return pDimensions.height * 0.655F;
   }

   /**
    * The speed it takes to move the entity's head rotation through the faceEntity method.
    */
   public int getMaxHeadXRot() {
      return 1;
   }

   public int getMaxHeadYRot() {
      return 1;
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      return Bucketable.bucketMobPickup(pPlayer, pHand, this).orElse(super.mobInteract(pPlayer, pHand));
   }

   public void saveToBucketTag(ItemStack pStack) {
      Bucketable.saveDefaultDataToBucketTag(this, pStack);
      CompoundTag compoundtag = pStack.getOrCreateTag();
      compoundtag.putInt("Variant", this.getVariant().getId());
      compoundtag.putInt("Age", this.getAge());
      Brain<?> brain = this.getBrain();
      if (brain.hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)) {
         compoundtag.putLong("HuntingCooldown", brain.getTimeUntilExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN));
      }

   }

   public void loadFromBucketTag(CompoundTag pTag) {
      Bucketable.loadDefaultDataFromBucketTag(this, pTag);
      this.setVariant(Axolotl.Variant.byId(pTag.getInt("Variant")));
      if (pTag.contains("Age")) {
         this.setAge(pTag.getInt("Age"));
      }

      if (pTag.contains("HuntingCooldown")) {
         this.getBrain().setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, pTag.getLong("HuntingCooldown"));
      }

   }

   public ItemStack getBucketItemStack() {
      return new ItemStack(Items.AXOLOTL_BUCKET);
   }

   public SoundEvent getPickupSound() {
      return SoundEvents.BUCKET_FILL_AXOLOTL;
   }

   public boolean canBeSeenAsEnemy() {
      return !this.isPlayingDead() && super.canBeSeenAsEnemy();
   }

   public static void onStopAttacking(Axolotl pAxolotl, LivingEntity pTarget) {
      Level level = pAxolotl.level();
      if (pTarget.isDeadOrDying()) {
         DamageSource damagesource = pTarget.getLastDamageSource();
         if (damagesource != null) {
            Entity entity = damagesource.getEntity();
            if (entity != null && entity.getType() == EntityType.PLAYER) {
               Player player = (Player)entity;
               List<Player> list = level.getEntitiesOfClass(Player.class, pAxolotl.getBoundingBox().inflate(20.0D));
               if (list.contains(player)) {
                  pAxolotl.applySupportingEffects(player);
               }
            }
         }
      }

   }

   public void applySupportingEffects(Player pPlayer) {
      MobEffectInstance mobeffectinstance = pPlayer.getEffect(MobEffects.REGENERATION);
      if (mobeffectinstance == null || mobeffectinstance.endsWithin(2399)) {
         int i = mobeffectinstance != null ? mobeffectinstance.getDuration() : 0;
         int j = Math.min(2400, 100 + i);
         pPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, j, 0), this);
      }

      pPlayer.removeEffect(MobEffects.DIG_SLOWDOWN);
   }

   public boolean requiresCustomPersistence() {
      return super.requiresCustomPersistence() || this.fromBucket();
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.AXOLOTL_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.AXOLOTL_DEATH;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return this.isInWater() ? SoundEvents.AXOLOTL_IDLE_WATER : SoundEvents.AXOLOTL_IDLE_AIR;
   }

   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.AXOLOTL_SPLASH;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.AXOLOTL_SWIM;
   }

   protected Brain.Provider<Axolotl> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
      return AxolotlAi.makeBrain(this.brainProvider().makeBrain(pDynamic));
   }

   public Brain<Axolotl> getBrain() {
      return (Brain<Axolotl>)super.getBrain();
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   public void travel(Vec3 pTravelVector) {
      if (this.isControlledByLocalInstance() && this.isInWater()) {
         this.moveRelative(this.getSpeed(), pTravelVector);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
      } else {
         super.travel(pTravelVector);
      }

   }

   protected void usePlayerItem(Player pPlayer, InteractionHand pHand, ItemStack pStack) {
      if (pStack.is(Items.TROPICAL_FISH_BUCKET)) {
         pPlayer.setItemInHand(pHand, new ItemStack(Items.WATER_BUCKET));
      } else {
         super.usePlayerItem(pPlayer, pHand, pStack);
      }

   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return !this.fromBucket() && !this.hasCustomName();
   }

   public static boolean checkAxolotlSpawnRules(EntityType<? extends LivingEntity> pAxolotl, ServerLevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.AXOLOTLS_SPAWNABLE_ON);
   }

   public static class AxolotlGroupData extends AgeableMob.AgeableMobGroupData {
      public final Axolotl.Variant[] types;

      public AxolotlGroupData(Axolotl.Variant... pTypes) {
         super(false);
         this.types = pTypes;
      }

      public Axolotl.Variant getVariant(RandomSource pRandom) {
         return this.types[pRandom.nextInt(this.types.length)];
      }
   }

   class AxolotlLookControl extends SmoothSwimmingLookControl {
      public AxolotlLookControl(Axolotl pAxolotl, int pMaxYRotFromCenter) {
         super(pAxolotl, pMaxYRotFromCenter);
      }

      /**
       * Updates look
       */
      public void tick() {
         if (!Axolotl.this.isPlayingDead()) {
            super.tick();
         }

      }
   }

   static class AxolotlMoveControl extends SmoothSwimmingMoveControl {
      private final Axolotl axolotl;

      public AxolotlMoveControl(Axolotl pAxolotl) {
         super(pAxolotl, 85, 10, 0.1F, 0.5F, false);
         this.axolotl = pAxolotl;
      }

      public void tick() {
         if (!this.axolotl.isPlayingDead()) {
            super.tick();
         }

      }
   }

   public static enum Variant implements StringRepresentable {
      LUCY(0, "lucy", true),
      WILD(1, "wild", true),
      GOLD(2, "gold", true),
      CYAN(3, "cyan", true),
      BLUE(4, "blue", false);

      private static final IntFunction<Axolotl.Variant> BY_ID = ByIdMap.continuous(Axolotl.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
      public static final Codec<Axolotl.Variant> CODEC = StringRepresentable.fromEnum(Axolotl.Variant::values);
      private final int id;
      private final String name;
      private final boolean common;

      private Variant(int pId, String pName, boolean pCommon) {
         this.id = pId;
         this.name = pName;
         this.common = pCommon;
      }

      public int getId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }

      public static Axolotl.Variant byId(int pId) {
         return BY_ID.apply(pId);
      }

      public static Axolotl.Variant getCommonSpawnVariant(RandomSource pRandom) {
         return getSpawnVariant(pRandom, true);
      }

      public static Axolotl.Variant getRareSpawnVariant(RandomSource pRandom) {
         return getSpawnVariant(pRandom, false);
      }

      private static Axolotl.Variant getSpawnVariant(RandomSource pRandom, boolean pCommon) {
         Axolotl.Variant[] aaxolotl$variant = Arrays.stream(values()).filter((p_149252_) -> {
            return p_149252_.common == pCommon;
         }).toArray((p_149244_) -> {
            return new Axolotl.Variant[p_149244_];
         });
         return Util.getRandom(aaxolotl$variant, pRandom);
      }
   }
}