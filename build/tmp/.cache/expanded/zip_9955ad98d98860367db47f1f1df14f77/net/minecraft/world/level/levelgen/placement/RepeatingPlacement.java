package net.minecraft.world.level.levelgen.placement;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public abstract class RepeatingPlacement extends PlacementModifier {
   protected abstract int count(RandomSource pRandom, BlockPos pPos);

   public Stream<BlockPos> getPositions(PlacementContext pContext, RandomSource pRandom, BlockPos pPos) {
      return IntStream.range(0, this.count(pRandom, pPos)).mapToObj((p_191912_) -> {
         return pPos;
      });
   }
}