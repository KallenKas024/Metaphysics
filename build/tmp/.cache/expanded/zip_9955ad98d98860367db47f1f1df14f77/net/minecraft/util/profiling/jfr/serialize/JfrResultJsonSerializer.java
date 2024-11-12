package net.minecraft.util.profiling.jfr.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;
import net.minecraft.Util;
import net.minecraft.util.profiling.jfr.Percentiles;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.NetworkPacketSummary;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.ChunkStatus;

public class JfrResultJsonSerializer {
   private static final String BYTES_PER_SECOND = "bytesPerSecond";
   private static final String COUNT = "count";
   private static final String DURATION_NANOS_TOTAL = "durationNanosTotal";
   private static final String TOTAL_BYTES = "totalBytes";
   private static final String COUNT_PER_SECOND = "countPerSecond";
   final Gson gson = (new GsonBuilder()).setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();

   public String format(JfrStatsResult pResult) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("startedEpoch", pResult.recordingStarted().toEpochMilli());
      jsonobject.addProperty("endedEpoch", pResult.recordingEnded().toEpochMilli());
      jsonobject.addProperty("durationMs", pResult.recordingDuration().toMillis());
      Duration duration = pResult.worldCreationDuration();
      if (duration != null) {
         jsonobject.addProperty("worldGenDurationMs", duration.toMillis());
      }

      jsonobject.add("heap", this.heap(pResult.heapSummary()));
      jsonobject.add("cpuPercent", this.cpu(pResult.cpuLoadStats()));
      jsonobject.add("network", this.network(pResult));
      jsonobject.add("fileIO", this.fileIO(pResult));
      jsonobject.add("serverTick", this.serverTicks(pResult.tickTimes()));
      jsonobject.add("threadAllocation", this.threadAllocations(pResult.threadAllocationSummary()));
      jsonobject.add("chunkGen", this.chunkGen(pResult.chunkGenSummary()));
      return this.gson.toJson((JsonElement)jsonobject);
   }

   private JsonElement heap(GcHeapStat.Summary pSummary) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("allocationRateBytesPerSecond", pSummary.allocationRateBytesPerSecond());
      jsonobject.addProperty("gcCount", pSummary.totalGCs());
      jsonobject.addProperty("gcOverHeadPercent", pSummary.gcOverHead());
      jsonobject.addProperty("gcTotalDurationMs", pSummary.gcTotalDuration().toMillis());
      return jsonobject;
   }

   private JsonElement chunkGen(List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> pSummary) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("durationNanosTotal", pSummary.stream().mapToDouble((p_185567_) -> {
         return (double)p_185567_.getSecond().totalDuration().toNanos();
      }).sum());
      JsonArray jsonarray = Util.make(new JsonArray(), (p_185558_) -> {
         jsonobject.add("status", p_185558_);
      });

      for(Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> pair : pSummary) {
         TimedStatSummary<ChunkGenStat> timedstatsummary = pair.getSecond();
         JsonObject jsonobject1 = Util.make(new JsonObject(), jsonarray::add);
         jsonobject1.addProperty("state", pair.getFirst().toString());
         jsonobject1.addProperty("count", timedstatsummary.count());
         jsonobject1.addProperty("durationNanosTotal", timedstatsummary.totalDuration().toNanos());
         jsonobject1.addProperty("durationNanosAvg", timedstatsummary.totalDuration().toNanos() / (long)timedstatsummary.count());
         JsonObject jsonobject2 = Util.make(new JsonObject(), (p_185561_) -> {
            jsonobject1.add("durationNanosPercentiles", p_185561_);
         });
         timedstatsummary.percentilesNanos().forEach((p_185584_, p_185585_) -> {
            jsonobject2.addProperty("p" + p_185584_, p_185585_);
         });
         Function<ChunkGenStat, JsonElement> function = (p_185538_) -> {
            JsonObject jsonobject3 = new JsonObject();
            jsonobject3.addProperty("durationNanos", p_185538_.duration().toNanos());
            jsonobject3.addProperty("level", p_185538_.level());
            jsonobject3.addProperty("chunkPosX", p_185538_.chunkPos().x);
            jsonobject3.addProperty("chunkPosZ", p_185538_.chunkPos().z);
            jsonobject3.addProperty("worldPosX", p_185538_.worldPos().x());
            jsonobject3.addProperty("worldPosZ", p_185538_.worldPos().z());
            return jsonobject3;
         };
         jsonobject1.add("fastest", function.apply(timedstatsummary.fastest()));
         jsonobject1.add("slowest", function.apply(timedstatsummary.slowest()));
         jsonobject1.add("secondSlowest", (JsonElement)(timedstatsummary.secondSlowest() != null ? function.apply(timedstatsummary.secondSlowest()) : JsonNull.INSTANCE));
      }

      return jsonobject;
   }

   private JsonElement threadAllocations(ThreadAllocationStat.Summary pSummary) {
      JsonArray jsonarray = new JsonArray();
      pSummary.allocationsPerSecondByThread().forEach((p_185554_, p_185555_) -> {
         jsonarray.add(Util.make(new JsonObject(), (p_185571_) -> {
            p_185571_.addProperty("thread", p_185554_);
            p_185571_.addProperty("bytesPerSecond", p_185555_);
         }));
      });
      return jsonarray;
   }

   private JsonElement serverTicks(List<TickTimeStat> pStats) {
      if (pStats.isEmpty()) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         double[] adouble = pStats.stream().mapToDouble((p_185548_) -> {
            return (double)p_185548_.currentAverage().toNanos() / 1000000.0D;
         }).toArray();
         DoubleSummaryStatistics doublesummarystatistics = DoubleStream.of(adouble).summaryStatistics();
         jsonobject.addProperty("minMs", doublesummarystatistics.getMin());
         jsonobject.addProperty("averageMs", doublesummarystatistics.getAverage());
         jsonobject.addProperty("maxMs", doublesummarystatistics.getMax());
         Map<Integer, Double> map = Percentiles.evaluate(adouble);
         map.forEach((p_185564_, p_185565_) -> {
            jsonobject.addProperty("p" + p_185564_, p_185565_);
         });
         return jsonobject;
      }
   }

   private JsonElement fileIO(JfrStatsResult pResult) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("write", this.fileIoSummary(pResult.fileWrites()));
      jsonobject.add("read", this.fileIoSummary(pResult.fileReads()));
      return jsonobject;
   }

   private JsonElement fileIoSummary(FileIOStat.Summary pSummary) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("totalBytes", pSummary.totalBytes());
      jsonobject.addProperty("count", pSummary.counts());
      jsonobject.addProperty("bytesPerSecond", pSummary.bytesPerSecond());
      jsonobject.addProperty("countPerSecond", pSummary.countsPerSecond());
      JsonArray jsonarray = new JsonArray();
      jsonobject.add("topContributors", jsonarray);
      pSummary.topTenContributorsByTotalBytes().forEach((p_185581_) -> {
         JsonObject jsonobject1 = new JsonObject();
         jsonarray.add(jsonobject1);
         jsonobject1.addProperty("path", p_185581_.getFirst());
         jsonobject1.addProperty("totalBytes", p_185581_.getSecond());
      });
      return jsonobject;
   }

   private JsonElement network(JfrStatsResult pResult) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("sent", this.packets(pResult.sentPacketsSummary()));
      jsonobject.add("received", this.packets(pResult.receivedPacketsSummary()));
      return jsonobject;
   }

   private JsonElement packets(NetworkPacketSummary pSummary) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("totalBytes", pSummary.getTotalSize());
      jsonobject.addProperty("count", pSummary.getTotalCount());
      jsonobject.addProperty("bytesPerSecond", pSummary.getSizePerSecond());
      jsonobject.addProperty("countPerSecond", pSummary.getCountsPerSecond());
      JsonArray jsonarray = new JsonArray();
      jsonobject.add("topContributors", jsonarray);
      pSummary.largestSizeContributors().forEach((p_185551_) -> {
         JsonObject jsonobject1 = new JsonObject();
         jsonarray.add(jsonobject1);
         NetworkPacketSummary.PacketIdentification networkpacketsummary$packetidentification = p_185551_.getFirst();
         NetworkPacketSummary.PacketCountAndSize networkpacketsummary$packetcountandsize = p_185551_.getSecond();
         jsonobject1.addProperty("protocolId", networkpacketsummary$packetidentification.protocolId());
         jsonobject1.addProperty("packetId", networkpacketsummary$packetidentification.packetId());
         jsonobject1.addProperty("packetName", networkpacketsummary$packetidentification.packetName());
         jsonobject1.addProperty("totalBytes", networkpacketsummary$packetcountandsize.totalSize());
         jsonobject1.addProperty("count", networkpacketsummary$packetcountandsize.totalCount());
      });
      return jsonobject;
   }

   private JsonElement cpu(List<CpuLoadStat> pStats) {
      JsonObject jsonobject = new JsonObject();
      BiFunction<List<CpuLoadStat>, ToDoubleFunction<CpuLoadStat>, JsonObject> bifunction = (p_185575_, p_185576_) -> {
         JsonObject jsonobject1 = new JsonObject();
         DoubleSummaryStatistics doublesummarystatistics = p_185575_.stream().mapToDouble(p_185576_).summaryStatistics();
         jsonobject1.addProperty("min", doublesummarystatistics.getMin());
         jsonobject1.addProperty("average", doublesummarystatistics.getAverage());
         jsonobject1.addProperty("max", doublesummarystatistics.getMax());
         return jsonobject1;
      };
      jsonobject.add("jvm", bifunction.apply(pStats, CpuLoadStat::jvm));
      jsonobject.add("userJvm", bifunction.apply(pStats, CpuLoadStat::userJvm));
      jsonobject.add("system", bifunction.apply(pStats, CpuLoadStat::system));
      return jsonobject;
   }
}