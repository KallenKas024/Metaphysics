package net.minecraft.world.item;

import net.minecraft.world.level.block.Block;

public class ItemNameBlockItem extends BlockItem {
   public ItemNameBlockItem(Block pBlock, Item.Properties pProperties) {
      super(pBlock, pProperties);
   }

   /**
    * Returns the unlocalized name of this item.
    */
   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }
}