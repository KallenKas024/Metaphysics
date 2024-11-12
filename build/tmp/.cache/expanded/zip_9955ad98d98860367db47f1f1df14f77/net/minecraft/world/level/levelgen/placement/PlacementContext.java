package net.minecraft.world.level.levelgen.placement;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class PlacementContext extends WorldGenerationContext {
   private final WorldGenLevel level;
   private final ChunkGenerator generator;
   private final Optional<PlacedFeature> topFeature;

   public PlacementContext(WorldGenLevel pLevel, ChunkGenerator pGenerator, Optional<PlacedFeature> pTopFeature) {
      super(pGenerator, pLevel);
      this.level = pLevel;
      this.generator = pGenerator;
      this.topFeature = pTopFeature;
   }

   public int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ) {
      return this.level.getHeight(pHeightmapType, pX, pZ);
   }

   public CarvingMask getCarvingMask(ChunkPos pChunkPos, GenerationStep.Carving pStep) {
      return ((ProtoChunk)this.level.getChunk(pChunkPos.x, pChunkPos.z)).getOrCreateCarvingMask(pStep);
   }

   public BlockState getBlockState(BlockPos pPos) {
      return this.level.getBlockState(pPos);
   }

   public int getMinBuildHeight() {
      return this.level.getMinBuildHeight();
   }

   public WorldGenLevel getLevel() {
      return this.level;
   }

   public Optional<PlacedFeature> topFeature() {
      return this.topFeature;
   }

   public ChunkGenerator generator() {
      return this.generator;
   }
}