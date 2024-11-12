package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class SnowGolem extends AbstractGolem implements Shearable, RangedAttackMob, net.minecraftforge.common.IForgeShearable {
   private static final EntityDataAccessor<Byte> DATA_PUMPKIN_ID = SynchedEntityData.defineId(SnowGolem.class, EntityDataSerializers.BYTE);
   private static final byte PUMPKIN_FLAG = 16;
   private static final float EYE_HEIGHT = 1.7F;

   public SnowGolem(EntityType<? extends SnowGolem> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25D, 20, 10.0F));
      this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D, 1.0000001E-5F));
      this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, (p_29932_) -> {
         return p_29932_ instanceof Enemy;
      }));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0D).add(Attributes.MOVEMENT_SPEED, (double)0.2F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_PUMPKIN_ID, (byte)16);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("Pumpkin", this.hasPumpkin());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("Pumpkin")) {
         this.setPumpkin(pCompound.getBoolean("Pumpkin"));
      }

   }

   public boolean isSensitiveToWater() {
      return true;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide) {
         if (this.level().getBiome(this.blockPosition()).is(BiomeTags.SNOW_GOLEM_MELTS)) {
            this.hurt(this.damageSources().onFire(), 1.0F);
         }

         if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
            return;
         }

         BlockState blockstate = Blocks.SNOW.defaultBlockState();

         for(int i = 0; i < 4; ++i) {
            int j = Mth.floor(this.getX() + (double)((float)(i % 2 * 2 - 1) * 0.25F));
            int k = Mth.floor(this.getY());
            int l = Mth.floor(this.getZ() + (double)((float)(i / 2 % 2 * 2 - 1) * 0.25F));
            BlockPos blockpos = new BlockPos(j, k, l);
            if (this.level().isEmptyBlock(blockpos) && blockstate.canSurvive(this.level(), blockpos)) {
               this.level().setBlockAndUpdate(blockpos, blockstate);
               this.level().gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(this, blockstate));
            }
         }
      }

   }

   /**
    * Attack the specified entity using a ranged attack.
    */
   public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
      Snowball snowball = new Snowball(this.level(), this);
      double d0 = pTarget.getEyeY() - (double)1.1F;
      double d1 = pTarget.getX() - this.getX();
      double d2 = d0 - snowball.getY();
      double d3 = pTarget.getZ() - this.getZ();
      double d4 = Math.sqrt(d1 * d1 + d3 * d3) * (double)0.2F;
      snowball.shoot(d1, d2 + d4, d3, 1.6F, 12.0F);
      this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level().addFreshEntity(snowball);
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return 1.7F;
   }

   protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (false && itemstack.getItem() == Items.SHEARS && this.readyForShearing()) { //Forge: Moved to onSheared
         this.shear(SoundSource.PLAYERS);
         this.gameEvent(GameEvent.SHEAR, pPlayer);
         if (!this.level().isClientSide) {
            itemstack.hurtAndBreak(1, pPlayer, (p_29910_) -> {
               p_29910_.broadcastBreakEvent(pHand);
            });
         }

         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public void shear(SoundSource pCategory) {
      this.level().playSound((Player)null, this, SoundEvents.SNOW_GOLEM_SHEAR, pCategory, 1.0F, 1.0F);
      if (!this.level().isClientSide()) {
         this.setPumpkin(false);
         this.spawnAtLocation(new ItemStack(Items.CARVED_PUMPKIN), 1.7F);
      }

   }

   public boolean readyForShearing() {
      return this.isAlive() && this.hasPumpkin();
   }

   public boolean hasPumpkin() {
      return (this.entityData.get(DATA_PUMPKIN_ID) & 16) != 0;
   }

   public void setPumpkin(boolean pPumpkinEquipped) {
      byte b0 = this.entityData.get(DATA_PUMPKIN_ID);
      if (pPumpkinEquipped) {
         this.entityData.set(DATA_PUMPKIN_ID, (byte)(b0 | 16));
      } else {
         this.entityData.set(DATA_PUMPKIN_ID, (byte)(b0 & -17));
      }

   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.SNOW_GOLEM_AMBIENT;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.SNOW_GOLEM_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.SNOW_GOLEM_DEATH;
   }

   public Vec3 getLeashOffset() {
      return new Vec3(0.0D, (double)(0.75F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
   }

   @Override
   public boolean isShearable(@org.jetbrains.annotations.NotNull ItemStack item, Level world, BlockPos pos) {
      return readyForShearing();
   }

   @org.jetbrains.annotations.NotNull
   @Override
   public java.util.List<ItemStack> onSheared(@Nullable Player player, @org.jetbrains.annotations.NotNull ItemStack item, Level world, BlockPos pos, int fortune) {
      world.playSound(null, this, SoundEvents.SNOW_GOLEM_SHEAR, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 1.0F, 1.0F);
      this.gameEvent(GameEvent.SHEAR, player);
      if (!world.isClientSide()) {
         setPumpkin(false);
         return java.util.Collections.singletonList(new ItemStack(Items.CARVED_PUMPKIN));
      }
      return java.util.Collections.emptyList();
   }
}
