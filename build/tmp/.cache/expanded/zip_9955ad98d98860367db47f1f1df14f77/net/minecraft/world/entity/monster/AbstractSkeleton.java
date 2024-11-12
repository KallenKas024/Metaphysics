package net.minecraft.world.entity.monster;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractSkeleton extends Monster implements RangedAttackMob {
   private final RangedBowAttackGoal<AbstractSkeleton> bowGoal = new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F);
   private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal(this, 1.2D, false) {
      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         super.stop();
         AbstractSkeleton.this.setAggressive(false);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         AbstractSkeleton.this.setAggressive(true);
      }
   };

   protected AbstractSkeleton(EntityType<? extends AbstractSkeleton> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.reassessWeaponGoal();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new RestrictSunGoal(this));
      this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0D));
      this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Wolf.class, 6.0F, 1.0D, 1.2D));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25D);
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(this.getStepSound(), 0.15F, 1.0F);
   }

   protected abstract SoundEvent getStepSound();

   public MobType getMobType() {
      return MobType.UNDEAD;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      boolean flag = this.isSunBurnTick();
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

      super.aiStep();
   }

   /**
    * Handles updating while riding another entity
    */
   public void rideTick() {
      super.rideTick();
      Entity entity = this.getControlledVehicle();
      if (entity instanceof PathfinderMob pathfindermob) {
         this.yBodyRot = pathfindermob.yBodyRot;
      }

   }

   protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
      super.populateDefaultEquipmentSlots(pRandom, pDifficulty);
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      RandomSource randomsource = pLevel.getRandom();
      this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
      this.populateDefaultEquipmentEnchantments(randomsource, pDifficulty);
      this.reassessWeaponGoal();
      this.setCanPickUpLoot(randomsource.nextFloat() < 0.55F * pDifficulty.getSpecialMultiplier());
      if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
         LocalDate localdate = LocalDate.now();
         int i = localdate.get(ChronoField.DAY_OF_MONTH);
         int j = localdate.get(ChronoField.MONTH_OF_YEAR);
         if (j == 10 && i == 31 && randomsource.nextFloat() < 0.25F) {
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(randomsource.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
            this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
         }
      }

      return pSpawnData;
   }

   /**
    * Sets this entity's combat AI.
    */
   public void reassessWeaponGoal() {
      if (this.level() != null && !this.level().isClientSide) {
         this.goalSelector.removeGoal(this.meleeGoal);
         this.goalSelector.removeGoal(this.bowGoal);
         ItemStack itemstack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.BowItem));
         if (itemstack.is(Items.BOW)) {
            int i = 20;
            if (this.level().getDifficulty() != Difficulty.HARD) {
               i = 40;
            }

            this.bowGoal.setMinAttackInterval(i);
            this.goalSelector.addGoal(4, this.bowGoal);
         } else {
            this.goalSelector.addGoal(4, this.meleeGoal);
         }

      }
   }

   /**
    * Attack the specified entity using a ranged attack.
    */
   public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
      ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.BowItem)));
      AbstractArrow abstractarrow = this.getArrow(itemstack, pDistanceFactor);
      if (this.getMainHandItem().getItem() instanceof net.minecraft.world.item.BowItem)
         abstractarrow = ((net.minecraft.world.item.BowItem)this.getMainHandItem().getItem()).customArrow(abstractarrow);
      double d0 = pTarget.getX() - this.getX();
      double d1 = pTarget.getY(0.3333333333333333D) - abstractarrow.getY();
      double d2 = pTarget.getZ() - this.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      abstractarrow.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
      this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      this.level().addFreshEntity(abstractarrow);
   }

   /**
    * Fires an arrow
    */
   protected AbstractArrow getArrow(ItemStack pArrowStack, float pVelocity) {
      return ProjectileUtil.getMobArrow(this, pArrowStack, pVelocity);
   }

   public boolean canFireProjectileWeapon(ProjectileWeaponItem pProjectileWeapon) {
      return pProjectileWeapon == Items.BOW;
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.reassessWeaponGoal();
   }

   public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {
      super.setItemSlot(pSlot, pStack);
      if (!this.level().isClientSide) {
         this.reassessWeaponGoal();
      }

   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return 1.74F;
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return -0.6D;
   }

   public boolean isShaking() {
      return this.isFullyFrozen();
   }
}
