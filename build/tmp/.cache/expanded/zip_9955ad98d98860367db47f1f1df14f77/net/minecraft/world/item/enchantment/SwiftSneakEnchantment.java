package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class SwiftSneakEnchantment extends Enchantment {
   public SwiftSneakEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.ARMOR_LEGS, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pLevel) {
      return pLevel * 25;
   }

   public int getMaxCost(int pLevel) {
      return this.getMinCost(pLevel) + 50;
   }

   /**
    * Checks if the enchantment should be considered a treasure enchantment. These enchantments can not be obtained
    * using the enchantment table. The mending enchantment is an example of a treasure enchantment.
    * @return Whether the enchantment is a treasure enchantment.
    */
   public boolean isTreasureOnly() {
      return true;
   }

   /**
    * Checks if the enchantment can be traded by NPCs like villagers.
    * @return Whether this enchantment can be traded.
    */
   public boolean isTradeable() {
      return false;
   }

   /**
    * Checks if the enchantment can be discovered by game mechanics which pull random enchantments from the enchantment
    * registry.
    * @return Whether the enchantment can be discovered.
    */
   public boolean isDiscoverable() {
      return false;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 3;
   }
}