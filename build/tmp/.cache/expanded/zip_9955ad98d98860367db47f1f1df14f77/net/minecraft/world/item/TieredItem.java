package net.minecraft.world.item;

public class TieredItem extends Item {
   private final Tier tier;

   public TieredItem(Tier pTier, Item.Properties pProperties) {
      super(pProperties.defaultDurability(pTier.getUses()));
      this.tier = pTier;
   }

   public Tier getTier() {
      return this.tier;
   }

   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   public int getEnchantmentValue() {
      return this.tier.getEnchantmentValue();
   }

   /**
    * Return whether this item is repairable in an anvil.
    */
   public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
      return this.tier.getRepairIngredient().test(pRepair) || super.isValidRepairItem(pToRepair, pRepair);
   }
}