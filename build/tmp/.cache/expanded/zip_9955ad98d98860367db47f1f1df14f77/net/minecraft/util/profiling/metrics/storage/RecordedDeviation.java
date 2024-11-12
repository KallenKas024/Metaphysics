package net.minecraft.util.profiling.metrics.storage;

import java.time.Instant;
import net.minecraft.util.profiling.ProfileResults;

public final class RecordedDeviation {
   public final Instant timestamp;
   public final int tick;
   public final ProfileResults profilerResultAtTick;

   public RecordedDeviation(Instant pTimestamp, int pTick, ProfileResults pProfilerResultAtTick) {
      this.timestamp = pTimestamp;
      this.tick = pTick;
      this.profilerResultAtTick = pProfilerResultAtTick;
   }
}