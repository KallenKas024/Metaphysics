package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureManager {
   private final LevelAccessor level;
   private final WorldOptions worldOptions;
   private final StructureCheck structureCheck;

   public StructureManager(LevelAccessor pLevel, WorldOptions pWorldOptions, StructureCheck pStructureCheck) {
      this.level = pLevel;
      this.worldOptions = pWorldOptions;
      this.structureCheck = pStructureCheck;
   }

   public StructureManager forWorldGenRegion(WorldGenRegion pRegion) {
      if (pRegion.getLevel() != this.level) {
         throw new IllegalStateException("Using invalid structure manager (source level: " + pRegion.getLevel() + ", region: " + pRegion);
      } else {
         return new StructureManager(pRegion, this.worldOptions, this.structureCheck);
      }
   }

   public List<StructureStart> startsForStructure(ChunkPos pChunkPos, Predicate<Structure> pStructurePredicate) {
      Map<Structure, LongSet> map = this.level.getChunk(pChunkPos.x, pChunkPos.z, ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
      ImmutableList.Builder<StructureStart> builder = ImmutableList.builder();

      for(Map.Entry<Structure, LongSet> entry : map.entrySet()) {
         Structure structure = entry.getKey();
         if (pStructurePredicate.test(structure)) {
            this.fillStartsForStructure(structure, entry.getValue(), builder::add);
         }
      }

      return builder.build();
   }

   public List<StructureStart> startsForStructure(SectionPos pSectionPos, Structure pStructure) {
      LongSet longset = this.level.getChunk(pSectionPos.x(), pSectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForStructure(pStructure);
      ImmutableList.Builder<StructureStart> builder = ImmutableList.builder();
      this.fillStartsForStructure(pStructure, longset, builder::add);
      return builder.build();
   }

   public void fillStartsForStructure(Structure pStructure, LongSet pStructureRefs, Consumer<StructureStart> pStartConsumer) {
      for(long i : pStructureRefs) {
         SectionPos sectionpos = SectionPos.of(new ChunkPos(i), this.level.getMinSection());
         StructureStart structurestart = this.getStartForStructure(sectionpos, pStructure, this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_STARTS));
         if (structurestart != null && structurestart.isValid()) {
            pStartConsumer.accept(structurestart);
         }
      }

   }

   @Nullable
   public StructureStart getStartForStructure(SectionPos pSectionPos, Structure pStructure, StructureAccess pStructureAccess) {
      return pStructureAccess.getStartForStructure(pStructure);
   }

   public void setStartForStructure(SectionPos pSectionPos, Structure pStructure, StructureStart pStructureStart, StructureAccess pStructureAccess) {
      pStructureAccess.setStartForStructure(pStructure, pStructureStart);
   }

   public void addReferenceForStructure(SectionPos pSectionPos, Structure pStructure, long pReference, StructureAccess pStructureAccess) {
      pStructureAccess.addReferenceForStructure(pStructure, pReference);
   }

   public boolean shouldGenerateStructures() {
      return this.worldOptions.generateStructures();
   }

   public StructureStart getStructureAt(BlockPos pPos, Structure pStructure) {
      for(StructureStart structurestart : this.startsForStructure(SectionPos.of(pPos), pStructure)) {
         if (structurestart.getBoundingBox().isInside(pPos)) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   public StructureStart getStructureWithPieceAt(BlockPos pPos, ResourceKey<Structure> pStructureKey) {
      Structure structure = this.registryAccess().registryOrThrow(Registries.STRUCTURE).get(pStructureKey);
      return structure == null ? StructureStart.INVALID_START : this.getStructureWithPieceAt(pPos, structure);
   }

   public StructureStart getStructureWithPieceAt(BlockPos pPos, TagKey<Structure> pStructureTag) {
      Registry<Structure> registry = this.registryAccess().registryOrThrow(Registries.STRUCTURE);

      for(StructureStart structurestart : this.startsForStructure(new ChunkPos(pPos), (p_258967_) -> {
         return registry.getHolder(registry.getId(p_258967_)).map((p_248425_) -> {
            return p_248425_.is(pStructureTag);
         }).orElse(false);
      })) {
         if (this.structureHasPieceAt(pPos, structurestart)) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   public StructureStart getStructureWithPieceAt(BlockPos pPos, Structure pStructure) {
      for(StructureStart structurestart : this.startsForStructure(SectionPos.of(pPos), pStructure)) {
         if (this.structureHasPieceAt(pPos, structurestart)) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   public boolean structureHasPieceAt(BlockPos pPos, StructureStart pStructureStart) {
      for(StructurePiece structurepiece : pStructureStart.getPieces()) {
         if (structurepiece.getBoundingBox().isInside(pPos)) {
            return true;
         }
      }

      return false;
   }

   public boolean hasAnyStructureAt(BlockPos pPos) {
      SectionPos sectionpos = SectionPos.of(pPos);
      return this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
   }

   public Map<Structure, LongSet> getAllStructuresAt(BlockPos pPos) {
      SectionPos sectionpos = SectionPos.of(pPos);
      return this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
   }

   public StructureCheckResult checkStructurePresence(ChunkPos pChunkPos, Structure pStructure, boolean pSkipKnownStructures) {
      return this.structureCheck.checkStart(pChunkPos, pStructure, pSkipKnownStructures);
   }

   public void addReference(StructureStart pStructureStart) {
      pStructureStart.addReference();
      this.structureCheck.incrementReference(pStructureStart.getChunkPos(), pStructureStart.getStructure());
   }

   public RegistryAccess registryAccess() {
      return this.level.registryAccess();
   }
}