package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowDamageEnchantment extends Enchantment {
   public ArrowDamageEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.BOW, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 1 + (pEnchantmentLevel - 1) * 10;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 15;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 5;
   }
}