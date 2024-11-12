package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;

public interface EntityBlock {
   @Nullable
   BlockEntity newBlockEntity(BlockPos pPos, BlockState pState);

   @Nullable
   default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return null;
   }

   @Nullable
   default <T extends BlockEntity> GameEventListener getListener(ServerLevel pLevel, T pBlockEntity) {
      if (pBlockEntity instanceof GameEventListener.Holder<?> holder) {
         return holder.getListener();
      } else {
         return null;
      }
   }
}