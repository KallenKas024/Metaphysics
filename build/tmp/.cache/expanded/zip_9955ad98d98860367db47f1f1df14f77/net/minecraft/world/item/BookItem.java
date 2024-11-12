package net.minecraft.world.item;

public class BookItem extends Item {
   public BookItem(Item.Properties pProperties) {
      super(pProperties);
   }

   /**
    * Checks isDamagable and if it cannot be stacked
    */
   public boolean isEnchantable(ItemStack pStack) {
      return pStack.getCount() == 1;
   }

   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   public int getEnchantmentValue() {
      return 1;
   }
}