package net.minecraft.world.item.enchantment;

import java.util.Map;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class ThornsEnchantment extends Enchantment {
   private static final float CHANCE_PER_LEVEL = 0.15F;

   public ThornsEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.ARMOR_CHEST, pApplicableSlots);
   }

   /**
    * Returns the minimal value of enchantability needed on the enchantment level passed.
    */
   public int getMinCost(int pEnchantmentLevel) {
      return 10 + 20 * (pEnchantmentLevel - 1);
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
      return pStack.getItem() instanceof ArmorItem ? true : super.canEnchant(pStack);
   }

   /**
    * Whenever an entity that has this enchantment on one of its associated items is damaged this method will be called.
    * @param pUser The target with the enchantment.
    * @param pAttacker The entity that attacked the target.
    * @param pLevel The level of the enchantment.
    */
   public void doPostHurt(LivingEntity pUser, Entity pAttacker, int pLevel) {
      RandomSource randomsource = pUser.getRandom();
      Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.THORNS, pUser);
      if (shouldHit(pLevel, randomsource)) {
         if (pAttacker != null) {
            pAttacker.hurt(pUser.damageSources().thorns(pUser), (float)getDamage(pLevel, randomsource));
         }

         if (entry != null) {
            entry.getValue().hurtAndBreak(2, pUser, (p_45208_) -> {
               p_45208_.broadcastBreakEvent(entry.getKey());
            });
         }
      }

   }

   public static boolean shouldHit(int pLevel, RandomSource pRandom) {
      if (pLevel <= 0) {
         return false;
      } else {
         return pRandom.nextFloat() < 0.15F * (float)pLevel;
      }
   }

   public static int getDamage(int pLevel, RandomSource pRandom) {
      return pLevel > 10 ? pLevel - 10 : 1 + pRandom.nextInt(4);
   }
}