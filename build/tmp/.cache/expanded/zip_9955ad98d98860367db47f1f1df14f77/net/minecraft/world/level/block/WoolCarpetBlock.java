package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoolCarpetBlock extends CarpetBlock {
   private final DyeColor color;

   public WoolCarpetBlock(DyeColor pColor, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.color = pColor;
   }

   public DyeColor getColor() {
      return this.color;
   }
}