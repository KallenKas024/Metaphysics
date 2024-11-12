package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CarverDebugSettings {
   public static final CarverDebugSettings DEFAULT = new CarverDebugSettings(false, Blocks.ACACIA_BUTTON.defaultBlockState(), Blocks.CANDLE.defaultBlockState(), Blocks.ORANGE_STAINED_GLASS.defaultBlockState(), Blocks.GLASS.defaultBlockState());
   public static final Codec<CarverDebugSettings> CODEC = RecordCodecBuilder.create((p_159135_) -> {
      return p_159135_.group(Codec.BOOL.optionalFieldOf("debug_mode", Boolean.valueOf(false)).forGetter(CarverDebugSettings::isDebugMode), BlockState.CODEC.optionalFieldOf("air_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getAirState), BlockState.CODEC.optionalFieldOf("water_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getWaterState), BlockState.CODEC.optionalFieldOf("lava_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getLavaState), BlockState.CODEC.optionalFieldOf("barrier_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getBarrierState)).apply(p_159135_, CarverDebugSettings::new);
   });
   private final boolean debugMode;
   private final BlockState airState;
   private final BlockState waterState;
   private final BlockState lavaState;
   private final BlockState barrierState;

   public static CarverDebugSettings of(boolean pDebugMode, BlockState pAirState, BlockState pWaterState, BlockState pLavaState, BlockState pBarrierState) {
      return new CarverDebugSettings(pDebugMode, pAirState, pWaterState, pLavaState, pBarrierState);
   }

   public static CarverDebugSettings of(BlockState pAirState, BlockState pWaterState, BlockState pLavaState, BlockState pBarrierState) {
      return new CarverDebugSettings(false, pAirState, pWaterState, pLavaState, pBarrierState);
   }

   public static CarverDebugSettings of(boolean pDebugMode, BlockState pAirState) {
      return new CarverDebugSettings(pDebugMode, pAirState, DEFAULT.getWaterState(), DEFAULT.getLavaState(), DEFAULT.getBarrierState());
   }

   private CarverDebugSettings(boolean p_159123_, BlockState p_159124_, BlockState p_159125_, BlockState p_159126_, BlockState p_159127_) {
      this.debugMode = p_159123_;
      this.airState = p_159124_;
      this.waterState = p_159125_;
      this.lavaState = p_159126_;
      this.barrierState = p_159127_;
   }

   public boolean isDebugMode() {
      return this.debugMode;
   }

   public BlockState getAirState() {
      return this.airState;
   }

   public BlockState getWaterState() {
      return this.waterState;
   }

   public BlockState getLavaState() {
      return this.lavaState;
   }

   public BlockState getBarrierState() {
      return this.barrierState;
   }
}