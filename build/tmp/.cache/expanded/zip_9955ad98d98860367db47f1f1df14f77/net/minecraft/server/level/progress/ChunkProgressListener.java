package net.minecraft.server.level.progress;

import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public interface ChunkProgressListener {
   void updateSpawnPos(ChunkPos pCenter);

   void onStatusChange(ChunkPos pChunkPosition, @Nullable ChunkStatus pNewStatus);

   void start();

   void stop();
}