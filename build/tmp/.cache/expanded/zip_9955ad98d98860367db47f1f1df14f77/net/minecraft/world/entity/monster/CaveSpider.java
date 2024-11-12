package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class CaveSpider extends Spider {
   public CaveSpider(EntityType<? extends CaveSpider> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public static AttributeSupplier.Builder createCaveSpider() {
      return Spider.createAttributes().add(Attributes.MAX_HEALTH, 12.0D);
   }

   public boolean doHurtTarget(Entity pEntity) {
      if (super.doHurtTarget(pEntity)) {
         if (pEntity instanceof LivingEntity) {
            int i = 0;
            if (this.level().getDifficulty() == Difficulty.NORMAL) {
               i = 7;
            } else if (this.level().getDifficulty() == Difficulty.HARD) {
               i = 15;
            }

            if (i > 0) {
               ((LivingEntity)pEntity).addEffect(new MobEffectInstance(MobEffects.POISON, i * 20, 0), this);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      return pSpawnData;
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return 0.45F;
   }
}