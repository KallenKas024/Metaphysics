package net.minecraft.world.entity.monster;

import com.google.common.collect.Maps;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class Vindicator extends AbstractIllager {
   private static final String TAG_JOHNNY = "Johnny";
   static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = (p_34082_) -> {
      return p_34082_ == Difficulty.NORMAL || p_34082_ == Difficulty.HARD;
   };
   boolean isJohnny;

   public Vindicator(EntityType<? extends Vindicator> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new Vindicator.VindicatorBreakDoorGoal(this));
      this.goalSelector.addGoal(2, new AbstractIllager.RaiderOpenDoorGoal(this));
      this.goalSelector.addGoal(3, new Raider.HoldGroundAttackGoal(this, 10.0F));
      this.goalSelector.addGoal(4, new Vindicator.VindicatorMeleeAttackGoal(this));
      this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers());
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
      this.targetSelector.addGoal(4, new Vindicator.VindicatorJohnnyAttackGoal(this));
      this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6D));
      this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
   }

   protected void customServerAiStep() {
      if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this)) {
         boolean flag = ((ServerLevel)this.level()).isRaided(this.blockPosition());
         ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(flag);
      }

      super.customServerAiStep();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.35F).add(Attributes.FOLLOW_RANGE, 12.0D).add(Attributes.MAX_HEALTH, 24.0D).add(Attributes.ATTACK_DAMAGE, 5.0D);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.isJohnny) {
         pCompound.putBoolean("Johnny", true);
      }

   }

   public AbstractIllager.IllagerArmPose getArmPose() {
      if (this.isAggressive()) {
         return AbstractIllager.IllagerArmPose.ATTACKING;
      } else {
         return this.isCelebrating() ? AbstractIllager.IllagerArmPose.CELEBRATING : AbstractIllager.IllagerArmPose.CROSSED;
      }
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("Johnny", 99)) {
         this.isJohnny = pCompound.getBoolean("Johnny");
      }

   }

   public SoundEvent getCelebrateSound() {
      return SoundEvents.VINDICATOR_CELEBRATE;
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      SpawnGroupData spawngroupdata = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
      RandomSource randomsource = pLevel.getRandom();
      this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
      this.populateDefaultEquipmentEnchantments(randomsource, pDifficulty);
      return spawngroupdata;
   }

   protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
      if (this.getCurrentRaid() == null) {
         this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
      }

   }

   /**
    * Returns whether this Entity is on the same team as the given Entity.
    */
   public boolean isAlliedTo(Entity pEntity) {
      if (super.isAlliedTo(pEntity)) {
         return true;
      } else if (pEntity instanceof LivingEntity && ((LivingEntity)pEntity).getMobType() == MobType.ILLAGER) {
         return this.getTeam() == null && pEntity.getTeam() == null;
      } else {
         return false;
      }
   }

   public void setCustomName(@Nullable Component pName) {
      super.setCustomName(pName);
      if (!this.isJohnny && pName != null && pName.getString().equals("Johnny")) {
         this.isJohnny = true;
      }

   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.VINDICATOR_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.VINDICATOR_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.VINDICATOR_HURT;
   }

   public void applyRaidBuffs(int pWave, boolean pUnusedFalse) {
      ItemStack itemstack = new ItemStack(Items.IRON_AXE);
      Raid raid = this.getCurrentRaid();
      int i = 1;
      if (pWave > raid.getNumGroups(Difficulty.NORMAL)) {
         i = 2;
      }

      boolean flag = this.random.nextFloat() <= raid.getEnchantOdds();
      if (flag) {
         Map<Enchantment, Integer> map = Maps.newHashMap();
         map.put(Enchantments.SHARPNESS, i);
         EnchantmentHelper.setEnchantments(map, itemstack);
      }

      this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
   }

   static class VindicatorBreakDoorGoal extends BreakDoorGoal {
      public VindicatorBreakDoorGoal(Mob p_34112_) {
         super(p_34112_, 6, Vindicator.DOOR_BREAKING_PREDICATE);
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         Vindicator vindicator = (Vindicator)this.mob;
         return vindicator.hasActiveRaid() && super.canContinueToUse();
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         Vindicator vindicator = (Vindicator)this.mob;
         return vindicator.hasActiveRaid() && vindicator.random.nextInt(reducedTickDelay(10)) == 0 && super.canUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         this.mob.setNoActionTime(0);
      }
   }

   static class VindicatorJohnnyAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
      public VindicatorJohnnyAttackGoal(Vindicator pVindicator) {
         super(pVindicator, LivingEntity.class, 0, true, true, LivingEntity::attackable);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return ((Vindicator)this.mob).isJohnny && super.canUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         this.mob.setNoActionTime(0);
      }
   }

   class VindicatorMeleeAttackGoal extends MeleeAttackGoal {
      public VindicatorMeleeAttackGoal(Vindicator pVindicator) {
         super(pVindicator, 1.0D, false);
      }

      protected double getAttackReachSqr(LivingEntity pAttackTarget) {
         if (this.mob.getVehicle() instanceof Ravager) {
            float f = this.mob.getVehicle().getBbWidth() - 0.1F;
            return (double)(f * 2.0F * f * 2.0F + pAttackTarget.getBbWidth());
         } else {
            return super.getAttackReachSqr(pAttackTarget);
         }
      }
   }
}