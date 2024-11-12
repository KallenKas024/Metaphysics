package net.minecraft.world.damagesource;

import net.minecraft.util.Mth;

public class CombatRules {
   public static final float MAX_ARMOR = 20.0F;
   public static final float ARMOR_PROTECTION_DIVIDER = 25.0F;
   public static final float BASE_ARMOR_TOUGHNESS = 2.0F;
   public static final float MIN_ARMOR_RATIO = 0.2F;
   private static final int NUM_ARMOR_ITEMS = 4;

   public static float getDamageAfterAbsorb(float pDamage, float pTotalArmor, float pToughnessAttribute) {
      float f = 2.0F + pToughnessAttribute / 4.0F;
      float f1 = Mth.clamp(pTotalArmor - pDamage / f, pTotalArmor * 0.2F, 20.0F);
      return pDamage * (1.0F - f1 / 25.0F);
   }

   public static float getDamageAfterMagicAbsorb(float pDamage, float pEnchantModifiers) {
      float f = Mth.clamp(pEnchantModifiers, 0.0F, 20.0F);
      return pDamage * (1.0F - f / 25.0F);
   }
}