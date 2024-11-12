package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BedBlockEntity extends BlockEntity {
   private DyeColor color;

   public BedBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.BED, pPos, pBlockState);
      this.color = ((BedBlock)pBlockState.getBlock()).getColor();
   }

   public BedBlockEntity(BlockPos pPos, BlockState pBlockState, DyeColor pColor) {
      super(BlockEntityType.BED, pPos, pBlockState);
      this.color = pColor;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public DyeColor getColor() {
      return this.color;
   }

   public void setColor(DyeColor pColor) {
      this.color = pColor;
   }
}