package net.minecraft.world.item.enchantment;

import net.minecraft.util.random.WeightedEntry;

/**
 * Defines an immutable instance of an enchantment and its level.
 */
public class EnchantmentInstance extends WeightedEntry.IntrusiveBase {
   /** The enchantment being represented. */
   public final Enchantment enchantment;
   /** The level of the enchantment. */
   public final int level;

   public EnchantmentInstance(Enchantment pEnchantment, int pLevel) {
      super(pEnchantment.getRarity().getWeight());
      this.enchantment = pEnchantment;
      this.level = pLevel;
   }
}