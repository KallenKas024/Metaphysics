package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class Bee extends Animal implements NeutralMob, FlyingAnimal {
   public static final float FLAP_DEGREES_PER_TICK = 120.32113F;
   public static final int TICKS_PER_FLAP = Mth.ceil(1.4959966F);
   private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.INT);
   private static final int FLAG_ROLL = 2;
   private static final int FLAG_HAS_STUNG = 4;
   private static final int FLAG_HAS_NECTAR = 8;
   private static final int STING_DEATH_COUNTDOWN = 1200;
   private static final int TICKS_BEFORE_GOING_TO_KNOWN_FLOWER = 2400;
   private static final int TICKS_WITHOUT_NECTAR_BEFORE_GOING_HOME = 3600;
   private static final int MIN_ATTACK_DIST = 4;
   private static final int MAX_CROPS_GROWABLE = 10;
   private static final int POISON_SECONDS_NORMAL = 10;
   private static final int POISON_SECONDS_HARD = 18;
   private static final int TOO_FAR_DISTANCE = 32;
   private static final int HIVE_CLOSE_ENOUGH_DISTANCE = 2;
   private static final int PATHFIND_TO_HIVE_WHEN_CLOSER_THAN = 16;
   private static final int HIVE_SEARCH_DISTANCE = 20;
   public static final String TAG_CROPS_GROWN_SINCE_POLLINATION = "CropsGrownSincePollination";
   public static final String TAG_CANNOT_ENTER_HIVE_TICKS = "CannotEnterHiveTicks";
   public static final String TAG_TICKS_SINCE_POLLINATION = "TicksSincePollination";
   public static final String TAG_HAS_STUNG = "HasStung";
   public static final String TAG_HAS_NECTAR = "HasNectar";
   public static final String TAG_FLOWER_POS = "FlowerPos";
   public static final String TAG_HIVE_POS = "HivePos";
   private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
   @Nullable
   private UUID persistentAngerTarget;
   private float rollAmount;
   private float rollAmountO;
   private int timeSinceSting;
   int ticksWithoutNectarSinceExitingHive;
   private int stayOutOfHiveCountdown;
   private int numCropsGrownSincePollination;
   private static final int COOLDOWN_BEFORE_LOCATING_NEW_HIVE = 200;
   int remainingCooldownBeforeLocatingNewHive;
   private static final int COOLDOWN_BEFORE_LOCATING_NEW_FLOWER = 200;
   int remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(this.random, 20, 60);
   @Nullable
   BlockPos savedFlowerPos;
   @Nullable
   BlockPos hivePos;
   Bee.BeePollinateGoal beePollinateGoal;
   Bee.BeeGoToHiveGoal goToHiveGoal;
   private Bee.BeeGoToKnownFlowerGoal goToKnownFlowerGoal;
   private int underWaterTicks;

   public Bee(EntityType<? extends Bee> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.moveControl = new FlyingMoveControl(this, 20, true);
      this.lookControl = new Bee.BeeLookControl(this);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_FLAGS_ID, (byte)0);
      this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
   }

   public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
      return pLevel.getBlockState(pPos).isAir() ? 10.0F : 0.0F;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new Bee.BeeAttackGoal(this, (double)1.4F, true));
      this.goalSelector.addGoal(1, new Bee.BeeEnterHiveGoal());
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(ItemTags.FLOWERS), false));
      this.beePollinateGoal = new Bee.BeePollinateGoal();
      this.goalSelector.addGoal(4, this.beePollinateGoal);
      this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
      this.goalSelector.addGoal(5, new Bee.BeeLocateHiveGoal());
      this.goToHiveGoal = new Bee.BeeGoToHiveGoal();
      this.goalSelector.addGoal(5, this.goToHiveGoal);
      this.goToKnownFlowerGoal = new Bee.BeeGoToKnownFlowerGoal();
      this.goalSelector.addGoal(6, this.goToKnownFlowerGoal);
      this.goalSelector.addGoal(7, new Bee.BeeGrowCropGoal());
      this.goalSelector.addGoal(8, new Bee.BeeWanderGoal());
      this.goalSelector.addGoal(9, new FloatGoal(this));
      this.targetSelector.addGoal(1, (new Bee.BeeHurtByOtherGoal(this)).setAlertOthers(new Class[0]));
      this.targetSelector.addGoal(2, new Bee.BeeBecomeAngryTargetGoal(this));
      this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.hasHive()) {
         pCompound.put("HivePos", NbtUtils.writeBlockPos(this.getHivePos()));
      }

      if (this.hasSavedFlowerPos()) {
         pCompound.put("FlowerPos", NbtUtils.writeBlockPos(this.getSavedFlowerPos()));
      }

      pCompound.putBoolean("HasNectar", this.hasNectar());
      pCompound.putBoolean("HasStung", this.hasStung());
      pCompound.putInt("TicksSincePollination", this.ticksWithoutNectarSinceExitingHive);
      pCompound.putInt("CannotEnterHiveTicks", this.stayOutOfHiveCountdown);
      pCompound.putInt("CropsGrownSincePollination", this.numCropsGrownSincePollination);
      this.addPersistentAngerSaveData(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      this.hivePos = null;
      if (pCompound.contains("HivePos")) {
         this.hivePos = NbtUtils.readBlockPos(pCompound.getCompound("HivePos"));
      }

      this.savedFlowerPos = null;
      if (pCompound.contains("FlowerPos")) {
         this.savedFlowerPos = NbtUtils.readBlockPos(pCompound.getCompound("FlowerPos"));
      }

      super.readAdditionalSaveData(pCompound);
      this.setHasNectar(pCompound.getBoolean("HasNectar"));
      this.setHasStung(pCompound.getBoolean("HasStung"));
      this.ticksWithoutNectarSinceExitingHive = pCompound.getInt("TicksSincePollination");
      this.stayOutOfHiveCountdown = pCompound.getInt("CannotEnterHiveTicks");
      this.numCropsGrownSincePollination = pCompound.getInt("CropsGrownSincePollination");
      this.readPersistentAngerSaveData(this.level(), pCompound);
   }

   public boolean doHurtTarget(Entity pEntity) {
      boolean flag = pEntity.hurt(this.damageSources().sting(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
      if (flag) {
         this.doEnchantDamageEffects(this, pEntity);
         if (pEntity instanceof LivingEntity) {
            ((LivingEntity)pEntity).setStingerCount(((LivingEntity)pEntity).getStingerCount() + 1);
            int i = 0;
            if (this.level().getDifficulty() == Difficulty.NORMAL) {
               i = 10;
            } else if (this.level().getDifficulty() == Difficulty.HARD) {
               i = 18;
            }

            if (i > 0) {
               ((LivingEntity)pEntity).addEffect(new MobEffectInstance(MobEffects.POISON, i * 20, 0), this);
            }
         }

         this.setHasStung(true);
         this.stopBeingAngry();
         this.playSound(SoundEvents.BEE_STING, 1.0F, 1.0F);
      }

      return flag;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.hasNectar() && this.getCropsGrownSincePollination() < 10 && this.random.nextFloat() < 0.05F) {
         for(int i = 0; i < this.random.nextInt(2) + 1; ++i) {
            this.spawnFluidParticle(this.level(), this.getX() - (double)0.3F, this.getX() + (double)0.3F, this.getZ() - (double)0.3F, this.getZ() + (double)0.3F, this.getY(0.5D), ParticleTypes.FALLING_NECTAR);
         }
      }

      this.updateRollAmount();
   }

   private void spawnFluidParticle(Level pLevel, double pStartX, double pEndX, double pStartZ, double pEndZ, double pPosY, ParticleOptions pParticleOption) {
      pLevel.addParticle(pParticleOption, Mth.lerp(pLevel.random.nextDouble(), pStartX, pEndX), pPosY, Mth.lerp(pLevel.random.nextDouble(), pStartZ, pEndZ), 0.0D, 0.0D, 0.0D);
   }

   void pathfindRandomlyTowards(BlockPos pPos) {
      Vec3 vec3 = Vec3.atBottomCenterOf(pPos);
      int i = 0;
      BlockPos blockpos = this.blockPosition();
      int j = (int)vec3.y - blockpos.getY();
      if (j > 2) {
         i = 4;
      } else if (j < -2) {
         i = -4;
      }

      int k = 6;
      int l = 8;
      int i1 = blockpos.distManhattan(pPos);
      if (i1 < 15) {
         k = i1 / 2;
         l = i1 / 2;
      }

      Vec3 vec31 = AirRandomPos.getPosTowards(this, k, l, i, vec3, (double)((float)Math.PI / 10F));
      if (vec31 != null) {
         this.navigation.setMaxVisitedNodesMultiplier(0.5F);
         this.navigation.moveTo(vec31.x, vec31.y, vec31.z, 1.0D);
      }
   }

   @Nullable
   public BlockPos getSavedFlowerPos() {
      return this.savedFlowerPos;
   }

   public boolean hasSavedFlowerPos() {
      return this.savedFlowerPos != null;
   }

   public void setSavedFlowerPos(BlockPos pSavedFlowerPos) {
      this.savedFlowerPos = pSavedFlowerPos;
   }

   @VisibleForDebug
   public int getTravellingTicks() {
      return Math.max(this.goToHiveGoal.travellingTicks, this.goToKnownFlowerGoal.travellingTicks);
   }

   @VisibleForDebug
   public List<BlockPos> getBlacklistedHives() {
      return this.goToHiveGoal.blacklistedTargets;
   }

   private boolean isTiredOfLookingForNectar() {
      return this.ticksWithoutNectarSinceExitingHive > 3600;
   }

   boolean wantsToEnterHive() {
      if (this.stayOutOfHiveCountdown <= 0 && !this.beePollinateGoal.isPollinating() && !this.hasStung() && this.getTarget() == null) {
         boolean flag = this.isTiredOfLookingForNectar() || this.level().isRaining() || this.level().isNight() || this.hasNectar();
         return flag && !this.isHiveNearFire();
      } else {
         return false;
      }
   }

   public void setStayOutOfHiveCountdown(int pStayOutOfHiveCountdown) {
      this.stayOutOfHiveCountdown = pStayOutOfHiveCountdown;
   }

   public float getRollAmount(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.rollAmountO, this.rollAmount);
   }

   private void updateRollAmount() {
      this.rollAmountO = this.rollAmount;
      if (this.isRolling()) {
         this.rollAmount = Math.min(1.0F, this.rollAmount + 0.2F);
      } else {
         this.rollAmount = Math.max(0.0F, this.rollAmount - 0.24F);
      }

   }

   protected void customServerAiStep() {
      boolean flag = this.hasStung();
      if (this.isInWaterOrBubble()) {
         ++this.underWaterTicks;
      } else {
         this.underWaterTicks = 0;
      }

      if (this.underWaterTicks > 20) {
         this.hurt(this.damageSources().drown(), 1.0F);
      }

      if (flag) {
         ++this.timeSinceSting;
         if (this.timeSinceSting % 5 == 0 && this.random.nextInt(Mth.clamp(1200 - this.timeSinceSting, 1, 1200)) == 0) {
            this.hurt(this.damageSources().generic(), this.getHealth());
         }
      }

      if (!this.hasNectar()) {
         ++this.ticksWithoutNectarSinceExitingHive;
      }

      if (!this.level().isClientSide) {
         this.updatePersistentAnger((ServerLevel)this.level(), false);
      }

   }

   public void resetTicksWithoutNectarSinceExitingHive() {
      this.ticksWithoutNectarSinceExitingHive = 0;
   }

   private boolean isHiveNearFire() {
      if (this.hivePos == null) {
         return false;
      } else {
         BlockEntity blockentity = this.level().getBlockEntity(this.hivePos);
         return blockentity instanceof BeehiveBlockEntity && ((BeehiveBlockEntity)blockentity).isFireNearby();
      }
   }

   public int getRemainingPersistentAngerTime() {
      return this.entityData.get(DATA_REMAINING_ANGER_TIME);
   }

   public void setRemainingPersistentAngerTime(int pTime) {
      this.entityData.set(DATA_REMAINING_ANGER_TIME, pTime);
   }

   @Nullable
   public UUID getPersistentAngerTarget() {
      return this.persistentAngerTarget;
   }

   public void setPersistentAngerTarget(@Nullable UUID pTarget) {
      this.persistentAngerTarget = pTarget;
   }

   public void startPersistentAngerTimer() {
      this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
   }

   private boolean doesHiveHaveSpace(BlockPos pHivePos) {
      BlockEntity blockentity = this.level().getBlockEntity(pHivePos);
      if (blockentity instanceof BeehiveBlockEntity) {
         return !((BeehiveBlockEntity)blockentity).isFull();
      } else {
         return false;
      }
   }

   @VisibleForDebug
   public boolean hasHive() {
      return this.hivePos != null;
   }

   @Nullable
   @VisibleForDebug
   public BlockPos getHivePos() {
      return this.hivePos;
   }

   @VisibleForDebug
   public GoalSelector getGoalSelector() {
      return this.goalSelector;
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendBeeInfo(this);
   }

   int getCropsGrownSincePollination() {
      return this.numCropsGrownSincePollination;
   }

   private void resetNumCropsGrownSincePollination() {
      this.numCropsGrownSincePollination = 0;
   }

   void incrementNumCropsGrownSincePollination() {
      ++this.numCropsGrownSincePollination;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         if (this.stayOutOfHiveCountdown > 0) {
            --this.stayOutOfHiveCountdown;
         }

         if (this.remainingCooldownBeforeLocatingNewHive > 0) {
            --this.remainingCooldownBeforeLocatingNewHive;
         }

         if (this.remainingCooldownBeforeLocatingNewFlower > 0) {
            --this.remainingCooldownBeforeLocatingNewFlower;
         }

         boolean flag = this.isAngry() && !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0D;
         this.setRolling(flag);
         if (this.tickCount % 20 == 0 && !this.isHiveValid()) {
            this.hivePos = null;
         }
      }

   }

   boolean isHiveValid() {
      if (!this.hasHive()) {
         return false;
      } else if (this.isTooFarAway(this.hivePos)) {
         return false;
      } else {
         BlockEntity blockentity = this.level().getBlockEntity(this.hivePos);
         return blockentity instanceof BeehiveBlockEntity;
      }
   }

   public boolean hasNectar() {
      return this.getFlag(8);
   }

   void setHasNectar(boolean pHasNectar) {
      if (pHasNectar) {
         this.resetTicksWithoutNectarSinceExitingHive();
      }

      this.setFlag(8, pHasNectar);
   }

   public boolean hasStung() {
      return this.getFlag(4);
   }

   private void setHasStung(boolean pHasStung) {
      this.setFlag(4, pHasStung);
   }

   private boolean isRolling() {
      return this.getFlag(2);
   }

   private void setRolling(boolean pIsRolling) {
      this.setFlag(2, pIsRolling);
   }

   boolean isTooFarAway(BlockPos pPos) {
      return !this.closerThan(pPos, 32);
   }

   private void setFlag(int pFlagId, boolean pValue) {
      if (pValue) {
         this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | pFlagId));
      } else {
         this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~pFlagId));
      }

   }

   private boolean getFlag(int pFlagId) {
      return (this.entityData.get(DATA_FLAGS_ID) & pFlagId) != 0;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.FLYING_SPEED, (double)0.6F).add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.ATTACK_DAMAGE, 2.0D).add(Attributes.FOLLOW_RANGE, 48.0D);
   }

   protected PathNavigation createNavigation(Level pLevel) {
      FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel) {
         public boolean isStableDestination(BlockPos p_27947_) {
            return !this.level.getBlockState(p_27947_.below()).isAir();
         }

         public void tick() {
            if (!Bee.this.beePollinateGoal.isPollinating()) {
               super.tick();
            }
         }
      };
      flyingpathnavigation.setCanOpenDoors(false);
      flyingpathnavigation.setCanFloat(false);
      flyingpathnavigation.setCanPassDoors(true);
      return flyingpathnavigation;
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return pStack.is(ItemTags.FLOWERS);
   }

   boolean isFlowerValid(BlockPos pPos) {
      return this.level().isLoaded(pPos) && this.level().getBlockState(pPos).is(BlockTags.FLOWERS);
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
   }

   protected SoundEvent getAmbientSound() {
      return null;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.BEE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.BEE_DEATH;
   }

   /**
    * Returns the volume for the sounds this mob makes.
    */
   protected float getSoundVolume() {
      return 0.4F;
   }

   @Nullable
   public Bee getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      return EntityType.BEE.create(pLevel);
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return this.isBaby() ? pSize.height * 0.5F : pSize.height * 0.5F;
   }

   protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
   }

   public boolean isFlapping() {
      return this.isFlying() && this.tickCount % TICKS_PER_FLAP == 0;
   }

   public boolean isFlying() {
      return !this.onGround();
   }

   public void dropOffNectar() {
      this.setHasNectar(false);
      this.resetNumCropsGrownSincePollination();
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         if (!this.level().isClientSide) {
            this.beePollinateGoal.stopPollinating();
         }

         return super.hurt(pSource, pAmount);
      }
   }

   public MobType getMobType() {
      return MobType.ARTHROPOD;
   }

   @Deprecated // FORGE: use jumpInFluid instead
   protected void jumpInLiquid(TagKey<Fluid> pFluidTag) {
      this.jumpInLiquidInternal();
   }

   private void jumpInLiquidInternal() {
      this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.01D, 0.0D));
   }

   @Override
   public void jumpInFluid(net.minecraftforge.fluids.FluidType type) {
      this.jumpInLiquidInternal();
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.2F));
   }

   boolean closerThan(BlockPos pPos, int pDistance) {
      return pPos.closerThan(this.blockPosition(), (double)pDistance);
   }

   abstract class BaseBeeGoal extends Goal {
      public abstract boolean canBeeUse();

      public abstract boolean canBeeContinueToUse();

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.canBeeUse() && !Bee.this.isAngry();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.canBeeContinueToUse() && !Bee.this.isAngry();
      }
   }

   class BeeAttackGoal extends MeleeAttackGoal {
      BeeAttackGoal(PathfinderMob pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
         super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && Bee.this.isAngry() && !Bee.this.hasStung();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return super.canContinueToUse() && Bee.this.isAngry() && !Bee.this.hasStung();
      }
   }

   static class BeeBecomeAngryTargetGoal extends NearestAttackableTargetGoal<Player> {
      BeeBecomeAngryTargetGoal(Bee pMob) {
         super(pMob, Player.class, 10, true, false, pMob::isAngryAt);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.beeCanTarget() && super.canUse();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         boolean flag = this.beeCanTarget();
         if (flag && this.mob.getTarget() != null) {
            return super.canContinueToUse();
         } else {
            this.targetMob = null;
            return false;
         }
      }

      private boolean beeCanTarget() {
         Bee bee = (Bee)this.mob;
         return bee.isAngry() && !bee.hasStung();
      }
   }

   class BeeEnterHiveGoal extends Bee.BaseBeeGoal {
      public boolean canBeeUse() {
         if (Bee.this.hasHive() && Bee.this.wantsToEnterHive() && Bee.this.hivePos.closerToCenterThan(Bee.this.position(), 2.0D)) {
            BlockEntity blockentity = Bee.this.level().getBlockEntity(Bee.this.hivePos);
            if (blockentity instanceof BeehiveBlockEntity) {
               BeehiveBlockEntity beehiveblockentity = (BeehiveBlockEntity)blockentity;
               if (!beehiveblockentity.isFull()) {
                  return true;
               }

               Bee.this.hivePos = null;
            }
         }

         return false;
      }

      public boolean canBeeContinueToUse() {
         return false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         BlockEntity blockentity = Bee.this.level().getBlockEntity(Bee.this.hivePos);
         if (blockentity instanceof BeehiveBlockEntity beehiveblockentity) {
            beehiveblockentity.addOccupant(Bee.this, Bee.this.hasNectar());
         }

      }
   }

   @VisibleForDebug
   public class BeeGoToHiveGoal extends Bee.BaseBeeGoal {
      public static final int MAX_TRAVELLING_TICKS = 600;
      int travellingTicks = Bee.this.level().random.nextInt(10);
      private static final int MAX_BLACKLISTED_TARGETS = 3;
      final List<BlockPos> blacklistedTargets = Lists.newArrayList();
      @Nullable
      private Path lastPath;
      private static final int TICKS_BEFORE_HIVE_DROP = 60;
      private int ticksStuck;

      BeeGoToHiveGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      public boolean canBeeUse() {
         return Bee.this.hivePos != null && !Bee.this.hasRestriction() && Bee.this.wantsToEnterHive() && !this.hasReachedTarget(Bee.this.hivePos) && Bee.this.level().getBlockState(Bee.this.hivePos).is(BlockTags.BEEHIVES);
      }

      public boolean canBeeContinueToUse() {
         return this.canBeeUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.travellingTicks = 0;
         this.ticksStuck = 0;
         super.start();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.travellingTicks = 0;
         this.ticksStuck = 0;
         Bee.this.navigation.stop();
         Bee.this.navigation.resetMaxVisitedNodesMultiplier();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (Bee.this.hivePos != null) {
            ++this.travellingTicks;
            if (this.travellingTicks > this.adjustedTickDelay(600)) {
               this.dropAndBlacklistHive();
            } else if (!Bee.this.navigation.isInProgress()) {
               if (!Bee.this.closerThan(Bee.this.hivePos, 16)) {
                  if (Bee.this.isTooFarAway(Bee.this.hivePos)) {
                     this.dropHive();
                  } else {
                     Bee.this.pathfindRandomlyTowards(Bee.this.hivePos);
                  }
               } else {
                  boolean flag = this.pathfindDirectlyTowards(Bee.this.hivePos);
                  if (!flag) {
                     this.dropAndBlacklistHive();
                  } else if (this.lastPath != null && Bee.this.navigation.getPath().sameAs(this.lastPath)) {
                     ++this.ticksStuck;
                     if (this.ticksStuck > 60) {
                        this.dropHive();
                        this.ticksStuck = 0;
                     }
                  } else {
                     this.lastPath = Bee.this.navigation.getPath();
                  }

               }
            }
         }
      }

      private boolean pathfindDirectlyTowards(BlockPos pPos) {
         Bee.this.navigation.setMaxVisitedNodesMultiplier(10.0F);
         Bee.this.navigation.moveTo((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), 1.0D);
         return Bee.this.navigation.getPath() != null && Bee.this.navigation.getPath().canReach();
      }

      boolean isTargetBlacklisted(BlockPos pPos) {
         return this.blacklistedTargets.contains(pPos);
      }

      private void blacklistTarget(BlockPos pPos) {
         this.blacklistedTargets.add(pPos);

         while(this.blacklistedTargets.size() > 3) {
            this.blacklistedTargets.remove(0);
         }

      }

      void clearBlacklist() {
         this.blacklistedTargets.clear();
      }

      private void dropAndBlacklistHive() {
         if (Bee.this.hivePos != null) {
            this.blacklistTarget(Bee.this.hivePos);
         }

         this.dropHive();
      }

      private void dropHive() {
         Bee.this.hivePos = null;
         Bee.this.remainingCooldownBeforeLocatingNewHive = 200;
      }

      private boolean hasReachedTarget(BlockPos pPos) {
         if (Bee.this.closerThan(pPos, 2)) {
            return true;
         } else {
            Path path = Bee.this.navigation.getPath();
            return path != null && path.getTarget().equals(pPos) && path.canReach() && path.isDone();
         }
      }
   }

   public class BeeGoToKnownFlowerGoal extends Bee.BaseBeeGoal {
      private static final int MAX_TRAVELLING_TICKS = 600;
      int travellingTicks = Bee.this.level().random.nextInt(10);

      BeeGoToKnownFlowerGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      public boolean canBeeUse() {
         return Bee.this.savedFlowerPos != null && !Bee.this.hasRestriction() && this.wantsToGoToKnownFlower() && Bee.this.isFlowerValid(Bee.this.savedFlowerPos) && !Bee.this.closerThan(Bee.this.savedFlowerPos, 2);
      }

      public boolean canBeeContinueToUse() {
         return this.canBeeUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.travellingTicks = 0;
         super.start();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.travellingTicks = 0;
         Bee.this.navigation.stop();
         Bee.this.navigation.resetMaxVisitedNodesMultiplier();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (Bee.this.savedFlowerPos != null) {
            ++this.travellingTicks;
            if (this.travellingTicks > this.adjustedTickDelay(600)) {
               Bee.this.savedFlowerPos = null;
            } else if (!Bee.this.navigation.isInProgress()) {
               if (Bee.this.isTooFarAway(Bee.this.savedFlowerPos)) {
                  Bee.this.savedFlowerPos = null;
               } else {
                  Bee.this.pathfindRandomlyTowards(Bee.this.savedFlowerPos);
               }
            }
         }
      }

      private boolean wantsToGoToKnownFlower() {
         return Bee.this.ticksWithoutNectarSinceExitingHive > 2400;
      }
   }

   class BeeGrowCropGoal extends Bee.BaseBeeGoal {
      static final int GROW_CHANCE = 30;

      public boolean canBeeUse() {
         if (Bee.this.getCropsGrownSincePollination() >= 10) {
            return false;
         } else if (Bee.this.random.nextFloat() < 0.3F) {
            return false;
         } else {
            return Bee.this.hasNectar() && Bee.this.isHiveValid();
         }
      }

      public boolean canBeeContinueToUse() {
         return this.canBeeUse();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (Bee.this.random.nextInt(this.adjustedTickDelay(30)) == 0) {
            for(int i = 1; i <= 2; ++i) {
               BlockPos blockpos = Bee.this.blockPosition().below(i);
               BlockState blockstate = Bee.this.level().getBlockState(blockpos);
               Block block = blockstate.getBlock();
               BlockState blockstate1 = null;
               if (blockstate.is(BlockTags.BEE_GROWABLES)) {
                  if (block instanceof CropBlock) {
                     CropBlock cropblock = (CropBlock)block;
                     if (!cropblock.isMaxAge(blockstate)) {
                        blockstate1 = cropblock.getStateForAge(cropblock.getAge(blockstate) + 1);
                     }
                  } else if (block instanceof StemBlock) {
                     int k = blockstate.getValue(StemBlock.AGE);
                     if (k < 7) {
                        blockstate1 = blockstate.setValue(StemBlock.AGE, Integer.valueOf(k + 1));
                     }
                  } else if (blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                     int j = blockstate.getValue(SweetBerryBushBlock.AGE);
                     if (j < 3) {
                        blockstate1 = blockstate.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(j + 1));
                     }
                  } else if (blockstate.is(Blocks.CAVE_VINES) || blockstate.is(Blocks.CAVE_VINES_PLANT)) {
                     ((BonemealableBlock)blockstate.getBlock()).performBonemeal((ServerLevel)Bee.this.level(), Bee.this.random, blockpos, blockstate);
                  }

                  if (blockstate1 != null) {
                     Bee.this.level().levelEvent(2005, blockpos, 0);
                     Bee.this.level().setBlockAndUpdate(blockpos, blockstate1);
                     Bee.this.incrementNumCropsGrownSincePollination();
                  }
               }
            }

         }
      }
   }

   class BeeHurtByOtherGoal extends HurtByTargetGoal {
      BeeHurtByOtherGoal(Bee pMob) {
         super(pMob);
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return Bee.this.isAngry() && super.canContinueToUse();
      }

      protected void alertOther(Mob pMob, LivingEntity pTarget) {
         if (pMob instanceof Bee && this.mob.hasLineOfSight(pTarget)) {
            pMob.setTarget(pTarget);
         }

      }
   }

   class BeeLocateHiveGoal extends Bee.BaseBeeGoal {
      public boolean canBeeUse() {
         return Bee.this.remainingCooldownBeforeLocatingNewHive == 0 && !Bee.this.hasHive() && Bee.this.wantsToEnterHive();
      }

      public boolean canBeeContinueToUse() {
         return false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Bee.this.remainingCooldownBeforeLocatingNewHive = 200;
         List<BlockPos> list = this.findNearbyHivesWithSpace();
         if (!list.isEmpty()) {
            for(BlockPos blockpos : list) {
               if (!Bee.this.goToHiveGoal.isTargetBlacklisted(blockpos)) {
                  Bee.this.hivePos = blockpos;
                  return;
               }
            }

            Bee.this.goToHiveGoal.clearBlacklist();
            Bee.this.hivePos = list.get(0);
         }
      }

      private List<BlockPos> findNearbyHivesWithSpace() {
         BlockPos blockpos = Bee.this.blockPosition();
         PoiManager poimanager = ((ServerLevel)Bee.this.level()).getPoiManager();
         Stream<PoiRecord> stream = poimanager.getInRange((p_218130_) -> {
            return p_218130_.is(PoiTypeTags.BEE_HOME);
         }, blockpos, 20, PoiManager.Occupancy.ANY);
         return stream.map(PoiRecord::getPos).filter(Bee.this::doesHiveHaveSpace).sorted(Comparator.comparingDouble((p_148811_) -> {
            return p_148811_.distSqr(blockpos);
         })).collect(Collectors.toList());
      }
   }

   class BeeLookControl extends LookControl {
      BeeLookControl(Mob pMob) {
         super(pMob);
      }

      /**
       * Updates look
       */
      public void tick() {
         if (!Bee.this.isAngry()) {
            super.tick();
         }
      }

      protected boolean resetXRotOnTick() {
         return !Bee.this.beePollinateGoal.isPollinating();
      }
   }

   class BeePollinateGoal extends Bee.BaseBeeGoal {
      private static final int MIN_POLLINATION_TICKS = 400;
      private static final int MIN_FIND_FLOWER_RETRY_COOLDOWN = 20;
      private static final int MAX_FIND_FLOWER_RETRY_COOLDOWN = 60;
      private final Predicate<BlockState> VALID_POLLINATION_BLOCKS = (p_28074_) -> {
         if (p_28074_.hasProperty(BlockStateProperties.WATERLOGGED) && p_28074_.getValue(BlockStateProperties.WATERLOGGED)) {
            return false;
         } else if (p_28074_.is(BlockTags.FLOWERS)) {
            if (p_28074_.is(Blocks.SUNFLOWER)) {
               return p_28074_.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
            } else {
               return true;
            }
         } else {
            return false;
         }
      };
      private static final double ARRIVAL_THRESHOLD = 0.1D;
      private static final int POSITION_CHANGE_CHANCE = 25;
      private static final float SPEED_MODIFIER = 0.35F;
      private static final float HOVER_HEIGHT_WITHIN_FLOWER = 0.6F;
      private static final float HOVER_POS_OFFSET = 0.33333334F;
      private int successfulPollinatingTicks;
      private int lastSoundPlayedTick;
      private boolean pollinating;
      @Nullable
      private Vec3 hoverPos;
      private int pollinatingTicks;
      private static final int MAX_POLLINATING_TICKS = 600;

      BeePollinateGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      public boolean canBeeUse() {
         if (Bee.this.remainingCooldownBeforeLocatingNewFlower > 0) {
            return false;
         } else if (Bee.this.hasNectar()) {
            return false;
         } else if (Bee.this.level().isRaining()) {
            return false;
         } else {
            Optional<BlockPos> optional = this.findNearbyFlower();
            if (optional.isPresent()) {
               Bee.this.savedFlowerPos = optional.get();
               Bee.this.navigation.moveTo((double)Bee.this.savedFlowerPos.getX() + 0.5D, (double)Bee.this.savedFlowerPos.getY() + 0.5D, (double)Bee.this.savedFlowerPos.getZ() + 0.5D, (double)1.2F);
               return true;
            } else {
               Bee.this.remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(Bee.this.random, 20, 60);
               return false;
            }
         }
      }

      public boolean canBeeContinueToUse() {
         if (!this.pollinating) {
            return false;
         } else if (!Bee.this.hasSavedFlowerPos()) {
            return false;
         } else if (Bee.this.level().isRaining()) {
            return false;
         } else if (this.hasPollinatedLongEnough()) {
            return Bee.this.random.nextFloat() < 0.2F;
         } else if (Bee.this.tickCount % 20 == 0 && !Bee.this.isFlowerValid(Bee.this.savedFlowerPos)) {
            Bee.this.savedFlowerPos = null;
            return false;
         } else {
            return true;
         }
      }

      private boolean hasPollinatedLongEnough() {
         return this.successfulPollinatingTicks > 400;
      }

      boolean isPollinating() {
         return this.pollinating;
      }

      void stopPollinating() {
         this.pollinating = false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.successfulPollinatingTicks = 0;
         this.pollinatingTicks = 0;
         this.lastSoundPlayedTick = 0;
         this.pollinating = true;
         Bee.this.resetTicksWithoutNectarSinceExitingHive();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         if (this.hasPollinatedLongEnough()) {
            Bee.this.setHasNectar(true);
         }

         this.pollinating = false;
         Bee.this.navigation.stop();
         Bee.this.remainingCooldownBeforeLocatingNewFlower = 200;
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         ++this.pollinatingTicks;
         if (this.pollinatingTicks > 600) {
            Bee.this.savedFlowerPos = null;
         } else {
            Vec3 vec3 = Vec3.atBottomCenterOf(Bee.this.savedFlowerPos).add(0.0D, (double)0.6F, 0.0D);
            if (vec3.distanceTo(Bee.this.position()) > 1.0D) {
               this.hoverPos = vec3;
               this.setWantedPos();
            } else {
               if (this.hoverPos == null) {
                  this.hoverPos = vec3;
               }

               boolean flag = Bee.this.position().distanceTo(this.hoverPos) <= 0.1D;
               boolean flag1 = true;
               if (!flag && this.pollinatingTicks > 600) {
                  Bee.this.savedFlowerPos = null;
               } else {
                  if (flag) {
                     boolean flag2 = Bee.this.random.nextInt(25) == 0;
                     if (flag2) {
                        this.hoverPos = new Vec3(vec3.x() + (double)this.getOffset(), vec3.y(), vec3.z() + (double)this.getOffset());
                        Bee.this.navigation.stop();
                     } else {
                        flag1 = false;
                     }

                     Bee.this.getLookControl().setLookAt(vec3.x(), vec3.y(), vec3.z());
                  }

                  if (flag1) {
                     this.setWantedPos();
                  }

                  ++this.successfulPollinatingTicks;
                  if (Bee.this.random.nextFloat() < 0.05F && this.successfulPollinatingTicks > this.lastSoundPlayedTick + 60) {
                     this.lastSoundPlayedTick = this.successfulPollinatingTicks;
                     Bee.this.playSound(SoundEvents.BEE_POLLINATE, 1.0F, 1.0F);
                  }

               }
            }
         }
      }

      private void setWantedPos() {
         Bee.this.getMoveControl().setWantedPosition(this.hoverPos.x(), this.hoverPos.y(), this.hoverPos.z(), (double)0.35F);
      }

      private float getOffset() {
         return (Bee.this.random.nextFloat() * 2.0F - 1.0F) * 0.33333334F;
      }

      private Optional<BlockPos> findNearbyFlower() {
         return this.findNearestBlock(this.VALID_POLLINATION_BLOCKS, 5.0D);
      }

      private Optional<BlockPos> findNearestBlock(Predicate<BlockState> pPredicate, double pDistance) {
         BlockPos blockpos = Bee.this.blockPosition();
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         for(int i = 0; (double)i <= pDistance; i = i > 0 ? -i : 1 - i) {
            for(int j = 0; (double)j < pDistance; ++j) {
               for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                  for(int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
                     blockpos$mutableblockpos.setWithOffset(blockpos, k, i - 1, l);
                     if (blockpos.closerThan(blockpos$mutableblockpos, pDistance) && pPredicate.test(Bee.this.level().getBlockState(blockpos$mutableblockpos))) {
                        return Optional.of(blockpos$mutableblockpos);
                     }
                  }
               }
            }
         }

         return Optional.empty();
      }
   }

   class BeeWanderGoal extends Goal {
      private static final int WANDER_THRESHOLD = 22;

      BeeWanderGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return Bee.this.navigation.isDone() && Bee.this.random.nextInt(10) == 0;
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return Bee.this.navigation.isInProgress();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Vec3 vec3 = this.findPos();
         if (vec3 != null) {
            Bee.this.navigation.moveTo(Bee.this.navigation.createPath(BlockPos.containing(vec3), 1), 1.0D);
         }

      }

      @Nullable
      private Vec3 findPos() {
         Vec3 vec3;
         if (Bee.this.isHiveValid() && !Bee.this.closerThan(Bee.this.hivePos, 22)) {
            Vec3 vec31 = Vec3.atCenterOf(Bee.this.hivePos);
            vec3 = vec31.subtract(Bee.this.position()).normalize();
         } else {
            vec3 = Bee.this.getViewVector(0.0F);
         }

         int i = 8;
         Vec3 vec32 = HoverRandomPos.getPos(Bee.this, 8, 7, vec3.x, vec3.z, ((float)Math.PI / 2F), 3, 1);
         return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(Bee.this, 8, 4, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
      }
   }
}
