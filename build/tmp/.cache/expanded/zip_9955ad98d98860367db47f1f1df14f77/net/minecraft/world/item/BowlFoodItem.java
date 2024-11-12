package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BowlFoodItem extends Item {
   public BowlFoodItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
      ItemStack itemstack = super.finishUsingItem(pStack, pLevel, pEntityLiving);
      return pEntityLiving instanceof Player && ((Player)pEntityLiving).getAbilities().instabuild ? itemstack : new ItemStack(Items.BOWL);
   }
}