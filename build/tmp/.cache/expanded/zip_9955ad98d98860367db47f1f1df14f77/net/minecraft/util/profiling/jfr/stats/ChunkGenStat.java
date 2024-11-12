package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public record ChunkGenStat(Duration duration, ChunkPos chunkPos, ColumnPos worldPos, ChunkStatus status, String level) implements TimedStat {
   public static ChunkGenStat from(RecordedEvent pEvent) {
      return new ChunkGenStat(pEvent.getDuration(), new ChunkPos(pEvent.getInt("chunkPosX"), pEvent.getInt("chunkPosX")), new ColumnPos(pEvent.getInt("worldPosX"), pEvent.getInt("worldPosZ")), ChunkStatus.byName(pEvent.getString("status")), pEvent.getString("level"));
   }

   public Duration duration() {
      return this.duration;
   }
}