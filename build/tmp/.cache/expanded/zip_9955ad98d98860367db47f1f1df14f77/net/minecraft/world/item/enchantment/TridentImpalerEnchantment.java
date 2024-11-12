package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;

public class TridentImpalerEnchantment extends Enchantment {
   public TridentImpalerEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.TRIDENT, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 1 + (pEnchantmentLevel - 1) * 8;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 20;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 5;
   }

   /**
    * Calculates the additional damage that will be dealt by an item with this enchantment. This alternative to
    * calcModifierDamage is sensitive to the targets EnumCreatureAttribute.
    * @param pLevel The level of the enchantment being used.
    */
   public float getDamageBonus(int pLevel, MobType pCreatureType) {
      return pCreatureType == MobType.WATER ? (float)pLevel * 2.5F : 0.0F;
   }
}