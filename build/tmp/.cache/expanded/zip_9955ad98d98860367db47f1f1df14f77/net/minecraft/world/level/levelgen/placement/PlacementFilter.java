package net.minecraft.world.level.levelgen.placement;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public abstract class PlacementFilter extends PlacementModifier {
   public final Stream<BlockPos> getPositions(PlacementContext pContext, RandomSource pRandom, BlockPos pPos) {
      return this.shouldPlace(pContext, pRandom, pPos) ? Stream.of(pPos) : Stream.of();
   }

   protected abstract boolean shouldPlace(PlacementContext pContext, RandomSource pRandom, BlockPos pPos);
}