package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.level.block.state.BlockState;

public interface WeatheringCopper extends ChangeOverTimeBlock<WeatheringCopper.WeatherState> {
   Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(() -> {
      return ImmutableBiMap.<Block, Block>builder().put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER).put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER).put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER).put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER).put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER).put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER).put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB).put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB).put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB).put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS).put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS).put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS).build();
   });
   Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> {
      return NEXT_BY_BLOCK.get().inverse();
   });

   static Optional<Block> getPrevious(Block pBlock) {
      return Optional.ofNullable(PREVIOUS_BY_BLOCK.get().get(pBlock));
   }

   static Block getFirst(Block pBlock) {
      Block block = pBlock;

      for(Block block1 = PREVIOUS_BY_BLOCK.get().get(pBlock); block1 != null; block1 = PREVIOUS_BY_BLOCK.get().get(block1)) {
         block = block1;
      }

      return block;
   }

   static Optional<BlockState> getPrevious(BlockState pState) {
      return getPrevious(pState.getBlock()).map((p_154903_) -> {
         return p_154903_.withPropertiesOf(pState);
      });
   }

   static Optional<Block> getNext(Block pBlock) {
      return Optional.ofNullable(NEXT_BY_BLOCK.get().get(pBlock));
   }

   static BlockState getFirst(BlockState pState) {
      return getFirst(pState.getBlock()).withPropertiesOf(pState);
   }

   default Optional<BlockState> getNext(BlockState pState) {
      return getNext(pState.getBlock()).map((p_154896_) -> {
         return p_154896_.withPropertiesOf(pState);
      });
   }

   default float getChanceModifier() {
      return this.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? 0.75F : 1.0F;
   }

   public static enum WeatherState {
      UNAFFECTED,
      EXPOSED,
      WEATHERED,
      OXIDIZED;
   }
}