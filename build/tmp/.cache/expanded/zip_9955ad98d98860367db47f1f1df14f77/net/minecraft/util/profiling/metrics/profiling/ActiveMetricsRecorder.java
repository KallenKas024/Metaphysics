package net.minecraft.util.profiling.metrics.profiling;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.profiling.metrics.storage.RecordedDeviation;

public class ActiveMetricsRecorder implements MetricsRecorder {
   public static final int PROFILING_MAX_DURATION_SECONDS = 10;
   @Nullable
   private static Consumer<Path> globalOnReportFinished = null;
   private final Map<MetricSampler, List<RecordedDeviation>> deviationsBySampler = new Object2ObjectOpenHashMap<>();
   private final ContinuousProfiler taskProfiler;
   private final Executor ioExecutor;
   private final MetricsPersister metricsPersister;
   private final Consumer<ProfileResults> onProfilingEnd;
   private final Consumer<Path> onReportFinished;
   private final MetricsSamplerProvider metricsSamplerProvider;
   private final LongSupplier wallTimeSource;
   private final long deadlineNano;
   private int currentTick;
   private ProfileCollector singleTickProfiler;
   private volatile boolean killSwitch;
   private Set<MetricSampler> thisTickSamplers = ImmutableSet.of();

   private ActiveMetricsRecorder(MetricsSamplerProvider pMetricsSamplerProvider, LongSupplier pWallTimeSource, Executor pIoExecutor, MetricsPersister pMetricPersister, Consumer<ProfileResults> pOnProfilerEnd, Consumer<Path> pOnReportFinished) {
      this.metricsSamplerProvider = pMetricsSamplerProvider;
      this.wallTimeSource = pWallTimeSource;
      this.taskProfiler = new ContinuousProfiler(pWallTimeSource, () -> {
         return this.currentTick;
      });
      this.ioExecutor = pIoExecutor;
      this.metricsPersister = pMetricPersister;
      this.onProfilingEnd = pOnProfilerEnd;
      this.onReportFinished = globalOnReportFinished == null ? pOnReportFinished : pOnReportFinished.andThen(globalOnReportFinished);
      this.deadlineNano = pWallTimeSource.getAsLong() + TimeUnit.NANOSECONDS.convert(10L, TimeUnit.SECONDS);
      this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> {
         return this.currentTick;
      }, false);
      this.taskProfiler.enable();
   }

   public static ActiveMetricsRecorder createStarted(MetricsSamplerProvider pMetricsSamplerProvider, LongSupplier pWallTimeSource, Executor pIoExecutor, MetricsPersister pMetricsPersister, Consumer<ProfileResults> pOnProfilerEnd, Consumer<Path> pOnReportFinished) {
      return new ActiveMetricsRecorder(pMetricsSamplerProvider, pWallTimeSource, pIoExecutor, pMetricsPersister, pOnProfilerEnd, pOnReportFinished);
   }

   public synchronized void end() {
      if (this.isRecording()) {
         this.killSwitch = true;
      }
   }

   public synchronized void cancel() {
      if (this.isRecording()) {
         this.singleTickProfiler = InactiveProfiler.INSTANCE;
         this.onProfilingEnd.accept(EmptyProfileResults.EMPTY);
         this.cleanup(this.thisTickSamplers);
      }
   }

   public void startTick() {
      this.verifyStarted();
      this.thisTickSamplers = this.metricsSamplerProvider.samplers(() -> {
         return this.singleTickProfiler;
      });

      for(MetricSampler metricsampler : this.thisTickSamplers) {
         metricsampler.onStartTick();
      }

      ++this.currentTick;
   }

   public void endTick() {
      this.verifyStarted();
      if (this.currentTick != 0) {
         for(MetricSampler metricsampler : this.thisTickSamplers) {
            metricsampler.onEndTick(this.currentTick);
            if (metricsampler.triggersThreshold()) {
               RecordedDeviation recordeddeviation = new RecordedDeviation(Instant.now(), this.currentTick, this.singleTickProfiler.getResults());
               this.deviationsBySampler.computeIfAbsent(metricsampler, (p_146131_) -> {
                  return Lists.newArrayList();
               }).add(recordeddeviation);
            }
         }

         if (!this.killSwitch && this.wallTimeSource.getAsLong() <= this.deadlineNano) {
            this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> {
               return this.currentTick;
            }, false);
         } else {
            this.killSwitch = false;
            ProfileResults profileresults = this.taskProfiler.getResults();
            this.singleTickProfiler = InactiveProfiler.INSTANCE;
            this.onProfilingEnd.accept(profileresults);
            this.scheduleSaveResults(profileresults);
         }
      }
   }

   public boolean isRecording() {
      return this.taskProfiler.isEnabled();
   }

   public ProfilerFiller getProfiler() {
      return ProfilerFiller.tee(this.taskProfiler.getFiller(), this.singleTickProfiler);
   }

   private void verifyStarted() {
      if (!this.isRecording()) {
         throw new IllegalStateException("Not started!");
      }
   }

   private void scheduleSaveResults(ProfileResults pResults) {
      HashSet<MetricSampler> hashset = new HashSet<>(this.thisTickSamplers);
      this.ioExecutor.execute(() -> {
         Path path = this.metricsPersister.saveReports(hashset, this.deviationsBySampler, pResults);
         this.cleanup(hashset);
         this.onReportFinished.accept(path);
      });
   }

   private void cleanup(Collection<MetricSampler> pSamplers) {
      for(MetricSampler metricsampler : pSamplers) {
         metricsampler.onFinished();
      }

      this.deviationsBySampler.clear();
      this.taskProfiler.disable();
   }

   public static void registerGlobalCompletionCallback(Consumer<Path> pGlobalOnReportFinished) {
      globalOnReportFinished = pGlobalOnReportFinished;
   }
}