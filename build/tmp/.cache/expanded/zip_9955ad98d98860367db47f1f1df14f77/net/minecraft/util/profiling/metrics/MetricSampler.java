package net.minecraft.util.profiling.metrics;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;

public class MetricSampler {
   private final String name;
   private final MetricCategory category;
   private final DoubleSupplier sampler;
   private final ByteBuf ticks;
   private final ByteBuf values;
   private volatile boolean isRunning;
   @Nullable
   private final Runnable beforeTick;
   @Nullable
   final MetricSampler.ThresholdTest thresholdTest;
   private double currentValue;

   protected MetricSampler(String pName, MetricCategory pCategory, DoubleSupplier pSampler, @Nullable Runnable pBeforeTick, @Nullable MetricSampler.ThresholdTest pThresholdTest) {
      this.name = pName;
      this.category = pCategory;
      this.beforeTick = pBeforeTick;
      this.sampler = pSampler;
      this.thresholdTest = pThresholdTest;
      this.values = ByteBufAllocator.DEFAULT.buffer();
      this.ticks = ByteBufAllocator.DEFAULT.buffer();
      this.isRunning = true;
   }

   public static MetricSampler create(String pName, MetricCategory pCategory, DoubleSupplier pSampler) {
      return new MetricSampler(pName, pCategory, pSampler, (Runnable)null, (MetricSampler.ThresholdTest)null);
   }

   public static <T> MetricSampler create(String pName, MetricCategory pCategory, T pContext, ToDoubleFunction<T> pSampler) {
      return builder(pName, pCategory, pSampler, pContext).build();
   }

   public static <T> MetricSampler.MetricSamplerBuilder<T> builder(String pName, MetricCategory pCategory, ToDoubleFunction<T> pSampler, T pContext) {
      return new MetricSampler.MetricSamplerBuilder<>(pName, pCategory, pSampler, pContext);
   }

   public void onStartTick() {
      if (!this.isRunning) {
         throw new IllegalStateException("Not running");
      } else {
         if (this.beforeTick != null) {
            this.beforeTick.run();
         }

      }
   }

   public void onEndTick(int pTickTime) {
      this.verifyRunning();
      this.currentValue = this.sampler.getAsDouble();
      this.values.writeDouble(this.currentValue);
      this.ticks.writeInt(pTickTime);
   }

   public void onFinished() {
      this.verifyRunning();
      this.values.release();
      this.ticks.release();
      this.isRunning = false;
   }

   private void verifyRunning() {
      if (!this.isRunning) {
         throw new IllegalStateException(String.format(Locale.ROOT, "Sampler for metric %s not started!", this.name));
      }
   }

   DoubleSupplier getSampler() {
      return this.sampler;
   }

   public String getName() {
      return this.name;
   }

   public MetricCategory getCategory() {
      return this.category;
   }

   public MetricSampler.SamplerResult result() {
      Int2DoubleMap int2doublemap = new Int2DoubleOpenHashMap();
      int i = Integer.MIN_VALUE;

      int j;
      int k;
      for(j = Integer.MIN_VALUE; this.values.isReadable(8); j = k) {
         k = this.ticks.readInt();
         if (i == Integer.MIN_VALUE) {
            i = k;
         }

         int2doublemap.put(k, this.values.readDouble());
      }

      return new MetricSampler.SamplerResult(i, j, int2doublemap);
   }

   public boolean triggersThreshold() {
      return this.thresholdTest != null && this.thresholdTest.test(this.currentValue);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         MetricSampler metricsampler = (MetricSampler)pOther;
         return this.name.equals(metricsampler.name) && this.category.equals(metricsampler.category);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   public static class MetricSamplerBuilder<T> {
      private final String name;
      private final MetricCategory category;
      private final DoubleSupplier sampler;
      private final T context;
      @Nullable
      private Runnable beforeTick;
      @Nullable
      private MetricSampler.ThresholdTest thresholdTest;

      public MetricSamplerBuilder(String pName, MetricCategory pCategory, ToDoubleFunction<T> pSampler, T pContext) {
         this.name = pName;
         this.category = pCategory;
         this.sampler = () -> {
            return pSampler.applyAsDouble(pContext);
         };
         this.context = pContext;
      }

      public MetricSampler.MetricSamplerBuilder<T> withBeforeTick(Consumer<T> pBeforeTick) {
         this.beforeTick = () -> {
            pBeforeTick.accept(this.context);
         };
         return this;
      }

      public MetricSampler.MetricSamplerBuilder<T> withThresholdAlert(MetricSampler.ThresholdTest pThresholdTest) {
         this.thresholdTest = pThresholdTest;
         return this;
      }

      public MetricSampler build() {
         return new MetricSampler(this.name, this.category, this.sampler, this.beforeTick, this.thresholdTest);
      }
   }

   public static class SamplerResult {
      private final Int2DoubleMap recording;
      private final int firstTick;
      private final int lastTick;

      public SamplerResult(int pFirstTick, int pLastTick, Int2DoubleMap pRecording) {
         this.firstTick = pFirstTick;
         this.lastTick = pLastTick;
         this.recording = pRecording;
      }

      public double valueAtTick(int pTick) {
         return this.recording.get(pTick);
      }

      public int getFirstTick() {
         return this.firstTick;
      }

      public int getLastTick() {
         return this.lastTick;
      }
   }

   public interface ThresholdTest {
      boolean test(double pValue);
   }

   public static class ValueIncreasedByPercentage implements MetricSampler.ThresholdTest {
      private final float percentageIncreaseThreshold;
      private double previousValue = Double.MIN_VALUE;

      public ValueIncreasedByPercentage(float pPercentageIncreaseThreshold) {
         this.percentageIncreaseThreshold = pPercentageIncreaseThreshold;
      }

      public boolean test(double pValue) {
         boolean flag;
         if (this.previousValue != Double.MIN_VALUE && !(pValue <= this.previousValue)) {
            flag = (pValue - this.previousValue) / this.previousValue >= (double)this.percentageIncreaseThreshold;
         } else {
            flag = false;
         }

         this.previousValue = pValue;
         return flag;
      }
   }
}