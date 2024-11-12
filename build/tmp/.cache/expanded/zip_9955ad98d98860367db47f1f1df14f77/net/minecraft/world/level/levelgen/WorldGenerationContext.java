package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class WorldGenerationContext {
   private final int minY;
   private final int height;

   public WorldGenerationContext(ChunkGenerator pGenerator, LevelHeightAccessor pLevel) {
      this.minY = Math.max(pLevel.getMinBuildHeight(), pGenerator.getMinY());
      this.height = Math.min(pLevel.getHeight(), pGenerator.getGenDepth());
   }

   public int getMinGenY() {
      return this.minY;
   }

   public int getGenDepth() {
      return this.height;
   }
}