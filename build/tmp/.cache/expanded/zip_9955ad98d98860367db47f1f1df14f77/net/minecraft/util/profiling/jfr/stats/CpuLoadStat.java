package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record CpuLoadStat(double jvm, double userJvm, double system) {
   public static CpuLoadStat from(RecordedEvent pEvent) {
      return new CpuLoadStat((double)pEvent.getFloat("jvmSystem"), (double)pEvent.getFloat("jvmUser"), (double)pEvent.getFloat("machineTotal"));
   }
}