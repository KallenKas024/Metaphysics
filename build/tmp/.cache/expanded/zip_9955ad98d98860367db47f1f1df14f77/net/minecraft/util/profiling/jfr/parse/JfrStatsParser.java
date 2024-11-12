package net.minecraft.util.profiling.jfr.parse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.NetworkPacketSummary;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;

public class JfrStatsParser {
   private Instant recordingStarted = Instant.EPOCH;
   private Instant recordingEnded = Instant.EPOCH;
   private final List<ChunkGenStat> chunkGenStats = Lists.newArrayList();
   private final List<CpuLoadStat> cpuLoadStat = Lists.newArrayList();
   private final Map<NetworkPacketSummary.PacketIdentification, JfrStatsParser.MutableCountAndSize> receivedPackets = Maps.newHashMap();
   private final Map<NetworkPacketSummary.PacketIdentification, JfrStatsParser.MutableCountAndSize> sentPackets = Maps.newHashMap();
   private final List<FileIOStat> fileWrites = Lists.newArrayList();
   private final List<FileIOStat> fileReads = Lists.newArrayList();
   private int garbageCollections;
   private Duration gcTotalDuration = Duration.ZERO;
   private final List<GcHeapStat> gcHeapStats = Lists.newArrayList();
   private final List<ThreadAllocationStat> threadAllocationStats = Lists.newArrayList();
   private final List<TickTimeStat> tickTimes = Lists.newArrayList();
   @Nullable
   private Duration worldCreationDuration = null;

   private JfrStatsParser(Stream<RecordedEvent> pEvents) {
      this.capture(pEvents);
   }

   public static JfrStatsResult parse(Path pFile) {
      try (final RecordingFile recordingfile = new RecordingFile(pFile)) {
         Iterator<RecordedEvent> iterator = new Iterator<RecordedEvent>() {
            public boolean hasNext() {
               return recordingfile.hasMoreEvents();
            }

            public RecordedEvent next() {
               if (!this.hasNext()) {
                  throw new NoSuchElementException();
               } else {
                  try {
                     return recordingfile.readEvent();
                  } catch (IOException ioexception1) {
                     throw new UncheckedIOException(ioexception1);
                  }
               }
            }
         };
         Stream<RecordedEvent> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 1297), false);
         return (new JfrStatsParser(stream)).results();
      } catch (IOException ioexception) {
         throw new UncheckedIOException(ioexception);
      }
   }

   private JfrStatsResult results() {
      Duration duration = Duration.between(this.recordingStarted, this.recordingEnded);
      return new JfrStatsResult(this.recordingStarted, this.recordingEnded, duration, this.worldCreationDuration, this.tickTimes, this.cpuLoadStat, GcHeapStat.summary(duration, this.gcHeapStats, this.gcTotalDuration, this.garbageCollections), ThreadAllocationStat.summary(this.threadAllocationStats), collectPacketStats(duration, this.receivedPackets), collectPacketStats(duration, this.sentPackets), FileIOStat.summary(duration, this.fileWrites), FileIOStat.summary(duration, this.fileReads), this.chunkGenStats);
   }

   private void capture(Stream<RecordedEvent> pEvents) {
      pEvents.forEach((p_185457_) -> {
         if (p_185457_.getEndTime().isAfter(this.recordingEnded) || this.recordingEnded.equals(Instant.EPOCH)) {
            this.recordingEnded = p_185457_.getEndTime();
         }

         if (p_185457_.getStartTime().isBefore(this.recordingStarted) || this.recordingStarted.equals(Instant.EPOCH)) {
            this.recordingStarted = p_185457_.getStartTime();
         }

         switch (p_185457_.getEventType().getName()) {
            case "minecraft.ChunkGeneration":
               this.chunkGenStats.add(ChunkGenStat.from(p_185457_));
               break;
            case "minecraft.LoadWorld":
               this.worldCreationDuration = p_185457_.getDuration();
               break;
            case "minecraft.ServerTickTime":
               this.tickTimes.add(TickTimeStat.from(p_185457_));
               break;
            case "minecraft.PacketReceived":
               this.incrementPacket(p_185457_, p_185457_.getInt("bytes"), this.receivedPackets);
               break;
            case "minecraft.PacketSent":
               this.incrementPacket(p_185457_, p_185457_.getInt("bytes"), this.sentPackets);
               break;
            case "jdk.ThreadAllocationStatistics":
               this.threadAllocationStats.add(ThreadAllocationStat.from(p_185457_));
               break;
            case "jdk.GCHeapSummary":
               this.gcHeapStats.add(GcHeapStat.from(p_185457_));
               break;
            case "jdk.CPULoad":
               this.cpuLoadStat.add(CpuLoadStat.from(p_185457_));
               break;
            case "jdk.FileWrite":
               this.appendFileIO(p_185457_, this.fileWrites, "bytesWritten");
               break;
            case "jdk.FileRead":
               this.appendFileIO(p_185457_, this.fileReads, "bytesRead");
               break;
            case "jdk.GarbageCollection":
               ++this.garbageCollections;
               this.gcTotalDuration = this.gcTotalDuration.plus(p_185457_.getDuration());
         }

      });
   }

   private void incrementPacket(RecordedEvent pEvent, int pIncrement, Map<NetworkPacketSummary.PacketIdentification, JfrStatsParser.MutableCountAndSize> pPackets) {
      pPackets.computeIfAbsent(NetworkPacketSummary.PacketIdentification.from(pEvent), (p_185446_) -> {
         return new JfrStatsParser.MutableCountAndSize();
      }).increment(pIncrement);
   }

   private void appendFileIO(RecordedEvent pEvent, List<FileIOStat> pStats, String pId) {
      pStats.add(new FileIOStat(pEvent.getDuration(), pEvent.getString("path"), pEvent.getLong(pId)));
   }

   private static NetworkPacketSummary collectPacketStats(Duration pRecordingDuration, Map<NetworkPacketSummary.PacketIdentification, JfrStatsParser.MutableCountAndSize> pPackets) {
      List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> list = pPackets.entrySet().stream().map((p_185453_) -> {
         return Pair.of(p_185453_.getKey(), p_185453_.getValue().toCountAndSize());
      }).toList();
      return new NetworkPacketSummary(pRecordingDuration, list);
   }

   public static final class MutableCountAndSize {
      private long count;
      private long totalSize;

      public void increment(int pIncrement) {
         this.totalSize += (long)pIncrement;
         ++this.count;
      }

      public NetworkPacketSummary.PacketCountAndSize toCountAndSize() {
         return new NetworkPacketSummary.PacketCountAndSize(this.count, this.totalSize);
      }
   }
}