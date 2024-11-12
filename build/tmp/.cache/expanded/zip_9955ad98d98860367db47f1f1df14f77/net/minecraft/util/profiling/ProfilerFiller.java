package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraft.util.profiling.metrics.MetricCategory;

public interface ProfilerFiller {
   String ROOT = "root";

   void startTick();

   void endTick();

   /**
    * Start section
    */
   void push(String pName);

   void push(Supplier<String> pNameSupplier);

   /**
    * End section
    */
   void pop();

   void popPush(String pName);

   void popPush(Supplier<String> pNameSupplier);

   void markForCharting(MetricCategory pCategory);

   default void incrementCounter(String pEntryId) {
      this.incrementCounter(pEntryId, 1);
   }

   void incrementCounter(String pCounterName, int pIncrement);

   default void incrementCounter(Supplier<String> pEntryIdSupplier) {
      this.incrementCounter(pEntryIdSupplier, 1);
   }

   void incrementCounter(Supplier<String> pCounterNameSupplier, int pIncrement);

   static ProfilerFiller tee(final ProfilerFiller pFirst, final ProfilerFiller pSecond) {
      if (pFirst == InactiveProfiler.INSTANCE) {
         return pSecond;
      } else {
         return pSecond == InactiveProfiler.INSTANCE ? pFirst : new ProfilerFiller() {
            public void startTick() {
               pFirst.startTick();
               pSecond.startTick();
            }

            public void endTick() {
               pFirst.endTick();
               pSecond.endTick();
            }

            /**
             * Start section
             */
            public void push(String p_18594_) {
               pFirst.push(p_18594_);
               pSecond.push(p_18594_);
            }

            public void push(Supplier<String> p_18596_) {
               pFirst.push(p_18596_);
               pSecond.push(p_18596_);
            }

            public void markForCharting(MetricCategory p_145961_) {
               pFirst.markForCharting(p_145961_);
               pSecond.markForCharting(p_145961_);
            }

            /**
             * End section
             */
            public void pop() {
               pFirst.pop();
               pSecond.pop();
            }

            public void popPush(String p_18599_) {
               pFirst.popPush(p_18599_);
               pSecond.popPush(p_18599_);
            }

            public void popPush(Supplier<String> p_18601_) {
               pFirst.popPush(p_18601_);
               pSecond.popPush(p_18601_);
            }

            public void incrementCounter(String p_185263_, int p_185264_) {
               pFirst.incrementCounter(p_185263_, p_185264_);
               pSecond.incrementCounter(p_185263_, p_185264_);
            }

            public void incrementCounter(Supplier<String> p_185266_, int p_185267_) {
               pFirst.incrementCounter(p_185266_, p_185267_);
               pSecond.incrementCounter(p_185266_, p_185267_);
            }
         };
      }
   }
}