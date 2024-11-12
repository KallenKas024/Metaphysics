package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ChunkStorage implements AutoCloseable {
   public static final int LAST_MONOLYTH_STRUCTURE_DATA_VERSION = 1493;
   private final IOWorker worker;
   protected final DataFixer fixerUpper;
   @Nullable
   private volatile LegacyStructureDataHandler legacyStructureHandler;

   public ChunkStorage(Path pRegionFolder, DataFixer pFixerUpper, boolean pSync) {
      this.fixerUpper = pFixerUpper;
      this.worker = new IOWorker(pRegionFolder, pSync, "chunk");
   }

   public boolean isOldChunkAround(ChunkPos pPos, int pRadius) {
      return this.worker.isOldChunkAround(pPos, pRadius);
   }

   public CompoundTag upgradeChunkTag(ResourceKey<Level> pLevelKey, Supplier<DimensionDataStorage> pStorage, CompoundTag pChunkData, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> pChunkGeneratorKey) {
      int i = getVersion(pChunkData);
      if (i < 1493) {
         pChunkData = DataFixTypes.CHUNK.update(this.fixerUpper, pChunkData, i, 1493);
         if (pChunkData.getCompound("Level").getBoolean("hasLegacyStructureData")) {
            LegacyStructureDataHandler legacystructuredatahandler = this.getLegacyStructureHandler(pLevelKey, pStorage);
            pChunkData = legacystructuredatahandler.updateFromLegacy(pChunkData);
         }
      }

      injectDatafixingContext(pChunkData, pLevelKey, pChunkGeneratorKey);
      pChunkData = DataFixTypes.CHUNK.updateToCurrentVersion(this.fixerUpper, pChunkData, Math.max(1493, i));
      if (i < SharedConstants.getCurrentVersion().getDataVersion().getVersion()) {
         NbtUtils.addCurrentDataVersion(pChunkData);
      }

      pChunkData.remove("__context");
      return pChunkData;
   }

   private LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> pLevel, Supplier<DimensionDataStorage> pStorage) {
      LegacyStructureDataHandler legacystructuredatahandler = this.legacyStructureHandler;
      if (legacystructuredatahandler == null) {
         synchronized(this) {
            legacystructuredatahandler = this.legacyStructureHandler;
            if (legacystructuredatahandler == null) {
               this.legacyStructureHandler = legacystructuredatahandler = LegacyStructureDataHandler.getLegacyStructureHandler(pLevel, pStorage.get());
            }
         }
      }

      return legacystructuredatahandler;
   }

   public static void injectDatafixingContext(CompoundTag pChunkData, ResourceKey<Level> pLevelKey, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> pChunkGeneratorKey) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("dimension", pLevelKey.location().toString());
      pChunkGeneratorKey.ifPresent((p_196917_) -> {
         compoundtag.putString("generator", p_196917_.location().toString());
      });
      pChunkData.put("__context", compoundtag);
   }

   public static int getVersion(CompoundTag pChunkData) {
      return NbtUtils.getDataVersion(pChunkData, -1);
   }

   public CompletableFuture<Optional<CompoundTag>> read(ChunkPos pChunkPos) {
      return this.worker.loadAsync(pChunkPos);
   }

   public void write(ChunkPos pChunkPos, CompoundTag pChunkData) {
      this.worker.store(pChunkPos, pChunkData);
      if (this.legacyStructureHandler != null) {
         this.legacyStructureHandler.removeIndex(pChunkPos.toLong());
      }

   }

   public void flushWorker() {
      this.worker.synchronize(true).join();
   }

   public void close() throws IOException {
      this.worker.close();
   }

   public ChunkScanAccess chunkScanner() {
      return this.worker;
   }
}