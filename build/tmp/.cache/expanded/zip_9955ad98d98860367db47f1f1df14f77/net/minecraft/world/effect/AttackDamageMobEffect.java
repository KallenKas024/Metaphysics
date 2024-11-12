package net.minecraft.world.effect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttackDamageMobEffect extends MobEffect {
   protected final double multiplier;

   protected AttackDamageMobEffect(MobEffectCategory pCategory, int pColor, double pMultiplier) {
      super(pCategory, pColor);
      this.multiplier = pMultiplier;
   }

   public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
      return this.multiplier * (double)(pAmplifier + 1);
   }
}