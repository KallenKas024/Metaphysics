package net.minecraft.world.level.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class LayeredCauldronBlock extends AbstractCauldronBlock {
   public static final int MIN_FILL_LEVEL = 1;
   public static final int MAX_FILL_LEVEL = 3;
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
   private static final int BASE_CONTENT_HEIGHT = 6;
   private static final double HEIGHT_PER_LEVEL = 3.0D;
   public static final Predicate<Biome.Precipitation> RAIN = (p_153553_) -> {
      return p_153553_ == Biome.Precipitation.RAIN;
   };
   public static final Predicate<Biome.Precipitation> SNOW = (p_153526_) -> {
      return p_153526_ == Biome.Precipitation.SNOW;
   };
   private final Predicate<Biome.Precipitation> fillPredicate;

   public LayeredCauldronBlock(BlockBehaviour.Properties pProperties, Predicate<Biome.Precipitation> pFillPredicate, Map<Item, CauldronInteraction> pInteractions) {
      super(pProperties, pInteractions);
      this.fillPredicate = pFillPredicate;
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(1)));
   }

   public boolean isFull(BlockState pState) {
      return pState.getValue(LEVEL) == 3;
   }

   protected boolean canReceiveStalactiteDrip(Fluid pFluid) {
      return pFluid == Fluids.WATER && this.fillPredicate == RAIN;
   }

   protected double getContentHeight(BlockState pState) {
      return (6.0D + (double)pState.getValue(LEVEL).intValue() * 3.0D) / 16.0D;
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      if (!pLevel.isClientSide && pEntity.isOnFire() && this.isEntityInsideContent(pState, pPos, pEntity)) {
         pEntity.clearFire();
         if (pEntity.mayInteract(pLevel, pPos)) {
            this.handleEntityOnFireInside(pState, pLevel, pPos);
         }
      }

   }

   protected void handleEntityOnFireInside(BlockState pState, Level pLevel, BlockPos pPos) {
      lowerFillLevel(pState, pLevel, pPos);
   }

   public static void lowerFillLevel(BlockState pState, Level pLevel, BlockPos pPos) {
      int i = pState.getValue(LEVEL) - 1;
      BlockState blockstate = i == 0 ? Blocks.CAULDRON.defaultBlockState() : pState.setValue(LEVEL, Integer.valueOf(i));
      pLevel.setBlockAndUpdate(pPos, blockstate);
      pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate));
   }

   public void handlePrecipitation(BlockState pState, Level pLevel, BlockPos pPos, Biome.Precipitation pPrecipitation) {
      if (CauldronBlock.shouldHandlePrecipitation(pLevel, pPrecipitation) && pState.getValue(LEVEL) != 3 && this.fillPredicate.test(pPrecipitation)) {
         BlockState blockstate = pState.cycle(LEVEL);
         pLevel.setBlockAndUpdate(pPos, blockstate);
         pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate));
      }
   }

   /**
    * Returns the analog signal this block emits. This is the signal a comparator can read from it.
    * 
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
      return pState.getValue(LEVEL);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LEVEL);
   }

   protected void receiveStalactiteDrip(BlockState pState, Level pLevel, BlockPos pPos, Fluid pFluid) {
      if (!this.isFull(pState)) {
         BlockState blockstate = pState.setValue(LEVEL, Integer.valueOf(pState.getValue(LEVEL) + 1));
         pLevel.setBlockAndUpdate(pPos, blockstate);
         pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate));
         pLevel.levelEvent(1047, pPos, 0);
      }
   }
}