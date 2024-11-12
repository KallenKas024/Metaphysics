package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class HealthBoostMobEffect extends MobEffect {
   public HealthBoostMobEffect(MobEffectCategory pCategory, int pColor) {
      super(pCategory, pColor);
   }

   public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
      super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
      if (pLivingEntity.getHealth() > pLivingEntity.getMaxHealth()) {
         pLivingEntity.setHealth(pLivingEntity.getMaxHealth());
      }

   }
}