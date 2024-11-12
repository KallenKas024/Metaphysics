package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowKnockbackEnchantment extends Enchantment {
   public ArrowKnockbackEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.BOW, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 12 + (pEnchantmentLevel - 1) * 20;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 25;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 2;
   }
}