package net.minecraft.world.entity.animal;

import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Wolf extends TamableAnimal implements NeutralMob {
   private static final EntityDataAccessor<Boolean> DATA_INTERESTED_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
   public static final Predicate<LivingEntity> PREY_SELECTOR = (p_289448_) -> {
      EntityType<?> entitytype = p_289448_.getType();
      return entitytype == EntityType.SHEEP || entitytype == EntityType.RABBIT || entitytype == EntityType.FOX;
   };
   private static final float START_HEALTH = 8.0F;
   private static final float TAME_HEALTH = 20.0F;
   private float interestedAngle;
   private float interestedAngleO;
   private boolean isWet;
   private boolean isShaking;
   private float shakeAnim;
   private float shakeAnimO;
   private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
   @Nullable
   private UUID persistentAngerTarget;

   public Wolf(EntityType<? extends Wolf> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setTame(false);
      this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(1, new Wolf.WolfPanicGoal(1.5D));
      this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
      this.goalSelector.addGoal(3, new Wolf.WolfAvoidEntityGoal<>(this, Llama.class, 24.0F, 1.5D, 1.5D));
      this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
      this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
      this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
      this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
      this.goalSelector.addGoal(9, new BegGoal(this, 8.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
      this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
      this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(this, Animal.class, false, PREY_SELECTOR));
      this.targetSelector.addGoal(6, new NonTameRandomTargetGoal<>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
      this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
      this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.ATTACK_DAMAGE, 2.0D);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_INTERESTED_ID, false);
      this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
      this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putByte("CollarColor", (byte)this.getCollarColor().getId());
      this.addPersistentAngerSaveData(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("CollarColor", 99)) {
         this.setCollarColor(DyeColor.byId(pCompound.getInt("CollarColor")));
      }

      this.readPersistentAngerSaveData(this.level(), pCompound);
   }

   protected SoundEvent getAmbientSound() {
      if (this.isAngry()) {
         return SoundEvents.WOLF_GROWL;
      } else if (this.random.nextInt(3) == 0) {
         return this.isTame() && this.getHealth() < 10.0F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
      } else {
         return SoundEvents.WOLF_AMBIENT;
      }
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.WOLF_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.WOLF_DEATH;
   }

   /**
    * Returns the volume for the sounds this mob makes.
    */
   protected float getSoundVolume() {
      return 0.4F;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide && this.isWet && !this.isShaking && !this.isPathFinding() && this.onGround()) {
         this.isShaking = true;
         this.shakeAnim = 0.0F;
         this.shakeAnimO = 0.0F;
         this.level().broadcastEntityEvent(this, (byte)8);
      }

      if (!this.level().isClientSide) {
         this.updatePersistentAnger((ServerLevel)this.level(), true);
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.isAlive()) {
         this.interestedAngleO = this.interestedAngle;
         if (this.isInterested()) {
            this.interestedAngle += (1.0F - this.interestedAngle) * 0.4F;
         } else {
            this.interestedAngle += (0.0F - this.interestedAngle) * 0.4F;
         }

         if (this.isInWaterRainOrBubble()) {
            this.isWet = true;
            if (this.isShaking && !this.level().isClientSide) {
               this.level().broadcastEntityEvent(this, (byte)56);
               this.cancelShake();
            }
         } else if ((this.isWet || this.isShaking) && this.isShaking) {
            if (this.shakeAnim == 0.0F) {
               this.playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
               this.gameEvent(GameEvent.ENTITY_SHAKE);
            }

            this.shakeAnimO = this.shakeAnim;
            this.shakeAnim += 0.05F;
            if (this.shakeAnimO >= 2.0F) {
               this.isWet = false;
               this.isShaking = false;
               this.shakeAnimO = 0.0F;
               this.shakeAnim = 0.0F;
            }

            if (this.shakeAnim > 0.4F) {
               float f = (float)this.getY();
               int i = (int)(Mth.sin((this.shakeAnim - 0.4F) * (float)Math.PI) * 7.0F);
               Vec3 vec3 = this.getDeltaMovement();

               for(int j = 0; j < i; ++j) {
                  float f1 = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                  float f2 = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                  this.level().addParticle(ParticleTypes.SPLASH, this.getX() + (double)f1, (double)(f + 0.8F), this.getZ() + (double)f2, vec3.x, vec3.y, vec3.z);
               }
            }
         }

      }
   }

   private void cancelShake() {
      this.isShaking = false;
      this.shakeAnim = 0.0F;
      this.shakeAnimO = 0.0F;
   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      this.isWet = false;
      this.isShaking = false;
      this.shakeAnimO = 0.0F;
      this.shakeAnim = 0.0F;
      super.die(pCause);
   }

   /**
    * True if the wolf is wet
    */
   public boolean isWet() {
      return this.isWet;
   }

   /**
    * Used when calculating the amount of shading to apply while the wolf is wet.
    */
   public float getWetShade(float pPartialTicks) {
      return Math.min(0.5F + Mth.lerp(pPartialTicks, this.shakeAnimO, this.shakeAnim) / 2.0F * 0.5F, 1.0F);
   }

   public float getBodyRollAngle(float pPartialTicks, float pOffset) {
      float f = (Mth.lerp(pPartialTicks, this.shakeAnimO, this.shakeAnim) + pOffset) / 1.8F;
      if (f < 0.0F) {
         f = 0.0F;
      } else if (f > 1.0F) {
         f = 1.0F;
      }

      return Mth.sin(f * (float)Math.PI) * Mth.sin(f * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
   }

   public float getHeadRollAngle(float pPartialTicks) {
      return Mth.lerp(pPartialTicks, this.interestedAngleO, this.interestedAngle) * 0.15F * (float)Math.PI;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return pSize.height * 0.8F;
   }

   /**
    * The speed it takes to move the entity's head rotation through the faceEntity method.
    */
   public int getMaxHeadXRot() {
      return this.isInSittingPose() ? 20 : super.getMaxHeadXRot();
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         Entity entity = pSource.getEntity();
         if (!this.level().isClientSide) {
            this.setOrderedToSit(false);
         }

         if (entity != null && !(entity instanceof Player) && !(entity instanceof AbstractArrow)) {
            pAmount = (pAmount + 1.0F) / 2.0F;
         }

         return super.hurt(pSource, pAmount);
      }
   }

   public boolean doHurtTarget(Entity pEntity) {
      boolean flag = pEntity.hurt(this.damageSources().mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
      if (flag) {
         this.doEnchantDamageEffects(this, pEntity);
      }

      return flag;
   }

   public void setTame(boolean pTamed) {
      super.setTame(pTamed);
      if (pTamed) {
         this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0D);
         this.setHealth(20.0F);
      } else {
         this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0D);
      }

      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      Item item = itemstack.getItem();
      if (this.level().isClientSide) {
         boolean flag = this.isOwnedBy(pPlayer) || this.isTame() || itemstack.is(Items.BONE) && !this.isTame() && !this.isAngry();
         return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
      } else if (this.isTame()) {
         if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
            this.heal((float)itemstack.getFoodProperties(this).getNutrition());
            if (!pPlayer.getAbilities().instabuild) {
               itemstack.shrink(1);
            }

            this.gameEvent(GameEvent.EAT, this);
            return InteractionResult.SUCCESS;
         } else {
            if (item instanceof DyeItem) {
               DyeItem dyeitem = (DyeItem)item;
               if (this.isOwnedBy(pPlayer)) {
                  DyeColor dyecolor = dyeitem.getDyeColor();
                  if (dyecolor != this.getCollarColor()) {
                     this.setCollarColor(dyecolor);
                     if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                     }

                     return InteractionResult.SUCCESS;
                  }

                  return super.mobInteract(pPlayer, pHand);
               }
            }

            InteractionResult interactionresult = super.mobInteract(pPlayer, pHand);
            if ((!interactionresult.consumesAction() || this.isBaby()) && this.isOwnedBy(pPlayer)) {
               this.setOrderedToSit(!this.isOrderedToSit());
               this.jumping = false;
               this.navigation.stop();
               this.setTarget((LivingEntity)null);
               return InteractionResult.SUCCESS;
            } else {
               return interactionresult;
            }
         }
      } else if (itemstack.is(Items.BONE) && !this.isAngry()) {
         if (!pPlayer.getAbilities().instabuild) {
            itemstack.shrink(1);
         }

         if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
            this.tame(pPlayer);
            this.navigation.stop();
            this.setTarget((LivingEntity)null);
            this.setOrderedToSit(true);
            this.level().broadcastEntityEvent(this, (byte)7);
         } else {
            this.level().broadcastEntityEvent(this, (byte)6);
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 8) {
         this.isShaking = true;
         this.shakeAnim = 0.0F;
         this.shakeAnimO = 0.0F;
      } else if (pId == 56) {
         this.cancelShake();
      } else {
         super.handleEntityEvent(pId);
      }

   }

   public float getTailAngle() {
      if (this.isAngry()) {
         return 1.5393804F;
      } else {
         return this.isTame() ? (0.55F - (this.getMaxHealth() - this.getHealth()) * 0.02F) * (float)Math.PI : ((float)Math.PI / 5F);
      }
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      Item item = pStack.getItem();
      return item.isEdible() && pStack.getFoodProperties(this).isMeat();
   }

   /**
    * Will return how many at most can spawn in a chunk at once.
    */
   public int getMaxSpawnClusterSize() {
      return 8;
   }

   public int getRemainingPersistentAngerTime() {
      return this.entityData.get(DATA_REMAINING_ANGER_TIME);
   }

   public void setRemainingPersistentAngerTime(int pTime) {
      this.entityData.set(DATA_REMAINING_ANGER_TIME, pTime);
   }

   public void startPersistentAngerTimer() {
      this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
   }

   @Nullable
   public UUID getPersistentAngerTarget() {
      return this.persistentAngerTarget;
   }

   public void setPersistentAngerTarget(@Nullable UUID pTarget) {
      this.persistentAngerTarget = pTarget;
   }

   public DyeColor getCollarColor() {
      return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
   }

   public void setCollarColor(DyeColor pCollarColor) {
      this.entityData.set(DATA_COLLAR_COLOR, pCollarColor.getId());
   }

   @Nullable
   public Wolf getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      Wolf wolf = EntityType.WOLF.create(pLevel);
      if (wolf != null) {
         UUID uuid = this.getOwnerUUID();
         if (uuid != null) {
            wolf.setOwnerUUID(uuid);
            wolf.setTame(true);
         }
      }

      return wolf;
   }

   public void setIsInterested(boolean pIsInterested) {
      this.entityData.set(DATA_INTERESTED_ID, pIsInterested);
   }

   /**
    * Returns {@code true} if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(Animal pOtherAnimal) {
      if (pOtherAnimal == this) {
         return false;
      } else if (!this.isTame()) {
         return false;
      } else if (!(pOtherAnimal instanceof Wolf)) {
         return false;
      } else {
         Wolf wolf = (Wolf)pOtherAnimal;
         if (!wolf.isTame()) {
            return false;
         } else if (wolf.isInSittingPose()) {
            return false;
         } else {
            return this.isInLove() && wolf.isInLove();
         }
      }
   }

   public boolean isInterested() {
      return this.entityData.get(DATA_INTERESTED_ID);
   }

   public boolean wantsToAttack(LivingEntity pTarget, LivingEntity pOwner) {
      if (!(pTarget instanceof Creeper) && !(pTarget instanceof Ghast)) {
         if (pTarget instanceof Wolf) {
            Wolf wolf = (Wolf)pTarget;
            return !wolf.isTame() || wolf.getOwner() != pOwner;
         } else if (pTarget instanceof Player && pOwner instanceof Player && !((Player)pOwner).canHarmPlayer((Player)pTarget)) {
            return false;
         } else if (pTarget instanceof AbstractHorse && ((AbstractHorse)pTarget).isTamed()) {
            return false;
         } else {
            return !(pTarget instanceof TamableAnimal) || !((TamableAnimal)pTarget).isTame();
         }
      } else {
         return false;
      }
   }

   public boolean canBeLeashed(Player pPlayer) {
      return !this.isAngry() && super.canBeLeashed(pPlayer);
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }

   public static boolean checkWolfSpawnRules(EntityType<Wolf> pWolf, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.WOLVES_SPAWNABLE_ON) && isBrightEnoughToSpawn(pLevel, pPos);
   }

   class WolfAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
      private final Wolf wolf;

      public WolfAvoidEntityGoal(Wolf pWolf, Class<T> pEntityClassToAvoid, float pMaxDist, double pWalkSpeedModifier, double pSprintSpeedModifier) {
         super(pWolf, pEntityClassToAvoid, pMaxDist, pWalkSpeedModifier, pSprintSpeedModifier);
         this.wolf = pWolf;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (super.canUse() && this.toAvoid instanceof Llama) {
            return !this.wolf.isTame() && this.avoidLlama((Llama)this.toAvoid);
         } else {
            return false;
         }
      }

      private boolean avoidLlama(Llama pLlama) {
         return pLlama.getStrength() >= Wolf.this.random.nextInt(5);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Wolf.this.setTarget((LivingEntity)null);
         super.start();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         Wolf.this.setTarget((LivingEntity)null);
         super.tick();
      }
   }

   class WolfPanicGoal extends PanicGoal {
      public WolfPanicGoal(double pSpeedModifier) {
         super(Wolf.this, pSpeedModifier);
      }

      protected boolean shouldPanic() {
         return this.mob.isFreezing() || this.mob.isOnFire();
      }
   }
}
