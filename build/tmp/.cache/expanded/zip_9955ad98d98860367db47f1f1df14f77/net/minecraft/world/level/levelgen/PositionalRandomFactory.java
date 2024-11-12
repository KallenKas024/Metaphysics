package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public interface PositionalRandomFactory {
   default RandomSource at(BlockPos pPos) {
      return this.at(pPos.getX(), pPos.getY(), pPos.getZ());
   }

   default RandomSource fromHashOf(ResourceLocation pName) {
      return this.fromHashOf(pName.toString());
   }

   RandomSource fromHashOf(String pName);

   RandomSource at(int pX, int pY, int pZ);

   @VisibleForTesting
   void parityConfigString(StringBuilder pBuilder);
}