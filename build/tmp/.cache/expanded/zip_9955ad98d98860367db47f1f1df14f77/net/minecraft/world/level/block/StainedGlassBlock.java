package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class StainedGlassBlock extends AbstractGlassBlock implements BeaconBeamBlock {
   private final DyeColor color;

   public StainedGlassBlock(DyeColor pDyeColor, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.color = pDyeColor;
   }

   public DyeColor getColor() {
      return this.color;
   }
}