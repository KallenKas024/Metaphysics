package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class AirItem extends Item {
   private final Block block;

   public AirItem(Block pBlock, Item.Properties pProperties) {
      super(pProperties);
      this.block = pBlock;
   }

   /**
    * Returns the unlocalized name of this item.
    */
   public String getDescriptionId() {
      return this.block.getDescriptionId();
   }

   /**
    * Allows items to add custom lines of information to the mouseover description.
    */
   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      this.block.appendHoverText(pStack, pLevel, pTooltip, pFlag);
   }
}