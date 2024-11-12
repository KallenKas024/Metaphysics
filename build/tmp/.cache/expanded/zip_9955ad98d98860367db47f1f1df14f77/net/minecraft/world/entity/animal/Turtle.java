package net.minecraft.world.entity.animal;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Turtle extends Animal {
   private static final EntityDataAccessor<BlockPos> HOME_POS = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BLOCK_POS);
   private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<BlockPos> TRAVEL_POS = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BLOCK_POS);
   private static final EntityDataAccessor<Boolean> GOING_HOME = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> TRAVELLING = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
   public static final Ingredient FOOD_ITEMS = Ingredient.of(Blocks.SEAGRASS.asItem());
   int layEggCounter;
   public static final Predicate<LivingEntity> BABY_ON_LAND_SELECTOR = (p_289447_) -> {
      return p_289447_.isBaby() && !p_289447_.isInWater();
   };

   public Turtle(EntityType<? extends Turtle> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
      this.setPathfindingMalus(BlockPathTypes.DOOR_IRON_CLOSED, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DOOR_WOOD_CLOSED, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DOOR_OPEN, -1.0F);
      this.moveControl = new Turtle.TurtleMoveControl(this);
      this.setMaxUpStep(1.0F);
   }

   public void setHomePos(BlockPos pHomePos) {
      this.entityData.set(HOME_POS, pHomePos);
   }

   BlockPos getHomePos() {
      return this.entityData.get(HOME_POS);
   }

   void setTravelPos(BlockPos pTravelPos) {
      this.entityData.set(TRAVEL_POS, pTravelPos);
   }

   BlockPos getTravelPos() {
      return this.entityData.get(TRAVEL_POS);
   }

   public boolean hasEgg() {
      return this.entityData.get(HAS_EGG);
   }

   void setHasEgg(boolean pHasEgg) {
      this.entityData.set(HAS_EGG, pHasEgg);
   }

   public boolean isLayingEgg() {
      return this.entityData.get(LAYING_EGG);
   }

   void setLayingEgg(boolean pIsLayingEgg) {
      this.layEggCounter = pIsLayingEgg ? 1 : 0;
      this.entityData.set(LAYING_EGG, pIsLayingEgg);
   }

   boolean isGoingHome() {
      return this.entityData.get(GOING_HOME);
   }

   void setGoingHome(boolean pIsGoingHome) {
      this.entityData.set(GOING_HOME, pIsGoingHome);
   }

   boolean isTravelling() {
      return this.entityData.get(TRAVELLING);
   }

   void setTravelling(boolean pIsTravelling) {
      this.entityData.set(TRAVELLING, pIsTravelling);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(HOME_POS, BlockPos.ZERO);
      this.entityData.define(HAS_EGG, false);
      this.entityData.define(TRAVEL_POS, BlockPos.ZERO);
      this.entityData.define(GOING_HOME, false);
      this.entityData.define(TRAVELLING, false);
      this.entityData.define(LAYING_EGG, false);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("HomePosX", this.getHomePos().getX());
      pCompound.putInt("HomePosY", this.getHomePos().getY());
      pCompound.putInt("HomePosZ", this.getHomePos().getZ());
      pCompound.putBoolean("HasEgg", this.hasEgg());
      pCompound.putInt("TravelPosX", this.getTravelPos().getX());
      pCompound.putInt("TravelPosY", this.getTravelPos().getY());
      pCompound.putInt("TravelPosZ", this.getTravelPos().getZ());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      int i = pCompound.getInt("HomePosX");
      int j = pCompound.getInt("HomePosY");
      int k = pCompound.getInt("HomePosZ");
      this.setHomePos(new BlockPos(i, j, k));
      super.readAdditionalSaveData(pCompound);
      this.setHasEgg(pCompound.getBoolean("HasEgg"));
      int l = pCompound.getInt("TravelPosX");
      int i1 = pCompound.getInt("TravelPosY");
      int j1 = pCompound.getInt("TravelPosZ");
      this.setTravelPos(new BlockPos(l, i1, j1));
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      this.setHomePos(this.blockPosition());
      this.setTravelPos(BlockPos.ZERO);
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public static boolean checkTurtleSpawnRules(EntityType<Turtle> pTurtle, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pPos.getY() < pLevel.getSeaLevel() + 4 && TurtleEggBlock.onSand(pLevel, pPos) && isBrightEnoughToSpawn(pLevel, pPos);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new Turtle.TurtlePanicGoal(this, 1.2D));
      this.goalSelector.addGoal(1, new Turtle.TurtleBreedGoal(this, 1.0D));
      this.goalSelector.addGoal(1, new Turtle.TurtleLayEggGoal(this, 1.0D));
      this.goalSelector.addGoal(2, new TemptGoal(this, 1.1D, FOOD_ITEMS, false));
      this.goalSelector.addGoal(3, new Turtle.TurtleGoToWaterGoal(this, 1.0D));
      this.goalSelector.addGoal(4, new Turtle.TurtleGoHomeGoal(this, 1.0D));
      this.goalSelector.addGoal(7, new Turtle.TurtleTravelGoal(this, 1.0D));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(9, new Turtle.TurtleRandomStrollGoal(this, 1.0D, 100));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D).add(Attributes.MOVEMENT_SPEED, 0.25D);
   }

   public boolean isPushedByFluid() {
      return false;
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   public MobType getMobType() {
      return MobType.WATER;
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 200;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return !this.isInWater() && this.onGround() && !this.isBaby() ? SoundEvents.TURTLE_AMBIENT_LAND : super.getAmbientSound();
   }

   protected void playSwimSound(float pVolume) {
      super.playSwimSound(pVolume * 1.5F);
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.TURTLE_SWIM;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return this.isBaby() ? SoundEvents.TURTLE_HURT_BABY : SoundEvents.TURTLE_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return this.isBaby() ? SoundEvents.TURTLE_DEATH_BABY : SoundEvents.TURTLE_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      SoundEvent soundevent = this.isBaby() ? SoundEvents.TURTLE_SHAMBLE_BABY : SoundEvents.TURTLE_SHAMBLE;
      this.playSound(soundevent, 0.15F, 1.0F);
   }

   public boolean canFallInLove() {
      return super.canFallInLove() && !this.hasEgg();
   }

   protected float nextStep() {
      return this.moveDist + 0.15F;
   }

   public float getScale() {
      return this.isBaby() ? 0.3F : 1.0F;
   }

   protected PathNavigation createNavigation(Level pLevel) {
      return new Turtle.TurtlePathNavigation(this, pLevel);
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      return EntityType.TURTLE.create(pLevel);
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return pStack.is(Blocks.SEAGRASS.asItem());
   }

   public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
      if (!this.isGoingHome() && pLevel.getFluidState(pPos).is(FluidTags.WATER)) {
         return 10.0F;
      } else {
         return TurtleEggBlock.onSand(pLevel, pPos) ? 10.0F : pLevel.getPathfindingCostFromLightLevels(pPos);
      }
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.isAlive() && this.isLayingEgg() && this.layEggCounter >= 1 && this.layEggCounter % 5 == 0) {
         BlockPos blockpos = this.blockPosition();
         if (TurtleEggBlock.onSand(this.level(), blockpos)) {
            this.level().levelEvent(2001, blockpos, Block.getId(this.level().getBlockState(blockpos.below())));
         }
      }

   }

   protected void ageBoundaryReached() {
      super.ageBoundaryReached();
      if (!this.isBaby() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
         this.spawnAtLocation(Items.SCUTE, 1);
      }

   }

   public void travel(Vec3 pTravelVector) {
      if (this.isControlledByLocalInstance() && this.isInWater()) {
         this.moveRelative(0.1F, pTravelVector);
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
         if (this.getTarget() == null && (!this.isGoingHome() || !this.getHomePos().closerToCenterThan(this.position(), 20.0D))) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
         }
      } else {
         super.travel(pTravelVector);
      }

   }

   public boolean canBeLeashed(Player pPlayer) {
      return false;
   }

   public void thunderHit(ServerLevel pLevel, LightningBolt pLightning) {
      this.hurt(this.damageSources().lightningBolt(), Float.MAX_VALUE);
   }

   static class TurtleBreedGoal extends BreedGoal {
      private final Turtle turtle;

      TurtleBreedGoal(Turtle pTurtle, double pSpeedModifier) {
         super(pTurtle, pSpeedModifier);
         this.turtle = pTurtle;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return super.canUse() && !this.turtle.hasEgg();
      }

      /**
       * Spawns a baby animal of the same type.
       */
      protected void breed() {
         ServerPlayer serverplayer = this.animal.getLoveCause();
         if (serverplayer == null && this.partner.getLoveCause() != null) {
            serverplayer = this.partner.getLoveCause();
         }

         if (serverplayer != null) {
            serverplayer.awardStat(Stats.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger(serverplayer, this.animal, this.partner, (AgeableMob)null);
         }

         this.turtle.setHasEgg(true);
         this.animal.setAge(6000);
         this.partner.setAge(6000);
         this.animal.resetLove();
         this.partner.resetLove();
         RandomSource randomsource = this.animal.getRandom();
         if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), randomsource.nextInt(7) + 1));
         }

      }
   }

   static class TurtleGoHomeGoal extends Goal {
      private final Turtle turtle;
      private final double speedModifier;
      private boolean stuck;
      private int closeToHomeTryTicks;
      private static final int GIVE_UP_TICKS = 600;

      TurtleGoHomeGoal(Turtle pTurtle, double pSpeedModifier) {
         this.turtle = pTurtle;
         this.speedModifier = pSpeedModifier;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.turtle.isBaby()) {
            return false;
         } else if (this.turtle.hasEgg()) {
            return true;
         } else if (this.turtle.getRandom().nextInt(reducedTickDelay(700)) != 0) {
            return false;
         } else {
            return !this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 64.0D);
         }
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.turtle.setGoingHome(true);
         this.stuck = false;
         this.closeToHomeTryTicks = 0;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.turtle.setGoingHome(false);
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return !this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 7.0D) && !this.stuck && this.closeToHomeTryTicks <= this.adjustedTickDelay(600);
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         BlockPos blockpos = this.turtle.getHomePos();
         boolean flag = blockpos.closerToCenterThan(this.turtle.position(), 16.0D);
         if (flag) {
            ++this.closeToHomeTryTicks;
         }

         if (this.turtle.getNavigation().isDone()) {
            Vec3 vec3 = Vec3.atBottomCenterOf(blockpos);
            Vec3 vec31 = DefaultRandomPos.getPosTowards(this.turtle, 16, 3, vec3, (double)((float)Math.PI / 10F));
            if (vec31 == null) {
               vec31 = DefaultRandomPos.getPosTowards(this.turtle, 8, 7, vec3, (double)((float)Math.PI / 2F));
            }

            if (vec31 != null && !flag && !this.turtle.level().getBlockState(BlockPos.containing(vec31)).is(Blocks.WATER)) {
               vec31 = DefaultRandomPos.getPosTowards(this.turtle, 16, 5, vec3, (double)((float)Math.PI / 2F));
            }

            if (vec31 == null) {
               this.stuck = true;
               return;
            }

            this.turtle.getNavigation().moveTo(vec31.x, vec31.y, vec31.z, this.speedModifier);
         }

      }
   }

   static class TurtleGoToWaterGoal extends MoveToBlockGoal {
      private static final int GIVE_UP_TICKS = 1200;
      private final Turtle turtle;

      TurtleGoToWaterGoal(Turtle pTurtle, double pSpeedModifier) {
         super(pTurtle, pTurtle.isBaby() ? 2.0D : pSpeedModifier, 24);
         this.turtle = pTurtle;
         this.verticalSearchStart = -1;
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return !this.turtle.isInWater() && this.tryTicks <= 1200 && this.isValidTarget(this.turtle.level(), this.blockPos);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.turtle.isBaby() && !this.turtle.isInWater()) {
            return super.canUse();
         } else {
            return !this.turtle.isGoingHome() && !this.turtle.isInWater() && !this.turtle.hasEgg() ? super.canUse() : false;
         }
      }

      public boolean shouldRecalculatePath() {
         return this.tryTicks % 160 == 0;
      }

      /**
       * Return {@code true} to set given position as destination
       */
      protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
         return pLevel.getBlockState(pPos).is(Blocks.WATER);
      }
   }

   static class TurtleLayEggGoal extends MoveToBlockGoal {
      private final Turtle turtle;

      TurtleLayEggGoal(Turtle pTurtle, double pSpeedModifier) {
         super(pTurtle, pSpeedModifier, 16);
         this.turtle = pTurtle;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.turtle.hasEgg() && this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 9.0D) ? super.canUse() : false;
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return super.canContinueToUse() && this.turtle.hasEgg() && this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 9.0D);
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         super.tick();
         BlockPos blockpos = this.turtle.blockPosition();
         if (!this.turtle.isInWater() && this.isReachedTarget()) {
            if (this.turtle.layEggCounter < 1) {
               this.turtle.setLayingEgg(true);
            } else if (this.turtle.layEggCounter > this.adjustedTickDelay(200)) {
               Level level = this.turtle.level();
               level.playSound((Player)null, blockpos, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
               BlockPos blockpos1 = this.blockPos.above();
               BlockState blockstate = Blocks.TURTLE_EGG.defaultBlockState().setValue(TurtleEggBlock.EGGS, Integer.valueOf(this.turtle.random.nextInt(4) + 1));
               level.setBlock(blockpos1, blockstate, 3);
               level.gameEvent(GameEvent.BLOCK_PLACE, blockpos1, GameEvent.Context.of(this.turtle, blockstate));
               this.turtle.setHasEgg(false);
               this.turtle.setLayingEgg(false);
               this.turtle.setInLoveTime(600);
            }

            if (this.turtle.isLayingEgg()) {
               ++this.turtle.layEggCounter;
            }
         }

      }

      /**
       * Return {@code true} to set given position as destination
       */
      protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
         return !pLevel.isEmptyBlock(pPos.above()) ? false : TurtleEggBlock.isSand(pLevel, pPos);
      }
   }

   static class TurtleMoveControl extends MoveControl {
      private final Turtle turtle;

      TurtleMoveControl(Turtle pTurtle) {
         super(pTurtle);
         this.turtle = pTurtle;
      }

      private void updateSpeed() {
         if (this.turtle.isInWater()) {
            this.turtle.setDeltaMovement(this.turtle.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
            if (!this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 16.0D)) {
               this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 2.0F, 0.08F));
            }

            if (this.turtle.isBaby()) {
               this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 3.0F, 0.06F));
            }
         } else if (this.turtle.onGround()) {
            this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 2.0F, 0.06F));
         }

      }

      public void tick() {
         this.updateSpeed();
         if (this.operation == MoveControl.Operation.MOVE_TO && !this.turtle.getNavigation().isDone()) {
            double d0 = this.wantedX - this.turtle.getX();
            double d1 = this.wantedY - this.turtle.getY();
            double d2 = this.wantedZ - this.turtle.getZ();
            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            if (d3 < (double)1.0E-5F) {
               this.mob.setSpeed(0.0F);
            } else {
               d1 /= d3;
               float f = (float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
               this.turtle.setYRot(this.rotlerp(this.turtle.getYRot(), f, 90.0F));
               this.turtle.yBodyRot = this.turtle.getYRot();
               float f1 = (float)(this.speedModifier * this.turtle.getAttributeValue(Attributes.MOVEMENT_SPEED));
               this.turtle.setSpeed(Mth.lerp(0.125F, this.turtle.getSpeed(), f1));
               this.turtle.setDeltaMovement(this.turtle.getDeltaMovement().add(0.0D, (double)this.turtle.getSpeed() * d1 * 0.1D, 0.0D));
            }
         } else {
            this.turtle.setSpeed(0.0F);
         }
      }
   }

   static class TurtlePanicGoal extends PanicGoal {
      TurtlePanicGoal(Turtle pTurtle, double pSpeedModifier) {
         super(pTurtle, pSpeedModifier);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (!this.shouldPanic()) {
            return false;
         } else {
            BlockPos blockpos = this.lookForWater(this.mob.level(), this.mob, 7);
            if (blockpos != null) {
               this.posX = (double)blockpos.getX();
               this.posY = (double)blockpos.getY();
               this.posZ = (double)blockpos.getZ();
               return true;
            } else {
               return this.findRandomPosition();
            }
         }
      }
   }

   static class TurtlePathNavigation extends AmphibiousPathNavigation {
      TurtlePathNavigation(Turtle pTurtle, Level pLevel) {
         super(pTurtle, pLevel);
      }

      public boolean isStableDestination(BlockPos pPos) {
         Mob mob = this.mob;
         if (mob instanceof Turtle turtle) {
            if (turtle.isTravelling()) {
               return this.level.getBlockState(pPos).is(Blocks.WATER);
            }
         }

         return !this.level.getBlockState(pPos.below()).isAir();
      }
   }

   static class TurtleRandomStrollGoal extends RandomStrollGoal {
      private final Turtle turtle;

      TurtleRandomStrollGoal(Turtle pTurtle, double pSpeedModifier, int pInterval) {
         super(pTurtle, pSpeedModifier, pInterval);
         this.turtle = pTurtle;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return !this.mob.isInWater() && !this.turtle.isGoingHome() && !this.turtle.hasEgg() ? super.canUse() : false;
      }
   }

   static class TurtleTravelGoal extends Goal {
      private final Turtle turtle;
      private final double speedModifier;
      private boolean stuck;

      TurtleTravelGoal(Turtle pTurtle, double pSpeedModifier) {
         this.turtle = pTurtle;
         this.speedModifier = pSpeedModifier;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return !this.turtle.isGoingHome() && !this.turtle.hasEgg() && this.turtle.isInWater();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         int i = 512;
         int j = 4;
         RandomSource randomsource = this.turtle.random;
         int k = randomsource.nextInt(1025) - 512;
         int l = randomsource.nextInt(9) - 4;
         int i1 = randomsource.nextInt(1025) - 512;
         if ((double)l + this.turtle.getY() > (double)(this.turtle.level().getSeaLevel() - 1)) {
            l = 0;
         }

         BlockPos blockpos = BlockPos.containing((double)k + this.turtle.getX(), (double)l + this.turtle.getY(), (double)i1 + this.turtle.getZ());
         this.turtle.setTravelPos(blockpos);
         this.turtle.setTravelling(true);
         this.stuck = false;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.turtle.getNavigation().isDone()) {
            Vec3 vec3 = Vec3.atBottomCenterOf(this.turtle.getTravelPos());
            Vec3 vec31 = DefaultRandomPos.getPosTowards(this.turtle, 16, 3, vec3, (double)((float)Math.PI / 10F));
            if (vec31 == null) {
               vec31 = DefaultRandomPos.getPosTowards(this.turtle, 8, 7, vec3, (double)((float)Math.PI / 2F));
            }

            if (vec31 != null) {
               int i = Mth.floor(vec31.x);
               int j = Mth.floor(vec31.z);
               int k = 34;
               if (!this.turtle.level().hasChunksAt(i - 34, j - 34, i + 34, j + 34)) {
                  vec31 = null;
               }
            }

            if (vec31 == null) {
               this.stuck = true;
               return;
            }

            this.turtle.getNavigation().moveTo(vec31.x, vec31.y, vec31.z, this.speedModifier);
         }

      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return !this.turtle.getNavigation().isDone() && !this.stuck && !this.turtle.isGoingHome() && !this.turtle.isInLove() && !this.turtle.hasEgg();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.turtle.setTravelling(false);
         super.stop();
      }
   }
}