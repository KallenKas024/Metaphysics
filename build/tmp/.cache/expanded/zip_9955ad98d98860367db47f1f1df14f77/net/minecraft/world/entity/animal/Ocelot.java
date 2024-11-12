package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Ocelot extends Animal {
   public static final double CROUCH_SPEED_MOD = 0.6D;
   public static final double WALK_SPEED_MOD = 0.8D;
   public static final double SPRINT_SPEED_MOD = 1.33D;
   private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(Items.COD, Items.SALMON);
   private static final EntityDataAccessor<Boolean> DATA_TRUSTING = SynchedEntityData.defineId(Ocelot.class, EntityDataSerializers.BOOLEAN);
   @Nullable
   private Ocelot.OcelotAvoidEntityGoal<Player> ocelotAvoidPlayersGoal;
   @Nullable
   private Ocelot.OcelotTemptGoal temptGoal;

   public Ocelot(EntityType<? extends Ocelot> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.reassessTrustingGoals();
   }

   boolean isTrusting() {
      return this.entityData.get(DATA_TRUSTING);
   }

   private void setTrusting(boolean pTrusting) {
      this.entityData.set(DATA_TRUSTING, pTrusting);
      this.reassessTrustingGoals();
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("Trusting", this.isTrusting());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setTrusting(pCompound.getBoolean("Trusting"));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_TRUSTING, false);
   }

   protected void registerGoals() {
      this.temptGoal = new Ocelot.OcelotTemptGoal(this, 0.6D, TEMPT_INGREDIENT, true);
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(3, this.temptGoal);
      this.goalSelector.addGoal(7, new LeapAtTargetGoal(this, 0.3F));
      this.goalSelector.addGoal(8, new OcelotAttackGoal(this));
      this.goalSelector.addGoal(9, new BreedGoal(this, 0.8D));
      this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 0.8D, 1.0000001E-5F));
      this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Chicken.class, false));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR));
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

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return !this.isTrusting() && this.tickCount > 2400;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.ATTACK_DAMAGE, 3.0D);
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.OCELOT_AMBIENT;
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 900;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.OCELOT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.OCELOT_DEATH;
   }

   private float getAttackDamage() {
      return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
   }

   public boolean doHurtTarget(Entity pEntity) {
      return pEntity.hurt(this.damageSources().mobAttack(this), this.getAttackDamage());
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if ((this.temptGoal == null || this.temptGoal.isRunning()) && !this.isTrusting() && this.isFood(itemstack) && pPlayer.distanceToSqr(this) < 9.0D) {
         this.usePlayerItem(pPlayer, pHand, itemstack);
         if (!this.level().isClientSide) {
            if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
               this.setTrusting(true);
               this.spawnTrustingParticles(true);
               this.level().broadcastEntityEvent(this, (byte)41);
            } else {
               this.spawnTrustingParticles(false);
               this.level().broadcastEntityEvent(this, (byte)40);
            }
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 41) {
         this.spawnTrustingParticles(true);
      } else if (pId == 40) {
         this.spawnTrustingParticles(false);
      } else {
         super.handleEntityEvent(pId);
      }

   }

   private void spawnTrustingParticles(boolean pIsTrusted) {
      ParticleOptions particleoptions = ParticleTypes.HEART;
      if (!pIsTrusted) {
         particleoptions = ParticleTypes.SMOKE;
      }

      for(int i = 0; i < 7; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level().addParticle(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   protected void reassessTrustingGoals() {
      if (this.ocelotAvoidPlayersGoal == null) {
         this.ocelotAvoidPlayersGoal = new Ocelot.OcelotAvoidEntityGoal<>(this, Player.class, 16.0F, 0.8D, 1.33D);
      }

      this.goalSelector.removeGoal(this.ocelotAvoidPlayersGoal);
      if (!this.isTrusting()) {
         this.goalSelector.addGoal(4, this.ocelotAvoidPlayersGoal);
      }

   }

   @Nullable
   public Ocelot getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
      return EntityType.OCELOT.create(pLevel);
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return TEMPT_INGREDIENT.test(pStack);
   }

   public static boolean checkOcelotSpawnRules(EntityType<Ocelot> pOcelot, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pRandom.nextInt(3) != 0;
   }

   public boolean checkSpawnObstruction(LevelReader pLevel) {
      if (pLevel.isUnobstructed(this) && !pLevel.containsAnyLiquid(this.getBoundingBox())) {
         BlockPos blockpos = this.blockPosition();
         if (blockpos.getY() < pLevel.getSeaLevel()) {
            return false;
         }

         BlockState blockstate = pLevel.getBlockState(blockpos.below());
         if (blockstate.is(Blocks.GRASS_BLOCK) || blockstate.is(BlockTags.LEAVES)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      if (pSpawnData == null) {
         pSpawnData = new AgeableMob.AgeableMobGroupData(1.0F);
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }

   public boolean isSteppingCarefully() {
      return this.isCrouching() || super.isSteppingCarefully();
   }

   static class OcelotAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
      private final Ocelot ocelot;

      public OcelotAvoidEntityGoal(Ocelot pOcelot, Class<T> pEntityClassToAvoid, float pMaxDist, double pWalkSpeedModifier, double pSprintSpeedModifier) {
         super(pOcelot, pEntityClassToAvoid, pMaxDist, pWalkSpeedModifier, pSprintSpeedModifier, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
         this.ocelot = pOcelot;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return !this.ocelot.isTrusting() && super.canUse();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return !this.ocelot.isTrusting() && super.canContinueToUse();
      }
   }

   static class OcelotTemptGoal extends TemptGoal {
      private final Ocelot ocelot;

      public OcelotTemptGoal(Ocelot pOcelot, double pSpeedModifier, Ingredient pItems, boolean pCanScare) {
         super(pOcelot, pSpeedModifier, pItems, pCanScare);
         this.ocelot = pOcelot;
      }

      protected boolean canScare() {
         return super.canScare() && !this.ocelot.isTrusting();
      }
   }
}
