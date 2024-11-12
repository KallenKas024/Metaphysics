package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import jdk.jfr.consumer.RecordedEvent;

public record TickTimeStat(Instant timestamp, Duration currentAverage) {
   public static TickTimeStat from(RecordedEvent pEvent) {
      return new TickTimeStat(pEvent.getStartTime(), pEvent.getDuration("averageTickDuration"));
   }
}