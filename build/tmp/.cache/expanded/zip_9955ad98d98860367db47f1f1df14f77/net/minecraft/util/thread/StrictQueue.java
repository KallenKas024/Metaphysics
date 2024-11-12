package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

public interface StrictQueue<T, F> {
   @Nullable
   F pop();

   boolean push(T pValue);

   boolean isEmpty();

   int size();

   public static final class FixedPriorityQueue implements StrictQueue<StrictQueue.IntRunnable, Runnable> {
      private final Queue<Runnable>[] queues;
      private final AtomicInteger size = new AtomicInteger();

      public FixedPriorityQueue(int pSize) {
         this.queues = new Queue[pSize];

         for(int i = 0; i < pSize; ++i) {
            this.queues[i] = Queues.newConcurrentLinkedQueue();
         }

      }

      @Nullable
      public Runnable pop() {
         for(Queue<Runnable> queue : this.queues) {
            Runnable runnable = queue.poll();
            if (runnable != null) {
               this.size.decrementAndGet();
               return runnable;
            }
         }

         return null;
      }

      public boolean push(StrictQueue.IntRunnable pValue) {
         int i = pValue.priority;
         if (i < this.queues.length && i >= 0) {
            this.queues[i].add(pValue);
            this.size.incrementAndGet();
            return true;
         } else {
            throw new IndexOutOfBoundsException(String.format(Locale.ROOT, "Priority %d not supported. Expected range [0-%d]", i, this.queues.length - 1));
         }
      }

      public boolean isEmpty() {
         return this.size.get() == 0;
      }

      public int size() {
         return this.size.get();
      }
   }

   public static final class IntRunnable implements Runnable {
      final int priority;
      private final Runnable task;

      public IntRunnable(int pPriority, Runnable pTask) {
         this.priority = pPriority;
         this.task = pTask;
      }

      public void run() {
         this.task.run();
      }

      public int getPriority() {
         return this.priority;
      }
   }

   public static final class QueueStrictQueue<T> implements StrictQueue<T, T> {
      private final Queue<T> queue;

      public QueueStrictQueue(Queue<T> pQueue) {
         this.queue = pQueue;
      }

      @Nullable
      public T pop() {
         return this.queue.poll();
      }

      public boolean push(T pValue) {
         return this.queue.add(pValue);
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }

      public int size() {
         return this.queue.size();
      }
   }
}