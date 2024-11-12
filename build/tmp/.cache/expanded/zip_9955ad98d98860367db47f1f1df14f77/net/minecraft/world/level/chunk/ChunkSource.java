package net.minecraft.world.level.chunk;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public abstract class ChunkSource implements LightChunkGetter, AutoCloseable {
   @Nullable
   public LevelChunk getChunk(int pChunkX, int pChunkZ, boolean pLoad) {
      return (LevelChunk)this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL, pLoad);
   }

   @Nullable
   public LevelChunk getChunkNow(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, false);
   }

   @Nullable
   public LightChunk getChunkForLighting(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, ChunkStatus.EMPTY, false);
   }

   /**
    * @return {@code true} if a chunk is loaded at the provided position, without forcing a chunk load.
    */
   public boolean hasChunk(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL, false) != null;
   }

   /**
    * Gets the chunk at the provided position, if it exists.
    * Note: This method <strong>can deadlock</strong> when called from within an existing chunk load, as it will be
    * stuck waiting for the current chunk to load!
    * @param pLoad If this should force a chunk load. When {@code false}, this will return null if the chunk is not
    * loaded.
    */
   @Nullable
   public abstract ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad);

   public abstract void tick(BooleanSupplier pHasTimeLeft, boolean pTickChunks);

   /**
    * @return A human-readable string representing data about this chunk source.
    */
   public abstract String gatherStats();

   public abstract int getLoadedChunksCount();

   public void close() throws IOException {
   }

   public abstract LevelLightEngine getLightEngine();

   public void setSpawnSettings(boolean pHostile, boolean pPeaceful) {
   }

   public void updateChunkForced(ChunkPos pPos, boolean pAdd) {
   }
}