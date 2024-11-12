package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class TippedArrowItem extends ArrowItem {
   public TippedArrowItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public ItemStack getDefaultInstance() {
      return PotionUtils.setPotion(super.getDefaultInstance(), Potions.POISON);
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      PotionUtils.addPotionTooltip(pStack, pTooltip, 0.125F);
   }

   /**
    * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
    * different names based on their damage or NBT.
    */
   public String getDescriptionId(ItemStack pStack) {
      return PotionUtils.getPotion(pStack).getName(this.getDescriptionId() + ".effect.");
   }
}