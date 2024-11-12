package net.minecraft.world.entity.animal.goat;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Goat extends Animal {
   public static final EntityDimensions LONG_JUMPING_DIMENSIONS = EntityDimensions.scalable(0.9F, 1.3F).scale(0.7F);
   private static final int ADULT_ATTACK_DAMAGE = 2;
   private static final int BABY_ATTACK_DAMAGE = 1;
   protected static final ImmutableList<SensorType<? extends Sensor<? super Goat>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.GOAT_TEMPTATIONS);
   protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleType.RAM_TARGET, MemoryModuleType.IS_PANICKING);
   public static final int GOAT_FALL_DAMAGE_REDUCTION = 10;
   public static final double GOAT_SCREAMING_CHANCE = 0.02D;
   public static final double UNIHORN_CHANCE = (double)0.1F;
   private static final EntityDataAccessor<Boolean> DATA_IS_SCREAMING_GOAT = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_HAS_LEFT_HORN = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_HAS_RIGHT_HORN = SynchedEntityData.defineId(Goat.class, EntityDataSerializers.BOOLEAN);
   private boolean isLoweringHead;
   private int lowerHeadTick;

   public Goat(EntityType<? extends Goat> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.getNavigation().setCanFloat(true);
      this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
   }

   public ItemStack createHorn() {
      RandomSource randomsource = RandomSource.create((long)this.getUUID().hashCode());
      TagKey<Instrument> tagkey = this.isScreamingGoat() ? InstrumentTags.SCREAMING_GOAT_HORNS : InstrumentTags.REGULAR_GOAT_HORNS;
      HolderSet<Instrument> holderset = BuiltInRegistries.INSTRUMENT.getOrCreateTag(tagkey);
      return InstrumentItem.create(Items.GOAT_HORN, holderset.getRandomElement(randomsource).get());
   }

   protected Brain.Provider<Goat> brainProvider() {
      return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
   }

   protected Brain<?> makeBrain(Dynamic<?> pDynamic) {
      return GoatAi.makeBrain(this.brainProvider().makeBrain(pDynamic));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.MOVEMENT_SPEED, (double)0.2F).add(Attributes.ATTACK_DAMAGE, 2.0D);
   }

   protected void ageBoundaryReached() {
      if (this.isBaby()) {
         this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1.0D);
         this.removeHorns();
      } else {
         this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0D);
         this.addHorns();
      }

   }

   protected int calculateFallDamage(float pFallDistance, float pDamageMultiplier) {
      return super.calculateFallDamage(pFallDistance, pDamageMultiplier) - 10;
   }

   protected SoundEvent getAmbientSound() {
      return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_AMBIENT : SoundEvents.GOAT_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_HURT : SoundEvents.GOAT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_DEATH : SoundEvents.GOAT_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pState) {
      this.playSound(SoundEvents.GOAT_STEP, 0.15F, 1.0F);
   }

   protected SoundEvent getMilkingSound() {
      return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
   }

   @Nullable
   public Goat getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      Goat goat = EntityType.GOAT.create(pLevel);
      if (goat != null) {
         boolean flag1;
         label22: {
            label21: {
               GoatAi.initMemories(goat, pLevel.getRandom());
               AgeableMob ageablemob = (AgeableMob)(pLevel.getRandom().nextBoolean() ? this : pOtherParent);
               if (ageablemob instanceof Goat) {
                  Goat goat1 = (Goat)ageablemob;
                  if (goat1.isScreamingGoat()) {
                     break label21;
                  }
               }

               if (!(pLevel.getRandom().nextDouble() < 0.02D)) {
                  flag1 = false;
                  break label22;
               }
            }

            flag1 = true;
         }

         boolean flag = flag1;
         goat.setScreamingGoat(flag);
      }

      return goat;
   }

   public Brain<Goat> getBrain() {
      return (Brain<Goat>)super.getBrain();
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("goatBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().pop();
      this.level().getProfiler().push("goatActivityUpdate");
      GoatAi.updateActivity(this);
      this.level().getProfiler().pop();
      super.customServerAiStep();
   }

   public int getMaxHeadYRot() {
      return 15;
   }

   /**
    * Sets the head's Y rotation of the entity.
    */
   public void setYHeadRot(float pRotation) {
      int i = this.getMaxHeadYRot();
      float f = Mth.degreesDifference(this.yBodyRot, pRotation);
      float f1 = Mth.clamp(f, (float)(-i), (float)i);
      super.setYHeadRot(this.yBodyRot + f1);
   }

   public SoundEvent getEatingSound(ItemStack pStack) {
      return this.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_EAT : SoundEvents.GOAT_EAT;
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.is(Items.BUCKET) && !this.isBaby()) {
         pPlayer.playSound(this.getMilkingSound(), 1.0F, 1.0F);
         ItemStack itemstack1 = ItemUtils.createFilledResult(itemstack, pPlayer, Items.MILK_BUCKET.getDefaultInstance());
         pPlayer.setItemInHand(pHand, itemstack1);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         InteractionResult interactionresult = super.mobInteract(pPlayer, pHand);
         if (interactionresult.consumesAction() && this.isFood(itemstack)) {
            this.level().playSound((Player)null, this, this.getEatingSound(itemstack), SoundSource.NEUTRAL, 1.0F, Mth.randomBetween(this.level().random, 0.8F, 1.2F));
         }

         return interactionresult;
      }
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      RandomSource randomsource = pLevel.getRandom();
      GoatAi.initMemories(this, randomsource);
      this.setScreamingGoat(randomsource.nextDouble() < 0.02D);
      this.ageBoundaryReached();
      if (!this.isBaby() && (double)randomsource.nextFloat() < (double)0.1F) {
         EntityDataAccessor<Boolean> entitydataaccessor = randomsource.nextBoolean() ? DATA_HAS_LEFT_HORN : DATA_HAS_RIGHT_HORN;
         this.entityData.set(entitydataaccessor, false);
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   public EntityDimensions getDimensions(Pose pPose) {
      return pPose == Pose.LONG_JUMPING ? LONG_JUMPING_DIMENSIONS.scale(this.getScale()) : super.getDimensions(pPose);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("IsScreamingGoat", this.isScreamingGoat());
      pCompound.putBoolean("HasLeftHorn", this.hasLeftHorn());
      pCompound.putBoolean("HasRightHorn", this.hasRightHorn());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setScreamingGoat(pCompound.getBoolean("IsScreamingGoat"));
      this.entityData.set(DATA_HAS_LEFT_HORN, pCompound.getBoolean("HasLeftHorn"));
      this.entityData.set(DATA_HAS_RIGHT_HORN, pCompound.getBoolean("HasRightHorn"));
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 58) {
         this.isLoweringHead = true;
      } else if (pId == 59) {
         this.isLoweringHead = false;
      } else {
         super.handleEntityEvent(pId);
      }

   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.isLoweringHead) {
         ++this.lowerHeadTick;
      } else {
         this.lowerHeadTick -= 2;
      }

      this.lowerHeadTick = Mth.clamp(this.lowerHeadTick, 0, 20);
      super.aiStep();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_IS_SCREAMING_GOAT, false);
      this.entityData.define(DATA_HAS_LEFT_HORN, true);
      this.entityData.define(DATA_HAS_RIGHT_HORN, true);
   }

   public boolean hasLeftHorn() {
      return this.entityData.get(DATA_HAS_LEFT_HORN);
   }

   public boolean hasRightHorn() {
      return this.entityData.get(DATA_HAS_RIGHT_HORN);
   }

   public boolean dropHorn() {
      boolean flag = this.hasLeftHorn();
      boolean flag1 = this.hasRightHorn();
      if (!flag && !flag1) {
         return false;
      } else {
         EntityDataAccessor<Boolean> entitydataaccessor;
         if (!flag) {
            entitydataaccessor = DATA_HAS_RIGHT_HORN;
         } else if (!flag1) {
            entitydataaccessor = DATA_HAS_LEFT_HORN;
         } else {
            entitydataaccessor = this.random.nextBoolean() ? DATA_HAS_LEFT_HORN : DATA_HAS_RIGHT_HORN;
         }

         this.entityData.set(entitydataaccessor, false);
         Vec3 vec3 = this.position();
         ItemStack itemstack = this.createHorn();
         double d0 = (double)Mth.randomBetween(this.random, -0.2F, 0.2F);
         double d1 = (double)Mth.randomBetween(this.random, 0.3F, 0.7F);
         double d2 = (double)Mth.randomBetween(this.random, -0.2F, 0.2F);
         ItemEntity itementity = new ItemEntity(this.level(), vec3.x(), vec3.y(), vec3.z(), itemstack, d0, d1, d2);
         this.level().addFreshEntity(itementity);
         return true;
      }
   }

   public void addHorns() {
      this.entityData.set(DATA_HAS_LEFT_HORN, true);
      this.entityData.set(DATA_HAS_RIGHT_HORN, true);
   }

   public void removeHorns() {
      this.entityData.set(DATA_HAS_LEFT_HORN, false);
      this.entityData.set(DATA_HAS_RIGHT_HORN, false);
   }

   public boolean isScreamingGoat() {
      return this.entityData.get(DATA_IS_SCREAMING_GOAT);
   }

   public void setScreamingGoat(boolean pIsScreamingGoat) {
      this.entityData.set(DATA_IS_SCREAMING_GOAT, pIsScreamingGoat);
   }

   public float getRammingXHeadRot() {
      return (float)this.lowerHeadTick / 20.0F * 30.0F * ((float)Math.PI / 180F);
   }

   public static boolean checkGoatSpawnRules(EntityType<? extends Animal> pGoat, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.GOATS_SPAWNABLE_ON) && isBrightEnoughToSpawn(pLevel, pPos);
   }
}