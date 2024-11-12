package net.minecraft.util.profiling;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;

public class InactiveProfiler implements ProfileCollector {
   public static final InactiveProfiler INSTANCE = new InactiveProfiler();

   private InactiveProfiler() {
   }

   public void startTick() {
   }

   public void endTick() {
   }

   /**
    * Start section
    */
   public void push(String pName) {
   }

   public void push(Supplier<String> pNameSupplier) {
   }

   public void markForCharting(MetricCategory pCategory) {
   }

   /**
    * End section
    */
   public void pop() {
   }

   public void popPush(String pName) {
   }

   public void popPush(Supplier<String> pNameSupplier) {
   }

   public void incrementCounter(String pCounterName, int pIncrement) {
   }

   public void incrementCounter(Supplier<String> pCounterNameSupplier, int pIncrement) {
   }

   public ProfileResults getResults() {
      return EmptyProfileResults.EMPTY;
   }

   @Nullable
   public ActiveProfiler.PathEntry getEntry(String pEntryId) {
      return null;
   }

   public Set<Pair<String, MetricCategory>> getChartedPaths() {
      return ImmutableSet.of();
   }
}