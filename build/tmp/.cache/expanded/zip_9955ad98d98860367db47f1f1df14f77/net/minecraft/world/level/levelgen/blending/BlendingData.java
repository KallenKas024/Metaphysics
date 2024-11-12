package net.minecraft.world.level.levelgen.blending;

import com.google.common.primitives.Doubles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

public class BlendingData {
   private static final double BLENDING_DENSITY_FACTOR = 0.1D;
   protected static final int CELL_WIDTH = 4;
   protected static final int CELL_HEIGHT = 8;
   protected static final int CELL_RATIO = 2;
   private static final double SOLID_DENSITY = 1.0D;
   private static final double AIR_DENSITY = -1.0D;
   private static final int CELLS_PER_SECTION_Y = 2;
   private static final int QUARTS_PER_SECTION = QuartPos.fromBlock(16);
   private static final int CELL_HORIZONTAL_MAX_INDEX_INSIDE = QUARTS_PER_SECTION - 1;
   private static final int CELL_HORIZONTAL_MAX_INDEX_OUTSIDE = QUARTS_PER_SECTION;
   private static final int CELL_COLUMN_INSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_INSIDE + 1;
   private static final int CELL_COLUMN_OUTSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_OUTSIDE + 1;
   private static final int CELL_COLUMN_COUNT = CELL_COLUMN_INSIDE_COUNT + CELL_COLUMN_OUTSIDE_COUNT;
   private final LevelHeightAccessor areaWithOldGeneration;
   private static final List<Block> SURFACE_BLOCKS = List.of(Blocks.PODZOL, Blocks.GRAVEL, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.COARSE_DIRT, Blocks.SAND, Blocks.RED_SAND, Blocks.MYCELIUM, Blocks.SNOW_BLOCK, Blocks.TERRACOTTA, Blocks.DIRT);
   protected static final double NO_VALUE = Double.MAX_VALUE;
   private boolean hasCalculatedData;
   private final double[] heights;
   private final List<List<Holder<Biome>>> biomes;
   private final transient double[][] densities;
   private static final Codec<double[]> DOUBLE_ARRAY_CODEC = Codec.DOUBLE.listOf().xmap(Doubles::toArray, Doubles::asList);
   public static final Codec<BlendingData> CODEC = RecordCodecBuilder.<BlendingData>create((p_224754_) -> {
      return p_224754_.group(Codec.INT.fieldOf("min_section").forGetter((p_224767_) -> {
         return p_224767_.areaWithOldGeneration.getMinSection();
      }), Codec.INT.fieldOf("max_section").forGetter((p_224765_) -> {
         return p_224765_.areaWithOldGeneration.getMaxSection();
      }), DOUBLE_ARRAY_CODEC.optionalFieldOf("heights").forGetter((p_224762_) -> {
         return DoubleStream.of(p_224762_.heights).anyMatch((p_224745_) -> {
            return p_224745_ != Double.MAX_VALUE;
         }) ? Optional.of(p_224762_.heights) : Optional.empty();
      })).apply(p_224754_, BlendingData::new);
   }).comapFlatMap(BlendingData::validateArraySize, Function.identity());

   private static DataResult<BlendingData> validateArraySize(BlendingData p_190321_) {
      return p_190321_.heights.length != CELL_COLUMN_COUNT ? DataResult.error(() -> {
         return "heights has to be of length " + CELL_COLUMN_COUNT;
      }) : DataResult.success(p_190321_);
   }

   private BlendingData(int p_224740_, int p_224741_, Optional<double[]> p_224742_) {
      this.heights = p_224742_.orElse(Util.make(new double[CELL_COLUMN_COUNT], (p_224756_) -> {
         Arrays.fill(p_224756_, Double.MAX_VALUE);
      }));
      this.densities = new double[CELL_COLUMN_COUNT][];
      ObjectArrayList<List<Holder<Biome>>> objectarraylist = new ObjectArrayList<>(CELL_COLUMN_COUNT);
      objectarraylist.size(CELL_COLUMN_COUNT);
      this.biomes = objectarraylist;
      int i = SectionPos.sectionToBlockCoord(p_224740_);
      int j = SectionPos.sectionToBlockCoord(p_224741_) - i;
      this.areaWithOldGeneration = LevelHeightAccessor.create(i, j);
   }

   @Nullable
   public static BlendingData getOrUpdateBlendingData(WorldGenRegion pRegion, int pChunkX, int pChunkZ) {
      ChunkAccess chunkaccess = pRegion.getChunk(pChunkX, pChunkZ);
      BlendingData blendingdata = chunkaccess.getBlendingData();
      if (blendingdata != null && chunkaccess.getHighestGeneratedStatus().isOrAfter(ChunkStatus.BIOMES)) {
         blendingdata.calculateData(chunkaccess, sideByGenerationAge(pRegion, pChunkX, pChunkZ, false));
         return blendingdata;
      } else {
         return null;
      }
   }

   public static Set<Direction8> sideByGenerationAge(WorldGenLevel pLevel, int pChunkX, int pChunkZ, boolean pOldNoiseGeneration) {
      Set<Direction8> set = EnumSet.noneOf(Direction8.class);

      for(Direction8 direction8 : Direction8.values()) {
         int i = pChunkX + direction8.getStepX();
         int j = pChunkZ + direction8.getStepZ();
         if (pLevel.getChunk(i, j).isOldNoiseGeneration() == pOldNoiseGeneration) {
            set.add(direction8);
         }
      }

      return set;
   }

   private void calculateData(ChunkAccess pChunk, Set<Direction8> pDirections) {
      if (!this.hasCalculatedData) {
         if (pDirections.contains(Direction8.NORTH) || pDirections.contains(Direction8.WEST) || pDirections.contains(Direction8.NORTH_WEST)) {
            this.addValuesForColumn(getInsideIndex(0, 0), pChunk, 0, 0);
         }

         if (pDirections.contains(Direction8.NORTH)) {
            for(int i = 1; i < QUARTS_PER_SECTION; ++i) {
               this.addValuesForColumn(getInsideIndex(i, 0), pChunk, 4 * i, 0);
            }
         }

         if (pDirections.contains(Direction8.WEST)) {
            for(int j = 1; j < QUARTS_PER_SECTION; ++j) {
               this.addValuesForColumn(getInsideIndex(0, j), pChunk, 0, 4 * j);
            }
         }

         if (pDirections.contains(Direction8.EAST)) {
            for(int k = 1; k < QUARTS_PER_SECTION; ++k) {
               this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, k), pChunk, 15, 4 * k);
            }
         }

         if (pDirections.contains(Direction8.SOUTH)) {
            for(int l = 0; l < QUARTS_PER_SECTION; ++l) {
               this.addValuesForColumn(getOutsideIndex(l, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), pChunk, 4 * l, 15);
            }
         }

         if (pDirections.contains(Direction8.EAST) && pDirections.contains(Direction8.NORTH_EAST)) {
            this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, 0), pChunk, 15, 0);
         }

         if (pDirections.contains(Direction8.EAST) && pDirections.contains(Direction8.SOUTH) && pDirections.contains(Direction8.SOUTH_EAST)) {
            this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), pChunk, 15, 15);
         }

         this.hasCalculatedData = true;
      }
   }

   private void addValuesForColumn(int pIndex, ChunkAccess pChunk, int pX, int pZ) {
      if (this.heights[pIndex] == Double.MAX_VALUE) {
         this.heights[pIndex] = (double)this.getHeightAtXZ(pChunk, pX, pZ);
      }

      this.densities[pIndex] = this.getDensityColumn(pChunk, pX, pZ, Mth.floor(this.heights[pIndex]));
      this.biomes.set(pIndex, this.getBiomeColumn(pChunk, pX, pZ));
   }

   private int getHeightAtXZ(ChunkAccess pChunk, int pX, int pZ) {
      int i;
      if (pChunk.hasPrimedHeightmap(Heightmap.Types.WORLD_SURFACE_WG)) {
         i = Math.min(pChunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pX, pZ) + 1, this.areaWithOldGeneration.getMaxBuildHeight());
      } else {
         i = this.areaWithOldGeneration.getMaxBuildHeight();
      }

      int j = this.areaWithOldGeneration.getMinBuildHeight();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(pX, i, pZ);

      while(blockpos$mutableblockpos.getY() > j) {
         blockpos$mutableblockpos.move(Direction.DOWN);
         if (SURFACE_BLOCKS.contains(pChunk.getBlockState(blockpos$mutableblockpos).getBlock())) {
            return blockpos$mutableblockpos.getY();
         }
      }

      return j;
   }

   private static double read1(ChunkAccess pChunk, BlockPos.MutableBlockPos pPos) {
      return isGround(pChunk, pPos.move(Direction.DOWN)) ? 1.0D : -1.0D;
   }

   private static double read7(ChunkAccess pChunk, BlockPos.MutableBlockPos pPos) {
      double d0 = 0.0D;

      for(int i = 0; i < 7; ++i) {
         d0 += read1(pChunk, pPos);
      }

      return d0;
   }

   private double[] getDensityColumn(ChunkAccess pChunk, int pX, int pZ, int pHeight) {
      double[] adouble = new double[this.cellCountPerColumn()];
      Arrays.fill(adouble, -1.0D);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(pX, this.areaWithOldGeneration.getMaxBuildHeight(), pZ);
      double d0 = read7(pChunk, blockpos$mutableblockpos);

      for(int i = adouble.length - 2; i >= 0; --i) {
         double d1 = read1(pChunk, blockpos$mutableblockpos);
         double d2 = read7(pChunk, blockpos$mutableblockpos);
         adouble[i] = (d0 + d1 + d2) / 15.0D;
         d0 = d2;
      }

      int j = this.getCellYIndex(Mth.floorDiv(pHeight, 8));
      if (j >= 0 && j < adouble.length - 1) {
         double d4 = ((double)pHeight + 0.5D) % 8.0D / 8.0D;
         double d5 = (1.0D - d4) / d4;
         double d3 = Math.max(d5, 1.0D) * 0.25D;
         adouble[j + 1] = -d5 / d3;
         adouble[j] = 1.0D / d3;
      }

      return adouble;
   }

   private List<Holder<Biome>> getBiomeColumn(ChunkAccess pChunk, int pX, int pZ) {
      ObjectArrayList<Holder<Biome>> objectarraylist = new ObjectArrayList<>(this.quartCountPerColumn());
      objectarraylist.size(this.quartCountPerColumn());

      for(int i = 0; i < objectarraylist.size(); ++i) {
         int j = i + QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight());
         objectarraylist.set(i, pChunk.getNoiseBiome(QuartPos.fromBlock(pX), j, QuartPos.fromBlock(pZ)));
      }

      return objectarraylist;
   }

   private static boolean isGround(ChunkAccess pChunk, BlockPos pPos) {
      BlockState blockstate = pChunk.getBlockState(pPos);
      if (blockstate.isAir()) {
         return false;
      } else if (blockstate.is(BlockTags.LEAVES)) {
         return false;
      } else if (blockstate.is(BlockTags.LOGS)) {
         return false;
      } else if (!blockstate.is(Blocks.BROWN_MUSHROOM_BLOCK) && !blockstate.is(Blocks.RED_MUSHROOM_BLOCK)) {
         return !blockstate.getCollisionShape(pChunk, pPos).isEmpty();
      } else {
         return false;
      }
   }

   protected double getHeight(int pX, int pY, int pZ) {
      if (pX != CELL_HORIZONTAL_MAX_INDEX_OUTSIDE && pZ != CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
         return pX != 0 && pZ != 0 ? Double.MAX_VALUE : this.heights[getInsideIndex(pX, pZ)];
      } else {
         return this.heights[getOutsideIndex(pX, pZ)];
      }
   }

   private double getDensity(@Nullable double[] pHeights, int pY) {
      if (pHeights == null) {
         return Double.MAX_VALUE;
      } else {
         int i = this.getCellYIndex(pY);
         return i >= 0 && i < pHeights.length ? pHeights[i] * 0.1D : Double.MAX_VALUE;
      }
   }

   protected double getDensity(int pX, int pY, int pZ) {
      if (pY == this.getMinY()) {
         return 0.1D;
      } else if (pX != CELL_HORIZONTAL_MAX_INDEX_OUTSIDE && pZ != CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
         return pX != 0 && pZ != 0 ? Double.MAX_VALUE : this.getDensity(this.densities[getInsideIndex(pX, pZ)], pY);
      } else {
         return this.getDensity(this.densities[getOutsideIndex(pX, pZ)], pY);
      }
   }

   protected void iterateBiomes(int pX, int pY, int pZ, BlendingData.BiomeConsumer pConsumer) {
      if (pY >= QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight()) && pY < QuartPos.fromBlock(this.areaWithOldGeneration.getMaxBuildHeight())) {
         int i = pY - QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight());

         for(int j = 0; j < this.biomes.size(); ++j) {
            if (this.biomes.get(j) != null) {
               Holder<Biome> holder = this.biomes.get(j).get(i);
               if (holder != null) {
                  pConsumer.consume(pX + getX(j), pZ + getZ(j), holder);
               }
            }
         }

      }
   }

   protected void iterateHeights(int pX, int pZ, BlendingData.HeightConsumer pConsumer) {
      for(int i = 0; i < this.heights.length; ++i) {
         double d0 = this.heights[i];
         if (d0 != Double.MAX_VALUE) {
            pConsumer.consume(pX + getX(i), pZ + getZ(i), d0);
         }
      }

   }

   protected void iterateDensities(int pX, int pZ, int pMinY, int pMaxY, BlendingData.DensityConsumer pConsumer) {
      int i = this.getColumnMinY();
      int j = Math.max(0, pMinY - i);
      int k = Math.min(this.cellCountPerColumn(), pMaxY - i);

      for(int l = 0; l < this.densities.length; ++l) {
         double[] adouble = this.densities[l];
         if (adouble != null) {
            int i1 = pX + getX(l);
            int j1 = pZ + getZ(l);

            for(int k1 = j; k1 < k; ++k1) {
               pConsumer.consume(i1, k1 + i, j1, adouble[k1] * 0.1D);
            }
         }
      }

   }

   private int cellCountPerColumn() {
      return this.areaWithOldGeneration.getSectionsCount() * 2;
   }

   private int quartCountPerColumn() {
      return QuartPos.fromSection(this.areaWithOldGeneration.getSectionsCount());
   }

   private int getColumnMinY() {
      return this.getMinY() + 1;
   }

   private int getMinY() {
      return this.areaWithOldGeneration.getMinSection() * 2;
   }

   private int getCellYIndex(int pY) {
      return pY - this.getColumnMinY();
   }

   private static int getInsideIndex(int pX, int pZ) {
      return CELL_HORIZONTAL_MAX_INDEX_INSIDE - pX + pZ;
   }

   private static int getOutsideIndex(int pX, int pZ) {
      return CELL_COLUMN_INSIDE_COUNT + pX + CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - pZ;
   }

   private static int getX(int pIndex) {
      if (pIndex < CELL_COLUMN_INSIDE_COUNT) {
         return zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_INSIDE - pIndex);
      } else {
         int i = pIndex - CELL_COLUMN_INSIDE_COUNT;
         return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - i);
      }
   }

   private static int getZ(int pIndex) {
      if (pIndex < CELL_COLUMN_INSIDE_COUNT) {
         return zeroIfNegative(pIndex - CELL_HORIZONTAL_MAX_INDEX_INSIDE);
      } else {
         int i = pIndex - CELL_COLUMN_INSIDE_COUNT;
         return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - zeroIfNegative(i - CELL_HORIZONTAL_MAX_INDEX_OUTSIDE);
      }
   }

   private static int zeroIfNegative(int pValue) {
      return pValue & ~(pValue >> 31);
   }

   public LevelHeightAccessor getAreaWithOldGeneration() {
      return this.areaWithOldGeneration;
   }

   protected interface BiomeConsumer {
      void consume(int pX, int pZ, Holder<Biome> pBiome);
   }

   protected interface DensityConsumer {
      void consume(int pX, int pY, int pZ, double pDensity);
   }

   protected interface HeightConsumer {
      void consume(int pX, int pZ, double pHeight);
   }
}