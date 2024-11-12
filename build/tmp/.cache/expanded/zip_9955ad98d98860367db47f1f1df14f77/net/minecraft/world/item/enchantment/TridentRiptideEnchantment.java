package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentRiptideEnchantment extends Enchantment {
   public TridentRiptideEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.TRIDENT, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 10 + pEnchantmentLevel * 7;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return 50;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 3;
   }

   /**
    * Determines if the enchantment passed can be applied together with this enchantment.
    * @param pEnch The other enchantment to test compatibility with.
    */
   public boolean checkCompatibility(Enchantment pEnch) {
      return super.checkCompatibility(pEnch) && pEnch != Enchantments.LOYALTY && pEnch != Enchantments.CHANNELING;
   }
}