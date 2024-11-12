package net.minecraft.world.entity.monster;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Zombie extends Monster {
   private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
   private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);
   private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_SPECIAL_TYPE_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_DROWNED_CONVERSION_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
   public static final float ZOMBIE_LEADER_CHANCE = 0.05F;
   public static final int REINFORCEMENT_ATTEMPTS = 50;
   public static final int REINFORCEMENT_RANGE_MAX = 40;
   public static final int REINFORCEMENT_RANGE_MIN = 7;
   protected static final float BABY_EYE_HEIGHT_ADJUSTMENT = 0.81F;
   private static final float BREAK_DOOR_CHANCE = 0.1F;
   private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = (p_34284_) -> {
      return p_34284_ == Difficulty.HARD;
   };
   private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, DOOR_BREAKING_PREDICATE);
   private boolean canBreakDoors;
   private int inWaterTime;
   private int conversionTime;

   public Zombie(EntityType<? extends Zombie> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public Zombie(Level pLevel) {
      this(EntityType.ZOMBIE, pLevel);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(4, new Zombie.ZombieAttackTurtleEggGoal(this, 1.0D, 3));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.addBehaviourGoals();
   }

   protected void addBehaviourGoals() {
      this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, false));
      this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, this::canBreakDoors));
      this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
      this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglin.class));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
      this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35.0D).add(Attributes.MOVEMENT_SPEED, (double)0.23F).add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.ARMOR, 2.0D).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DATA_BABY_ID, false);
      this.getEntityData().define(DATA_SPECIAL_TYPE_ID, 0);
      this.getEntityData().define(DATA_DROWNED_CONVERSION_ID, false);
   }

   public boolean isUnderWaterConverting() {
      return this.getEntityData().get(DATA_DROWNED_CONVERSION_ID);
   }

   public boolean canBreakDoors() {
      return this.canBreakDoors;
   }

   /**
    * Sets or removes EntityAIBreakDoor task
    */
   public void setCanBreakDoors(boolean pCanBreakDoors) {
      if (this.supportsBreakDoorGoal() && GoalUtils.hasGroundPathNavigation(this)) {
         if (this.canBreakDoors != pCanBreakDoors) {
            this.canBreakDoors = pCanBreakDoors;
            ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(pCanBreakDoors);
            if (pCanBreakDoors) {
               this.goalSelector.addGoal(1, this.breakDoorGoal);
            } else {
               this.goalSelector.removeGoal(this.breakDoorGoal);
            }
         }
      } else if (this.canBreakDoors) {
         this.goalSelector.removeGoal(this.breakDoorGoal);
         this.canBreakDoors = false;
      }

   }

   protected boolean supportsBreakDoorGoal() {
      return true;
   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return this.getEntityData().get(DATA_BABY_ID);
   }

   public int getExperienceReward() {
      if (this.isBaby()) {
         this.xpReward = (int)((double)this.xpReward * 2.5D);
      }

      return super.getExperienceReward();
   }

   /**
    * Set whether this zombie is a child.
    */
   public void setBaby(boolean pChildZombie) {
      this.getEntityData().set(DATA_BABY_ID, pChildZombie);
      if (this.level() != null && !this.level().isClientSide) {
         AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
         attributeinstance.removeModifier(SPEED_MODIFIER_BABY);
         if (pChildZombie) {
            attributeinstance.addTransientModifier(SPEED_MODIFIER_BABY);
         }
      }

   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      if (DATA_BABY_ID.equals(pKey)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(pKey);
   }

   protected boolean convertsInWater() {
      return true;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (!this.level().isClientSide && this.isAlive() && !this.isNoAi()) {
         if (this.isUnderWaterConverting()) {
            --this.conversionTime;
            if (this.conversionTime < 0 && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(this, EntityType.DROWNED, (timer) -> this.conversionTime = timer)) {
               this.doUnderWaterConversion();
            }
         } else if (this.convertsInWater()) {
            if (this.isEyeInFluid(FluidTags.WATER)) {
               ++this.inWaterTime;
               if (this.inWaterTime >= 600) {
                  this.startUnderWaterConversion(300);
               }
            } else {
               this.inWaterTime = -1;
            }
         }
      }

      super.tick();
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.isAlive()) {
         boolean flag = this.isSunSensitive() && this.isSunBurnTick();
         if (flag) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!itemstack.isEmpty()) {
               if (itemstack.isDamageableItem()) {
                  itemstack.setDamageValue(itemstack.getDamageValue() + this.random.nextInt(2));
                  if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
                     this.broadcastBreakEvent(EquipmentSlot.HEAD);
                     this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                  }
               }

               flag = false;
            }

            if (flag) {
               this.setSecondsOnFire(8);
            }
         }
      }

      super.aiStep();
   }

   private void startUnderWaterConversion(int pConversionTime) {
      this.conversionTime = pConversionTime;
      this.getEntityData().set(DATA_DROWNED_CONVERSION_ID, true);
   }

   protected void doUnderWaterConversion() {
      this.convertToZombieType(EntityType.DROWNED);
      if (!this.isSilent()) {
         this.level().levelEvent((Player)null, 1040, this.blockPosition(), 0);
      }

   }

   protected void convertToZombieType(EntityType<? extends Zombie> pEntityType) {
      Zombie zombie = this.convertTo(pEntityType, true);
      if (zombie != null) {
         zombie.handleAttributes(zombie.level().getCurrentDifficultyAt(zombie.blockPosition()).getSpecialMultiplier());
         zombie.setCanBreakDoors(zombie.supportsBreakDoorGoal() && this.canBreakDoors());
         net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, zombie);
      }

   }

   protected boolean isSunSensitive() {
      return true;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (!super.hurt(pSource, pAmount)) {
         return false;
      } else if (!(this.level() instanceof ServerLevel)) {
         return false;
      } else {
         ServerLevel serverlevel = (ServerLevel)this.level();
         LivingEntity livingentity = this.getTarget();
         if (livingentity == null && pSource.getEntity() instanceof LivingEntity) {
            livingentity = (LivingEntity)pSource.getEntity();
         }

            int i = Mth.floor(this.getX());
            int j = Mth.floor(this.getY());
            int k = Mth.floor(this.getZ());
         net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent event = net.minecraftforge.event.ForgeEventFactory.fireZombieSummonAid(this, level(), i, j, k, livingentity, this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).getValue());
         if (event.getResult() == net.minecraftforge.eventbus.api.Event.Result.DENY) return true;
         if (event.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW  ||
            livingentity != null && this.level().getDifficulty() == Difficulty.HARD && (double)this.random.nextFloat() < this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).getValue() && this.level()
                    .getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            Zombie zombie = event.getCustomSummonedAid() != null && event.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW ? event.getCustomSummonedAid() : EntityType.ZOMBIE.create(this.level());

            for(int l = 0; l < 50; ++l) {
               int i1 = i + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
               int j1 = j + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
               int k1 = k + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
               BlockPos blockpos = new BlockPos(i1, j1, k1);
               EntityType<?> entitytype = zombie.getType();
               SpawnPlacements.Type spawnplacements$type = SpawnPlacements.getPlacementType(entitytype);
               if (NaturalSpawner.isSpawnPositionOk(spawnplacements$type, this.level(), blockpos, entitytype) && SpawnPlacements.checkSpawnRules(entitytype, serverlevel, MobSpawnType.REINFORCEMENT, blockpos, this.level().random)) {
                  zombie.setPos((double)i1, (double)j1, (double)k1);
                  if (!this.level().hasNearbyAlivePlayer((double)i1, (double)j1, (double)k1, 7.0D) && this.level().isUnobstructed(zombie) && this.level().noCollision(zombie) && !this.level().containsAnyLiquid(zombie.getBoundingBox())) {
                     if (livingentity != null)
                     zombie.setTarget(livingentity);
                     zombie.finalizeSpawn(serverlevel, this.level().getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.REINFORCEMENT, (SpawnGroupData)null, (CompoundTag)null);
                     serverlevel.addFreshEntityWithPassengers(zombie);
                     this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Zombie reinforcement caller charge", (double)-0.05F, AttributeModifier.Operation.ADDITION));
                     zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Zombie reinforcement callee charge", (double)-0.05F, AttributeModifier.Operation.ADDITION));
                     break;
                  }
               }
            }
         }

         return true;
      }
   }

   public boolean doHurtTarget(Entity pEntity) {
      boolean flag = super.doHurtTarget(pEntity);
      if (flag) {
         float f = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
         if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
            pEntity.setSecondsOnFire(2 * (int)f);
         }
      }

      return flag;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ZOMBIE_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.ZOMBIE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ZOMBIE_DEATH;
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.ZOMBIE_STEP;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   public MobType getMobType() {
      return MobType.UNDEAD;
   }

   protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
      super.populateDefaultEquipmentSlots(pRandom, pDifficulty);
      if (pRandom.nextFloat() < (this.level().getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
         int i = pRandom.nextInt(3);
         if (i == 0) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
         } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
         }
      }

   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("IsBaby", this.isBaby());
      pCompound.putBoolean("CanBreakDoors", this.canBreakDoors());
      pCompound.putInt("InWaterTime", this.isInWater() ? this.inWaterTime : -1);
      pCompound.putInt("DrownedConversionTime", this.isUnderWaterConverting() ? this.conversionTime : -1);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setBaby(pCompound.getBoolean("IsBaby"));
      this.setCanBreakDoors(pCompound.getBoolean("CanBreakDoors"));
      this.inWaterTime = pCompound.getInt("InWaterTime");
      if (pCompound.contains("DrownedConversionTime", 99) && pCompound.getInt("DrownedConversionTime") > -1) {
         this.startUnderWaterConversion(pCompound.getInt("DrownedConversionTime"));
      }

   }

   public boolean killedEntity(ServerLevel pLevel, LivingEntity pEntity) {
      boolean flag = super.killedEntity(pLevel, pEntity);
      if ((pLevel.getDifficulty() == Difficulty.NORMAL || pLevel.getDifficulty() == Difficulty.HARD) && pEntity instanceof Villager villager && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(pEntity, EntityType.ZOMBIE_VILLAGER, (timer) -> {})) {
         if (pLevel.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
            return flag;
         }

         ZombieVillager zombievillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
         if (zombievillager != null) {
            zombievillager.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(zombievillager.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true), (CompoundTag)null);
            zombievillager.setVillagerData(villager.getVillagerData());
            zombievillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
            zombievillager.setTradeOffers(villager.getOffers().createTag());
            zombievillager.setVillagerXp(villager.getVillagerXp());
         net.minecraftforge.event.ForgeEventFactory.onLivingConvert(pEntity, zombievillager);
            if (!this.isSilent()) {
               pLevel.levelEvent((Player)null, 1026, this.blockPosition(), 0);
            }

            flag = false;
         }
      }

      return flag;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return this.isBaby() ? 0.93F : 1.74F;
   }

   public boolean canHoldItem(ItemStack pStack) {
      return pStack.is(Items.EGG) && this.isBaby() && this.isPassenger() ? false : super.canHoldItem(pStack);
   }

   public boolean wantsToPickUp(ItemStack pStack) {
      return pStack.is(Items.GLOW_INK_SAC) ? false : super.wantsToPickUp(pStack);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      RandomSource randomsource = pLevel.getRandom();
      pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      float f = pDifficulty.getSpecialMultiplier();
      this.setCanPickUpLoot(randomsource.nextFloat() < 0.55F * f);
      if (pSpawnData == null) {
         pSpawnData = new Zombie.ZombieGroupData(getSpawnAsBabyOdds(randomsource), true);
      }

      if (pSpawnData instanceof Zombie.ZombieGroupData zombie$zombiegroupdata) {
         if (zombie$zombiegroupdata.isBaby) {
            this.setBaby(true);
            if (zombie$zombiegroupdata.canSpawnJockey) {
               if ((double)randomsource.nextFloat() < 0.05D) {
                  List<Chicken> list = pLevel.getEntitiesOfClass(Chicken.class, this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D), EntitySelector.ENTITY_NOT_BEING_RIDDEN);
                  if (!list.isEmpty()) {
                     Chicken chicken = list.get(0);
                     chicken.setChickenJockey(true);
                     this.startRiding(chicken);
                  }
               } else if ((double)randomsource.nextFloat() < 0.05D) {
                  Chicken chicken1 = EntityType.CHICKEN.create(this.level());
                  if (chicken1 != null) {
                     chicken1.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                     chicken1.finalizeSpawn(pLevel, pDifficulty, MobSpawnType.JOCKEY, (SpawnGroupData)null, (CompoundTag)null);
                     chicken1.setChickenJockey(true);
                     this.startRiding(chicken1);
                     pLevel.addFreshEntity(chicken1);
                  }
               }
            }
         }

         this.setCanBreakDoors(this.supportsBreakDoorGoal() && randomsource.nextFloat() < f * 0.1F);
         this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
         this.populateDefaultEquipmentEnchantments(randomsource, pDifficulty);
      }

      if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
         LocalDate localdate = LocalDate.now();
         int i = localdate.get(ChronoField.DAY_OF_MONTH);
         int j = localdate.get(ChronoField.MONTH_OF_YEAR);
         if (j == 10 && i == 31 && randomsource.nextFloat() < 0.25F) {
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(randomsource.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
            this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
         }
      }

      this.handleAttributes(f);
      return pSpawnData;
   }

   public static boolean getSpawnAsBabyOdds(RandomSource pRandom) {
      return pRandom.nextFloat() < net.minecraftforge.common.ForgeConfig.SERVER.zombieBabyChance.get();
   }

   protected void handleAttributes(float pDifficulty) {
      this.randomizeReinforcementsChance();
      this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(new AttributeModifier("Random spawn bonus", this.random.nextDouble() * (double)0.05F, AttributeModifier.Operation.ADDITION));
      double d0 = this.random.nextDouble() * 1.5D * (double)pDifficulty;
      if (d0 > 1.0D) {
         this.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Random zombie-spawn bonus", d0, AttributeModifier.Operation.MULTIPLY_TOTAL));
      }

      if (this.random.nextFloat() < pDifficulty * 0.05F) {
         this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25D + 0.5D, AttributeModifier.Operation.ADDITION));
         this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0D + 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
         this.setCanBreakDoors(this.supportsBreakDoorGoal());
      }

   }

   protected void randomizeReinforcementsChance() {
      this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.random.nextDouble() * net.minecraftforge.common.ForgeConfig.SERVER.zombieBaseSummonChance.get());
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return this.isBaby() ? 0.0D : -0.45D;
   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
      Entity entity = pSource.getEntity();
      if (entity instanceof Creeper creeper) {
         if (creeper.canDropMobsSkull()) {
            ItemStack itemstack = this.getSkull();
            if (!itemstack.isEmpty()) {
               creeper.increaseDroppedSkulls();
               this.spawnAtLocation(itemstack);
            }
         }
      }

   }

   protected ItemStack getSkull() {
      return new ItemStack(Items.ZOMBIE_HEAD);
   }

   class ZombieAttackTurtleEggGoal extends RemoveBlockGoal {
      ZombieAttackTurtleEggGoal(PathfinderMob pMob, double pSpeedModifier, int pVerticalSearchRange) {
         super(Blocks.TURTLE_EGG, pMob, pSpeedModifier, pVerticalSearchRange);
      }

      public void playDestroyProgressSound(LevelAccessor pLevel, BlockPos pPos) {
         pLevel.playSound((Player)null, pPos, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 0.5F, 0.9F + Zombie.this.random.nextFloat() * 0.2F);
      }

      public void playBreakSound(Level pLevel, BlockPos pPos) {
         pLevel.playSound((Player)null, pPos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + pLevel.random.nextFloat() * 0.2F);
      }

      public double acceptedDistance() {
         return 1.14D;
      }
   }

   public static class ZombieGroupData implements SpawnGroupData {
      public final boolean isBaby;
      public final boolean canSpawnJockey;

      public ZombieGroupData(boolean pIsBaby, boolean pCanSpawnJockey) {
         this.isBaby = pIsBaby;
         this.canSpawnJockey = pCanSpawnJockey;
      }
   }
}
