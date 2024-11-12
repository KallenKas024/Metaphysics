package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class CauldronBlock extends AbstractCauldronBlock {
   private static final float RAIN_FILL_CHANCE = 0.05F;
   private static final float POWDER_SNOW_FILL_CHANCE = 0.1F;

   public CauldronBlock(BlockBehaviour.Properties p_51403_) {
      super(p_51403_, CauldronInteraction.EMPTY);
   }

   public boolean isFull(BlockState pState) {
      return false;
   }

   protected static boolean shouldHandlePrecipitation(Level pLevel, Biome.Precipitation pPrecipitation) {
      if (pPrecipitation == Biome.Precipitation.RAIN) {
         return pLevel.getRandom().nextFloat() < 0.05F;
      } else if (pPrecipitation == Biome.Precipitation.SNOW) {
         return pLevel.getRandom().nextFloat() < 0.1F;
      } else {
         return false;
      }
   }

   public void handlePrecipitation(BlockState pState, Level pLevel, BlockPos pPos, Biome.Precipitation pPrecipitation) {
      if (shouldHandlePrecipitation(pLevel, pPrecipitation)) {
         if (pPrecipitation == Biome.Precipitation.RAIN) {
            pLevel.setBlockAndUpdate(pPos, Blocks.WATER_CAULDRON.defaultBlockState());
            pLevel.gameEvent((Entity)null, GameEvent.BLOCK_CHANGE, pPos);
         } else if (pPrecipitation == Biome.Precipitation.SNOW) {
            pLevel.setBlockAndUpdate(pPos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
            pLevel.gameEvent((Entity)null, GameEvent.BLOCK_CHANGE, pPos);
         }

      }
   }

   protected boolean canReceiveStalactiteDrip(Fluid pFluid) {
      return true;
   }

   protected void receiveStalactiteDrip(BlockState pState, Level pLevel, BlockPos pPos, Fluid pFluid) {
      if (pFluid == Fluids.WATER) {
         BlockState blockstate = Blocks.WATER_CAULDRON.defaultBlockState();
         pLevel.setBlockAndUpdate(pPos, blockstate);
         pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate));
         pLevel.levelEvent(1047, pPos, 0);
      } else if (pFluid == Fluids.LAVA) {
         BlockState blockstate1 = Blocks.LAVA_CAULDRON.defaultBlockState();
         pLevel.setBlockAndUpdate(pPos, blockstate1);
         pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate1));
         pLevel.levelEvent(1046, pPos, 0);
      }

   }
}