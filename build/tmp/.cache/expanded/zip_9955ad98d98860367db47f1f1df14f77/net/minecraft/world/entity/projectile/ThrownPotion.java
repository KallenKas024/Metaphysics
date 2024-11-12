package net.minecraft.world.entity.projectile;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownPotion extends ThrowableItemProjectile implements ItemSupplier {
   public static final double SPLASH_RANGE = 4.0D;
   private static final double SPLASH_RANGE_SQ = 16.0D;
   public static final Predicate<LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = (p_287524_) -> {
      return p_287524_.isSensitiveToWater() || p_287524_.isOnFire();
   };

   public ThrownPotion(EntityType<? extends ThrownPotion> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ThrownPotion(Level pLevel, LivingEntity pShooter) {
      super(EntityType.POTION, pShooter, pLevel);
   }

   public ThrownPotion(Level pLevel, double pX, double pY, double pZ) {
      super(EntityType.POTION, pX, pY, pZ, pLevel);
   }

   protected Item getDefaultItem() {
      return Items.SPLASH_POTION;
   }

   /**
    * Gets the amount of gravity to apply to the thrown entity with each tick.
    */
   protected float getGravity() {
      return 0.05F;
   }

   protected void onHitBlock(BlockHitResult pResult) {
      super.onHitBlock(pResult);
      if (!this.level().isClientSide) {
         ItemStack itemstack = this.getItem();
         Potion potion = PotionUtils.getPotion(itemstack);
         List<MobEffectInstance> list = PotionUtils.getMobEffects(itemstack);
         boolean flag = potion == Potions.WATER && list.isEmpty();
         Direction direction = pResult.getDirection();
         BlockPos blockpos = pResult.getBlockPos();
         BlockPos blockpos1 = blockpos.relative(direction);
         if (flag) {
            this.dowseFire(blockpos1);
            this.dowseFire(blockpos1.relative(direction.getOpposite()));

            for(Direction direction1 : Direction.Plane.HORIZONTAL) {
               this.dowseFire(blockpos1.relative(direction1));
            }
         }

      }
   }

   /**
    * Called when this EntityFireball hits a block or entity.
    */
   protected void onHit(HitResult pResult) {
      super.onHit(pResult);
      if (!this.level().isClientSide) {
         ItemStack itemstack = this.getItem();
         Potion potion = PotionUtils.getPotion(itemstack);
         List<MobEffectInstance> list = PotionUtils.getMobEffects(itemstack);
         boolean flag = potion == Potions.WATER && list.isEmpty();
         if (flag) {
            this.applyWater();
         } else if (!list.isEmpty()) {
            if (this.isLingering()) {
               this.makeAreaOfEffectCloud(itemstack, potion);
            } else {
               this.applySplash(list, pResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)pResult).getEntity() : null);
            }
         }

         int i = potion.hasInstantEffects() ? 2007 : 2002;
         this.level().levelEvent(i, this.blockPosition(), PotionUtils.getColor(itemstack));
         this.discard();
      }
   }

   private void applyWater() {
      AABB aabb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);

      for(LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, aabb, WATER_SENSITIVE_OR_ON_FIRE)) {
         double d0 = this.distanceToSqr(livingentity);
         if (d0 < 16.0D) {
            if (livingentity.isSensitiveToWater()) {
               livingentity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 1.0F);
            }

            if (livingentity.isOnFire() && livingentity.isAlive()) {
               livingentity.extinguishFire();
            }
         }
      }

      for(Axolotl axolotl : this.level().getEntitiesOfClass(Axolotl.class, aabb)) {
         axolotl.rehydrate();
      }

   }

   private void applySplash(List<MobEffectInstance> pEffectInstances, @Nullable Entity pTarget) {
      AABB aabb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
      List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, aabb);
      if (!list.isEmpty()) {
         Entity entity = this.getEffectSource();

         for(LivingEntity livingentity : list) {
            if (livingentity.isAffectedByPotions()) {
               double d0 = this.distanceToSqr(livingentity);
               if (d0 < 16.0D) {
                  double d1;
                  if (livingentity == pTarget) {
                     d1 = 1.0D;
                  } else {
                     d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                  }

                  for(MobEffectInstance mobeffectinstance : pEffectInstances) {
                     MobEffect mobeffect = mobeffectinstance.getEffect();
                     if (mobeffect.isInstantenous()) {
                        mobeffect.applyInstantenousEffect(this, this.getOwner(), livingentity, mobeffectinstance.getAmplifier(), d1);
                     } else {
                        int i = mobeffectinstance.mapDuration((p_267930_) -> {
                           return (int)(d1 * (double)p_267930_ + 0.5D);
                        });
                        MobEffectInstance mobeffectinstance1 = new MobEffectInstance(mobeffect, i, mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible());
                        if (!mobeffectinstance1.endsWithin(20)) {
                           livingentity.addEffect(mobeffectinstance1, entity);
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private void makeAreaOfEffectCloud(ItemStack pStack, Potion pPotion) {
      AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
      Entity entity = this.getOwner();
      if (entity instanceof LivingEntity) {
         areaeffectcloud.setOwner((LivingEntity)entity);
      }

      areaeffectcloud.setRadius(3.0F);
      areaeffectcloud.setRadiusOnUse(-0.5F);
      areaeffectcloud.setWaitTime(10);
      areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());
      areaeffectcloud.setPotion(pPotion);

      for(MobEffectInstance mobeffectinstance : PotionUtils.getCustomEffects(pStack)) {
         areaeffectcloud.addEffect(new MobEffectInstance(mobeffectinstance));
      }

      CompoundTag compoundtag = pStack.getTag();
      if (compoundtag != null && compoundtag.contains("CustomPotionColor", 99)) {
         areaeffectcloud.setFixedColor(compoundtag.getInt("CustomPotionColor"));
      }

      this.level().addFreshEntity(areaeffectcloud);
   }

   private boolean isLingering() {
      return this.getItem().is(Items.LINGERING_POTION);
   }

   private void dowseFire(BlockPos pPos) {
      BlockState blockstate = this.level().getBlockState(pPos);
      if (blockstate.is(BlockTags.FIRE)) {
         this.level().removeBlock(pPos, false);
      } else if (AbstractCandleBlock.isLit(blockstate)) {
         AbstractCandleBlock.extinguish((Player)null, blockstate, this.level(), pPos);
      } else if (CampfireBlock.isLitCampfire(blockstate)) {
         this.level().levelEvent((Player)null, 1009, pPos, 0);
         CampfireBlock.dowse(this.getOwner(), this.level(), pPos, blockstate);
         this.level().setBlockAndUpdate(pPos, blockstate.setValue(CampfireBlock.LIT, Boolean.valueOf(false)));
      }

   }
}