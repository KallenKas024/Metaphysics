package net.minecraft.world.item;

/**
 * Constructor for the DyeableHorseArmor
 */
public class DyeableHorseArmorItem extends HorseArmorItem implements DyeableLeatherItem {
   /**
    * 
    * @param pProtection the given protection level of the {@code HorseArmorItem}
    * @param pIdentifier the texture path identifier for the {@code DyeableHorseArmorItem}, {@link
    * net.minecraft.world.item.HorseArmorItem}
    * @param pProperties the item properties
    */
   public DyeableHorseArmorItem(int pProtection, String pIdentifier, Item.Properties pProperties) {
      super(pProtection, pIdentifier, pProperties);
   }
   /**
    * 
    * @param pProtection the given protection level of the {@code HorseArmorItem}
    * @param pIdentifier the texture path identifier for the {@code DyeableHorseArmorItem}, {@link
    * net.minecraft.world.item.HorseArmorItem}
    * @param pProperties the item properties
    */
   public DyeableHorseArmorItem(int pProtection, net.minecraft.resources.ResourceLocation pIdentifier, Item.Properties pProperties) {
      super(pProtection, pIdentifier, pProperties);
   }
}
