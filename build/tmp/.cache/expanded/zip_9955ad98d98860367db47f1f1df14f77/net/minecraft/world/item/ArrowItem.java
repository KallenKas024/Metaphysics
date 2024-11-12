package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class ArrowItem extends Item {
   public ArrowItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public AbstractArrow createArrow(Level pLevel, ItemStack pStack, LivingEntity pShooter) {
      Arrow arrow = new Arrow(pLevel, pShooter);
      arrow.setEffectsFromItem(pStack);
      return arrow;
   }

   public boolean isInfinite(ItemStack stack, ItemStack bow, net.minecraft.world.entity.player.Player player) {
      int enchant = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.INFINITY_ARROWS, bow);
      return enchant <= 0 ? false : this.getClass() == ArrowItem.class;
   }
}
