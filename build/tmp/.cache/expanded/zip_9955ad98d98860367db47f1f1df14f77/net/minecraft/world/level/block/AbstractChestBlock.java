package net.minecraft.world.level.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractChestBlock<E extends BlockEntity> extends BaseEntityBlock {
   protected final Supplier<BlockEntityType<? extends E>> blockEntityType;

   protected AbstractChestBlock(BlockBehaviour.Properties pProperties, Supplier<BlockEntityType<? extends E>> pBlockEntityType) {
      super(pProperties);
      this.blockEntityType = pBlockEntityType;
   }

   public abstract DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState pState, Level pLevel, BlockPos pPos, boolean pOverride);
}