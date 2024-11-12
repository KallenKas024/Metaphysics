package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class WitherSkeleton extends AbstractSkeleton {
   public WitherSkeleton(EntityType<? extends WitherSkeleton> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
   }

   protected void registerGoals() {
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractPiglin.class, true));
      super.registerGoals();
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.WITHER_SKELETON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.WITHER_SKELETON_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.WITHER_SKELETON_DEATH;
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.WITHER_SKELETON_STEP;
   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
      Entity entity = pSource.getEntity();
      if (entity instanceof Creeper creeper) {
         if (creeper.canDropMobsSkull()) {
            creeper.increaseDroppedSkulls();
            this.spawnAtLocation(Items.WITHER_SKELETON_SKULL);
         }
      }

   }

   protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
      this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
   }

   protected void populateDefaultEquipmentEnchantments(RandomSource pRandom, DifficultyInstance pDifficulty) {
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      SpawnGroupData spawngroupdata = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D);
      this.reassessWeaponGoal();
      return spawngroupdata;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return 2.1F;
   }

   public boolean doHurtTarget(Entity pEntity) {
      if (!super.doHurtTarget(pEntity)) {
         return false;
      } else {
         if (pEntity instanceof LivingEntity) {
            ((LivingEntity)pEntity).addEffect(new MobEffectInstance(MobEffects.WITHER, 200), this);
         }

         return true;
      }
   }

   /**
    * Fires an arrow
    */
   protected AbstractArrow getArrow(ItemStack pArrowStack, float pDistanceFactor) {
      AbstractArrow abstractarrow = super.getArrow(pArrowStack, pDistanceFactor);
      abstractarrow.setSecondsOnFire(100);
      return abstractarrow;
   }

   public boolean canBeAffected(MobEffectInstance pPotioneffect) {
      return pPotioneffect.getEffect() == MobEffects.WITHER ? false : super.canBeAffected(pPotioneffect);
   }
}