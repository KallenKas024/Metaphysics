package net.minecraft.world.entity.animal.horse;

import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStandGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractHorse extends Animal implements ContainerListener, HasCustomInventoryScreen, OwnableEntity, PlayerRideableJumping, Saddleable {
   public static final int EQUIPMENT_SLOT_OFFSET = 400;
   public static final int CHEST_SLOT_OFFSET = 499;
   public static final int INVENTORY_SLOT_OFFSET = 500;
   public static final double BREEDING_CROSS_FACTOR = 0.15D;
   private static final float MIN_MOVEMENT_SPEED = (float)generateSpeed(() -> {
      return 0.0D;
   });
   private static final float MAX_MOVEMENT_SPEED = (float)generateSpeed(() -> {
      return 1.0D;
   });
   private static final float MIN_JUMP_STRENGTH = (float)generateJumpStrength(() -> {
      return 0.0D;
   });
   private static final float MAX_JUMP_STRENGTH = (float)generateJumpStrength(() -> {
      return 1.0D;
   });
   private static final float MIN_HEALTH = generateMaxHealth((p_272505_) -> {
      return 0;
   });
   private static final float MAX_HEALTH = generateMaxHealth((p_272504_) -> {
      return p_272504_ - 1;
   });
   private static final float BACKWARDS_MOVE_SPEED_FACTOR = 0.25F;
   private static final float SIDEWAYS_MOVE_SPEED_FACTOR = 0.5F;
   private static final Predicate<LivingEntity> PARENT_HORSE_SELECTOR = (p_30636_) -> {
      return p_30636_ instanceof AbstractHorse && ((AbstractHorse)p_30636_).isBred();
   };
   private static final TargetingConditions MOMMY_TARGETING = TargetingConditions.forNonCombat().range(16.0D).ignoreLineOfSight().selector(PARENT_HORSE_SELECTOR);
   private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Items.SUGAR, Blocks.HAY_BLOCK.asItem(), Items.APPLE, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
   private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BYTE);
   private static final int FLAG_TAME = 2;
   private static final int FLAG_SADDLE = 4;
   private static final int FLAG_BRED = 8;
   private static final int FLAG_EATING = 16;
   private static final int FLAG_STANDING = 32;
   private static final int FLAG_OPEN_MOUTH = 64;
   public static final int INV_SLOT_SADDLE = 0;
   public static final int INV_SLOT_ARMOR = 1;
   public static final int INV_BASE_COUNT = 2;
   private int eatingCounter;
   private int mouthCounter;
   private int standCounter;
   public int tailCounter;
   public int sprintCounter;
   protected boolean isJumping;
   protected SimpleContainer inventory;
   /** The higher this value, the more likely the horse is to be tamed next time a player rides it. */
   protected int temper;
   protected float playerJumpPendingScale;
   protected boolean allowStandSliding;
   private float eatAnim;
   private float eatAnimO;
   private float standAnim;
   private float standAnimO;
   private float mouthAnim;
   private float mouthAnimO;
   protected boolean canGallop = true;
   /** Used to determine the sound that the horse should make when it steps */
   protected int gallopSoundCounter;
   @Nullable
   private UUID owner;

   protected AbstractHorse(EntityType<? extends AbstractHorse> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setMaxUpStep(1.0F);
      this.createInventory();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.2D));
      this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2D));
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D, AbstractHorse.class));
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      if (this.canPerformRearing()) {
         this.goalSelector.addGoal(9, new RandomStandGoal(this));
      }

      this.addBehaviourGoals();
   }

   protected void addBehaviourGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE), false));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_FLAGS, (byte)0);
   }

   protected boolean getFlag(int pFlagId) {
      return (this.entityData.get(DATA_ID_FLAGS) & pFlagId) != 0;
   }

   protected void setFlag(int pFlagId, boolean pValue) {
      byte b0 = this.entityData.get(DATA_ID_FLAGS);
      if (pValue) {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 | pFlagId));
      } else {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 & ~pFlagId));
      }

   }

   public boolean isTamed() {
      return this.getFlag(2);
   }

   @Nullable
   public UUID getOwnerUUID() {
      return this.owner;
   }

   public void setOwnerUUID(@Nullable UUID pUuid) {
      this.owner = pUuid;
   }

   public boolean isJumping() {
      return this.isJumping;
   }

   public void setTamed(boolean pTamed) {
      this.setFlag(2, pTamed);
   }

   public void setIsJumping(boolean pJumping) {
      this.isJumping = pJumping;
   }

   protected void onLeashDistance(float pDistance) {
      if (pDistance > 6.0F && this.isEating()) {
         this.setEating(false);
      }

   }

   public boolean isEating() {
      return this.getFlag(16);
   }

   public boolean isStanding() {
      return this.getFlag(32);
   }

   public boolean isBred() {
      return this.getFlag(8);
   }

   public void setBred(boolean pBreeding) {
      this.setFlag(8, pBreeding);
   }

   public boolean isSaddleable() {
      return this.isAlive() && !this.isBaby() && this.isTamed();
   }

   public void equipSaddle(@Nullable SoundSource pSource) {
      this.inventory.setItem(0, new ItemStack(Items.SADDLE));
   }

   public void equipArmor(Player pPlayer, ItemStack pArmor) {
      if (this.isArmor(pArmor)) {
         this.inventory.setItem(1, pArmor.copyWithCount(1));
         if (!pPlayer.getAbilities().instabuild) {
            pArmor.shrink(1);
         }
      }

   }

   public boolean isSaddled() {
      return this.getFlag(4);
   }

   public int getTemper() {
      return this.temper;
   }

   public void setTemper(int pTemper) {
      this.temper = pTemper;
   }

   public int modifyTemper(int pAddedTemper) {
      int i = Mth.clamp(this.getTemper() + pAddedTemper, 0, this.getMaxTemper());
      this.setTemper(i);
      return i;
   }

   /**
    * Returns {@code true} if this entity should push and be pushed by other entities when colliding.
    */
   public boolean isPushable() {
      return !this.isVehicle();
   }

   private void eating() {
      this.openMouth();
      if (!this.isSilent()) {
         SoundEvent soundevent = this.getEatingSound();
         if (soundevent != null) {
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), soundevent, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         }
      }

   }

   public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
      if (pFallDistance > 1.0F) {
         this.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
      }

      int i = this.calculateFallDamage(pFallDistance, pMultiplier);
      if (i <= 0) {
         return false;
      } else {
         this.hurt(pSource, (float)i);
         if (this.isVehicle()) {
            for(Entity entity : this.getIndirectPassengers()) {
               entity.hurt(pSource, (float)i);
            }
         }

         this.playBlockFallSound();
         return true;
      }
   }

   protected int calculateFallDamage(float pDistance, float pDamageMultiplier) {
      return Mth.ceil((pDistance * 0.5F - 3.0F) * pDamageMultiplier);
   }

   protected int getInventorySize() {
      return 2;
   }

   protected void createInventory() {
      SimpleContainer simplecontainer = this.inventory;
      this.inventory = new SimpleContainer(this.getInventorySize());
      if (simplecontainer != null) {
         simplecontainer.removeListener(this);
         int i = Math.min(simplecontainer.getContainerSize(), this.inventory.getContainerSize());

         for(int j = 0; j < i; ++j) {
            ItemStack itemstack = simplecontainer.getItem(j);
            if (!itemstack.isEmpty()) {
               this.inventory.setItem(j, itemstack.copy());
            }
         }
      }

      this.inventory.addListener(this);
      this.updateContainerEquipment();
      this.itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this.inventory));
   }

   protected void updateContainerEquipment() {
      if (!this.level().isClientSide) {
         this.setFlag(4, !this.inventory.getItem(0).isEmpty());
      }
   }

   /**
    * Called by {@code InventoryBasic.onInventoryChanged()} on an array that is never filled.
    */
   public void containerChanged(Container pInvBasic) {
      boolean flag = this.isSaddled();
      this.updateContainerEquipment();
      if (this.tickCount > 20 && !flag && this.isSaddled()) {
         this.playSound(this.getSaddleSoundEvent(), 0.5F, 1.0F);
      }

   }

   public double getCustomJump() {
      return this.getAttributeValue(Attributes.JUMP_STRENGTH);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      boolean flag = super.hurt(pSource, pAmount);
      if (flag && this.random.nextInt(3) == 0) {
         this.standIfPossible();
      }

      return flag;
   }

   protected boolean canPerformRearing() {
      return true;
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getAngrySound() {
      return null;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      if (!pBlock.liquid()) {
         BlockState blockstate = this.level().getBlockState(pPos.above());
         SoundType soundtype = pBlock.getSoundType(level(), pPos, this);
         if (blockstate.is(Blocks.SNOW)) {
            soundtype = blockstate.getSoundType(level(), pPos, this);
         }

         if (this.isVehicle() && this.canGallop) {
            ++this.gallopSoundCounter;
            if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
               this.playGallopSound(soundtype);
            } else if (this.gallopSoundCounter <= 5) {
               this.playSound(SoundEvents.HORSE_STEP_WOOD, soundtype.getVolume() * 0.15F, soundtype.getPitch());
            }
         } else if (this.isWoodSoundType(soundtype)) {
            this.playSound(SoundEvents.HORSE_STEP_WOOD, soundtype.getVolume() * 0.15F, soundtype.getPitch());
         } else {
            this.playSound(SoundEvents.HORSE_STEP, soundtype.getVolume() * 0.15F, soundtype.getPitch());
         }

      }
   }

   private boolean isWoodSoundType(SoundType pSoundType) {
      return pSoundType == SoundType.WOOD || pSoundType == SoundType.NETHER_WOOD || pSoundType == SoundType.STEM || pSoundType == SoundType.CHERRY_WOOD || pSoundType == SoundType.BAMBOO_WOOD;
   }

   protected void playGallopSound(SoundType pSoundType) {
      this.playSound(SoundEvents.HORSE_GALLOP, pSoundType.getVolume() * 0.15F, pSoundType.getPitch());
   }

   public static AttributeSupplier.Builder createBaseHorseAttributes() {
      return Mob.createMobAttributes().add(Attributes.JUMP_STRENGTH).add(Attributes.MAX_HEALTH, 53.0D).add(Attributes.MOVEMENT_SPEED, (double)0.225F);
   }

   /**
    * Will return how many at most can spawn in a chunk at once.
    */
   public int getMaxSpawnClusterSize() {
      return 6;
   }

   public int getMaxTemper() {
      return 100;
   }

   /**
    * Returns the volume for the sounds this mob makes.
    */
   protected float getSoundVolume() {
      return 0.8F;
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 400;
   }

   public void openCustomInventoryScreen(Player pPlayer) {
      if (!this.level().isClientSide && (!this.isVehicle() || this.hasPassenger(pPlayer)) && this.isTamed()) {
         pPlayer.openHorseInventory(this, this.inventory);
      }

   }

   public InteractionResult fedFood(Player pPlayer, ItemStack pStack) {
      boolean flag = this.handleEating(pPlayer, pStack);
      if (!pPlayer.getAbilities().instabuild) {
         pStack.shrink(1);
      }

      if (this.level().isClientSide) {
         return InteractionResult.CONSUME;
      } else {
         return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
      }
   }

   protected boolean handleEating(Player pPlayer, ItemStack pStack) {
      boolean flag = false;
      float f = 0.0F;
      int i = 0;
      int j = 0;
      if (pStack.is(Items.WHEAT)) {
         f = 2.0F;
         i = 20;
         j = 3;
      } else if (pStack.is(Items.SUGAR)) {
         f = 1.0F;
         i = 30;
         j = 3;
      } else if (pStack.is(Blocks.HAY_BLOCK.asItem())) {
         f = 20.0F;
         i = 180;
      } else if (pStack.is(Items.APPLE)) {
         f = 3.0F;
         i = 60;
         j = 3;
      } else if (pStack.is(Items.GOLDEN_CARROT)) {
         f = 4.0F;
         i = 60;
         j = 5;
         if (!this.level().isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
            flag = true;
            this.setInLove(pPlayer);
         }
      } else if (pStack.is(Items.GOLDEN_APPLE) || pStack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
         f = 10.0F;
         i = 240;
         j = 10;
         if (!this.level().isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
            flag = true;
            this.setInLove(pPlayer);
         }
      }

      if (this.getHealth() < this.getMaxHealth() && f > 0.0F) {
         this.heal(f);
         flag = true;
      }

      if (this.isBaby() && i > 0) {
         this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
         if (!this.level().isClientSide) {
            this.ageUp(i);
         }

         flag = true;
      }

      if (j > 0 && (flag || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
         flag = true;
         if (!this.level().isClientSide) {
            this.modifyTemper(j);
         }
      }

      if (flag) {
         this.eating();
         this.gameEvent(GameEvent.EAT);
      }

      return flag;
   }

   protected void doPlayerRide(Player pPlayer) {
      this.setEating(false);
      this.setStanding(false);
      if (!this.level().isClientSide) {
         pPlayer.setYRot(this.getYRot());
         pPlayer.setXRot(this.getXRot());
         pPlayer.startRiding(this);
      }

   }

   /**
    * Dead and sleeping entities cannot move
    */
   public boolean isImmobile() {
      return super.isImmobile() && this.isVehicle() && this.isSaddled() || this.isEating() || this.isStanding();
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return FOOD_ITEMS.test(pStack);
   }

   private void moveTail() {
      this.tailCounter = 1;
   }

   protected void dropEquipment() {
      super.dropEquipment();
      if (this.inventory != null) {
         for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
               this.spawnAtLocation(itemstack);
            }
         }

      }
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.random.nextInt(200) == 0) {
         this.moveTail();
      }

      super.aiStep();
      if (!this.level().isClientSide && this.isAlive()) {
         if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
            this.heal(1.0F);
         }

         if (this.canEatGrass()) {
            if (!this.isEating() && !this.isVehicle() && this.random.nextInt(300) == 0 && this.level().getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
               this.setEating(true);
            }

            if (this.isEating() && ++this.eatingCounter > 50) {
               this.eatingCounter = 0;
               this.setEating(false);
            }
         }

         this.followMommy();
      }
   }

   protected void followMommy() {
      if (this.isBred() && this.isBaby() && !this.isEating()) {
         LivingEntity livingentity = this.level().getNearestEntity(AbstractHorse.class, MOMMY_TARGETING, this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().inflate(16.0D));
         if (livingentity != null && this.distanceToSqr(livingentity) > 4.0D) {
            this.navigation.createPath(livingentity, 0);
         }
      }

   }

   public boolean canEatGrass() {
      return true;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.mouthCounter > 0 && ++this.mouthCounter > 30) {
         this.mouthCounter = 0;
         this.setFlag(64, false);
      }

      if (this.isEffectiveAi() && this.standCounter > 0 && ++this.standCounter > 20) {
         this.standCounter = 0;
         this.setStanding(false);
      }

      if (this.tailCounter > 0 && ++this.tailCounter > 8) {
         this.tailCounter = 0;
      }

      if (this.sprintCounter > 0) {
         ++this.sprintCounter;
         if (this.sprintCounter > 300) {
            this.sprintCounter = 0;
         }
      }

      this.eatAnimO = this.eatAnim;
      if (this.isEating()) {
         this.eatAnim += (1.0F - this.eatAnim) * 0.4F + 0.05F;
         if (this.eatAnim > 1.0F) {
            this.eatAnim = 1.0F;
         }
      } else {
         this.eatAnim += (0.0F - this.eatAnim) * 0.4F - 0.05F;
         if (this.eatAnim < 0.0F) {
            this.eatAnim = 0.0F;
         }
      }

      this.standAnimO = this.standAnim;
      if (this.isStanding()) {
         this.eatAnim = 0.0F;
         this.eatAnimO = this.eatAnim;
         this.standAnim += (1.0F - this.standAnim) * 0.4F + 0.05F;
         if (this.standAnim > 1.0F) {
            this.standAnim = 1.0F;
         }
      } else {
         this.allowStandSliding = false;
         this.standAnim += (0.8F * this.standAnim * this.standAnim * this.standAnim - this.standAnim) * 0.6F - 0.05F;
         if (this.standAnim < 0.0F) {
            this.standAnim = 0.0F;
         }
      }

      this.mouthAnimO = this.mouthAnim;
      if (this.getFlag(64)) {
         this.mouthAnim += (1.0F - this.mouthAnim) * 0.7F + 0.05F;
         if (this.mouthAnim > 1.0F) {
            this.mouthAnim = 1.0F;
         }
      } else {
         this.mouthAnim += (0.0F - this.mouthAnim) * 0.7F - 0.05F;
         if (this.mouthAnim < 0.0F) {
            this.mouthAnim = 0.0F;
         }
      }

   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      if (!this.isVehicle() && !this.isBaby()) {
         if (this.isTamed() && pPlayer.isSecondaryUseActive()) {
            this.openCustomInventoryScreen(pPlayer);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         } else {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            if (!itemstack.isEmpty()) {
               InteractionResult interactionresult = itemstack.interactLivingEntity(pPlayer, this, pHand);
               if (interactionresult.consumesAction()) {
                  return interactionresult;
               }

               if (this.canWearArmor() && this.isArmor(itemstack) && !this.isWearingArmor()) {
                  this.equipArmor(pPlayer, itemstack);
                  return InteractionResult.sidedSuccess(this.level().isClientSide);
               }
            }

            this.doPlayerRide(pPlayer);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   private void openMouth() {
      if (!this.level().isClientSide) {
         this.mouthCounter = 1;
         this.setFlag(64, true);
      }

   }

   public void setEating(boolean pEating) {
      this.setFlag(16, pEating);
   }

   public void setStanding(boolean pStanding) {
      if (pStanding) {
         this.setEating(false);
      }

      this.setFlag(32, pStanding);
   }

   @Nullable
   public SoundEvent getAmbientStandSound() {
      return this.getAmbientSound();
   }

   public void standIfPossible() {
      if (this.canPerformRearing() && this.isEffectiveAi()) {
         this.standCounter = 1;
         this.setStanding(true);
      }

   }

   public void makeMad() {
      if (!this.isStanding()) {
         this.standIfPossible();
         SoundEvent soundevent = this.getAngrySound();
         if (soundevent != null) {
            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
         }
      }

   }

   public boolean tameWithName(Player pPlayer) {
      this.setOwnerUUID(pPlayer.getUUID());
      this.setTamed(true);
      if (pPlayer instanceof ServerPlayer) {
         CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)pPlayer, this);
      }

      this.level().broadcastEntityEvent(this, (byte)7);
      return true;
   }

   protected void tickRidden(Player pPlayer, Vec3 pTravelVector) {
      super.tickRidden(pPlayer, pTravelVector);
      Vec2 vec2 = this.getRiddenRotation(pPlayer);
      this.setRot(vec2.y, vec2.x);
      this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
      if (this.isControlledByLocalInstance()) {
         if (pTravelVector.z <= 0.0D) {
            this.gallopSoundCounter = 0;
         }

         if (this.onGround()) {
            this.setIsJumping(false);
            if (this.playerJumpPendingScale > 0.0F && !this.isJumping()) {
               this.executeRidersJump(this.playerJumpPendingScale, pTravelVector);
            }

            this.playerJumpPendingScale = 0.0F;
         }
      }

   }

   protected Vec2 getRiddenRotation(LivingEntity pEntity) {
      return new Vec2(pEntity.getXRot() * 0.5F, pEntity.getYRot());
   }

   protected Vec3 getRiddenInput(Player pPlayer, Vec3 pTravelVector) {
      if (this.onGround() && this.playerJumpPendingScale == 0.0F && this.isStanding() && !this.allowStandSliding) {
         return Vec3.ZERO;
      } else {
         float f = pPlayer.xxa * 0.5F;
         float f1 = pPlayer.zza;
         if (f1 <= 0.0F) {
            f1 *= 0.25F;
         }

         return new Vec3((double)f, 0.0D, (double)f1);
      }
   }

   protected float getRiddenSpeed(Player pPlayer) {
      return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
   }

   protected void executeRidersJump(float pPlayerJumpPendingScale, Vec3 pTravelVector) {
      double d0 = this.getCustomJump() * (double)pPlayerJumpPendingScale * (double)this.getBlockJumpFactor();
      double d1 = d0 + (double)this.getJumpBoostPower();
      Vec3 vec3 = this.getDeltaMovement();
      this.setDeltaMovement(vec3.x, d1, vec3.z);
      this.setIsJumping(true);
      this.hasImpulse = true;
               net.minecraftforge.common.ForgeHooks.onLivingJump(this);
      if (pTravelVector.z > 0.0D) {
         float f = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
         float f1 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
         this.setDeltaMovement(this.getDeltaMovement().add((double)(-0.4F * f * pPlayerJumpPendingScale), 0.0D, (double)(0.4F * f1 * pPlayerJumpPendingScale)));
      }

   }

   protected void playJumpSound() {
      this.playSound(SoundEvents.HORSE_JUMP, 0.4F, 1.0F);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("EatingHaystack", this.isEating());
      pCompound.putBoolean("Bred", this.isBred());
      pCompound.putInt("Temper", this.getTemper());
      pCompound.putBoolean("Tame", this.isTamed());
      if (this.getOwnerUUID() != null) {
         pCompound.putUUID("Owner", this.getOwnerUUID());
      }

      if (!this.inventory.getItem(0).isEmpty()) {
         pCompound.put("SaddleItem", this.inventory.getItem(0).save(new CompoundTag()));
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setEating(pCompound.getBoolean("EatingHaystack"));
      this.setBred(pCompound.getBoolean("Bred"));
      this.setTemper(pCompound.getInt("Temper"));
      this.setTamed(pCompound.getBoolean("Tame"));
      UUID uuid;
      if (pCompound.hasUUID("Owner")) {
         uuid = pCompound.getUUID("Owner");
      } else {
         String s = pCompound.getString("Owner");
         uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
      }

      if (uuid != null) {
         this.setOwnerUUID(uuid);
      }

      if (pCompound.contains("SaddleItem", 10)) {
         ItemStack itemstack = ItemStack.of(pCompound.getCompound("SaddleItem"));
         if (itemstack.is(Items.SADDLE)) {
            this.inventory.setItem(0, itemstack);
         }
      }

      this.updateContainerEquipment();
   }

   /**
    * Returns {@code true} if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(Animal pOtherAnimal) {
      return false;
   }

   /**
    * Returns {@code true} if the horse entity ready to mate. (no rider, not riding, tame, adult, not steril...)
    */
   protected boolean canParent() {
      return !this.isVehicle() && !this.isPassenger() && this.isTamed() && !this.isBaby() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      return null;
   }

   protected void setOffspringAttributes(AgeableMob pParent, AbstractHorse pChild) {
      this.setOffspringAttribute(pParent, pChild, Attributes.MAX_HEALTH, (double)MIN_HEALTH, (double)MAX_HEALTH);
      this.setOffspringAttribute(pParent, pChild, Attributes.JUMP_STRENGTH, (double)MIN_JUMP_STRENGTH, (double)MAX_JUMP_STRENGTH);
      this.setOffspringAttribute(pParent, pChild, Attributes.MOVEMENT_SPEED, (double)MIN_MOVEMENT_SPEED, (double)MAX_MOVEMENT_SPEED);
   }

   private void setOffspringAttribute(AgeableMob pOtherParent, AbstractHorse pChild, Attribute pAttribute, double pMin, double pMax) {
      double d0 = createOffspringAttribute(this.getAttributeBaseValue(pAttribute), pOtherParent.getAttributeBaseValue(pAttribute), pMin, pMax, this.random);
      pChild.getAttribute(pAttribute).setBaseValue(d0);
   }

   static double createOffspringAttribute(double pValue1, double pValue2, double pMin, double pMax, RandomSource pRandom) {
      if (pMax <= pMin) {
         throw new IllegalArgumentException("Incorrect range for an attribute");
      } else {
         pValue1 = Mth.clamp(pValue1, pMin, pMax);
         pValue2 = Mth.clamp(pValue2, pMin, pMax);
         double d0 = 0.15D * (pMax - pMin);
         double d1 = Math.abs(pValue1 - pValue2) + d0 * 2.0D;
         double d2 = (pValue1 + pValue2) / 2.0D;
         double d3 = (pRandom.nextDouble() + pRandom.nextDouble() + pRandom.nextDouble()) / 3.0D - 0.5D;
         double d4 = d2 + d1 * d3;
         if (d4 > pMax) {
            double d6 = d4 - pMax;
            return pMax - d6;
         } else if (d4 < pMin) {
            double d5 = pMin - d4;
            return pMin + d5;
         } else {
            return d4;
         }
      }
   }

   public float getEatAnim(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.eatAnimO, this.eatAnim);
   }

   public float getStandAnim(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.standAnimO, this.standAnim);
   }

   public float getMouthAnim(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.mouthAnimO, this.mouthAnim);
   }

   public void onPlayerJump(int pJumpPower) {
      if (this.isSaddled()) {
         if (pJumpPower < 0) {
            pJumpPower = 0;
         } else {
            this.allowStandSliding = true;
            this.standIfPossible();
         }

         if (pJumpPower >= 90) {
            this.playerJumpPendingScale = 1.0F;
         } else {
            this.playerJumpPendingScale = 0.4F + 0.4F * (float)pJumpPower / 90.0F;
         }

      }
   }

   public boolean canJump() {
      return this.isSaddled();
   }

   public void handleStartJump(int pJumpPower) {
      this.allowStandSliding = true;
      this.standIfPossible();
      this.playJumpSound();
   }

   public void handleStopJump() {
   }

   /**
    * Spawns particles for the horse entity.
    * @param pTamed whether to spawn hearts or smoke.
    */
   protected void spawnTamingParticles(boolean pTamed) {
      ParticleOptions particleoptions = pTamed ? ParticleTypes.HEART : ParticleTypes.SMOKE;

      for(int i = 0; i < 7; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level().addParticle(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 7) {
         this.spawnTamingParticles(true);
      } else if (pId == 6) {
         this.spawnTamingParticles(false);
      } else {
         super.handleEntityEvent(pId);
      }

   }

   protected void positionRider(Entity pPassenger, Entity.MoveFunction pCallback) {
      super.positionRider(pPassenger, pCallback);
      if (this.standAnimO > 0.0F) {
         float f = Mth.sin(this.yBodyRot * ((float)Math.PI / 180F));
         float f1 = Mth.cos(this.yBodyRot * ((float)Math.PI / 180F));
         float f2 = 0.7F * this.standAnimO;
         float f3 = 0.15F * this.standAnimO;
         pCallback.accept(pPassenger, this.getX() + (double)(f2 * f), this.getY() + this.getPassengersRidingOffset() + pPassenger.getMyRidingOffset() + (double)f3, this.getZ() - (double)(f2 * f1));
         if (pPassenger instanceof LivingEntity) {
            ((LivingEntity)pPassenger).yBodyRot = this.yBodyRot;
         }
      }

   }

   protected static float generateMaxHealth(IntUnaryOperator pOperator) {
      return 15.0F + (float)pOperator.applyAsInt(8) + (float)pOperator.applyAsInt(9);
   }

   protected static double generateJumpStrength(DoubleSupplier pSupplier) {
      return (double)0.4F + pSupplier.getAsDouble() * 0.2D + pSupplier.getAsDouble() * 0.2D + pSupplier.getAsDouble() * 0.2D;
   }

   protected static double generateSpeed(DoubleSupplier pSupplier) {
      return ((double)0.45F + pSupplier.getAsDouble() * 0.3D + pSupplier.getAsDouble() * 0.3D + pSupplier.getAsDouble() * 0.3D) * 0.25D;
   }

   /**
    * Returns {@code true} if this entity should move as if it were on a ladder (either because it's actually on a
    * ladder, or for AI reasons)
    */
   public boolean onClimbable() {
      return false;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return pSize.height * 0.95F;
   }

   public boolean canWearArmor() {
      return false;
   }

   public boolean isWearingArmor() {
      return !this.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
   }

   public boolean isArmor(ItemStack pStack) {
      return false;
   }

   private SlotAccess createEquipmentSlotAccess(final int pSlot, final Predicate<ItemStack> pStackFilter) {
      return new SlotAccess() {
         public ItemStack get() {
            return AbstractHorse.this.inventory.getItem(pSlot);
         }

         public boolean set(ItemStack p_149528_) {
            if (!pStackFilter.test(p_149528_)) {
               return false;
            } else {
               AbstractHorse.this.inventory.setItem(pSlot, p_149528_);
               AbstractHorse.this.updateContainerEquipment();
               return true;
            }
         }
      };
   }

   public SlotAccess getSlot(int pSlot) {
      int i = pSlot - 400;
      if (i >= 0 && i < 2 && i < this.inventory.getContainerSize()) {
         if (i == 0) {
            return this.createEquipmentSlotAccess(i, (p_149518_) -> {
               return p_149518_.isEmpty() || p_149518_.is(Items.SADDLE);
            });
         }

         if (i == 1) {
            if (!this.canWearArmor()) {
               return SlotAccess.NULL;
            }

            return this.createEquipmentSlotAccess(i, (p_149516_) -> {
               return p_149516_.isEmpty() || this.isArmor(p_149516_);
            });
         }
      }

      int j = pSlot - 500 + 2;
      return j >= 2 && j < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, j) : super.getSlot(pSlot);
   }

   /**
    * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
    * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
    */
   @Nullable
   public LivingEntity getControllingPassenger() {
      Entity entity = this.getFirstPassenger();
      if (entity instanceof Mob) {
         return (Mob)entity;
      } else {
         if (this.isSaddled()) {
            entity = this.getFirstPassenger();
            if (entity instanceof Player) {
               return (Player)entity;
            }
         }

         return null;
      }
   }

   @Nullable
   private Vec3 getDismountLocationInDirection(Vec3 pDirection, LivingEntity pPassenger) {
      double d0 = this.getX() + pDirection.x;
      double d1 = this.getBoundingBox().minY;
      double d2 = this.getZ() + pDirection.z;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(Pose pose : pPassenger.getDismountPoses()) {
         blockpos$mutableblockpos.set(d0, d1, d2);
         double d3 = this.getBoundingBox().maxY + 0.75D;

         while(true) {
            double d4 = this.level().getBlockFloorHeight(blockpos$mutableblockpos);
            if ((double)blockpos$mutableblockpos.getY() + d4 > d3) {
               break;
            }

            if (DismountHelper.isBlockFloorValid(d4)) {
               AABB aabb = pPassenger.getLocalBoundsForPose(pose);
               Vec3 vec3 = new Vec3(d0, (double)blockpos$mutableblockpos.getY() + d4, d2);
               if (DismountHelper.canDismountTo(this.level(), pPassenger, aabb.move(vec3))) {
                  pPassenger.setPose(pose);
                  return vec3;
               }
            }

            blockpos$mutableblockpos.move(Direction.UP);
            if (!((double)blockpos$mutableblockpos.getY() < d3)) {
               break;
            }
         }
      }

      return null;
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity) {
      Vec3 vec3 = getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)pLivingEntity.getBbWidth(), this.getYRot() + (pLivingEntity.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F));
      Vec3 vec31 = this.getDismountLocationInDirection(vec3, pLivingEntity);
      if (vec31 != null) {
         return vec31;
      } else {
         Vec3 vec32 = getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)pLivingEntity.getBbWidth(), this.getYRot() + (pLivingEntity.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F));
         Vec3 vec33 = this.getDismountLocationInDirection(vec32, pLivingEntity);
         return vec33 != null ? vec33 : this.position();
      }
   }

   protected void randomizeAttributes(RandomSource pRandom) {
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      if (pSpawnData == null) {
         pSpawnData = new AgeableMob.AgeableMobGroupData(0.2F);
      }

      this.randomizeAttributes(pLevel.getRandom());
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   private net.minecraftforge.common.util.LazyOptional<?> itemHandler = null;

   @Override
   public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.core.Direction facing) {
      if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER && itemHandler != null && this.isAlive())
         return itemHandler.cast();
      return super.getCapability(capability, facing);
   }

   @Override
   public void invalidateCaps() {
      super.invalidateCaps();
      if (itemHandler != null) {
         net.minecraftforge.common.util.LazyOptional<?> oldHandler = itemHandler;
         itemHandler = null;
         oldHandler.invalidate();
      }
   }

   public boolean hasInventoryChanged(Container pInventory) {
      return this.inventory != pInventory;
   }

   public int getAmbientStandInterval() {
      return this.getAmbientSoundInterval();
   }
}
