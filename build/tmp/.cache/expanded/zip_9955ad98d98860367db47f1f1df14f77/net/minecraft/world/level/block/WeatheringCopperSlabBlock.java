package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperSlabBlock extends SlabBlock implements WeatheringCopper {
   private final WeatheringCopper.WeatherState weatherState;

   public WeatheringCopperSlabBlock(WeatheringCopper.WeatherState pWeatherState, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.weatherState = pWeatherState;
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      this.onRandomTick(pState, pLevel, pPos, pRandom);
   }

   /**
    * @return whether this block needs random ticking.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return WeatheringCopper.getNext(pState.getBlock()).isPresent();
   }

   public WeatheringCopper.WeatherState getAge() {
      return this.weatherState;
   }
}