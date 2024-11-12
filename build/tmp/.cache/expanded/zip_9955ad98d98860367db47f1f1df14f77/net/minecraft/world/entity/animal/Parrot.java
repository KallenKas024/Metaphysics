package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Parrot extends ShoulderRidingEntity implements VariantHolder<Parrot.Variant>, FlyingAnimal {
   private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Parrot.class, EntityDataSerializers.INT);
   private static final Predicate<Mob> NOT_PARROT_PREDICATE = new Predicate<Mob>() {
      public boolean test(@Nullable Mob p_29453_) {
         return p_29453_ != null && Parrot.MOB_SOUND_MAP.containsKey(p_29453_.getType());
      }
   };
   private static final Item POISONOUS_FOOD = Items.COOKIE;
   private static final Set<Item> TAME_FOOD = Sets.newHashSet(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);
   static final Map<EntityType<?>, SoundEvent> MOB_SOUND_MAP = Util.make(Maps.newHashMap(), (p_29398_) -> {
      p_29398_.put(EntityType.BLAZE, SoundEvents.PARROT_IMITATE_BLAZE);
      p_29398_.put(EntityType.CAVE_SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
      p_29398_.put(EntityType.CREEPER, SoundEvents.PARROT_IMITATE_CREEPER);
      p_29398_.put(EntityType.DROWNED, SoundEvents.PARROT_IMITATE_DROWNED);
      p_29398_.put(EntityType.ELDER_GUARDIAN, SoundEvents.PARROT_IMITATE_ELDER_GUARDIAN);
      p_29398_.put(EntityType.ENDER_DRAGON, SoundEvents.PARROT_IMITATE_ENDER_DRAGON);
      p_29398_.put(EntityType.ENDERMITE, SoundEvents.PARROT_IMITATE_ENDERMITE);
      p_29398_.put(EntityType.EVOKER, SoundEvents.PARROT_IMITATE_EVOKER);
      p_29398_.put(EntityType.GHAST, SoundEvents.PARROT_IMITATE_GHAST);
      p_29398_.put(EntityType.GUARDIAN, SoundEvents.PARROT_IMITATE_GUARDIAN);
      p_29398_.put(EntityType.HOGLIN, SoundEvents.PARROT_IMITATE_HOGLIN);
      p_29398_.put(EntityType.HUSK, SoundEvents.PARROT_IMITATE_HUSK);
      p_29398_.put(EntityType.ILLUSIONER, SoundEvents.PARROT_IMITATE_ILLUSIONER);
      p_29398_.put(EntityType.MAGMA_CUBE, SoundEvents.PARROT_IMITATE_MAGMA_CUBE);
      p_29398_.put(EntityType.PHANTOM, SoundEvents.PARROT_IMITATE_PHANTOM);
      p_29398_.put(EntityType.PIGLIN, SoundEvents.PARROT_IMITATE_PIGLIN);
      p_29398_.put(EntityType.PIGLIN_BRUTE, SoundEvents.PARROT_IMITATE_PIGLIN_BRUTE);
      p_29398_.put(EntityType.PILLAGER, SoundEvents.PARROT_IMITATE_PILLAGER);
      p_29398_.put(EntityType.RAVAGER, SoundEvents.PARROT_IMITATE_RAVAGER);
      p_29398_.put(EntityType.SHULKER, SoundEvents.PARROT_IMITATE_SHULKER);
      p_29398_.put(EntityType.SILVERFISH, SoundEvents.PARROT_IMITATE_SILVERFISH);
      p_29398_.put(EntityType.SKELETON, SoundEvents.PARROT_IMITATE_SKELETON);
      p_29398_.put(EntityType.SLIME, SoundEvents.PARROT_IMITATE_SLIME);
      p_29398_.put(EntityType.SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
      p_29398_.put(EntityType.STRAY, SoundEvents.PARROT_IMITATE_STRAY);
      p_29398_.put(EntityType.VEX, SoundEvents.PARROT_IMITATE_VEX);
      p_29398_.put(EntityType.VINDICATOR, SoundEvents.PARROT_IMITATE_VINDICATOR);
      p_29398_.put(EntityType.WARDEN, SoundEvents.PARROT_IMITATE_WARDEN);
      p_29398_.put(EntityType.WITCH, SoundEvents.PARROT_IMITATE_WITCH);
      p_29398_.put(EntityType.WITHER, SoundEvents.PARROT_IMITATE_WITHER);
      p_29398_.put(EntityType.WITHER_SKELETON, SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
      p_29398_.put(EntityType.ZOGLIN, SoundEvents.PARROT_IMITATE_ZOGLIN);
      p_29398_.put(EntityType.ZOMBIE, SoundEvents.PARROT_IMITATE_ZOMBIE);
      p_29398_.put(EntityType.ZOMBIE_VILLAGER, SoundEvents.PARROT_IMITATE_ZOMBIE_VILLAGER);
   });
   public float flap;
   public float flapSpeed;
   public float oFlapSpeed;
   public float oFlap;
   private float flapping = 1.0F;
   private float nextFlap = 1.0F;
   private boolean partyParrot;
   @Nullable
   private BlockPos jukebox;

   public Parrot(EntityType<? extends Parrot> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.moveControl = new FlyingMoveControl(this, 10, false);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      this.setVariant(Util.getRandom(Parrot.Variant.values(), pLevel.getRandom()));
      if (pSpawnData == null) {
         pSpawnData = new AgeableMob.AgeableMobGroupData(false);
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return false;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new PanicGoal(this, 1.25D));
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
      this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0D, 5.0F, 1.0F, true));
      this.goalSelector.addGoal(2, new Parrot.ParrotWanderGoal(this, 1.0D));
      this.goalSelector.addGoal(3, new LandOnOwnersShoulderGoal(this));
      this.goalSelector.addGoal(3, new FollowMobGoal(this, 1.0D, 3.0F, 7.0F));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0D).add(Attributes.FLYING_SPEED, (double)0.4F).add(Attributes.MOVEMENT_SPEED, (double)0.2F);
   }

   protected PathNavigation createNavigation(Level pLevel) {
      FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
      flyingpathnavigation.setCanOpenDoors(false);
      flyingpathnavigation.setCanFloat(true);
      flyingpathnavigation.setCanPassDoors(true);
      return flyingpathnavigation;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return pSize.height * 0.6F;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.jukebox == null || !this.jukebox.closerToCenterThan(this.position(), 3.46D) || !this.level().getBlockState(this.jukebox).is(Blocks.JUKEBOX)) {
         this.partyParrot = false;
         this.jukebox = null;
      }

      if (this.level().random.nextInt(400) == 0) {
         imitateNearbyMobs(this.level(), this);
      }

      super.aiStep();
      this.calculateFlapping();
   }

   /**
    * Called when a record starts or stops playing. Used to make parrots start or stop partying.
    */
   public void setRecordPlayingNearby(BlockPos pPos, boolean pIsPartying) {
      this.jukebox = pPos;
      this.partyParrot = pIsPartying;
   }

   public boolean isPartyParrot() {
      return this.partyParrot;
   }

   private void calculateFlapping() {
      this.oFlap = this.flap;
      this.oFlapSpeed = this.flapSpeed;
      this.flapSpeed += (float)(!this.onGround() && !this.isPassenger() ? 4 : -1) * 0.3F;
      this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
      if (!this.onGround() && this.flapping < 1.0F) {
         this.flapping = 1.0F;
      }

      this.flapping *= 0.9F;
      Vec3 vec3 = this.getDeltaMovement();
      if (!this.onGround() && vec3.y < 0.0D) {
         this.setDeltaMovement(vec3.multiply(1.0D, 0.6D, 1.0D));
      }

      this.flap += this.flapping * 2.0F;
   }

   public static boolean imitateNearbyMobs(Level pLevel, Entity pParrot) {
      if (pParrot.isAlive() && !pParrot.isSilent() && pLevel.random.nextInt(2) == 0) {
         List<Mob> list = pLevel.getEntitiesOfClass(Mob.class, pParrot.getBoundingBox().inflate(20.0D), NOT_PARROT_PREDICATE);
         if (!list.isEmpty()) {
            Mob mob = list.get(pLevel.random.nextInt(list.size()));
            if (!mob.isSilent()) {
               SoundEvent soundevent = getImitatedSound(mob.getType());
               pLevel.playSound((Player)null, pParrot.getX(), pParrot.getY(), pParrot.getZ(), soundevent, pParrot.getSoundSource(), 0.7F, getPitch(pLevel.random));
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!this.isTame() && TAME_FOOD.contains(itemstack.getItem())) {
         if (!pPlayer.getAbilities().instabuild) {
            itemstack.shrink(1);
         }

         if (!this.isSilent()) {
            this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.PARROT_EAT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         }

         if (!this.level().isClientSide) {
            if (this.random.nextInt(10) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
               this.tame(pPlayer);
               this.level().broadcastEntityEvent(this, (byte)7);
            } else {
               this.level().broadcastEntityEvent(this, (byte)6);
            }
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else if (itemstack.is(POISONOUS_FOOD)) {
         if (!pPlayer.getAbilities().instabuild) {
            itemstack.shrink(1);
         }

         this.addEffect(new MobEffectInstance(MobEffects.POISON, 900));
         if (pPlayer.isCreative() || !this.isInvulnerable()) {
            this.hurt(this.damageSources().playerAttack(pPlayer), Float.MAX_VALUE);
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else if (!this.isFlying() && this.isTame() && this.isOwnedBy(pPlayer)) {
         if (!this.level().isClientSide) {
            this.setOrderedToSit(!this.isOrderedToSit());
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return false;
   }

   public static boolean checkParrotSpawnRules(EntityType<Parrot> pParrot, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.PARROTS_SPAWNABLE_ON) && isBrightEnoughToSpawn(pLevel, pPos);
   }

   protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
   }

   /**
    * Returns {@code true} if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(Animal pOtherAnimal) {
      return false;
   }

   @Nullable
   public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      return null;
   }

   public boolean doHurtTarget(Entity pEntity) {
      return pEntity.hurt(this.damageSources().mobAttack(this), 3.0F);
   }

   @Nullable
   public SoundEvent getAmbientSound() {
      return getAmbient(this.level(), this.level().random);
   }

   public static SoundEvent getAmbient(Level pLevel, RandomSource pRandom) {
      if (pLevel.getDifficulty() != Difficulty.PEACEFUL && pRandom.nextInt(1000) == 0) {
         List<EntityType<?>> list = Lists.newArrayList(MOB_SOUND_MAP.keySet());
         return getImitatedSound(list.get(pRandom.nextInt(list.size())));
      } else {
         return SoundEvents.PARROT_AMBIENT;
      }
   }

   private static SoundEvent getImitatedSound(EntityType<?> pType) {
      return MOB_SOUND_MAP.getOrDefault(pType, SoundEvents.PARROT_AMBIENT);
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.PARROT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PARROT_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.PARROT_STEP, 0.15F, 1.0F);
   }

   protected boolean isFlapping() {
      return this.flyDist > this.nextFlap;
   }

   protected void onFlap() {
      this.playSound(SoundEvents.PARROT_FLY, 0.15F, 1.0F);
      this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
   }

   /**
    * Gets the pitch of living sounds in living entities.
    */
   public float getVoicePitch() {
      return getPitch(this.random);
   }

   public static float getPitch(RandomSource pRandom) {
      return (pRandom.nextFloat() - pRandom.nextFloat()) * 0.2F + 1.0F;
   }

   public SoundSource getSoundSource() {
      return SoundSource.NEUTRAL;
   }

   /**
    * Returns {@code true} if this entity should push and be pushed by other entities when colliding.
    */
   public boolean isPushable() {
      return true;
   }

   protected void doPush(Entity pEntity) {
      if (!(pEntity instanceof Player)) {
         super.doPush(pEntity);
      }
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         if (!this.level().isClientSide) {
            this.setOrderedToSit(false);
         }

         return super.hurt(pSource, pAmount);
      }
   }

   public Parrot.Variant getVariant() {
      return Parrot.Variant.byId(this.entityData.get(DATA_VARIANT_ID));
   }

   public void setVariant(Parrot.Variant pVariant) {
      this.entityData.set(DATA_VARIANT_ID, pVariant.id);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_VARIANT_ID, 0);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Variant", this.getVariant().id);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setVariant(Parrot.Variant.byId(pCompound.getInt("Variant")));
   }

   public boolean isFlying() {
      return !this.onGround();
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }

   static class ParrotWanderGoal extends WaterAvoidingRandomFlyingGoal {
      public ParrotWanderGoal(PathfinderMob p_186224_, double p_186225_) {
         super(p_186224_, p_186225_);
      }

      @Nullable
      protected Vec3 getPosition() {
         Vec3 vec3 = null;
         if (this.mob.isInWater()) {
            vec3 = LandRandomPos.getPos(this.mob, 15, 15);
         }

         if (this.mob.getRandom().nextFloat() >= this.probability) {
            vec3 = this.getTreePos();
         }

         return vec3 == null ? super.getPosition() : vec3;
      }

      @Nullable
      private Vec3 getTreePos() {
         BlockPos blockpos = this.mob.blockPosition();
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
         BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();

         for(BlockPos blockpos1 : BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 3.0D), Mth.floor(this.mob.getY() - 6.0D), Mth.floor(this.mob.getZ() - 3.0D), Mth.floor(this.mob.getX() + 3.0D), Mth.floor(this.mob.getY() + 6.0D), Mth.floor(this.mob.getZ() + 3.0D))) {
            if (!blockpos.equals(blockpos1)) {
               BlockState blockstate = this.mob.level().getBlockState(blockpos$mutableblockpos1.setWithOffset(blockpos1, Direction.DOWN));
               boolean flag = blockstate.getBlock() instanceof LeavesBlock || blockstate.is(BlockTags.LOGS);
               if (flag && this.mob.level().isEmptyBlock(blockpos1) && this.mob.level().isEmptyBlock(blockpos$mutableblockpos.setWithOffset(blockpos1, Direction.UP))) {
                  return Vec3.atBottomCenterOf(blockpos1);
               }
            }
         }

         return null;
      }
   }

   public static enum Variant implements StringRepresentable {
      RED_BLUE(0, "red_blue"),
      BLUE(1, "blue"),
      GREEN(2, "green"),
      YELLOW_BLUE(3, "yellow_blue"),
      GRAY(4, "gray");

      public static final Codec<Parrot.Variant> CODEC = StringRepresentable.fromEnum(Parrot.Variant::values);
      private static final IntFunction<Parrot.Variant> BY_ID = ByIdMap.continuous(Parrot.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
      final int id;
      private final String name;

      private Variant(int pId, String pName) {
         this.id = pId;
         this.name = pName;
      }

      public int getId() {
         return this.id;
      }

      public static Parrot.Variant byId(int pId) {
         return BY_ID.apply(pId);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
