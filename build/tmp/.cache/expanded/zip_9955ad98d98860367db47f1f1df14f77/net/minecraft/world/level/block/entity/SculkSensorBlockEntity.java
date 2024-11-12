package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.slf4j.Logger;

public class SculkSensorBlockEntity extends BlockEntity implements GameEventListener.Holder<VibrationSystem.Listener>, VibrationSystem {
   private static final Logger LOGGER = LogUtils.getLogger();
   private VibrationSystem.Data vibrationData;
   private final VibrationSystem.Listener vibrationListener;
   private final VibrationSystem.User vibrationUser = this.createVibrationUser();
   private int lastVibrationFrequency;

   protected SculkSensorBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
      super(pType, pPos, pBlockState);
      this.vibrationData = new VibrationSystem.Data();
      this.vibrationListener = new VibrationSystem.Listener(this);
   }

   public SculkSensorBlockEntity(BlockPos pPos, BlockState pBlockState) {
      this(BlockEntityType.SCULK_SENSOR, pPos, pBlockState);
   }

   public VibrationSystem.User createVibrationUser() {
      return new SculkSensorBlockEntity.VibrationUser(this.getBlockPos());
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.lastVibrationFrequency = pTag.getInt("last_vibration_frequency");
      if (pTag.contains("listener", 10)) {
         VibrationSystem.Data.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, pTag.getCompound("listener"))).resultOrPartial(LOGGER::error).ifPresent((p_281146_) -> {
            this.vibrationData = p_281146_;
         });
      }

   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      pTag.putInt("last_vibration_frequency", this.lastVibrationFrequency);
      VibrationSystem.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationData).resultOrPartial(LOGGER::error).ifPresent((p_222820_) -> {
         pTag.put("listener", p_222820_);
      });
   }

   public VibrationSystem.Data getVibrationData() {
      return this.vibrationData;
   }

   public VibrationSystem.User getVibrationUser() {
      return this.vibrationUser;
   }

   public int getLastVibrationFrequency() {
      return this.lastVibrationFrequency;
   }

   public void setLastVibrationFrequency(int pLastVibrationFrequency) {
      this.lastVibrationFrequency = pLastVibrationFrequency;
   }

   public VibrationSystem.Listener getListener() {
      return this.vibrationListener;
   }

   protected class VibrationUser implements VibrationSystem.User {
      public static final int LISTENER_RANGE = 8;
      protected final BlockPos blockPos;
      private final PositionSource positionSource;

      public VibrationUser(BlockPos pPos) {
         this.blockPos = pPos;
         this.positionSource = new BlockPositionSource(pPos);
      }

      public int getListenerRadius() {
         return 8;
      }

      public PositionSource getPositionSource() {
         return this.positionSource;
      }

      public boolean canTriggerAvoidVibration() {
         return true;
      }

      public boolean canReceiveVibration(ServerLevel pLevel, BlockPos pPos, GameEvent pGameEvent, @Nullable GameEvent.Context pContext) {
         return !pPos.equals(this.blockPos) || pGameEvent != GameEvent.BLOCK_DESTROY && pGameEvent != GameEvent.BLOCK_PLACE ? SculkSensorBlock.canActivate(SculkSensorBlockEntity.this.getBlockState()) : false;
      }

      public void onReceiveVibration(ServerLevel pLevel, BlockPos pPos, GameEvent pGameEvent, @Nullable Entity pEntity, @Nullable Entity pPlayerEntity, float pDistance) {
         BlockState blockstate = SculkSensorBlockEntity.this.getBlockState();
         if (SculkSensorBlock.canActivate(blockstate)) {
            SculkSensorBlockEntity.this.setLastVibrationFrequency(VibrationSystem.getGameEventFrequency(pGameEvent));
            int i = VibrationSystem.getRedstoneStrengthForDistance(pDistance, this.getListenerRadius());
            Block block = blockstate.getBlock();
            if (block instanceof SculkSensorBlock) {
               SculkSensorBlock sculksensorblock = (SculkSensorBlock)block;
               sculksensorblock.activate(pEntity, pLevel, this.blockPos, blockstate, i, SculkSensorBlockEntity.this.getLastVibrationFrequency());
            }
         }

      }

      public void onDataChanged() {
         SculkSensorBlockEntity.this.setChanged();
      }

      public boolean requiresAdjacentChunksToBeTicking() {
         return true;
      }
   }
}