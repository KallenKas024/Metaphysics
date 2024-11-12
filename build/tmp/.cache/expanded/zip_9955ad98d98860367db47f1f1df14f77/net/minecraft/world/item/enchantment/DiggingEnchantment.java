package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DiggingEnchantment extends Enchantment {
   protected DiggingEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.DIGGER, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 1 + 10 * (pEnchantmentLevel - 1);
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return super.getMinCost(pEnchantmentLevel) + 50;
   }

   /**
    * Returns the maximum level that the enchantment can have.
    */
   public int getMaxLevel() {
      return 5;
   }

   /**
    * Determines if this enchantment can be applied to a specific ItemStack.
    * @param pStack The ItemStack to test.
    */
   public boolean canEnchant(ItemStack pStack) {
      return pStack.getItem() instanceof net.minecraft.world.item.ShearsItem ? true : super.canEnchant(pStack);
   }
}
