package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class FishingSpeedEnchantment extends Enchantment {
   protected FishingSpeedEnchantment(Enchantment.Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
      super(pRarity, pCategory, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 15 + (pEnchantmentLevel - 1) * 9;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return super.getMinCost(pEnchantmentLevel) + 50;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 3;
   }
}