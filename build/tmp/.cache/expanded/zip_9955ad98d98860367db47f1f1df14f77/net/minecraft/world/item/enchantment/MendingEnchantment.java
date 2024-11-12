package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class MendingEnchantment extends Enchantment {
   public MendingEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.BREAKABLE, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return pEnchantmentLevel * 25;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 50;
   }

   /**
    * Checks if the enchantment should be considered a treasure enchantment. These enchantments can not be obtained
    * using the enchantment table. The mending enchantment is an example of a treasure enchantment.
    * @return Whether the enchantment is a treasure enchantment.
    */
   public boolean isTreasureOnly() {
      return true;
   }
}