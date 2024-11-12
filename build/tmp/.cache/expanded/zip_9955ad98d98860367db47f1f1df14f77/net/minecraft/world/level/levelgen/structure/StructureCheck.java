package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public class StructureCheck {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int NO_STRUCTURE = -1;
   private final ChunkScanAccess storageAccess;
   private final RegistryAccess registryAccess;
   private final Registry<Biome> biomes;
   private final Registry<Structure> structureConfigs;
   private final StructureTemplateManager structureTemplateManager;
   private final ResourceKey<Level> dimension;
   private final ChunkGenerator chunkGenerator;
   private final RandomState randomState;
   private final LevelHeightAccessor heightAccessor;
   private final BiomeSource biomeSource;
   private final long seed;
   private final DataFixer fixerUpper;
   private final Long2ObjectMap<Object2IntMap<Structure>> loadedChunks = new Long2ObjectOpenHashMap<>();
   private final Map<Structure, Long2BooleanMap> featureChecks = new HashMap<>();

   public StructureCheck(ChunkScanAccess pStorageAccess, RegistryAccess pRegistryAccess, StructureTemplateManager pStructureTemplateManager, ResourceKey<Level> pDimension, ChunkGenerator pChunkGenerator, RandomState pRandomState, LevelHeightAccessor pHeightAccessor, BiomeSource pBiomeSource, long pSeed, DataFixer pFixerUpper) {
      this.storageAccess = pStorageAccess;
      this.registryAccess = pRegistryAccess;
      this.structureTemplateManager = pStructureTemplateManager;
      this.dimension = pDimension;
      this.chunkGenerator = pChunkGenerator;
      this.randomState = pRandomState;
      this.heightAccessor = pHeightAccessor;
      this.biomeSource = pBiomeSource;
      this.seed = pSeed;
      this.fixerUpper = pFixerUpper;
      this.biomes = pRegistryAccess.registryOrThrow(Registries.BIOME);
      this.structureConfigs = pRegistryAccess.registryOrThrow(Registries.STRUCTURE);
   }

   public StructureCheckResult checkStart(ChunkPos pChunkPos, Structure pStructure, boolean pSkipKnownStructures) {
      long i = pChunkPos.toLong();
      Object2IntMap<Structure> object2intmap = this.loadedChunks.get(i);
      if (object2intmap != null) {
         return this.checkStructureInfo(object2intmap, pStructure, pSkipKnownStructures);
      } else {
         StructureCheckResult structurecheckresult = this.tryLoadFromStorage(pChunkPos, pStructure, pSkipKnownStructures, i);
         if (structurecheckresult != null) {
            return structurecheckresult;
         } else {
            boolean flag = this.featureChecks.computeIfAbsent(pStructure, (p_226739_) -> {
               return new Long2BooleanOpenHashMap();
            }).computeIfAbsent(i, (p_226728_) -> {
               return this.canCreateStructure(pChunkPos, pStructure);
            });
            return !flag ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.CHUNK_LOAD_NEEDED;
         }
      }
   }

   private boolean canCreateStructure(ChunkPos pChunkPos, Structure pStructure) {
      return pStructure.findValidGenerationPoint(new Structure.GenerationContext(this.registryAccess, this.chunkGenerator, this.biomeSource, this.randomState, this.structureTemplateManager, this.seed, pChunkPos, this.heightAccessor, pStructure.biomes()::contains)).isPresent();
   }

   @Nullable
   private StructureCheckResult tryLoadFromStorage(ChunkPos pChunkPos, Structure pStructure, boolean pSkipKnownStructures, long pPackedChunkPos) {
      CollectFields collectfields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector("Level", "Structures", CompoundTag.TYPE, "Starts"), new FieldSelector("structures", CompoundTag.TYPE, "starts"));

      try {
         this.storageAccess.scanChunk(pChunkPos, collectfields).join();
      } catch (Exception exception1) {
         LOGGER.warn("Failed to read chunk {}", pChunkPos, exception1);
         return StructureCheckResult.CHUNK_LOAD_NEEDED;
      }

      Tag tag = collectfields.getResult();
      if (!(tag instanceof CompoundTag compoundtag)) {
         return null;
      } else {
         int i = ChunkStorage.getVersion(compoundtag);
         if (i <= 1493) {
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
         } else {
            ChunkStorage.injectDatafixingContext(compoundtag, this.dimension, this.chunkGenerator.getTypeNameForDataFixer());

            CompoundTag compoundtag1;
            try {
               compoundtag1 = DataFixTypes.CHUNK.updateToCurrentVersion(this.fixerUpper, compoundtag, i);
            } catch (Exception exception) {
               LOGGER.warn("Failed to partially datafix chunk {}", pChunkPos, exception);
               return StructureCheckResult.CHUNK_LOAD_NEEDED;
            }

            Object2IntMap<Structure> object2intmap = this.loadStructures(compoundtag1);
            if (object2intmap == null) {
               return null;
            } else {
               this.storeFullResults(pPackedChunkPos, object2intmap);
               return this.checkStructureInfo(object2intmap, pStructure, pSkipKnownStructures);
            }
         }
      }
   }

   @Nullable
   private Object2IntMap<Structure> loadStructures(CompoundTag pTag) {
      if (!pTag.contains("structures", 10)) {
         return null;
      } else {
         CompoundTag compoundtag = pTag.getCompound("structures");
         if (!compoundtag.contains("starts", 10)) {
            return null;
         } else {
            CompoundTag compoundtag1 = compoundtag.getCompound("starts");
            if (compoundtag1.isEmpty()) {
               return Object2IntMaps.emptyMap();
            } else {
               Object2IntMap<Structure> object2intmap = new Object2IntOpenHashMap<>();
               Registry<Structure> registry = this.registryAccess.registryOrThrow(Registries.STRUCTURE);

               for(String s : compoundtag1.getAllKeys()) {
                  ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
                  if (resourcelocation != null) {
                     Structure structure = registry.get(resourcelocation);
                     if (structure != null) {
                        CompoundTag compoundtag2 = compoundtag1.getCompound(s);
                        if (!compoundtag2.isEmpty()) {
                           String s1 = compoundtag2.getString("id");
                           if (!"INVALID".equals(s1)) {
                              int i = compoundtag2.getInt("references");
                              object2intmap.put(structure, i);
                           }
                        }
                     }
                  }
               }

               return object2intmap;
            }
         }
      }
   }

   private static Object2IntMap<Structure> deduplicateEmptyMap(Object2IntMap<Structure> pMap) {
      return pMap.isEmpty() ? Object2IntMaps.emptyMap() : pMap;
   }

   private StructureCheckResult checkStructureInfo(Object2IntMap<Structure> pStructureChunks, Structure pStructure, boolean pSkipKnownStructures) {
      int i = pStructureChunks.getOrDefault(pStructure, -1);
      return i == -1 || pSkipKnownStructures && i != 0 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.START_PRESENT;
   }

   public void onStructureLoad(ChunkPos pChunkPos, Map<Structure, StructureStart> pChunkStarts) {
      long i = pChunkPos.toLong();
      Object2IntMap<Structure> object2intmap = new Object2IntOpenHashMap<>();
      pChunkStarts.forEach((p_226749_, p_226750_) -> {
         if (p_226750_.isValid()) {
            object2intmap.put(p_226749_, p_226750_.getReferences());
         }

      });
      this.storeFullResults(i, object2intmap);
   }

   private void storeFullResults(long pChunkPos, Object2IntMap<Structure> pStructureChunks) {
      this.loadedChunks.put(pChunkPos, deduplicateEmptyMap(pStructureChunks));
      this.featureChecks.values().forEach((p_209956_) -> {
         p_209956_.remove(pChunkPos);
      });
   }

   public void incrementReference(ChunkPos pPos, Structure pStructure) {
      this.loadedChunks.compute(pPos.toLong(), (p_226745_, p_226746_) -> {
         if (p_226746_ == null || p_226746_.isEmpty()) {
            p_226746_ = new Object2IntOpenHashMap<>();
         }

         p_226746_.computeInt(pStructure, (p_226741_, p_226742_) -> {
            return p_226742_ == null ? 1 : p_226742_ + 1;
         });
         return p_226746_;
      });
   }
}