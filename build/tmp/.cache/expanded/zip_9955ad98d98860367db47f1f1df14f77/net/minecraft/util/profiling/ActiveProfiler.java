package net.minecraft.util.profiling;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

public class ActiveProfiler implements ProfileCollector {
   private static final long WARNING_TIME_NANOS = Duration.ofMillis(100L).toNanos();
   private static final Logger LOGGER = LogUtils.getLogger();
   private final List<String> paths = Lists.newArrayList();
   private final LongList startTimes = new LongArrayList();
   private final Map<String, ActiveProfiler.PathEntry> entries = Maps.newHashMap();
   private final IntSupplier getTickTime;
   private final LongSupplier getRealTime;
   private final long startTimeNano;
   private final int startTimeTicks;
   private String path = "";
   private boolean started;
   @Nullable
   private ActiveProfiler.PathEntry currentEntry;
   private final boolean warn;
   private final Set<Pair<String, MetricCategory>> chartedPaths = new ObjectArraySet<>();

   public ActiveProfiler(LongSupplier pStartTimeNano, IntSupplier pStartTimeTicks, boolean pWarn) {
      this.startTimeNano = pStartTimeNano.getAsLong();
      this.getRealTime = pStartTimeNano;
      this.startTimeTicks = pStartTimeTicks.getAsInt();
      this.getTickTime = pStartTimeTicks;
      this.warn = pWarn;
   }

   public void startTick() {
      if (this.started) {
         LOGGER.error("Profiler tick already started - missing endTick()?");
      } else {
         this.started = true;
         this.path = "";
         this.paths.clear();
         this.push("root");
      }
   }

   public void endTick() {
      if (!this.started) {
         LOGGER.error("Profiler tick already ended - missing startTick()?");
      } else {
         this.pop();
         this.started = false;
         if (!this.path.isEmpty()) {
            LOGGER.error("Profiler tick ended before path was fully popped (remainder: '{}'). Mismatched push/pop?", LogUtils.defer(() -> {
               return ProfileResults.demanglePath(this.path);
            }));
         }

      }
   }

   /**
    * Start section
    */
   public void push(String pName) {
      if (!this.started) {
         LOGGER.error("Cannot push '{}' to profiler if profiler tick hasn't started - missing startTick()?", (Object)pName);
      } else {
         if (!this.path.isEmpty()) {
            this.path = this.path + "\u001e";
         }

         this.path = this.path + pName;
         this.paths.add(this.path);
         this.startTimes.add(Util.getNanos());
         this.currentEntry = null;
      }
   }

   public void push(Supplier<String> pNameSupplier) {
      this.push(pNameSupplier.get());
   }

   public void markForCharting(MetricCategory pCategory) {
      this.chartedPaths.add(Pair.of(this.path, pCategory));
   }

   /**
    * End section
    */
   public void pop() {
      if (!this.started) {
         LOGGER.error("Cannot pop from profiler if profiler tick hasn't started - missing startTick()?");
      } else if (this.startTimes.isEmpty()) {
         LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
      } else {
         long i = Util.getNanos();
         long j = this.startTimes.removeLong(this.startTimes.size() - 1);
         this.paths.remove(this.paths.size() - 1);
         long k = i - j;
         ActiveProfiler.PathEntry activeprofiler$pathentry = this.getCurrentEntry();
         activeprofiler$pathentry.accumulatedDuration += k;
         ++activeprofiler$pathentry.count;
         activeprofiler$pathentry.maxDuration = Math.max(activeprofiler$pathentry.maxDuration, k);
         activeprofiler$pathentry.minDuration = Math.min(activeprofiler$pathentry.minDuration, k);
         if (this.warn && k > WARNING_TIME_NANOS) {
            LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", LogUtils.defer(() -> {
               return ProfileResults.demanglePath(this.path);
            }), LogUtils.defer(() -> {
               return (double)k / 1000000.0D;
            }));
         }

         this.path = this.paths.isEmpty() ? "" : this.paths.get(this.paths.size() - 1);
         this.currentEntry = null;
      }
   }

   public void popPush(String pName) {
      this.pop();
      this.push(pName);
   }

   public void popPush(Supplier<String> pNameSupplier) {
      this.pop();
      this.push(pNameSupplier);
   }

   private ActiveProfiler.PathEntry getCurrentEntry() {
      if (this.currentEntry == null) {
         this.currentEntry = this.entries.computeIfAbsent(this.path, (p_18405_) -> {
            return new ActiveProfiler.PathEntry();
         });
      }

      return this.currentEntry;
   }

   public void incrementCounter(String pCounterName, int pIncrement) {
      this.getCurrentEntry().counters.addTo(pCounterName, (long)pIncrement);
   }

   public void incrementCounter(Supplier<String> pCounterNameSupplier, int pIncrement) {
      this.getCurrentEntry().counters.addTo(pCounterNameSupplier.get(), (long)pIncrement);
   }

   public ProfileResults getResults() {
      return new FilledProfileResults(this.entries, this.startTimeNano, this.startTimeTicks, this.getRealTime.getAsLong(), this.getTickTime.getAsInt());
   }

   @Nullable
   public ActiveProfiler.PathEntry getEntry(String pEntryId) {
      return this.entries.get(pEntryId);
   }

   public Set<Pair<String, MetricCategory>> getChartedPaths() {
      return this.chartedPaths;
   }

   public static class PathEntry implements ProfilerPathEntry {
      long maxDuration = Long.MIN_VALUE;
      long minDuration = Long.MAX_VALUE;
      long accumulatedDuration;
      long count;
      final Object2LongOpenHashMap<String> counters = new Object2LongOpenHashMap<>();

      public long getDuration() {
         return this.accumulatedDuration;
      }

      public long getMaxDuration() {
         return this.maxDuration;
      }

      public long getCount() {
         return this.count;
      }

      public Object2LongMap<String> getCounters() {
         return Object2LongMaps.unmodifiable(this.counters);
      }
   }
}