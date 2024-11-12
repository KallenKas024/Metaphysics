package net.minecraft.world.level;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public interface LevelReader extends BlockAndTintGetter, CollisionGetter, SignalGetter, BiomeManager.NoiseBiomeSource {
   @Nullable
   ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull);

   /** @deprecated */
   @Deprecated
   boolean hasChunk(int pChunkX, int pChunkZ);

   int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ);

   int getSkyDarken();

   BiomeManager getBiomeManager();

   default Holder<Biome> getBiome(BlockPos pPos) {
      return this.getBiomeManager().getBiome(pPos);
   }

   default Stream<BlockState> getBlockStatesIfLoaded(AABB pAabb) {
      int i = Mth.floor(pAabb.minX);
      int j = Mth.floor(pAabb.maxX);
      int k = Mth.floor(pAabb.minY);
      int l = Mth.floor(pAabb.maxY);
      int i1 = Mth.floor(pAabb.minZ);
      int j1 = Mth.floor(pAabb.maxZ);
      return this.hasChunksAt(i, k, i1, j, l, j1) ? this.getBlockStates(pAabb) : Stream.empty();
   }

   default int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
      return pColorResolver.getColor(this.getBiome(pBlockPos).value(), (double)pBlockPos.getX(), (double)pBlockPos.getZ());
   }

   /**
    * Gets the biome at the given quart positions.
    * Note that the coordinates passed into this method are 1/4 the scale of block coordinates.
    */
   default Holder<Biome> getNoiseBiome(int pX, int pY, int pZ) {
      ChunkAccess chunkaccess = this.getChunk(QuartPos.toSection(pX), QuartPos.toSection(pZ), ChunkStatus.BIOMES, false);
      return chunkaccess != null ? chunkaccess.getNoiseBiome(pX, pY, pZ) : this.getUncachedNoiseBiome(pX, pY, pZ);
   }

   Holder<Biome> getUncachedNoiseBiome(int pX, int pY, int pZ);

   boolean isClientSide();

   /** @deprecated */
   @Deprecated
   int getSeaLevel();

   DimensionType dimensionType();

   default int getMinBuildHeight() {
      return this.dimensionType().minY();
   }

   default int getHeight() {
      return this.dimensionType().height();
   }

   default BlockPos getHeightmapPos(Heightmap.Types pHeightmapType, BlockPos pPos) {
      return new BlockPos(pPos.getX(), this.getHeight(pHeightmapType, pPos.getX(), pPos.getZ()), pPos.getZ());
   }

   /**
    * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
    * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
    */
   default boolean isEmptyBlock(BlockPos pPos) {
      return this.getBlockState(pPos).isAir();
   }

   default boolean canSeeSkyFromBelowWater(BlockPos pPos) {
      if (pPos.getY() >= this.getSeaLevel()) {
         return this.canSeeSky(pPos);
      } else {
         BlockPos blockpos = new BlockPos(pPos.getX(), this.getSeaLevel(), pPos.getZ());
         if (!this.canSeeSky(blockpos)) {
            return false;
         } else {
            for(BlockPos blockpos1 = blockpos.below(); blockpos1.getY() > pPos.getY(); blockpos1 = blockpos1.below()) {
               BlockState blockstate = this.getBlockState(blockpos1);
               if (blockstate.getLightBlock(this, blockpos1) > 0 && !blockstate.liquid()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   default float getPathfindingCostFromLightLevels(BlockPos pPos) {
      return this.getLightLevelDependentMagicValue(pPos) - 0.5F;
   }

   /** @deprecated */
   @Deprecated
   default float getLightLevelDependentMagicValue(BlockPos pPos) {
      float f = (float)this.getMaxLocalRawBrightness(pPos) / 15.0F;
      float f1 = f / (4.0F - 3.0F * f);
      return Mth.lerp(this.dimensionType().ambientLight(), f1, 1.0F);
   }

   default ChunkAccess getChunk(BlockPos pPos) {
      return this.getChunk(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()));
   }

   default ChunkAccess getChunk(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL, true);
   }

   default ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus) {
      return this.getChunk(pChunkX, pChunkZ, pRequiredStatus, true);
   }

   @Nullable
   default BlockGetter getChunkForCollisions(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, ChunkStatus.EMPTY, false);
   }

   default boolean isWaterAt(BlockPos pPos) {
      return this.getFluidState(pPos).is(FluidTags.WATER);
   }

   /**
    * Checks if any of the blocks within the aabb are liquids.
    */
   default boolean containsAnyLiquid(AABB pBb) {
      int i = Mth.floor(pBb.minX);
      int j = Mth.ceil(pBb.maxX);
      int k = Mth.floor(pBb.minY);
      int l = Mth.ceil(pBb.maxY);
      int i1 = Mth.floor(pBb.minZ);
      int j1 = Mth.ceil(pBb.maxZ);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               BlockState blockstate = this.getBlockState(blockpos$mutableblockpos.set(k1, l1, i2));
               if (!blockstate.getFluidState().isEmpty()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   default int getMaxLocalRawBrightness(BlockPos pPos) {
      return this.getMaxLocalRawBrightness(pPos, this.getSkyDarken());
   }

   default int getMaxLocalRawBrightness(BlockPos pPos, int pAmount) {
      return pPos.getX() >= -30000000 && pPos.getZ() >= -30000000 && pPos.getX() < 30000000 && pPos.getZ() < 30000000 ? this.getRawBrightness(pPos, pAmount) : 15;
   }

   /** @deprecated */
   @Deprecated
   default boolean hasChunkAt(int pX, int pZ) {
      return this.hasChunk(SectionPos.blockToSectionCoord(pX), SectionPos.blockToSectionCoord(pZ));
   }

   /** @deprecated */
   @Deprecated
   default boolean hasChunkAt(BlockPos pPos) {
      return this.hasChunkAt(pPos.getX(), pPos.getZ());
   }

   /** @deprecated */
   default boolean isAreaLoaded(BlockPos center, int range) {
      return this.hasChunksAt(center.offset(-range, -range, -range), center.offset(range, range, range));
   }

   @Deprecated
   default boolean hasChunksAt(BlockPos pFrom, BlockPos pTo) {
      return this.hasChunksAt(pFrom.getX(), pFrom.getY(), pFrom.getZ(), pTo.getX(), pTo.getY(), pTo.getZ());
   }

   /** @deprecated */
   @Deprecated
   default boolean hasChunksAt(int pFromX, int pFromY, int pFromZ, int pToX, int pToY, int pToZ) {
      return pToY >= this.getMinBuildHeight() && pFromY < this.getMaxBuildHeight() ? this.hasChunksAt(pFromX, pFromZ, pToX, pToZ) : false;
   }

   /** @deprecated */
   @Deprecated
   default boolean hasChunksAt(int pFromX, int pFromZ, int pToX, int pToZ) {
      int i = SectionPos.blockToSectionCoord(pFromX);
      int j = SectionPos.blockToSectionCoord(pToX);
      int k = SectionPos.blockToSectionCoord(pFromZ);
      int l = SectionPos.blockToSectionCoord(pToZ);

      for(int i1 = i; i1 <= j; ++i1) {
         for(int j1 = k; j1 <= l; ++j1) {
            if (!this.hasChunk(i1, j1)) {
               return false;
            }
         }
      }

      return true;
   }

   RegistryAccess registryAccess();

   FeatureFlagSet enabledFeatures();

   default <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<? extends T>> pRegistryKey) {
      Registry<T> registry = this.registryAccess().registryOrThrow(pRegistryKey);
      return registry.asLookup().filterFeatures(this.enabledFeatures());
   }
}
