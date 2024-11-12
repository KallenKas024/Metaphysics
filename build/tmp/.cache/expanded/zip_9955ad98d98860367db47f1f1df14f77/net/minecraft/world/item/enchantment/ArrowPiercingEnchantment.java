package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowPiercingEnchantment extends Enchantment {
   public ArrowPiercingEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.CROSSBOW, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 1 + (pEnchantmentLevel - 1) * 10;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return 50;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 4;
   }

   /**
    * Determines if the enchantment passed can be applied together with this enchantment.
    * @param pEnch The other enchantment to test compatibility with.
    */
   public boolean checkCompatibility(Enchantment pEnch) {
      return super.checkCompatibility(pEnch) && pEnch != Enchantments.MULTISHOT;
   }
}