package net.minecraft.server.level.progress;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public class StoringChunkProgressListener implements ChunkProgressListener {
   private final LoggerChunkProgressListener delegate;
   private final Long2ObjectOpenHashMap<ChunkStatus> statuses;
   private ChunkPos spawnPos = new ChunkPos(0, 0);
   private final int fullDiameter;
   private final int radius;
   private final int diameter;
   private boolean started;

   public StoringChunkProgressListener(int pRadius) {
      this.delegate = new LoggerChunkProgressListener(pRadius);
      this.fullDiameter = pRadius * 2 + 1;
      this.radius = pRadius + ChunkStatus.maxDistance();
      this.diameter = this.radius * 2 + 1;
      this.statuses = new Long2ObjectOpenHashMap<>();
   }

   public void updateSpawnPos(ChunkPos pCenter) {
      if (this.started) {
         this.delegate.updateSpawnPos(pCenter);
         this.spawnPos = pCenter;
      }
   }

   public void onStatusChange(ChunkPos pChunkPosition, @Nullable ChunkStatus pNewStatus) {
      if (this.started) {
         this.delegate.onStatusChange(pChunkPosition, pNewStatus);
         if (pNewStatus == null) {
            this.statuses.remove(pChunkPosition.toLong());
         } else {
            this.statuses.put(pChunkPosition.toLong(), pNewStatus);
         }

      }
   }

   public void start() {
      this.started = true;
      this.statuses.clear();
      this.delegate.start();
   }

   public void stop() {
      this.started = false;
      this.delegate.stop();
   }

   public int getFullDiameter() {
      return this.fullDiameter;
   }

   public int getDiameter() {
      return this.diameter;
   }

   public int getProgress() {
      return this.delegate.getProgress();
   }

   @Nullable
   public ChunkStatus getStatus(int pX, int pZ) {
      return this.statuses.get(ChunkPos.asLong(pX + this.spawnPos.x - this.radius, pZ + this.spawnPos.z - this.radius));
   }
}