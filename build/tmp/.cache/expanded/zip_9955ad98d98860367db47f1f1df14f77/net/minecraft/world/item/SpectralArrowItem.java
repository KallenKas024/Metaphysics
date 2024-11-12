package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.level.Level;

public class SpectralArrowItem extends ArrowItem {
   public SpectralArrowItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public AbstractArrow createArrow(Level pLevel, ItemStack pStack, LivingEntity pShooter) {
      return new SpectralArrow(pLevel, pShooter);
   }
}