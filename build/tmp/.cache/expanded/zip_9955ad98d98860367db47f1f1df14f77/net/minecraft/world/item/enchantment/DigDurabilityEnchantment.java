package net.minecraft.world.item.enchantment;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class DigDurabilityEnchantment extends Enchantment {
   protected DigDurabilityEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.BREAKABLE, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 5 + (pEnchantmentLevel - 1) * 8;
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

   /**
    * Determines if this enchantment can be applied to a specific ItemStack.
    * @param pStack The ItemStack to test.
    */
   public boolean canEnchant(ItemStack pStack) {
      return pStack.isDamageableItem() ? true : super.canEnchant(pStack);
   }

   /**
    * Used by ItemStack.attemptDamageItem. Randomly determines if a point of damage should be negated using the
    * enchantment level (par1). If the ItemStack is Armor then there is a flat 60% chance for damage to be negated no
    * matter the enchantment level, otherwise there is a 1-(par/1) chance for damage to be negated.
    */
   public static boolean shouldIgnoreDurabilityDrop(ItemStack pStack, int pLevel, RandomSource pRandom) {
      if (pStack.getItem() instanceof ArmorItem && pRandom.nextFloat() < 0.6F) {
         return false;
      } else {
         return pRandom.nextInt(pLevel + 1) > 0;
      }
   }
}