package net.minecraft.util.profiling.metrics;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class MetricsRegistry {
   public static final MetricsRegistry INSTANCE = new MetricsRegistry();
   private final WeakHashMap<ProfilerMeasured, Void> measuredInstances = new WeakHashMap<>();

   private MetricsRegistry() {
   }

   public void add(ProfilerMeasured pKey) {
      this.measuredInstances.put(pKey, (Void)null);
   }

   public List<MetricSampler> getRegisteredSamplers() {
      Map<String, List<MetricSampler>> map = this.measuredInstances.keySet().stream().flatMap((p_146079_) -> {
         return p_146079_.profiledMetrics().stream();
      }).collect(Collectors.groupingBy(MetricSampler::getName));
      return aggregateDuplicates(map);
   }

   private static List<MetricSampler> aggregateDuplicates(Map<String, List<MetricSampler>> pSamplers) {
      return pSamplers.entrySet().stream().map((p_146075_) -> {
         String s = p_146075_.getKey();
         List<MetricSampler> list = p_146075_.getValue();
         return (MetricSampler)(list.size() > 1 ? new MetricsRegistry.AggregatedMetricSampler(s, list) : list.get(0));
      }).collect(Collectors.toList());
   }

   static class AggregatedMetricSampler extends MetricSampler {
      private final List<MetricSampler> delegates;

      AggregatedMetricSampler(String pName, List<MetricSampler> pDelegates) {
         super(pName, pDelegates.get(0).getCategory(), () -> {
            return averageValueFromDelegates(pDelegates);
         }, () -> {
            beforeTick(pDelegates);
         }, thresholdTest(pDelegates));
         this.delegates = pDelegates;
      }

      private static MetricSampler.ThresholdTest thresholdTest(List<MetricSampler> pSamplers) {
         return (p_146091_) -> {
            return pSamplers.stream().anyMatch((p_146086_) -> {
               return p_146086_.thresholdTest != null ? p_146086_.thresholdTest.test(p_146091_) : false;
            });
         };
      }

      private static void beforeTick(List<MetricSampler> pSamplers) {
         for(MetricSampler metricsampler : pSamplers) {
            metricsampler.onStartTick();
         }

      }

      private static double averageValueFromDelegates(List<MetricSampler> pSamplers) {
         double d0 = 0.0D;

         for(MetricSampler metricsampler : pSamplers) {
            d0 += metricsampler.getSampler().getAsDouble();
         }

         return d0 / (double)pSamplers.size();
      }

      public boolean equals(@Nullable Object pOther) {
         if (this == pOther) {
            return true;
         } else if (pOther != null && this.getClass() == pOther.getClass()) {
            if (!super.equals(pOther)) {
               return false;
            } else {
               MetricsRegistry.AggregatedMetricSampler metricsregistry$aggregatedmetricsampler = (MetricsRegistry.AggregatedMetricSampler)pOther;
               return this.delegates.equals(metricsregistry$aggregatedmetricsampler.delegates);
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(super.hashCode(), this.delegates);
      }
   }
}