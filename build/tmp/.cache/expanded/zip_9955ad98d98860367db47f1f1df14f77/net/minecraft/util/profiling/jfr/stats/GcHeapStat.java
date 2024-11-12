package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public record GcHeapStat(Instant timestamp, long heapUsed, GcHeapStat.Timing timing) {
   public static GcHeapStat from(RecordedEvent pEvent) {
      return new GcHeapStat(pEvent.getStartTime(), pEvent.getLong("heapUsed"), pEvent.getString("when").equalsIgnoreCase("before gc") ? GcHeapStat.Timing.BEFORE_GC : GcHeapStat.Timing.AFTER_GC);
   }

   public static GcHeapStat.Summary summary(Duration pDuration, List<GcHeapStat> pStats, Duration pGcTotalDuration, int pTotalGCs) {
      return new GcHeapStat.Summary(pDuration, pGcTotalDuration, pTotalGCs, calculateAllocationRatePerSecond(pStats));
   }

   private static double calculateAllocationRatePerSecond(List<GcHeapStat> pStats) {
      long i = 0L;
      Map<GcHeapStat.Timing, List<GcHeapStat>> map = pStats.stream().collect(Collectors.groupingBy((p_185689_) -> {
         return p_185689_.timing;
      }));
      List<GcHeapStat> list = map.get(GcHeapStat.Timing.BEFORE_GC);
      List<GcHeapStat> list1 = map.get(GcHeapStat.Timing.AFTER_GC);

      for(int j = 1; j < list.size(); ++j) {
         GcHeapStat gcheapstat = list.get(j);
         GcHeapStat gcheapstat1 = list1.get(j - 1);
         i += gcheapstat.heapUsed - gcheapstat1.heapUsed;
      }

      Duration duration = Duration.between((pStats.get(1)).timestamp, (pStats.get(pStats.size() - 1)).timestamp);
      return (double)i / (double)duration.getSeconds();
   }

   public static record Summary(Duration duration, Duration gcTotalDuration, int totalGCs, double allocationRateBytesPerSecond) {
      public float gcOverHead() {
         return (float)this.gcTotalDuration.toMillis() / (float)this.duration.toMillis();
      }
   }

   static enum Timing {
      BEFORE_GC,
      AFTER_GC;
   }
}