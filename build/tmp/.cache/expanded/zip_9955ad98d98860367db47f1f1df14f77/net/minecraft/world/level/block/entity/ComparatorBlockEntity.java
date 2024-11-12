package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class ComparatorBlockEntity extends BlockEntity {
   private int output;

   public ComparatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.COMPARATOR, pPos, pBlockState);
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      pTag.putInt("OutputSignal", this.output);
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.output = pTag.getInt("OutputSignal");
   }

   public int getOutputSignal() {
      return this.output;
   }

   public void setOutputSignal(int pOutput) {
      this.output = pOutput;
   }
}