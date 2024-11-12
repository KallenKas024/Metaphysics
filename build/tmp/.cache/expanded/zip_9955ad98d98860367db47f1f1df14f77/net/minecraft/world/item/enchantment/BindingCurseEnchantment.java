package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BindingCurseEnchantment extends Enchantment {
   public BindingCurseEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.WEARABLE, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 25;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return 50;
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
    * Checks if the enchantment is considered a curse. These enchantments are treated as debuffs and can not be removed
    * from items under normal circumstances.
    * @return Whether the enchantment is a curse.
    */
   public boolean isCurse() {
      return true;
   }

   /**
    * Determines if this enchantment can be applied to a specific ItemStack.
    * @param pStack The ItemStack to test.
    */
   public boolean canEnchant(ItemStack pStack) {
      return !pStack.is(Items.SHIELD) && super.canEnchant(pStack);
   }
}