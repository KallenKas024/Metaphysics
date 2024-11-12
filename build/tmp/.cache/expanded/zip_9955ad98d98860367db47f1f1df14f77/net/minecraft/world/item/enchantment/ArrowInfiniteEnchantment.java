package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowInfiniteEnchantment extends Enchantment {
   public ArrowInfiniteEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.BOW, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 20;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return 50;
   }

   /**
    * Determines if the enchantment passed can be applied together with this enchantment.
    * @param pEnch The other enchantment to test compatibility with.
    */
   public boolean checkCompatibility(Enchantment pEnch) {
      return pEnch instanceof MendingEnchantment ? false : super.checkCompatibility(pEnch);
   }
}