package net.minecraft.world.entity.animal;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class Rabbit extends Animal implements VariantHolder<Rabbit.Variant> {
   public static final double STROLL_SPEED_MOD = 0.6D;
   public static final double BREED_SPEED_MOD = 0.8D;
   public static final double FOLLOW_SPEED_MOD = 1.0D;
   public static final double FLEE_SPEED_MOD = 2.2D;
   public static final double ATTACK_SPEED_MOD = 1.4D;
   private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.INT);
   private static final ResourceLocation KILLER_BUNNY = new ResourceLocation("killer_bunny");
   public static final int EVIL_ATTACK_POWER = 8;
   public static final int EVIL_ARMOR_VALUE = 8;
   private static final int MORE_CARROTS_DELAY = 40;
   private int jumpTicks;
   private int jumpDuration;
   private boolean wasOnGround;
   private int jumpDelayTicks;
   int moreCarrotTicks;

   public Rabbit(EntityType<? extends Rabbit> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.jumpControl = new Rabbit.RabbitJumpControl(this);
      this.moveControl = new Rabbit.RabbitMoveControl(this);
      this.setSpeedModifier(0.0D);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(1, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
      this.goalSelector.addGoal(1, new Rabbit.RabbitPanicGoal(this, 2.2D));
      this.goalSelector.addGoal(2, new BreedGoal(this, 0.8D));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, Ingredient.of(Items.CARROT, Items.GOLDEN_CARROT, Blocks.DANDELION), false));
      this.goalSelector.addGoal(4, new Rabbit.RabbitAvoidEntityGoal<>(this, Player.class, 8.0F, 2.2D, 2.2D));
      this.goalSelector.addGoal(4, new Rabbit.RabbitAvoidEntityGoal<>(this, Wolf.class, 10.0F, 2.2D, 2.2D));
      this.goalSelector.addGoal(4, new Rabbit.RabbitAvoidEntityGoal<>(this, Monster.class, 4.0F, 2.2D, 2.2D));
      this.goalSelector.addGoal(5, new Rabbit.RaidGardenGoal(this));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
      this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));
   }

   protected float getJumpPower() {
      float f = 0.3F;
      if (this.horizontalCollision || this.moveControl.hasWanted() && this.moveControl.getWantedY() > this.getY() + 0.5D) {
         f = 0.5F;
      }

      Path path = this.navigation.getPath();
      if (path != null && !path.isDone()) {
         Vec3 vec3 = path.getNextEntityPos(this);
         if (vec3.y > this.getY() + 0.5D) {
            f = 0.5F;
         }
      }

      if (this.moveControl.getSpeedModifier() <= 0.6D) {
         f = 0.2F;
      }

      return f + this.getJumpBoostPower();
   }

   /**
    * Causes this entity to do an upwards motion (jumping).
    */
   protected void jumpFromGround() {
      super.jumpFromGround();
      double d0 = this.moveControl.getSpeedModifier();
      if (d0 > 0.0D) {
         double d1 = this.getDeltaMovement().horizontalDistanceSqr();
         if (d1 < 0.01D) {
            this.moveRelative(0.1F, new Vec3(0.0D, 0.0D, 1.0D));
         }
      }

      if (!this.level().isClientSide) {
         this.level().broadcastEntityEvent(this, (byte)1);
      }

   }

   public float getJumpCompletion(float pPartialTick) {
      return this.jumpDuration == 0 ? 0.0F : ((float)this.jumpTicks + pPartialTick) / (float)this.jumpDuration;
   }

   public void setSpeedModifier(double pSpeedModifier) {
      this.getNavigation().setSpeedModifier(pSpeedModifier);
      this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), pSpeedModifier);
   }

   public void setJumping(boolean pJumping) {
      super.setJumping(pJumping);
      if (pJumping) {
         this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
      }

   }

   public void startJumping() {
      this.setJumping(true);
      this.jumpDuration = 10;
      this.jumpTicks = 0;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_TYPE_ID, Rabbit.Variant.BROWN.id);
   }

   public void customServerAiStep() {
      if (this.jumpDelayTicks > 0) {
         --this.jumpDelayTicks;
      }

      if (this.moreCarrotTicks > 0) {
         this.moreCarrotTicks -= this.random.nextInt(3);
         if (this.moreCarrotTicks < 0) {
            this.moreCarrotTicks = 0;
         }
      }

      if (this.onGround()) {
         if (!this.wasOnGround) {
            this.setJumping(false);
            this.checkLandingDelay();
         }

         if (this.getVariant() == Rabbit.Variant.EVIL && this.jumpDelayTicks == 0) {
            LivingEntity livingentity = this.getTarget();
            if (livingentity != null && this.distanceToSqr(livingentity) < 16.0D) {
               this.facePoint(livingentity.getX(), livingentity.getZ());
               this.moveControl.setWantedPosition(livingentity.getX(), livingentity.getY(), livingentity.getZ(), this.moveControl.getSpeedModifier());
               this.startJumping();
               this.wasOnGround = true;
            }
         }

         Rabbit.RabbitJumpControl rabbit$rabbitjumpcontrol = (Rabbit.RabbitJumpControl)this.jumpControl;
         if (!rabbit$rabbitjumpcontrol.wantJump()) {
            if (this.moveControl.hasWanted() && this.jumpDelayTicks == 0) {
               Path path = this.navigation.getPath();
               Vec3 vec3 = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
               if (path != null && !path.isDone()) {
                  vec3 = path.getNextEntityPos(this);
               }

               this.facePoint(vec3.x, vec3.z);
               this.startJumping();
            }
         } else if (!rabbit$rabbitjumpcontrol.canJump()) {
            this.enableJumpControl();
         }
      }

      this.wasOnGround = this.onGround();
   }

   public boolean canSpawnSprintParticle() {
      return false;
   }

   private void facePoint(double pX, double pZ) {
      this.setYRot((float)(Mth.atan2(pZ - this.getZ(), pX - this.getX()) * (double)(180F / (float)Math.PI)) - 90.0F);
   }

   private void enableJumpControl() {
      ((Rabbit.RabbitJumpControl)this.jumpControl).setCanJump(true);
   }

   private void disableJumpControl() {
      ((Rabbit.RabbitJumpControl)this.jumpControl).setCanJump(false);
   }

   private void setLandingDelay() {
      if (this.moveControl.getSpeedModifier() < 2.2D) {
         this.jumpDelayTicks = 10;
      } else {
         this.jumpDelayTicks = 1;
      }

   }

   private void checkLandingDelay() {
      this.setLandingDelay();
      this.disableJumpControl();
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.jumpTicks != this.jumpDuration) {
         ++this.jumpTicks;
      } else if (this.jumpDuration != 0) {
         this.jumpTicks = 0;
         this.jumpDuration = 0;
         this.setJumping(false);
      }

   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0D).add(Attributes.MOVEMENT_SPEED, (double)0.3F);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("RabbitType", this.getVariant().id);
      pCompound.putInt("MoreCarrotTicks", this.moreCarrotTicks);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setVariant(Rabbit.Variant.byId(pCompound.getInt("RabbitType")));
      this.moreCarrotTicks = pCompound.getInt("MoreCarrotTicks");
   }

   protected SoundEvent getJumpSound() {
      return SoundEvents.RABBIT_JUMP;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.RABBIT_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.RABBIT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.RABBIT_DEATH;
   }

   public boolean doHurtTarget(Entity pEntity) {
      if (this.getVariant() == Rabbit.Variant.EVIL) {
         this.playSound(SoundEvents.RABBIT_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
         return pEntity.hurt(this.damageSources().mobAttack(this), 8.0F);
      } else {
         return pEntity.hurt(this.damageSources().mobAttack(this), 3.0F);
      }
   }

   public SoundSource getSoundSource() {
      return this.getVariant() == Rabbit.Variant.EVIL ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
   }

   private static boolean isTemptingItem(ItemStack pStack) {
      return pStack.is(Items.CARROT) || pStack.is(Items.GOLDEN_CARROT) || pStack.is(Blocks.DANDELION.asItem());
   }

   @Nullable
   public Rabbit getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      Rabbit rabbit = EntityType.RABBIT.create(pLevel);
      if (rabbit != null) {
         Rabbit.Variant rabbit$variant = getRandomRabbitVariant(pLevel, this.blockPosition());
         if (this.random.nextInt(20) != 0) {
            label22: {
               if (pOtherParent instanceof Rabbit) {
                  Rabbit rabbit1 = (Rabbit)pOtherParent;
                  if (this.random.nextBoolean()) {
                     rabbit$variant = rabbit1.getVariant();
                     break label22;
                  }
               }

               rabbit$variant = this.getVariant();
            }
         }

         rabbit.setVariant(rabbit$variant);
      }

      return rabbit;
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return isTemptingItem(pStack);
   }

   public Rabbit.Variant getVariant() {
      return Rabbit.Variant.byId(this.entityData.get(DATA_TYPE_ID));
   }

   public void setVariant(Rabbit.Variant pVariant) {
      if (pVariant == Rabbit.Variant.EVIL) {
         this.getAttribute(Attributes.ARMOR).setBaseValue(8.0D);
         this.goalSelector.addGoal(4, new Rabbit.EvilRabbitAttackGoal(this));
         this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
         this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
         this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Wolf.class, true));
         if (!this.hasCustomName()) {
            this.setCustomName(Component.translatable(Util.makeDescriptionId("entity", KILLER_BUNNY)));
         }
      }

      this.entityData.set(DATA_TYPE_ID, pVariant.id);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      Rabbit.Variant rabbit$variant = getRandomRabbitVariant(pLevel, this.blockPosition());
      if (pSpawnData instanceof Rabbit.RabbitGroupData) {
         rabbit$variant = ((Rabbit.RabbitGroupData)pSpawnData).variant;
      } else {
         pSpawnData = new Rabbit.RabbitGroupData(rabbit$variant);
      }

      this.setVariant(rabbit$variant);
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   private static Rabbit.Variant getRandomRabbitVariant(LevelAccessor pLevel, BlockPos pPos) {
      Holder<Biome> holder = pLevel.getBiome(pPos);
      int i = pLevel.getRandom().nextInt(100);
      if (holder.is(BiomeTags.SPAWNS_WHITE_RABBITS)) {
         return i < 80 ? Rabbit.Variant.WHITE : Rabbit.Variant.WHITE_SPLOTCHED;
      } else if (holder.is(BiomeTags.SPAWNS_GOLD_RABBITS)) {
         return Rabbit.Variant.GOLD;
      } else {
         return i < 50 ? Rabbit.Variant.BROWN : (i < 90 ? Rabbit.Variant.SALT : Rabbit.Variant.BLACK);
      }
   }

   public static boolean checkRabbitSpawnRules(EntityType<Rabbit> pRabbit, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.RABBITS_SPAWNABLE_ON) && isBrightEnoughToSpawn(pLevel, pPos);
   }

   /**
    * Returns {@code true} if {@link #moreCarrotTicks} has reached zero
    */
   boolean wantsMoreFood() {
      return this.moreCarrotTicks <= 0;
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 1) {
         this.spawnSprintParticle();
         this.jumpDuration = 10;
         this.jumpTicks = 0;
      } else {
         super.handleEntityEvent(pId);
      }

   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }

   static class EvilRabbitAttackGoal extends MeleeAttackGoal {
      public EvilRabbitAttackGoal(Rabbit pRabbit) {
         super(pRabbit, 1.4D, true);
      }

      protected double getAttackReachSqr(LivingEntity pAttackTarget) {
         return (double)(4.0F + pAttackTarget.getBbWidth());
      }
   }

   static class RabbitAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
      private final Rabbit rabbit;

      public RabbitAvoidEntityGoal(Rabbit pRabbit, Class<T> pEntityClassToAvoid, float pMaxDist, double pWalkSpeedModifier, double pSprintSpeedModifier) {
         super(pRabbit, pEntityClassToAvoid, pMaxDist, pWalkSpeedModifier, pSprintSpeedModifier);
         this.rabbit = pRabbit;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.rabbit.getVariant() != Rabbit.Variant.EVIL && super.canUse();
      }
   }

   public static class RabbitGroupData extends AgeableMob.AgeableMobGroupData {
      public final Rabbit.Variant variant;

      public RabbitGroupData(Rabbit.Variant pVariant) {
         super(1.0F);
         this.variant = pVariant;
      }
   }

   public static class RabbitJumpControl extends JumpControl {
      private final Rabbit rabbit;
      private boolean canJump;

      public RabbitJumpControl(Rabbit pRabbit) {
         super(pRabbit);
         this.rabbit = pRabbit;
      }

      public boolean wantJump() {
         return this.jump;
      }

      public boolean canJump() {
         return this.canJump;
      }

      public void setCanJump(boolean pCanJump) {
         this.canJump = pCanJump;
      }

      /**
       * Called to actually make the entity jump if isJumping is true.
       */
      public void tick() {
         if (this.jump) {
            this.rabbit.startJumping();
            this.jump = false;
         }

      }
   }

   static class RabbitMoveControl extends MoveControl {
      private final Rabbit rabbit;
      private double nextJumpSpeed;

      public RabbitMoveControl(Rabbit pRabbit) {
         super(pRabbit);
         this.rabbit = pRabbit;
      }

      public void tick() {
         if (this.rabbit.onGround() && !this.rabbit.jumping && !((Rabbit.RabbitJumpControl)this.rabbit.jumpControl).wantJump()) {
            this.rabbit.setSpeedModifier(0.0D);
         } else if (this.hasWanted()) {
            this.rabbit.setSpeedModifier(this.nextJumpSpeed);
         }

         super.tick();
      }

      /**
       * Sets the speed and location to move to
       */
      public void setWantedPosition(double pX, double pY, double pZ, double pSpeed) {
         if (this.rabbit.isInWater()) {
            pSpeed = 1.5D;
         }

         super.setWantedPosition(pX, pY, pZ, pSpeed);
         if (pSpeed > 0.0D) {
            this.nextJumpSpeed = pSpeed;
         }

      }
   }

   static class RabbitPanicGoal extends PanicGoal {
      private final Rabbit rabbit;

      public RabbitPanicGoal(Rabbit pRabbit, double pSpeedModifier) {
         super(pRabbit, pSpeedModifier);
         this.rabbit = pRabbit;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         super.tick();
         this.rabbit.setSpeedModifier(this.speedModifier);
      }
   }

   static class RaidGardenGoal extends MoveToBlockGoal {
      private final Rabbit rabbit;
      private boolean wantsToRaid;
      private boolean canRaid;

      public RaidGardenGoal(Rabbit pRabbit) {
         super(pRabbit, (double)0.7F, 16);
         this.rabbit = pRabbit;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.nextStartTick <= 0) {
            if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.rabbit.level(), this.rabbit)) {
               return false;
            }

            this.canRaid = false;
            this.wantsToRaid = this.rabbit.wantsMoreFood();
         }

         return super.canUse();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.canRaid && super.canContinueToUse();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         super.tick();
         this.rabbit.getLookControl().setLookAt((double)this.blockPos.getX() + 0.5D, (double)(this.blockPos.getY() + 1), (double)this.blockPos.getZ() + 0.5D, 10.0F, (float)this.rabbit.getMaxHeadXRot());
         if (this.isReachedTarget()) {
            Level level = this.rabbit.level();
            BlockPos blockpos = this.blockPos.above();
            BlockState blockstate = level.getBlockState(blockpos);
            Block block = blockstate.getBlock();
            if (this.canRaid && block instanceof CarrotBlock) {
               int i = blockstate.getValue(CarrotBlock.AGE);
               if (i == 0) {
                  level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 2);
                  level.destroyBlock(blockpos, true, this.rabbit);
               } else {
                  level.setBlock(blockpos, blockstate.setValue(CarrotBlock.AGE, Integer.valueOf(i - 1)), 2);
                  level.levelEvent(2001, blockpos, Block.getId(blockstate));
               }

               this.rabbit.moreCarrotTicks = 40;
            }

            this.canRaid = false;
            this.nextStartTick = 10;
         }

      }

      /**
       * Return {@code true} to set given position as destination
       */
      protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
         BlockState blockstate = pLevel.getBlockState(pPos);
         if (blockstate.is(Blocks.FARMLAND) && this.wantsToRaid && !this.canRaid) {
            blockstate = pLevel.getBlockState(pPos.above());
            if (blockstate.getBlock() instanceof CarrotBlock && ((CarrotBlock)blockstate.getBlock()).isMaxAge(blockstate)) {
               this.canRaid = true;
               return true;
            }
         }

         return false;
      }
   }

   public static enum Variant implements StringRepresentable {
      BROWN(0, "brown"),
      WHITE(1, "white"),
      BLACK(2, "black"),
      WHITE_SPLOTCHED(3, "white_splotched"),
      GOLD(4, "gold"),
      SALT(5, "salt"),
      EVIL(99, "evil");

      private static final IntFunction<Rabbit.Variant> BY_ID = ByIdMap.sparse(Rabbit.Variant::id, values(), BROWN);
      public static final Codec<Rabbit.Variant> CODEC = StringRepresentable.fromEnum(Rabbit.Variant::values);
      final int id;
      private final String name;

      private Variant(int pId, String pName) {
         this.id = pId;
         this.name = pName;
      }

      public String getSerializedName() {
         return this.name;
      }

      public int id() {
         return this.id;
      }

      public static Rabbit.Variant byId(int pId) {
         return BY_ID.apply(pId);
      }
   }
}
