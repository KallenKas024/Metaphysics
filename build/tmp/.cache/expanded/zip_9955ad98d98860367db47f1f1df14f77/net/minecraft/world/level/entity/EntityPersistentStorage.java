package net.minecraft.world.level.entity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import net.minecraft.world.level.ChunkPos;

public interface EntityPersistentStorage<T> extends AutoCloseable {
   CompletableFuture<ChunkEntities<T>> loadEntities(ChunkPos pPos);

   void storeEntities(ChunkEntities<T> pEntities);

   void flush(boolean pSynchronize);

   default void close() throws IOException {
   }
}