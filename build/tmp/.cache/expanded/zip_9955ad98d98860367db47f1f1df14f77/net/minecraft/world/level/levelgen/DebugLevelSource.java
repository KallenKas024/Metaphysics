package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;

public class DebugLevelSource extends ChunkGenerator {
   public static final Codec<DebugLevelSource> CODEC = RecordCodecBuilder.create((p_255576_) -> {
      return p_255576_.group(RegistryOps.retrieveElement(Biomes.PLAINS)).apply(p_255576_, p_255576_.stable(DebugLevelSource::new));
   });
   private static final int BLOCK_MARGIN = 2;
   /** A list of all valid block states. */
   private static List<BlockState> ALL_BLOCKS = StreamSupport.stream(BuiltInRegistries.BLOCK.spliterator(), false).flatMap((p_208208_) -> {
      return p_208208_.getStateDefinition().getPossibleStates().stream();
   }).collect(Collectors.toList());
   private static int GRID_WIDTH = Mth.ceil(Mth.sqrt((float)ALL_BLOCKS.size()));
   private static int GRID_HEIGHT = Mth.ceil((float)ALL_BLOCKS.size() / (float)GRID_WIDTH);
   protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
   protected static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();
   public static final int HEIGHT = 70;
   public static final int BARRIER_HEIGHT = 60;

   public DebugLevelSource(Holder.Reference<Biome> p_255723_) {
      super(new FixedBiomeSource(p_255723_));
   }

   protected Codec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   public void buildSurface(WorldGenRegion pLevel, StructureManager pStructureManager, RandomState pRandom, ChunkAccess pChunk) {
   }

   public void applyBiomeDecoration(WorldGenLevel pLevel, ChunkAccess pChunk, StructureManager pStructureManager) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      ChunkPos chunkpos = pChunk.getPos();
      int i = chunkpos.x;
      int j = chunkpos.z;

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            int i1 = SectionPos.sectionToBlockCoord(i, k);
            int j1 = SectionPos.sectionToBlockCoord(j, l);
            pLevel.setBlock(blockpos$mutableblockpos.set(i1, 60, j1), BARRIER, 2);
            BlockState blockstate = getBlockStateFor(i1, j1);
            pLevel.setBlock(blockpos$mutableblockpos.set(i1, 70, j1), blockstate, 2);
         }
      }

   }

   public CompletableFuture<ChunkAccess> fillFromNoise(Executor pExecutor, Blender pBlender, RandomState pRandom, StructureManager pStructureManager, ChunkAccess pChunk) {
      return CompletableFuture.completedFuture(pChunk);
   }

   public int getBaseHeight(int pX, int pZ, Heightmap.Types pType, LevelHeightAccessor pLevel, RandomState pRandom) {
      return 0;
   }

   public NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor pHeight, RandomState pRandom) {
      return new NoiseColumn(0, new BlockState[0]);
   }

   public void addDebugScreenInfo(List<String> pInfo, RandomState pRandom, BlockPos pPos) {
   }

   public static BlockState getBlockStateFor(int pChunkX, int pChunkZ) {
      BlockState blockstate = AIR;
      if (pChunkX > 0 && pChunkZ > 0 && pChunkX % 2 != 0 && pChunkZ % 2 != 0) {
         pChunkX /= 2;
         pChunkZ /= 2;
         if (pChunkX <= GRID_WIDTH && pChunkZ <= GRID_HEIGHT) {
            int i = Mth.abs(pChunkX * GRID_WIDTH + pChunkZ);
            if (i < ALL_BLOCKS.size()) {
               blockstate = ALL_BLOCKS.get(i);
            }
         }
      }

      return blockstate;
   }

   public static void initValidStates() {
      ALL_BLOCKS = StreamSupport.stream(BuiltInRegistries.BLOCK.spliterator(), false).flatMap(block -> block.getStateDefinition().getPossibleStates().stream()).collect(Collectors.toList());
      GRID_WIDTH = Mth.ceil(Mth.sqrt(ALL_BLOCKS.size()));
      GRID_HEIGHT = Mth.ceil((float) (ALL_BLOCKS.size() / GRID_WIDTH));
   }

   public void applyCarvers(WorldGenRegion pLevel, long pSeed, RandomState pRandom, BiomeManager pBiomeManager, StructureManager pStructureManager, ChunkAccess pChunk, GenerationStep.Carving pStep) {
   }

   public void spawnOriginalMobs(WorldGenRegion pLevel) {
   }

   public int getMinY() {
      return 0;
   }

   public int getGenDepth() {
      return 384;
   }

   public int getSeaLevel() {
      return 63;
   }
}
