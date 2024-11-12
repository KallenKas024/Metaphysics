package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class Stray extends AbstractSkeleton {
   public Stray(EntityType<? extends Stray> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public static boolean checkStraySpawnRules(EntityType<Stray> pStray, ServerLevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      BlockPos blockpos = pPos;

      do {
         blockpos = blockpos.above();
      } while(pLevel.getBlockState(blockpos).is(Blocks.POWDER_SNOW));

      return checkMonsterSpawnRules(pStray, pLevel, pSpawnType, pPos, pRandom) && (pSpawnType == MobSpawnType.SPAWNER || pLevel.canSeeSky(blockpos.below()));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.STRAY_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.STRAY_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.STRAY_DEATH;
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.STRAY_STEP;
   }

   /**
    * Fires an arrow
    */
   protected AbstractArrow getArrow(ItemStack pArrowStack, float pDistanceFactor) {
      AbstractArrow abstractarrow = super.getArrow(pArrowStack, pDistanceFactor);
      if (abstractarrow instanceof Arrow) {
         ((Arrow)abstractarrow).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600));
      }

      return abstractarrow;
   }
}