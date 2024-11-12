package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class AbsoptionMobEffect extends MobEffect {
   protected AbsoptionMobEffect(MobEffectCategory pCategory, int pColor) {
      super(pCategory, pColor);
   }

   public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
      pLivingEntity.setAbsorptionAmount(pLivingEntity.getAbsorptionAmount() - (float)(4 * (pAmplifier + 1)));
      super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
   }

   public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
      pLivingEntity.setAbsorptionAmount(pLivingEntity.getAbsorptionAmount() + (float)(4 * (pAmplifier + 1)));
      super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
   }
}