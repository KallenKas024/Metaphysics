package net.minecraft.world.entity.animal;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.CatVariantTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

public class Cat extends TamableAnimal implements VariantHolder<CatVariant> {
   public static final double TEMPT_SPEED_MOD = 0.6D;
   public static final double WALK_SPEED_MOD = 0.8D;
   public static final double SPRINT_SPEED_MOD = 1.33D;
   private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(Items.COD, Items.SALMON);
   private static final EntityDataAccessor<CatVariant> DATA_VARIANT_ID = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.CAT_VARIANT);
   private static final EntityDataAccessor<Boolean> IS_LYING = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> RELAX_STATE_ONE = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
   private Cat.CatAvoidEntityGoal<Player> avoidPlayersGoal;
   @Nullable
   private TemptGoal temptGoal;
   private float lieDownAmount;
   private float lieDownAmountO;
   private float lieDownAmountTail;
   private float lieDownAmountOTail;
   private float relaxStateOneAmount;
   private float relaxStateOneAmountO;

   public Cat(EntityType<? extends Cat> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ResourceLocation getResourceLocation() {
      return this.getVariant().texture();
   }

   protected void registerGoals() {
      this.temptGoal = new Cat.CatTemptGoal(this, 0.6D, TEMPT_INGREDIENT, true);
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D));
      this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
      this.goalSelector.addGoal(3, new Cat.CatRelaxOnOwnerGoal(this));
      this.goalSelector.addGoal(4, this.temptGoal);
      this.goalSelector.addGoal(5, new CatLieOnBedGoal(this, 1.1D, 8));
      this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 5.0F, false));
      this.goalSelector.addGoal(7, new CatSitOnBlockGoal(this, 0.8D));
      this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3F));
      this.goalSelector.addGoal(9, new OcelotAttackGoal(this));
      this.goalSelector.addGoal(10, new BreedGoal(this, 0.8D));
      this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 0.8D, 1.0000001E-5F));
      this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 10.0F));
      this.targetSelector.addGoal(1, new NonTameRandomTargetGoal<>(this, Rabbit.class, false, (Predicate<LivingEntity>)null));
      this.targetSelector.addGoal(1, new NonTameRandomTargetGoal<>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
   }

   public CatVariant getVariant() {
      return this.entityData.get(DATA_VARIANT_ID);
   }

   public void setVariant(CatVariant pVariant) {
      this.entityData.set(DATA_VARIANT_ID, pVariant);
   }

   public void setLying(boolean pLying) {
      this.entityData.set(IS_LYING, pLying);
   }

   public boolean isLying() {
      return this.entityData.get(IS_LYING);
   }

   public void setRelaxStateOne(boolean pRelaxStateOne) {
      this.entityData.set(RELAX_STATE_ONE, pRelaxStateOne);
   }

   public boolean isRelaxStateOne() {
      return this.entityData.get(RELAX_STATE_ONE);
   }

   public DyeColor getCollarColor() {
      return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
   }

   public void setCollarColor(DyeColor pColor) {
      this.entityData.set(DATA_COLLAR_COLOR, pColor.getId());
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_VARIANT_ID, BuiltInRegistries.CAT_VARIANT.getOrThrow(CatVariant.BLACK));
      this.entityData.define(IS_LYING, false);
      this.entityData.define(RELAX_STATE_ONE, false);
      this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putString("variant", BuiltInRegistries.CAT_VARIANT.getKey(this.getVariant()).toString());
      pCompound.putByte("CollarColor", (byte)this.getCollarColor().getId());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      CatVariant catvariant = BuiltInRegistries.CAT_VARIANT.get(ResourceLocation.tryParse(pCompound.getString("variant")));
      if (catvariant != null) {
         this.setVariant(catvariant);
      }

      if (pCompound.contains("CollarColor", 99)) {
         this.setCollarColor(DyeColor.byId(pCompound.getInt("CollarColor")));
      }

   }

   public void customServerAiStep() {
      if (this.getMoveControl().hasWanted()) {
         double d0 = this.getMoveControl().getSpeedModifier();
         if (d0 == 0.6D) {
            this.setPose(Pose.CROUCHING);
            this.setSprinting(false);
         } else if (d0 == 1.33D) {
            this.setPose(Pose.STANDING);
            this.setSprinting(true);
         } else {
            this.setPose(Pose.STANDING);
            this.setSprinting(false);
         }
      } else {
         this.setPose(Pose.STANDING);
         this.setSprinting(false);
      }

   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.isTame()) {
         if (this.isInLove()) {
            return SoundEvents.CAT_PURR;
         } else {
            return this.random.nextInt(4) == 0 ? SoundEvents.CAT_PURREOW : SoundEvents.CAT_AMBIENT;
         }
      } else {
         return SoundEvents.CAT_STRAY_AMBIENT;
      }
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 120;
   }

   public void hiss() {
      this.playSound(SoundEvents.CAT_HISS, this.getSoundVolume(), this.getVoicePitch());
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.CAT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.CAT_DEATH;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.ATTACK_DAMAGE, 3.0D);
   }

   protected void usePlayerItem(Player pPlayer, InteractionHand pHand, ItemStack pStack) {
      if (this.isFood(pStack)) {
         this.playSound(SoundEvents.CAT_EAT, 1.0F, 1.0F);
      }

      super.usePlayerItem(pPlayer, pHand, pStack);
   }

   private float getAttackDamage() {
      return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
   }

   public boolean doHurtTarget(Entity pEntity) {
      return pEntity.hurt(this.damageSources().mobAttack(this), this.getAttackDamage());
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.temptGoal != null && this.temptGoal.isRunning() && !this.isTame() && this.tickCount % 100 == 0) {
         this.playSound(SoundEvents.CAT_BEG_FOR_FOOD, 1.0F, 1.0F);
      }

      this.handleLieDown();
   }

   private void handleLieDown() {
      if ((this.isLying() || this.isRelaxStateOne()) && this.tickCount % 5 == 0) {
         this.playSound(SoundEvents.CAT_PURR, 0.6F + 0.4F * (this.random.nextFloat() - this.random.nextFloat()), 1.0F);
      }

      this.updateLieDownAmount();
      this.updateRelaxStateOneAmount();
   }

   private void updateLieDownAmount() {
      this.lieDownAmountO = this.lieDownAmount;
      this.lieDownAmountOTail = this.lieDownAmountTail;
      if (this.isLying()) {
         this.lieDownAmount = Math.min(1.0F, this.lieDownAmount + 0.15F);
         this.lieDownAmountTail = Math.min(1.0F, this.lieDownAmountTail + 0.08F);
      } else {
         this.lieDownAmount = Math.max(0.0F, this.lieDownAmount - 0.22F);
         this.lieDownAmountTail = Math.max(0.0F, this.lieDownAmountTail - 0.13F);
      }

   }

   private void updateRelaxStateOneAmount() {
      this.relaxStateOneAmountO = this.relaxStateOneAmount;
      if (this.isRelaxStateOne()) {
         this.relaxStateOneAmount = Math.min(1.0F, this.relaxStateOneAmount + 0.1F);
      } else {
         this.relaxStateOneAmount = Math.max(0.0F, this.relaxStateOneAmount - 0.13F);
      }

   }

   public float getLieDownAmount(float pPartialTicks) {
      return Mth.lerp(pPartialTicks, this.lieDownAmountO, this.lieDownAmount);
   }

   public float getLieDownAmountTail(float pPartialTicks) {
      return Mth.lerp(pPartialTicks, this.lieDownAmountOTail, this.lieDownAmountTail);
   }

   public float getRelaxStateOneAmount(float pPartialTicks) {
      return Mth.lerp(pPartialTicks, this.relaxStateOneAmountO, this.relaxStateOneAmount);
   }

   @Nullable
   public Cat getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      Cat cat = EntityType.CAT.create(pLevel);
      if (cat != null && pOtherParent instanceof Cat cat1) {
         if (this.random.nextBoolean()) {
            cat.setVariant(this.getVariant());
         } else {
            cat.setVariant(cat1.getVariant());
         }

         if (this.isTame()) {
            cat.setOwnerUUID(this.getOwnerUUID());
            cat.setTame(true);
            if (this.random.nextBoolean()) {
               cat.setCollarColor(this.getCollarColor());
            } else {
               cat.setCollarColor(cat1.getCollarColor());
            }
         }
      }

      return cat;
   }

   /**
    * Returns {@code true} if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(Animal pOtherAnimal) {
      if (!this.isTame()) {
         return false;
      } else if (!(pOtherAnimal instanceof Cat)) {
         return false;
      } else {
         Cat cat = (Cat)pOtherAnimal;
         return cat.isTame() && super.canMate(pOtherAnimal);
      }
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      boolean flag = pLevel.getMoonBrightness() > 0.9F;
      TagKey<CatVariant> tagkey = flag ? CatVariantTags.FULL_MOON_SPAWNS : CatVariantTags.DEFAULT_SPAWNS;
      BuiltInRegistries.CAT_VARIANT.getTag(tagkey).flatMap((p_289435_) -> {
         return p_289435_.getRandomElement(pLevel.getRandom());
      }).ifPresent((p_262565_) -> {
         this.setVariant(p_262565_.value());
      });
      ServerLevel serverlevel = pLevel.getLevel();
      if (serverlevel.structureManager().getStructureWithPieceAt(this.blockPosition(), StructureTags.CATS_SPAWN_AS_BLACK).isValid()) {
         this.setVariant(BuiltInRegistries.CAT_VARIANT.getOrThrow(CatVariant.ALL_BLACK));
         this.setPersistenceRequired();
      }

      return pSpawnData;
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      Item item = itemstack.getItem();
      if (this.level().isClientSide) {
         if (this.isTame() && this.isOwnedBy(pPlayer)) {
            return InteractionResult.SUCCESS;
         } else {
            return !this.isFood(itemstack) || !(this.getHealth() < this.getMaxHealth()) && this.isTame() ? InteractionResult.PASS : InteractionResult.SUCCESS;
         }
      } else {
         if (this.isTame()) {
            if (this.isOwnedBy(pPlayer)) {
               if (!(item instanceof DyeItem)) {
                  if (item.isEdible() && this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                     this.heal((float)itemstack.getFoodProperties(this).getNutrition());
                     this.usePlayerItem(pPlayer, pHand, itemstack);
                     return InteractionResult.CONSUME;
                  }

                  InteractionResult interactionresult = super.mobInteract(pPlayer, pHand);
                  if (!interactionresult.consumesAction() || this.isBaby()) {
                     this.setOrderedToSit(!this.isOrderedToSit());
                  }

                  return interactionresult;
               }

               DyeColor dyecolor = ((DyeItem)item).getDyeColor();
               if (dyecolor != this.getCollarColor()) {
                  this.setCollarColor(dyecolor);
                  if (!pPlayer.getAbilities().instabuild) {
                     itemstack.shrink(1);
                  }

                  this.setPersistenceRequired();
                  return InteractionResult.CONSUME;
               }
            }
         } else if (this.isFood(itemstack)) {
            this.usePlayerItem(pPlayer, pHand, itemstack);
            if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
               this.tame(pPlayer);
               this.setOrderedToSit(true);
               this.level().broadcastEntityEvent(this, (byte)7);
            } else {
               this.level().broadcastEntityEvent(this, (byte)6);
            }

            this.setPersistenceRequired();
            return InteractionResult.CONSUME;
         }

         InteractionResult interactionresult1 = super.mobInteract(pPlayer, pHand);
         if (interactionresult1.consumesAction()) {
            this.setPersistenceRequired();
         }

         return interactionresult1;
      }
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return TEMPT_INGREDIENT.test(pStack);
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return pSize.height * 0.5F;
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return !this.isTame() && this.tickCount > 2400;
   }

   protected void reassessTameGoals() {
      if (this.avoidPlayersGoal == null) {
         this.avoidPlayersGoal = new Cat.CatAvoidEntityGoal<>(this, Player.class, 16.0F, 0.8D, 1.33D);
      }

      this.goalSelector.removeGoal(this.avoidPlayersGoal);
      if (!this.isTame()) {
         this.goalSelector.addGoal(4, this.avoidPlayersGoal);
      }

   }

   public boolean isSteppingCarefully() {
      return this.isCrouching() || super.isSteppingCarefully();
   }

   static class CatAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
      private final Cat cat;

      public CatAvoidEntityGoal(Cat pCat, Class<T> pEntityClassToAvoid, float pMaxDist, double pWalkSpeedModifier, double pSprintSpeedModifier) {
         super(pCat, pEntityClassToAvoid, pMaxDist, pWalkSpeedModifier, pSprintSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
         this.cat = pCat;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return !this.cat.isTame() && super.canUse();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return !this.cat.isTame() && super.canContinueToUse();
      }
   }

   static class CatRelaxOnOwnerGoal extends Goal {
      private final Cat cat;
      @Nullable
      private Player ownerPlayer;
      @Nullable
      private BlockPos goalPos;
      private int onBedTicks;

      public CatRelaxOnOwnerGoal(Cat pCat) {
         this.cat = pCat;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (!this.cat.isTame()) {
            return false;
         } else if (this.cat.isOrderedToSit()) {
            return false;
         } else {
            LivingEntity livingentity = this.cat.getOwner();
            if (livingentity instanceof Player) {
               this.ownerPlayer = (Player)livingentity;
               if (!livingentity.isSleeping()) {
                  return false;
               }

               if (this.cat.distanceToSqr(this.ownerPlayer) > 100.0D) {
                  return false;
               }

               BlockPos blockpos = this.ownerPlayer.blockPosition();
               BlockState blockstate = this.cat.level().getBlockState(blockpos);
               if (blockstate.is(BlockTags.BEDS)) {
                  this.goalPos = blockstate.getOptionalValue(BedBlock.FACING).map((p_28209_) -> {
                     return blockpos.relative(p_28209_.getOpposite());
                  }).orElseGet(() -> {
                     return new BlockPos(blockpos);
                  });
                  return !this.spaceIsOccupied();
               }
            }

            return false;
         }
      }

      private boolean spaceIsOccupied() {
         for(Cat cat : this.cat.level().getEntitiesOfClass(Cat.class, (new AABB(this.goalPos)).inflate(2.0D))) {
            if (cat != this.cat && (cat.isLying() || cat.isRelaxStateOne())) {
               return true;
            }
         }

         return false;
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.cat.isTame() && !this.cat.isOrderedToSit() && this.ownerPlayer != null && this.ownerPlayer.isSleeping() && this.goalPos != null && !this.spaceIsOccupied();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         if (this.goalPos != null) {
            this.cat.setInSittingPose(false);
            this.cat.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), (double)1.1F);
         }

      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.cat.setLying(false);
         float f = this.cat.level().getTimeOfDay(1.0F);
         if (this.ownerPlayer.getSleepTimer() >= 100 && (double)f > 0.77D && (double)f < 0.8D && (double)this.cat.level().getRandom().nextFloat() < 0.7D) {
            this.giveMorningGift();
         }

         this.onBedTicks = 0;
         this.cat.setRelaxStateOne(false);
         this.cat.getNavigation().stop();
      }

      private void giveMorningGift() {
         RandomSource randomsource = this.cat.getRandom();
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
         blockpos$mutableblockpos.set(this.cat.isLeashed() ? this.cat.getLeashHolder().blockPosition() : this.cat.blockPosition());
         this.cat.randomTeleport((double)(blockpos$mutableblockpos.getX() + randomsource.nextInt(11) - 5), (double)(blockpos$mutableblockpos.getY() + randomsource.nextInt(5) - 2), (double)(blockpos$mutableblockpos.getZ() + randomsource.nextInt(11) - 5), false);
         blockpos$mutableblockpos.set(this.cat.blockPosition());
         LootTable loottable = this.cat.level().getServer().getLootData().getLootTable(BuiltInLootTables.CAT_MORNING_GIFT);
         LootParams lootparams = (new LootParams.Builder((ServerLevel)this.cat.level())).withParameter(LootContextParams.ORIGIN, this.cat.position()).withParameter(LootContextParams.THIS_ENTITY, this.cat).create(LootContextParamSets.GIFT);

         for(ItemStack itemstack : loottable.getRandomItems(lootparams)) {
            this.cat.level().addFreshEntity(new ItemEntity(this.cat.level(), (double)blockpos$mutableblockpos.getX() - (double)Mth.sin(this.cat.yBodyRot * ((float)Math.PI / 180F)), (double)blockpos$mutableblockpos.getY(), (double)blockpos$mutableblockpos.getZ() + (double)Mth.cos(this.cat.yBodyRot * ((float)Math.PI / 180F)), itemstack));
         }

      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.ownerPlayer != null && this.goalPos != null) {
            this.cat.setInSittingPose(false);
            this.cat.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), (double)1.1F);
            if (this.cat.distanceToSqr(this.ownerPlayer) < 2.5D) {
               ++this.onBedTicks;
               if (this.onBedTicks > this.adjustedTickDelay(16)) {
                  this.cat.setLying(true);
                  this.cat.setRelaxStateOne(false);
               } else {
                  this.cat.lookAt(this.ownerPlayer, 45.0F, 45.0F);
                  this.cat.setRelaxStateOne(true);
               }
            } else {
               this.cat.setLying(false);
            }
         }

      }
   }

   static class CatTemptGoal extends TemptGoal {
      @Nullable
      private Player selectedPlayer;
      private final Cat cat;

      public CatTemptGoal(Cat pCat, double pSpeedModifier, Ingredient pItems, boolean pCanScare) {
         super(pCat, pSpeedModifier, pItems, pCanScare);
         this.cat = pCat;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         super.tick();
         if (this.selectedPlayer == null && this.mob.getRandom().nextInt(this.adjustedTickDelay(600)) == 0) {
            this.selectedPlayer = this.player;
         } else if (this.mob.getRandom().nextInt(this.adjustedTickDelay(500)) == 0) {
            this.selectedPlayer = null;
         }

      }

      protected boolean canScare() {
         return this.selectedPlayer != null && this.selectedPlayer.equals(this.player) ? false : super.canScare();
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && !this.cat.isTame();
      }
   }
}
