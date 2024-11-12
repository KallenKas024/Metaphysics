package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class TheEndPortalBlockEntity extends BlockEntity {
   protected TheEndPortalBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
      super(pType, pPos, pBlockState);
   }

   public TheEndPortalBlockEntity(BlockPos pPos, BlockState pBlockState) {
      this(BlockEntityType.END_PORTAL, pPos, pBlockState);
   }

   public boolean shouldRenderFace(Direction pFace) {
      return pFace.getAxis() == Direction.Axis.Y;
   }
}