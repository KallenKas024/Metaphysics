package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class CalibratedSculkSensorBlockEntity extends SculkSensorBlockEntity {
   public CalibratedSculkSensorBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.CALIBRATED_SCULK_SENSOR, pPos, pBlockState);
   }

   public VibrationSystem.User createVibrationUser() {
      return new CalibratedSculkSensorBlockEntity.VibrationUser(this.getBlockPos());
   }

   protected class VibrationUser extends SculkSensorBlockEntity.VibrationUser {
      public VibrationUser(BlockPos pPos) {
         super(pPos);
      }

      public int getListenerRadius() {
         return 16;
      }

      public boolean canReceiveVibration(ServerLevel pLevel, BlockPos pPos, GameEvent pGameEvent, @Nullable GameEvent.Context pContext) {
         int i = this.getBackSignal(pLevel, this.blockPos, CalibratedSculkSensorBlockEntity.this.getBlockState());
         return i != 0 && VibrationSystem.getGameEventFrequency(pGameEvent) != i ? false : super.canReceiveVibration(pLevel, pPos, pGameEvent, pContext);
      }

      private int getBackSignal(Level pLevel, BlockPos pPos, BlockState pState) {
         Direction direction = pState.getValue(CalibratedSculkSensorBlock.FACING).getOpposite();
         return pLevel.getSignal(pPos.relative(direction), direction);
      }
   }
}