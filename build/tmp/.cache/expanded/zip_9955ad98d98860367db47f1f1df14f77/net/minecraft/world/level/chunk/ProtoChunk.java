package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;

public class ProtoChunk extends ChunkAccess {
   @Nullable
   private volatile LevelLightEngine lightEngine;
   private volatile ChunkStatus status = ChunkStatus.EMPTY;
   private final List<CompoundTag> entities = Lists.newArrayList();
   private final Map<GenerationStep.Carving, CarvingMask> carvingMasks = new Object2ObjectArrayMap<>();
   @Nullable
   private BelowZeroRetrogen belowZeroRetrogen;
   private final ProtoChunkTicks<Block> blockTicks;
   private final ProtoChunkTicks<Fluid> fluidTicks;

   public ProtoChunk(ChunkPos pChunkPos, UpgradeData pUpgradeData, LevelHeightAccessor pLevelHeightAccessor, Registry<Biome> pBiomeRegistry, @Nullable BlendingData pBlendingData) {
      this(pChunkPos, pUpgradeData, (LevelChunkSection[])null, new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), pLevelHeightAccessor, pBiomeRegistry, pBlendingData);
   }

   public ProtoChunk(ChunkPos pChunkPos, UpgradeData pUpgradeData, @Nullable LevelChunkSection[] pSections, ProtoChunkTicks<Block> pBlockTicks, ProtoChunkTicks<Fluid> pLiquidTicks, LevelHeightAccessor pLevelHeightAccessor, Registry<Biome> pBiomeRegistry, @Nullable BlendingData pBlendingData) {
      super(pChunkPos, pUpgradeData, pLevelHeightAccessor, pBiomeRegistry, 0L, pSections, pBlendingData);
      this.blockTicks = pBlockTicks;
      this.fluidTicks = pLiquidTicks;
   }

   public TickContainerAccess<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public TickContainerAccess<Fluid> getFluidTicks() {
      return this.fluidTicks;
   }

   public ChunkAccess.TicksToSave getTicksForSerialization() {
      return new ChunkAccess.TicksToSave(this.blockTicks, this.fluidTicks);
   }

   public BlockState getBlockState(BlockPos pPos) {
      int i = pPos.getY();
      if (this.isOutsideBuildHeight(i)) {
         return Blocks.VOID_AIR.defaultBlockState();
      } else {
         LevelChunkSection levelchunksection = this.getSection(this.getSectionIndex(i));
         return levelchunksection.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : levelchunksection.getBlockState(pPos.getX() & 15, i & 15, pPos.getZ() & 15);
      }
   }

   public FluidState getFluidState(BlockPos pPos) {
      int i = pPos.getY();
      if (this.isOutsideBuildHeight(i)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         LevelChunkSection levelchunksection = this.getSection(this.getSectionIndex(i));
         return levelchunksection.hasOnlyAir() ? Fluids.EMPTY.defaultFluidState() : levelchunksection.getFluidState(pPos.getX() & 15, i & 15, pPos.getZ() & 15);
      }
   }

   @Nullable
   public BlockState setBlockState(BlockPos pPos, BlockState pState, boolean pIsMoving) {
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();
      if (j >= this.getMinBuildHeight() && j < this.getMaxBuildHeight()) {
         int l = this.getSectionIndex(j);
         LevelChunkSection levelchunksection = this.getSection(l);
         boolean flag = levelchunksection.hasOnlyAir();
         if (flag && pState.is(Blocks.AIR)) {
            return pState;
         } else {
            int i1 = SectionPos.sectionRelative(i);
            int j1 = SectionPos.sectionRelative(j);
            int k1 = SectionPos.sectionRelative(k);
            BlockState blockstate = levelchunksection.setBlockState(i1, j1, k1, pState);
            if (this.status.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
               boolean flag1 = levelchunksection.hasOnlyAir();
               if (flag1 != flag) {
                  this.lightEngine.updateSectionStatus(pPos, flag1);
               }

               if (LightEngine.hasDifferentLightProperties(this, pPos, blockstate, pState)) {
                  this.skyLightSources.update(this, i1, j, k1);
                  this.lightEngine.checkBlock(pPos);
               }
            }

            EnumSet<Heightmap.Types> enumset1 = this.getStatus().heightmapsAfter();
            EnumSet<Heightmap.Types> enumset = null;

            for(Heightmap.Types heightmap$types : enumset1) {
               Heightmap heightmap = this.heightmaps.get(heightmap$types);
               if (heightmap == null) {
                  if (enumset == null) {
                     enumset = EnumSet.noneOf(Heightmap.Types.class);
                  }

                  enumset.add(heightmap$types);
               }
            }

            if (enumset != null) {
               Heightmap.primeHeightmaps(this, enumset);
            }

            for(Heightmap.Types heightmap$types1 : enumset1) {
               this.heightmaps.get(heightmap$types1).update(i1, j, k1, pState);
            }

            return blockstate;
         }
      } else {
         return Blocks.VOID_AIR.defaultBlockState();
      }
   }

   public void setBlockEntity(BlockEntity pBlockEntity) {
      this.blockEntities.put(pBlockEntity.getBlockPos(), pBlockEntity);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pPos) {
      return this.blockEntities.get(pPos);
   }

   public Map<BlockPos, BlockEntity> getBlockEntities() {
      return this.blockEntities;
   }

   public void addEntity(CompoundTag pTag) {
      this.entities.add(pTag);
   }

   public void addEntity(Entity pEntity) {
      if (!pEntity.isPassenger()) {
         CompoundTag compoundtag = new CompoundTag();
         pEntity.save(compoundtag);
         this.addEntity(compoundtag);
      }
   }

   public void setStartForStructure(Structure pStructure, StructureStart pStructureStart) {
      BelowZeroRetrogen belowzeroretrogen = this.getBelowZeroRetrogen();
      if (belowzeroretrogen != null && pStructureStart.isValid()) {
         BoundingBox boundingbox = pStructureStart.getBoundingBox();
         LevelHeightAccessor levelheightaccessor = this.getHeightAccessorForGeneration();
         if (boundingbox.minY() < levelheightaccessor.getMinBuildHeight() || boundingbox.maxY() >= levelheightaccessor.getMaxBuildHeight()) {
            return;
         }
      }

      super.setStartForStructure(pStructure, pStructureStart);
   }

   public List<CompoundTag> getEntities() {
      return this.entities;
   }

   public ChunkStatus getStatus() {
      return this.status;
   }

   public void setStatus(ChunkStatus pStatus) {
      this.status = pStatus;
      if (this.belowZeroRetrogen != null && pStatus.isOrAfter(this.belowZeroRetrogen.targetStatus())) {
         this.setBelowZeroRetrogen((BelowZeroRetrogen)null);
      }

      this.setUnsaved(true);
   }

   /**
    * Gets the biome at the given quart positions.
    * Note that the coordinates passed into this method are 1/4 the scale of block coordinates.
    */
   public Holder<Biome> getNoiseBiome(int pX, int pY, int pZ) {
      if (this.getHighestGeneratedStatus().isOrAfter(ChunkStatus.BIOMES)) {
         return super.getNoiseBiome(pX, pY, pZ);
      } else {
         throw new IllegalStateException("Asking for biomes before we have biomes");
      }
   }

   public static short packOffsetCoordinates(BlockPos pPos) {
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();
      int l = i & 15;
      int i1 = j & 15;
      int j1 = k & 15;
      return (short)(l | i1 << 4 | j1 << 8);
   }

   public static BlockPos unpackOffsetCoordinates(short pPackedPos, int pYOffset, ChunkPos pChunkPos) {
      int i = SectionPos.sectionToBlockCoord(pChunkPos.x, pPackedPos & 15);
      int j = SectionPos.sectionToBlockCoord(pYOffset, pPackedPos >>> 4 & 15);
      int k = SectionPos.sectionToBlockCoord(pChunkPos.z, pPackedPos >>> 8 & 15);
      return new BlockPos(i, j, k);
   }

   public void markPosForPostprocessing(BlockPos pPos) {
      if (!this.isOutsideBuildHeight(pPos)) {
         ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(pPos.getY())).add(packOffsetCoordinates(pPos));
      }

   }

   public void addPackedPostProcess(short pPackedPosition, int pIndex) {
      ChunkAccess.getOrCreateOffsetList(this.postProcessing, pIndex).add(pPackedPosition);
   }

   public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
      return Collections.unmodifiableMap(this.pendingBlockEntities);
   }

   @Nullable
   public CompoundTag getBlockEntityNbtForSaving(BlockPos pPos) {
      BlockEntity blockentity = this.getBlockEntity(pPos);
      return blockentity != null ? blockentity.saveWithFullMetadata() : this.pendingBlockEntities.get(pPos);
   }

   public void removeBlockEntity(BlockPos pPos) {
      this.blockEntities.remove(pPos);
      this.pendingBlockEntities.remove(pPos);
   }

   @Nullable
   public CarvingMask getCarvingMask(GenerationStep.Carving pStep) {
      return this.carvingMasks.get(pStep);
   }

   public CarvingMask getOrCreateCarvingMask(GenerationStep.Carving pStep) {
      return this.carvingMasks.computeIfAbsent(pStep, (p_289528_) -> {
         return new CarvingMask(this.getHeight(), this.getMinBuildHeight());
      });
   }

   public void setCarvingMask(GenerationStep.Carving pStep, CarvingMask pCarvingMask) {
      this.carvingMasks.put(pStep, pCarvingMask);
   }

   public void setLightEngine(LevelLightEngine pLightEngine) {
      this.lightEngine = pLightEngine;
   }

   public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen pBelowZeroRetrogen) {
      this.belowZeroRetrogen = pBelowZeroRetrogen;
   }

   @Nullable
   public BelowZeroRetrogen getBelowZeroRetrogen() {
      return this.belowZeroRetrogen;
   }

   private static <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTicks<T> pTicks) {
      return new LevelChunkTicks<>(pTicks.scheduledTicks());
   }

   public LevelChunkTicks<Block> unpackBlockTicks() {
      return unpackTicks(this.blockTicks);
   }

   public LevelChunkTicks<Fluid> unpackFluidTicks() {
      return unpackTicks(this.fluidTicks);
   }

   public LevelHeightAccessor getHeightAccessorForGeneration() {
      return (LevelHeightAccessor)(this.isUpgrading() ? BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR : this);
   }
}