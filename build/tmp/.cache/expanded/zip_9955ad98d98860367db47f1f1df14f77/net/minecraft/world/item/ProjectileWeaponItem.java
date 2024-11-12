package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public abstract class ProjectileWeaponItem extends Item {
   public static final Predicate<ItemStack> ARROW_ONLY = (p_43017_) -> {
      return p_43017_.is(ItemTags.ARROWS);
   };
   public static final Predicate<ItemStack> ARROW_OR_FIREWORK = ARROW_ONLY.or((p_43015_) -> {
      return p_43015_.is(Items.FIREWORK_ROCKET);
   });

   public ProjectileWeaponItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public Predicate<ItemStack> getSupportedHeldProjectiles() {
      return this.getAllSupportedProjectiles();
   }

   /**
    * Get the predicate to match ammunition when searching the player's inventory, not their main/offhand
    */
   public abstract Predicate<ItemStack> getAllSupportedProjectiles();

   public static ItemStack getHeldProjectile(LivingEntity pShooter, Predicate<ItemStack> pIsAmmo) {
      if (pIsAmmo.test(pShooter.getItemInHand(InteractionHand.OFF_HAND))) {
         return pShooter.getItemInHand(InteractionHand.OFF_HAND);
      } else {
         return pIsAmmo.test(pShooter.getItemInHand(InteractionHand.MAIN_HAND)) ? pShooter.getItemInHand(InteractionHand.MAIN_HAND) : ItemStack.EMPTY;
      }
   }

   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   public int getEnchantmentValue() {
      return 1;
   }

   public abstract int getDefaultProjectileRange();
}