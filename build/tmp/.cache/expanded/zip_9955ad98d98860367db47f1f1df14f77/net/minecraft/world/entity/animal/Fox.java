package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Fox extends Animal implements VariantHolder<Fox.Type> {
   private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.BYTE);
   private static final int FLAG_SITTING = 1;
   public static final int FLAG_CROUCHING = 4;
   public static final int FLAG_INTERESTED = 8;
   public static final int FLAG_POUNCING = 16;
   private static final int FLAG_SLEEPING = 32;
   private static final int FLAG_FACEPLANTED = 64;
   private static final int FLAG_DEFENDING = 128;
   private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_0 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_1 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_UUID);
   static final Predicate<ItemEntity> ALLOWED_ITEMS = (p_289438_) -> {
      return !p_289438_.hasPickUpDelay() && p_289438_.isAlive();
   };
   private static final Predicate<Entity> TRUSTED_TARGET_SELECTOR = (p_287481_) -> {
      if (!(p_287481_ instanceof LivingEntity livingentity)) {
         return false;
      } else {
         return livingentity.getLastHurtMob() != null && livingentity.getLastHurtMobTimestamp() < livingentity.tickCount + 600;
      }
   };
   static final Predicate<Entity> STALKABLE_PREY = (p_28498_) -> {
      return p_28498_ instanceof Chicken || p_28498_ instanceof Rabbit;
   };
   private static final Predicate<Entity> AVOID_PLAYERS = (p_28463_) -> {
      return !p_28463_.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(p_28463_);
   };
   private static final int MIN_TICKS_BEFORE_EAT = 600;
   private Goal landTargetGoal;
   private Goal turtleEggTargetGoal;
   private Goal fishTargetGoal;
   private float interestedAngle;
   private float interestedAngleO;
   float crouchAmount;
   float crouchAmountO;
   private int ticksSinceEaten;

   public Fox(EntityType<? extends Fox> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.lookControl = new Fox.FoxLookControl();
      this.moveControl = new Fox.FoxMoveControl();
      this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 0.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 0.0F);
      this.setCanPickUpLoot(true);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_TRUSTED_ID_0, Optional.empty());
      this.entityData.define(DATA_TRUSTED_ID_1, Optional.empty());
      this.entityData.define(DATA_TYPE_ID, 0);
      this.entityData.define(DATA_FLAGS_ID, (byte)0);
   }

   protected void registerGoals() {
      this.landTargetGoal = new NearestAttackableTargetGoal<>(this, Animal.class, 10, false, false, (p_28604_) -> {
         return p_28604_ instanceof Chicken || p_28604_ instanceof Rabbit;
      });
      this.turtleEggTargetGoal = new NearestAttackableTargetGoal<>(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR);
      this.fishTargetGoal = new NearestAttackableTargetGoal<>(this, AbstractFish.class, 20, false, false, (p_28600_) -> {
         return p_28600_ instanceof AbstractSchoolingFish;
      });
      this.goalSelector.addGoal(0, new Fox.FoxFloatGoal());
      this.goalSelector.addGoal(0, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
      this.goalSelector.addGoal(1, new Fox.FaceplantGoal());
      this.goalSelector.addGoal(2, new Fox.FoxPanicGoal(2.2D));
      this.goalSelector.addGoal(3, new Fox.FoxBreedGoal(1.0D));
      this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Player.class, 16.0F, 1.6D, 1.4D, (p_289437_) -> {
         return AVOID_PLAYERS.test(p_289437_) && !this.trusts(p_289437_.getUUID()) && !this.isDefending();
      }));
      this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Wolf.class, 8.0F, 1.6D, 1.4D, (p_289439_) -> {
         return !((Wolf)p_289439_).isTame() && !this.isDefending();
      }));
      this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, PolarBear.class, 8.0F, 1.6D, 1.4D, (p_28585_) -> {
         return !this.isDefending();
      }));
      this.goalSelector.addGoal(5, new Fox.StalkPreyGoal());
      this.goalSelector.addGoal(6, new Fox.FoxPounceGoal());
      this.goalSelector.addGoal(6, new Fox.SeekShelterGoal(1.25D));
      this.goalSelector.addGoal(7, new Fox.FoxMeleeAttackGoal((double)1.2F, true));
      this.goalSelector.addGoal(7, new Fox.SleepGoal());
      this.goalSelector.addGoal(8, new Fox.FoxFollowParentGoal(this, 1.25D));
      this.goalSelector.addGoal(9, new Fox.FoxStrollThroughVillageGoal(32, 200));
      this.goalSelector.addGoal(10, new Fox.FoxEatBerriesGoal((double)1.2F, 12, 1));
      this.goalSelector.addGoal(10, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0D));
      this.goalSelector.addGoal(11, new Fox.FoxSearchForItemsGoal());
      this.goalSelector.addGoal(12, new Fox.FoxLookAtPlayerGoal(this, Player.class, 24.0F));
      this.goalSelector.addGoal(13, new Fox.PerchAndSearchGoal());
      this.targetSelector.addGoal(3, new Fox.DefendTrustedTargetGoal(LivingEntity.class, false, false, (p_289440_) -> {
         return TRUSTED_TARGET_SELECTOR.test(p_289440_) && !this.trusts(p_289440_.getUUID());
      }));
   }

   public SoundEvent getEatingSound(ItemStack pItemStack) {
      return SoundEvents.FOX_EAT;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
         ++this.ticksSinceEaten;
         ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
         if (this.canEat(itemstack)) {
            if (this.ticksSinceEaten > 600) {
               ItemStack itemstack1 = itemstack.finishUsingItem(this.level(), this);
               if (!itemstack1.isEmpty()) {
                  this.setItemSlot(EquipmentSlot.MAINHAND, itemstack1);
               }

               this.ticksSinceEaten = 0;
            } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1F) {
               this.playSound(this.getEatingSound(itemstack), 1.0F, 1.0F);
               this.level().broadcastEntityEvent(this, (byte)45);
            }
         }

         LivingEntity livingentity = this.getTarget();
         if (livingentity == null || !livingentity.isAlive()) {
            this.setIsCrouching(false);
            this.setIsInterested(false);
         }
      }

      if (this.isSleeping() || this.isImmobile()) {
         this.jumping = false;
         this.xxa = 0.0F;
         this.zza = 0.0F;
      }

      super.aiStep();
      if (this.isDefending() && this.random.nextFloat() < 0.05F) {
         this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
      }

   }

   /**
    * Dead and sleeping entities cannot move
    */
   protected boolean isImmobile() {
      return this.isDeadOrDying();
   }

   private boolean canEat(ItemStack pStack) {
      return pStack.getItem().isEdible() && this.getTarget() == null && this.onGround() && !this.isSleeping();
   }

   protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
      if (pRandom.nextFloat() < 0.2F) {
         float f = pRandom.nextFloat();
         ItemStack itemstack;
         if (f < 0.05F) {
            itemstack = new ItemStack(Items.EMERALD);
         } else if (f < 0.2F) {
            itemstack = new ItemStack(Items.EGG);
         } else if (f < 0.4F) {
            itemstack = pRandom.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
         } else if (f < 0.6F) {
            itemstack = new ItemStack(Items.WHEAT);
         } else if (f < 0.8F) {
            itemstack = new ItemStack(Items.LEATHER);
         } else {
            itemstack = new ItemStack(Items.FEATHER);
         }

         this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
      }

   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 45) {
         ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
         if (!itemstack.isEmpty()) {
            for(int i = 0; i < 8; ++i) {
               Vec3 vec3 = (new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D)).xRot(-this.getXRot() * ((float)Math.PI / 180F)).yRot(-this.getYRot() * ((float)Math.PI / 180F));
               this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemstack), this.getX() + this.getLookAngle().x / 2.0D, this.getY(), this.getZ() + this.getLookAngle().z / 2.0D, vec3.x, vec3.y + 0.05D, vec3.z);
            }
         }
      } else {
         super.handleEntityEvent(pId);
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.FOLLOW_RANGE, 32.0D).add(Attributes.ATTACK_DAMAGE, 2.0D);
   }

   @Nullable
   public Fox getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      Fox fox = EntityType.FOX.create(pLevel);
      if (fox != null) {
         fox.setVariant(this.random.nextBoolean() ? this.getVariant() : ((Fox)pOtherParent).getVariant());
      }

      return fox;
   }

   public static boolean checkFoxSpawnRules(EntityType<Fox> pFox, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.FOXES_SPAWNABLE_ON) && isBrightEnoughToSpawn(pLevel, pPos);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      Holder<Biome> holder = pLevel.getBiome(this.blockPosition());
      Fox.Type fox$type = Fox.Type.byBiome(holder);
      boolean flag = false;
      if (pSpawnData instanceof Fox.FoxGroupData fox$foxgroupdata) {
         fox$type = fox$foxgroupdata.type;
         if (fox$foxgroupdata.getGroupSize() >= 2) {
            flag = true;
         }
      } else {
         pSpawnData = new Fox.FoxGroupData(fox$type);
      }

      this.setVariant(fox$type);
      if (flag) {
         this.setAge(-24000);
      }

      if (pLevel instanceof ServerLevel) {
         this.setTargetGoals();
      }

      this.populateDefaultEquipmentSlots(pLevel.getRandom(), pDifficulty);
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   private void setTargetGoals() {
      if (this.getVariant() == Fox.Type.RED) {
         this.targetSelector.addGoal(4, this.landTargetGoal);
         this.targetSelector.addGoal(4, this.turtleEggTargetGoal);
         this.targetSelector.addGoal(6, this.fishTargetGoal);
      } else {
         this.targetSelector.addGoal(4, this.fishTargetGoal);
         this.targetSelector.addGoal(6, this.landTargetGoal);
         this.targetSelector.addGoal(6, this.turtleEggTargetGoal);
      }

   }

   protected void usePlayerItem(Player pPlayer, InteractionHand pHand, ItemStack pStack) {
      if (this.isFood(pStack)) {
         this.playSound(this.getEatingSound(pStack), 1.0F, 1.0F);
      }

      super.usePlayerItem(pPlayer, pHand, pStack);
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return this.isBaby() ? pSize.height * 0.85F : 0.4F;
   }

   public Fox.Type getVariant() {
      return Fox.Type.byId(this.entityData.get(DATA_TYPE_ID));
   }

   public void setVariant(Fox.Type pVariant) {
      this.entityData.set(DATA_TYPE_ID, pVariant.getId());
   }

   List<UUID> getTrustedUUIDs() {
      List<UUID> list = Lists.newArrayList();
      list.add(this.entityData.get(DATA_TRUSTED_ID_0).orElse((UUID)null));
      list.add(this.entityData.get(DATA_TRUSTED_ID_1).orElse((UUID)null));
      return list;
   }

   void addTrustedUUID(@Nullable UUID pUuid) {
      if (this.entityData.get(DATA_TRUSTED_ID_0).isPresent()) {
         this.entityData.set(DATA_TRUSTED_ID_1, Optional.ofNullable(pUuid));
      } else {
         this.entityData.set(DATA_TRUSTED_ID_0, Optional.ofNullable(pUuid));
      }

   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      List<UUID> list = this.getTrustedUUIDs();
      ListTag listtag = new ListTag();

      for(UUID uuid : list) {
         if (uuid != null) {
            listtag.add(NbtUtils.createUUID(uuid));
         }
      }

      pCompound.put("Trusted", listtag);
      pCompound.putBoolean("Sleeping", this.isSleeping());
      pCompound.putString("Type", this.getVariant().getSerializedName());
      pCompound.putBoolean("Sitting", this.isSitting());
      pCompound.putBoolean("Crouching", this.isCrouching());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      ListTag listtag = pCompound.getList("Trusted", 11);

      for(int i = 0; i < listtag.size(); ++i) {
         this.addTrustedUUID(NbtUtils.loadUUID(listtag.get(i)));
      }

      this.setSleeping(pCompound.getBoolean("Sleeping"));
      this.setVariant(Fox.Type.byName(pCompound.getString("Type")));
      this.setSitting(pCompound.getBoolean("Sitting"));
      this.setIsCrouching(pCompound.getBoolean("Crouching"));
      if (this.level() instanceof ServerLevel) {
         this.setTargetGoals();
      }

   }

   public boolean isSitting() {
      return this.getFlag(1);
   }

   public void setSitting(boolean pSitting) {
      this.setFlag(1, pSitting);
   }

   public boolean isFaceplanted() {
      return this.getFlag(64);
   }

   void setFaceplanted(boolean pFaceplanted) {
      this.setFlag(64, pFaceplanted);
   }

   boolean isDefending() {
      return this.getFlag(128);
   }

   void setDefending(boolean pDefending) {
      this.setFlag(128, pDefending);
   }

   /**
    * Returns whether player is sleeping or not
    */
   public boolean isSleeping() {
      return this.getFlag(32);
   }

   void setSleeping(boolean pSleeping) {
      this.setFlag(32, pSleeping);
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

   public boolean canTakeItem(ItemStack pItemstack) {
      EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(pItemstack);
      if (!this.getItemBySlot(equipmentslot).isEmpty()) {
         return false;
      } else {
         return equipmentslot == EquipmentSlot.MAINHAND && super.canTakeItem(pItemstack);
      }
   }

   public boolean canHoldItem(ItemStack pStack) {
      Item item = pStack.getItem();
      ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
      return itemstack.isEmpty() || this.ticksSinceEaten > 0 && item.isEdible() && !itemstack.getItem().isEdible();
   }

   private void spitOutItem(ItemStack pStack) {
      if (!pStack.isEmpty() && !this.level().isClientSide) {
         ItemEntity itementity = new ItemEntity(this.level(), this.getX() + this.getLookAngle().x, this.getY() + 1.0D, this.getZ() + this.getLookAngle().z, pStack);
         itementity.setPickUpDelay(40);
         itementity.setThrower(this.getUUID());
         this.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
         this.level().addFreshEntity(itementity);
      }
   }

   private void dropItemStack(ItemStack pStack) {
      ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), pStack);
      this.level().addFreshEntity(itementity);
   }

   /**
    * Tests if this entity should pick up a weapon or an armor piece. Entity drops current weapon or armor if the new
    * one is better.
    */
   protected void pickUpItem(ItemEntity pItemEntity) {
      ItemStack itemstack = pItemEntity.getItem();
      if (this.canHoldItem(itemstack)) {
         int i = itemstack.getCount();
         if (i > 1) {
            this.dropItemStack(itemstack.split(i - 1));
         }

         this.spitOutItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
         this.onItemPickup(pItemEntity);
         this.setItemSlot(EquipmentSlot.MAINHAND, itemstack.split(1));
         this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
         this.take(pItemEntity, itemstack.getCount());
         pItemEntity.discard();
         this.ticksSinceEaten = 0;
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.isEffectiveAi()) {
         boolean flag = this.isInWater();
         if (flag || this.getTarget() != null || this.level().isThundering()) {
            this.wakeUp();
         }

         if (flag || this.isSleeping()) {
            this.setSitting(false);
         }

         if (this.isFaceplanted() && this.level().random.nextFloat() < 0.2F) {
            BlockPos blockpos = this.blockPosition();
            BlockState blockstate = this.level().getBlockState(blockpos);
            this.level().levelEvent(2001, blockpos, Block.getId(blockstate));
         }
      }

      this.interestedAngleO = this.interestedAngle;
      if (this.isInterested()) {
         this.interestedAngle += (1.0F - this.interestedAngle) * 0.4F;
      } else {
         this.interestedAngle += (0.0F - this.interestedAngle) * 0.4F;
      }

      this.crouchAmountO = this.crouchAmount;
      if (this.isCrouching()) {
         this.crouchAmount += 0.2F;
         if (this.crouchAmount > 3.0F) {
            this.crouchAmount = 3.0F;
         }
      } else {
         this.crouchAmount = 0.0F;
      }

   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return pStack.is(ItemTags.FOX_FOOD);
   }

   protected void onOffspringSpawnedFromEgg(Player pPlayer, Mob pChild) {
      ((Fox)pChild).addTrustedUUID(pPlayer.getUUID());
   }

   public boolean isPouncing() {
      return this.getFlag(16);
   }

   public void setIsPouncing(boolean pIsPouncing) {
      this.setFlag(16, pIsPouncing);
   }

   public boolean isJumping() {
      return this.jumping;
   }

   public boolean isFullyCrouched() {
      return this.crouchAmount == 3.0F;
   }

   public void setIsCrouching(boolean pIsCrouching) {
      this.setFlag(4, pIsCrouching);
   }

   public boolean isCrouching() {
      return this.getFlag(4);
   }

   public void setIsInterested(boolean pIsInterested) {
      this.setFlag(8, pIsInterested);
   }

   public boolean isInterested() {
      return this.getFlag(8);
   }

   public float getHeadRollAngle(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.interestedAngleO, this.interestedAngle) * 0.11F * (float)Math.PI;
   }

   public float getCrouchAmount(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.crouchAmountO, this.crouchAmount);
   }

   /**
    * Sets the active target the Goal system uses for tracking
    */
   public void setTarget(@Nullable LivingEntity pLivingEntity) {
      if (this.isDefending() && pLivingEntity == null) {
         this.setDefending(false);
      }

      super.setTarget(pLivingEntity);
   }

   protected int calculateFallDamage(float pDistance, float pDamageMultiplier) {
      return Mth.ceil((pDistance - 5.0F) * pDamageMultiplier);
   }

   void wakeUp() {
      this.setSleeping(false);
   }

   void clearStates() {
      this.setIsInterested(false);
      this.setIsCrouching(false);
      this.setSitting(false);
      this.setSleeping(false);
      this.setDefending(false);
      this.setFaceplanted(false);
   }

   boolean canMove() {
      return !this.isSleeping() && !this.isSitting() && !this.isFaceplanted();
   }

   /**
    * Plays living's sound at its position
    */
   public void playAmbientSound() {
      SoundEvent soundevent = this.getAmbientSound();
      if (soundevent == SoundEvents.FOX_SCREECH) {
         this.playSound(soundevent, 2.0F, this.getVoicePitch());
      } else {
         super.playAmbientSound();
      }

   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.isSleeping()) {
         return SoundEvents.FOX_SLEEP;
      } else {
         if (!this.level().isDay() && this.random.nextFloat() < 0.1F) {
            List<Player> list = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0D, 16.0D, 16.0D), EntitySelector.NO_SPECTATORS);
            if (list.isEmpty()) {
               return SoundEvents.FOX_SCREECH;
            }
         }

         return SoundEvents.FOX_AMBIENT;
      }
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.FOX_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.FOX_DEATH;
   }

   boolean trusts(UUID pUuid) {
      return this.getTrustedUUIDs().contains(pUuid);
   }

   protected void dropAllDeathLoot(DamageSource pDamageSource) {
      super.dropAllDeathLoot(pDamageSource);
   }
   protected void dropEquipment() { // Forge: move extra drops to dropEquipment to allow them to be captured by LivingDropsEvent
      super.dropEquipment();
      ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
      if (!itemstack.isEmpty()) {
         this.spawnAtLocation(itemstack);
         this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      }

   }

   public static boolean isPathClear(Fox pFox, LivingEntity pLivingEntity) {
      double d0 = pLivingEntity.getZ() - pFox.getZ();
      double d1 = pLivingEntity.getX() - pFox.getX();
      double d2 = d0 / d1;
      int i = 6;

      for(int j = 0; j < 6; ++j) {
         double d3 = d2 == 0.0D ? 0.0D : d0 * (double)((float)j / 6.0F);
         double d4 = d2 == 0.0D ? d1 * (double)((float)j / 6.0F) : d3 / d2;

         for(int k = 1; k < 4; ++k) {
            if (!pFox.level().getBlockState(BlockPos.containing(pFox.getX() + d4, pFox.getY() + (double)k, pFox.getZ() + d3)).canBeReplaced()) {
               return false;
            }
         }
      }

      return true;
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)(0.55F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }

   class DefendTrustedTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {
      @Nullable
      private LivingEntity trustedLastHurtBy;
      @Nullable
      private LivingEntity trustedLastHurt;
      private int timestamp;

      public DefendTrustedTargetGoal(Class<LivingEntity> pTargetType, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pPredicate) {
         super(Fox.this, pTargetType, 10, pMustSee, pMustReach, pPredicate);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
         } else {
            for(UUID uuid : Fox.this.getTrustedUUIDs()) {
               if (uuid != null && Fox.this.level() instanceof ServerLevel) {
                  Entity entity = ((ServerLevel)Fox.this.level()).getEntity(uuid);
                  if (entity instanceof LivingEntity) {
                     LivingEntity livingentity = (LivingEntity)entity;
                     this.trustedLastHurt = livingentity;
                     this.trustedLastHurtBy = livingentity.getLastHurtByMob();
                     int i = livingentity.getLastHurtByMobTimestamp();
                     return i != this.timestamp && this.canAttack(this.trustedLastHurtBy, this.targetConditions);
                  }
               }
            }

            return false;
         }
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.setTarget(this.trustedLastHurtBy);
         this.target = this.trustedLastHurtBy;
         if (this.trustedLastHurt != null) {
            this.timestamp = this.trustedLastHurt.getLastHurtByMobTimestamp();
         }

         Fox.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
         Fox.this.setDefending(true);
         Fox.this.wakeUp();
         super.start();
      }
   }

   class FaceplantGoal extends Goal {
      int countdown;

      public FaceplantGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return Fox.this.isFaceplanted();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.canUse() && this.countdown > 0;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.countdown = this.adjustedTickDelay(40);
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         Fox.this.setFaceplanted(false);
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         --this.countdown;
      }
   }

   public class FoxAlertableEntitiesSelector implements Predicate<LivingEntity> {
      public boolean test(LivingEntity pEntity) {
         if (pEntity instanceof Fox) {
            return false;
         } else if (!(pEntity instanceof Chicken) && !(pEntity instanceof Rabbit) && !(pEntity instanceof Monster)) {
            if (pEntity instanceof TamableAnimal) {
               return !((TamableAnimal)pEntity).isTame();
            } else if (!(pEntity instanceof Player) || !pEntity.isSpectator() && !((Player)pEntity).isCreative()) {
               if (Fox.this.trusts(pEntity.getUUID())) {
                  return false;
               } else {
                  return !pEntity.isSleeping() && !pEntity.isDiscrete();
               }
            } else {
               return false;
            }
         } else {
            return true;
         }
      }
   }

   abstract class FoxBehaviorGoal extends Goal {
      private final TargetingConditions alertableTargeting = TargetingConditions.forCombat().range(12.0D).ignoreLineOfSight().selector(Fox.this.new FoxAlertableEntitiesSelector());

      protected boolean hasShelter() {
         BlockPos blockpos = BlockPos.containing(Fox.this.getX(), Fox.this.getBoundingBox().maxY, Fox.this.getZ());
         return !Fox.this.level().canSeeSky(blockpos) && Fox.this.getWalkTargetValue(blockpos) >= 0.0F;
      }

      protected boolean alertable() {
         return !Fox.this.level().getNearbyEntities(LivingEntity.class, this.alertableTargeting, Fox.this, Fox.this.getBoundingBox().inflate(12.0D, 6.0D, 12.0D)).isEmpty();
      }
   }

   class FoxBreedGoal extends BreedGoal {
      public FoxBreedGoal(double pSpeedModifier) {
         super(Fox.this, pSpeedModifier);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         ((Fox)this.animal).clearStates();
         ((Fox)this.partner).clearStates();
         super.start();
      }

      /**
       * Spawns a baby animal of the same type.
       */
      protected void breed() {
         ServerLevel serverlevel = (ServerLevel)this.level;
         Fox fox = (Fox)this.animal.getBreedOffspring(serverlevel, this.partner);
         final net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(animal, partner, fox);
         final boolean cancelled = net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
         fox = (Fox) event.getChild();
         if (cancelled) {
            //Reset the "inLove" state for the animals
            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            return;
         }
         if (fox != null) {
            ServerPlayer serverplayer = this.animal.getLoveCause();
            ServerPlayer serverplayer1 = this.partner.getLoveCause();
            ServerPlayer serverplayer2 = serverplayer;
            if (serverplayer != null) {
               fox.addTrustedUUID(serverplayer.getUUID());
            } else {
               serverplayer2 = serverplayer1;
            }

            if (serverplayer1 != null && serverplayer != serverplayer1) {
               fox.addTrustedUUID(serverplayer1.getUUID());
            }

            if (serverplayer2 != null) {
               serverplayer2.awardStat(Stats.ANIMALS_BRED);
               CriteriaTriggers.BRED_ANIMALS.trigger(serverplayer2, this.animal, this.partner, fox);
            }

            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            fox.setAge(-24000);
            fox.moveTo(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
            serverlevel.addFreshEntityWithPassengers(fox);
            this.level.broadcastEntityEvent(this.animal, (byte)18);
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
               this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
            }

         }
      }
   }

   public class FoxEatBerriesGoal extends MoveToBlockGoal {
      private static final int WAIT_TICKS = 40;
      protected int ticksWaited;

      public FoxEatBerriesGoal(double pSpeedModifier, int pSearchRange, int pVerticalSearchRange) {
         super(Fox.this, pSpeedModifier, pSearchRange, pVerticalSearchRange);
      }

      public double acceptedDistance() {
         return 2.0D;
      }

      public boolean shouldRecalculatePath() {
         return this.tryTicks % 100 == 0;
      }

      /**
       * Return {@code true} to set given position as destination
       */
      protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
         BlockState blockstate = pLevel.getBlockState(pPos);
         return blockstate.is(Blocks.SWEET_BERRY_BUSH) && blockstate.getValue(SweetBerryBushBlock.AGE) >= 2 || CaveVines.hasGlowBerries(blockstate);
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.isReachedTarget()) {
            if (this.ticksWaited >= 40) {
               this.onReachedTarget();
            } else {
               ++this.ticksWaited;
            }
         } else if (!this.isReachedTarget() && Fox.this.random.nextFloat() < 0.05F) {
            Fox.this.playSound(SoundEvents.FOX_SNIFF, 1.0F, 1.0F);
         }

         super.tick();
      }

      protected void onReachedTarget() {
         if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(Fox.this.level(), Fox.this)) {
            BlockState blockstate = Fox.this.level().getBlockState(this.blockPos);
            if (blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
               this.pickSweetBerries(blockstate);
            } else if (CaveVines.hasGlowBerries(blockstate)) {
               this.pickGlowBerry(blockstate);
            }

         }
      }

      private void pickGlowBerry(BlockState pState) {
         CaveVines.use(Fox.this, pState, Fox.this.level(), this.blockPos);
      }

      private void pickSweetBerries(BlockState pState) {
         int i = pState.getValue(SweetBerryBushBlock.AGE);
         pState.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1));
         int j = 1 + Fox.this.level().random.nextInt(2) + (i == 3 ? 1 : 0);
         ItemStack itemstack = Fox.this.getItemBySlot(EquipmentSlot.MAINHAND);
         if (itemstack.isEmpty()) {
            Fox.this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
            --j;
         }

         if (j > 0) {
            Block.popResource(Fox.this.level(), this.blockPos, new ItemStack(Items.SWEET_BERRIES, j));
         }

         Fox.this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
         Fox.this.level().setBlock(this.blockPos, pState.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1)), 2);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return !Fox.this.isSleeping() && super.canUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.ticksWaited = 0;
         Fox.this.setSitting(false);
         super.start();
      }
   }

   class FoxFloatGoal extends FloatGoal {
      public FoxFloatGoal() {
         super(Fox.this);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         Fox.this.clearStates();
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return Fox.this.isInWater() && Fox.this.getFluidHeight(FluidTags.WATER) > 0.25D || Fox.this.isInLava() || Fox.this.isInFluidType((fluidType, height) -> Fox.this.canSwimInFluidType(fluidType) && height > 0.25D);
      }
   }

   class FoxFollowParentGoal extends FollowParentGoal {
      private final Fox fox;

      public FoxFollowParentGoal(Fox pFox, double pSpeedModifier) {
         super(pFox, pSpeedModifier);
         this.fox = pFox;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return !this.fox.isDefending() && super.canUse();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return !this.fox.isDefending() && super.canContinueToUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.fox.clearStates();
         super.start();
      }
   }

   public static class FoxGroupData extends AgeableMob.AgeableMobGroupData {
      public final Fox.Type type;

      public FoxGroupData(Fox.Type pType) {
         super(false);
         this.type = pType;
      }
   }

   class FoxLookAtPlayerGoal extends LookAtPlayerGoal {
      public FoxLookAtPlayerGoal(Mob pMob, Class<? extends LivingEntity> pLookAtType, float pLookDistance) {
         super(pMob, pLookAtType, pLookDistance);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && !Fox.this.isFaceplanted() && !Fox.this.isInterested();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return super.canContinueToUse() && !Fox.this.isFaceplanted() && !Fox.this.isInterested();
      }
   }

   public class FoxLookControl extends LookControl {
      public FoxLookControl() {
         super(Fox.this);
      }

      /**
       * Updates look
       */
      public void tick() {
         if (!Fox.this.isSleeping()) {
            super.tick();
         }

      }

      protected boolean resetXRotOnTick() {
         return !Fox.this.isPouncing() && !Fox.this.isCrouching() && !Fox.this.isInterested() && !Fox.this.isFaceplanted();
      }
   }

   class FoxMeleeAttackGoal extends MeleeAttackGoal {
      public FoxMeleeAttackGoal(double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
         super(Fox.this, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
      }

      protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
         double d0 = this.getAttackReachSqr(pEnemy);
         if (pDistToEnemySqr <= d0 && this.isTimeToAttack()) {
            this.resetAttackCooldown();
            this.mob.doHurtTarget(pEnemy);
            Fox.this.playSound(SoundEvents.FOX_BITE, 1.0F, 1.0F);
         }

      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Fox.this.setIsInterested(false);
         super.start();
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return !Fox.this.isSitting() && !Fox.this.isSleeping() && !Fox.this.isCrouching() && !Fox.this.isFaceplanted() && super.canUse();
      }
   }

   class FoxMoveControl extends MoveControl {
      public FoxMoveControl() {
         super(Fox.this);
      }

      public void tick() {
         if (Fox.this.canMove()) {
            super.tick();
         }

      }
   }

   class FoxPanicGoal extends PanicGoal {
      public FoxPanicGoal(double pSpeedModifier) {
         super(Fox.this, pSpeedModifier);
      }

      public boolean shouldPanic() {
         return !Fox.this.isDefending() && super.shouldPanic();
      }
   }

   public class FoxPounceGoal extends JumpGoal {
      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (!Fox.this.isFullyCrouched()) {
            return false;
         } else {
            LivingEntity livingentity = Fox.this.getTarget();
            if (livingentity != null && livingentity.isAlive()) {
               if (livingentity.getMotionDirection() != livingentity.getDirection()) {
                  return false;
               } else {
                  boolean flag = Fox.isPathClear(Fox.this, livingentity);
                  if (!flag) {
                     Fox.this.getNavigation().createPath(livingentity, 0);
                     Fox.this.setIsCrouching(false);
                     Fox.this.setIsInterested(false);
                  }

                  return flag;
               }
            } else {
               return false;
            }
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         LivingEntity livingentity = Fox.this.getTarget();
         if (livingentity != null && livingentity.isAlive()) {
            double d0 = Fox.this.getDeltaMovement().y;
            return (!(d0 * d0 < (double)0.05F) || !(Math.abs(Fox.this.getXRot()) < 15.0F) || !Fox.this.onGround()) && !Fox.this.isFaceplanted();
         } else {
            return false;
         }
      }

      public boolean isInterruptable() {
         return false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Fox.this.setJumping(true);
         Fox.this.setIsPouncing(true);
         Fox.this.setIsInterested(false);
         LivingEntity livingentity = Fox.this.getTarget();
         if (livingentity != null) {
            Fox.this.getLookControl().setLookAt(livingentity, 60.0F, 30.0F);
            Vec3 vec3 = (new Vec3(livingentity.getX() - Fox.this.getX(), livingentity.getY() - Fox.this.getY(), livingentity.getZ() - Fox.this.getZ())).normalize();
            Fox.this.setDeltaMovement(Fox.this.getDeltaMovement().add(vec3.x * 0.8D, 0.9D, vec3.z * 0.8D));
         }

         Fox.this.getNavigation().stop();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         Fox.this.setIsCrouching(false);
         Fox.this.crouchAmount = 0.0F;
         Fox.this.crouchAmountO = 0.0F;
         Fox.this.setIsInterested(false);
         Fox.this.setIsPouncing(false);
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         LivingEntity livingentity = Fox.this.getTarget();
         if (livingentity != null) {
            Fox.this.getLookControl().setLookAt(livingentity, 60.0F, 30.0F);
         }

         if (!Fox.this.isFaceplanted()) {
            Vec3 vec3 = Fox.this.getDeltaMovement();
            if (vec3.y * vec3.y < (double)0.03F && Fox.this.getXRot() != 0.0F) {
               Fox.this.setXRot(Mth.rotLerp(0.2F, Fox.this.getXRot(), 0.0F));
            } else {
               double d0 = vec3.horizontalDistance();
               double d1 = Math.signum(-vec3.y) * Math.acos(d0 / vec3.length()) * (double)(180F / (float)Math.PI);
               Fox.this.setXRot((float)d1);
            }
         }

         if (livingentity != null && Fox.this.distanceTo(livingentity) <= 2.0F) {
            Fox.this.doHurtTarget(livingentity);
         } else if (Fox.this.getXRot() > 0.0F && Fox.this.onGround() && (float)Fox.this.getDeltaMovement().y != 0.0F && Fox.this.level().getBlockState(Fox.this.blockPosition()).is(Blocks.SNOW)) {
            Fox.this.setXRot(60.0F);
            Fox.this.setTarget((LivingEntity)null);
            Fox.this.setFaceplanted(true);
         }

      }
   }

   class FoxSearchForItemsGoal extends Goal {
      public FoxSearchForItemsGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (!Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            return false;
         } else if (Fox.this.getTarget() == null && Fox.this.getLastHurtByMob() == null) {
            if (!Fox.this.canMove()) {
               return false;
            } else if (Fox.this.getRandom().nextInt(reducedTickDelay(10)) != 0) {
               return false;
            } else {
               List<ItemEntity> list = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), Fox.ALLOWED_ITEMS);
               return !list.isEmpty() && Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
            }
         } else {
            return false;
         }
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         List<ItemEntity> list = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), Fox.ALLOWED_ITEMS);
         ItemStack itemstack = Fox.this.getItemBySlot(EquipmentSlot.MAINHAND);
         if (itemstack.isEmpty() && !list.isEmpty()) {
            Fox.this.getNavigation().moveTo(list.get(0), (double)1.2F);
         }

      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         List<ItemEntity> list = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), Fox.ALLOWED_ITEMS);
         if (!list.isEmpty()) {
            Fox.this.getNavigation().moveTo(list.get(0), (double)1.2F);
         }

      }
   }

   class FoxStrollThroughVillageGoal extends StrollThroughVillageGoal {
      public FoxStrollThroughVillageGoal(int pUnused32, int pInterval) {
         super(Fox.this, pInterval);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Fox.this.clearStates();
         super.start();
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && this.canFoxMove();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return super.canContinueToUse() && this.canFoxMove();
      }

      private boolean canFoxMove() {
         return !Fox.this.isSleeping() && !Fox.this.isSitting() && !Fox.this.isDefending() && Fox.this.getTarget() == null;
      }
   }

   class PerchAndSearchGoal extends Fox.FoxBehaviorGoal {
      private double relX;
      private double relZ;
      private int lookTime;
      private int looksRemaining;

      public PerchAndSearchGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return Fox.this.getLastHurtByMob() == null && Fox.this.getRandom().nextFloat() < 0.02F && !Fox.this.isSleeping() && Fox.this.getTarget() == null && Fox.this.getNavigation().isDone() && !this.alertable() && !Fox.this.isPouncing() && !Fox.this.isCrouching();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.looksRemaining > 0;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.resetLook();
         this.looksRemaining = 2 + Fox.this.getRandom().nextInt(3);
         Fox.this.setSitting(true);
         Fox.this.getNavigation().stop();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         Fox.this.setSitting(false);
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         --this.lookTime;
         if (this.lookTime <= 0) {
            --this.looksRemaining;
            this.resetLook();
         }

         Fox.this.getLookControl().setLookAt(Fox.this.getX() + this.relX, Fox.this.getEyeY(), Fox.this.getZ() + this.relZ, (float)Fox.this.getMaxHeadYRot(), (float)Fox.this.getMaxHeadXRot());
      }

      private void resetLook() {
         double d0 = (Math.PI * 2D) * Fox.this.getRandom().nextDouble();
         this.relX = Math.cos(d0);
         this.relZ = Math.sin(d0);
         this.lookTime = this.adjustedTickDelay(80 + Fox.this.getRandom().nextInt(20));
      }
   }

   class SeekShelterGoal extends FleeSunGoal {
      private int interval = reducedTickDelay(100);

      public SeekShelterGoal(double pSpeedModifier) {
         super(Fox.this, pSpeedModifier);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (!Fox.this.isSleeping() && this.mob.getTarget() == null) {
            if (Fox.this.level().isThundering() && Fox.this.level().canSeeSky(this.mob.blockPosition())) {
               return this.setWantedPos();
            } else if (this.interval > 0) {
               --this.interval;
               return false;
            } else {
               this.interval = 100;
               BlockPos blockpos = this.mob.blockPosition();
               return Fox.this.level().isDay() && Fox.this.level().canSeeSky(blockpos) && !((ServerLevel)Fox.this.level()).isVillage(blockpos) && this.setWantedPos();
            }
         } else {
            return false;
         }
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Fox.this.clearStates();
         super.start();
      }
   }

   class SleepGoal extends Fox.FoxBehaviorGoal {
      private static final int WAIT_TIME_BEFORE_SLEEP = reducedTickDelay(140);
      private int countdown = Fox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);

      public SleepGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (Fox.this.xxa == 0.0F && Fox.this.yya == 0.0F && Fox.this.zza == 0.0F) {
            return this.canSleep() || Fox.this.isSleeping();
         } else {
            return false;
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.canSleep();
      }

      private boolean canSleep() {
         if (this.countdown > 0) {
            --this.countdown;
            return false;
         } else {
            return Fox.this.level().isDay() && this.hasShelter() && !this.alertable() && !Fox.this.isInPowderSnow;
         }
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.countdown = Fox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);
         Fox.this.clearStates();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Fox.this.setSitting(false);
         Fox.this.setIsCrouching(false);
         Fox.this.setIsInterested(false);
         Fox.this.setJumping(false);
         Fox.this.setSleeping(true);
         Fox.this.getNavigation().stop();
         Fox.this.getMoveControl().setWantedPosition(Fox.this.getX(), Fox.this.getY(), Fox.this.getZ(), 0.0D);
      }
   }

   class StalkPreyGoal extends Goal {
      public StalkPreyGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (Fox.this.isSleeping()) {
            return false;
         } else {
            LivingEntity livingentity = Fox.this.getTarget();
            return livingentity != null && livingentity.isAlive() && Fox.STALKABLE_PREY.test(livingentity) && Fox.this.distanceToSqr(livingentity) > 36.0D && !Fox.this.isCrouching() && !Fox.this.isInterested() && !Fox.this.jumping;
         }
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Fox.this.setSitting(false);
         Fox.this.setFaceplanted(false);
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         LivingEntity livingentity = Fox.this.getTarget();
         if (livingentity != null && Fox.isPathClear(Fox.this, livingentity)) {
            Fox.this.setIsInterested(true);
            Fox.this.setIsCrouching(true);
            Fox.this.getNavigation().stop();
            Fox.this.getLookControl().setLookAt(livingentity, (float)Fox.this.getMaxHeadYRot(), (float)Fox.this.getMaxHeadXRot());
         } else {
            Fox.this.setIsInterested(false);
            Fox.this.setIsCrouching(false);
         }

      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         LivingEntity livingentity = Fox.this.getTarget();
         if (livingentity != null) {
            Fox.this.getLookControl().setLookAt(livingentity, (float)Fox.this.getMaxHeadYRot(), (float)Fox.this.getMaxHeadXRot());
            if (Fox.this.distanceToSqr(livingentity) <= 36.0D) {
               Fox.this.setIsInterested(true);
               Fox.this.setIsCrouching(true);
               Fox.this.getNavigation().stop();
            } else {
               Fox.this.getNavigation().moveTo(livingentity, 1.5D);
            }

         }
      }
   }

   public static enum Type implements StringRepresentable {
      RED(0, "red"),
      SNOW(1, "snow");

      public static final StringRepresentable.EnumCodec<Fox.Type> CODEC = StringRepresentable.fromEnum(Fox.Type::values);
      private static final IntFunction<Fox.Type> BY_ID = ByIdMap.continuous(Fox.Type::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
      private final int id;
      private final String name;

      private Type(int pId, String pName) {
         this.id = pId;
         this.name = pName;
      }

      public String getSerializedName() {
         return this.name;
      }

      public int getId() {
         return this.id;
      }

      public static Fox.Type byName(String pName) {
         return CODEC.byName(pName, RED);
      }

      public static Fox.Type byId(int pIndex) {
         return BY_ID.apply(pIndex);
      }

      public static Fox.Type byBiome(Holder<Biome> pBiome) {
         return pBiome.is(BiomeTags.SPAWNS_SNOW_FOXES) ? SNOW : RED;
      }
   }
}
