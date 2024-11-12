package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class FlatLevelSource extends ChunkGenerator {
   public static final Codec<FlatLevelSource> CODEC = RecordCodecBuilder.create((p_255577_) -> {
      return p_255577_.group(FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(FlatLevelSource::settings)).apply(p_255577_, p_255577_.stable(FlatLevelSource::new));
   });
   private final FlatLevelGeneratorSettings settings;

   public FlatLevelSource(FlatLevelGeneratorSettings p_256337_) {
      super(new FixedBiomeSource(p_256337_.getBiome()), Util.memoize(p_256337_::adjustGenerationSettings));
      this.settings = p_256337_;
   }

   public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> pStructureSetLookup, RandomState pRandomState, long pSeed) {
      Stream<Holder<StructureSet>> stream = this.settings.structureOverrides().map(HolderSet::stream).orElseGet(() -> {
         return pStructureSetLookup.listElements().map((p_255579_) -> {
            return p_255579_;
         });
      });
      return ChunkGeneratorStructureState.createForFlat(pRandomState, pSeed, this.biomeSource, stream);
   }

   protected Codec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   public FlatLevelGeneratorSettings settings() {
      return this.settings;
   }

   public void buildSurface(WorldGenRegion pLevel, StructureManager pStructureManager, RandomState pRandom, ChunkAccess pChunk) {
   }

   public int getSpawnHeight(LevelHeightAccessor pLevel) {
      return pLevel.getMinBuildHeight() + Math.min(pLevel.getHeight(), this.settings.getLayers().size());
   }

   public CompletableFuture<ChunkAccess> fillFromNoise(Executor pExecutor, Blender pBlender, RandomState pRandom, StructureManager pStructureManager, ChunkAccess pChunk) {
      List<BlockState> list = this.settings.getLayers();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      Heightmap heightmap = pChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
      Heightmap heightmap1 = pChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

      for(int i = 0; i < Math.min(pChunk.getHeight(), list.size()); ++i) {
         BlockState blockstate = list.get(i);
         if (blockstate != null) {
            int j = pChunk.getMinBuildHeight() + i;

            for(int k = 0; k < 16; ++k) {
               for(int l = 0; l < 16; ++l) {
                  pChunk.setBlockState(blockpos$mutableblockpos.set(k, j, l), blockstate, false);
                  heightmap.update(k, j, l, blockstate);
                  heightmap1.update(k, j, l, blockstate);
               }
            }
         }
      }

      return CompletableFuture.completedFuture(pChunk);
   }

   public int getBaseHeight(int pX, int pZ, Heightmap.Types pType, LevelHeightAccessor pLevel, RandomState pRandom) {
      List<BlockState> list = this.settings.getLayers();

      for(int i = Math.min(list.size(), pLevel.getMaxBuildHeight()) - 1; i >= 0; --i) {
         BlockState blockstate = list.get(i);
         if (blockstate != null && pType.isOpaque().test(blockstate)) {
            return pLevel.getMinBuildHeight() + i + 1;
         }
      }

      return pLevel.getMinBuildHeight();
   }

   public NoiseColumn getBaseColumn(int pX, int pZ, LevelHeightAccessor pHeight, RandomState pRandom) {
      return new NoiseColumn(pHeight.getMinBuildHeight(), this.settings.getLayers().stream().limit((long)pHeight.getHeight()).map((p_204549_) -> {
         return p_204549_ == null ? Blocks.AIR.defaultBlockState() : p_204549_;
      }).toArray((p_204543_) -> {
         return new BlockState[p_204543_];
      }));
   }

   public void addDebugScreenInfo(List<String> pInfo, RandomState pRandom, BlockPos pPos) {
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
      return -63;
   }
}