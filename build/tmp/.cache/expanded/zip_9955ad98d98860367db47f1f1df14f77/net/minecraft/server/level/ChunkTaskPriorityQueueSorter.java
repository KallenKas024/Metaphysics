package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class ChunkTaskPriorityQueueSorter implements ChunkHolder.LevelChangeListener, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<ProcessorHandle<?>, ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>>> queues;
   private final Set<ProcessorHandle<?>> sleeping;
   private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;

   public ChunkTaskPriorityQueueSorter(List<ProcessorHandle<?>> pQueues, Executor pTask, int pMaxTasks) {
      this.queues = pQueues.stream().collect(Collectors.toMap(Function.identity(), (p_140561_) -> {
         return new ChunkTaskPriorityQueue<>(p_140561_.name() + "_queue", pMaxTasks);
      }));
      this.sleeping = Sets.newHashSet(pQueues);
      this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(4), pTask, "sorter");
   }

   public boolean hasWork() {
      return this.mailbox.hasWork() || this.queues.values().stream().anyMatch(ChunkTaskPriorityQueue::hasWork);
   }

   public static <T> ChunkTaskPriorityQueueSorter.Message<T> message(Function<ProcessorHandle<Unit>, T> pTask, long pPos, IntSupplier pLevel) {
      return new ChunkTaskPriorityQueueSorter.Message<>(pTask, pPos, pLevel);
   }

   public static ChunkTaskPriorityQueueSorter.Message<Runnable> message(Runnable pTask, long pPos, IntSupplier pLevel) {
      return new ChunkTaskPriorityQueueSorter.Message<>((p_140634_) -> {
         return () -> {
            pTask.run();
            p_140634_.tell(Unit.INSTANCE);
         };
      }, pPos, pLevel);
   }

   public static ChunkTaskPriorityQueueSorter.Message<Runnable> message(ChunkHolder pChunkHolder, Runnable pTask) {
      return message(pTask, pChunkHolder.getPos().toLong(), pChunkHolder::getQueueLevel);
   }

   public static <T> ChunkTaskPriorityQueueSorter.Message<T> message(ChunkHolder pChunkHolder, Function<ProcessorHandle<Unit>, T> pTask) {
      return message(pTask, pChunkHolder.getPos().toLong(), pChunkHolder::getQueueLevel);
   }

   public static ChunkTaskPriorityQueueSorter.Release release(Runnable pTask, long pPos, boolean pClearQueue) {
      return new ChunkTaskPriorityQueueSorter.Release(pTask, pPos, pClearQueue);
   }

   public <T> ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<T>> getProcessor(ProcessorHandle<T> pProcessor, boolean p_140606_) {
      return this.mailbox.<ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<T>>>ask((p_140610_) -> {
         return new StrictQueue.IntRunnable(0, () -> {
            this.getQueue(pProcessor);
            p_140610_.tell(ProcessorHandle.of("chunk priority sorter around " + pProcessor.name(), (p_143176_) -> {
               this.submit(pProcessor, p_143176_.task, p_143176_.pos, p_143176_.level, p_140606_);
            }));
         });
      }).join();
   }

   public ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> getReleaseProcessor(ProcessorHandle<Runnable> pProcessor) {
      return this.mailbox.<ProcessorHandle<ChunkTaskPriorityQueueSorter.Release>>ask((p_140581_) -> {
         return new StrictQueue.IntRunnable(0, () -> {
            p_140581_.tell(ProcessorHandle.of("chunk priority sorter around " + pProcessor.name(), (p_143165_) -> {
               this.release(pProcessor, p_143165_.pos, p_143165_.task, p_143165_.clearQueue);
            }));
         });
      }).join();
   }

   public void onLevelChange(ChunkPos pChunkPos, IntSupplier p_140617_, int p_140618_, IntConsumer p_140619_) {
      this.mailbox.tell(new StrictQueue.IntRunnable(0, () -> {
         int i = p_140617_.getAsInt();
         this.queues.values().forEach((p_143155_) -> {
            p_143155_.resortChunkTasks(i, pChunkPos, p_140618_);
         });
         p_140619_.accept(p_140618_);
      }));
   }

   private <T> void release(ProcessorHandle<T> pProcessor, long pChunkPos, Runnable p_140572_, boolean pFullClear) {
      this.mailbox.tell(new StrictQueue.IntRunnable(1, () -> {
         ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunktaskpriorityqueue = this.getQueue(pProcessor);
         chunktaskpriorityqueue.release(pChunkPos, pFullClear);
         if (this.sleeping.remove(pProcessor)) {
            this.pollTask(chunktaskpriorityqueue, pProcessor);
         }

         p_140572_.run();
      }));
   }

   private <T> void submit(ProcessorHandle<T> pProcessor, Function<ProcessorHandle<Unit>, T> pTask, long pChunkPos, IntSupplier p_140593_, boolean p_140594_) {
      this.mailbox.tell(new StrictQueue.IntRunnable(2, () -> {
         ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> chunktaskpriorityqueue = this.getQueue(pProcessor);
         int i = p_140593_.getAsInt();
         chunktaskpriorityqueue.submit(Optional.of(pTask), pChunkPos, i);
         if (p_140594_) {
            chunktaskpriorityqueue.submit(Optional.empty(), pChunkPos, i);
         }

         if (this.sleeping.remove(pProcessor)) {
            this.pollTask(chunktaskpriorityqueue, pProcessor);
         }

      }));
   }

   private <T> void pollTask(ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> p_140646_, ProcessorHandle<T> p_140647_) {
      this.mailbox.tell(new StrictQueue.IntRunnable(3, () -> {
         Stream<Either<Function<ProcessorHandle<Unit>, T>, Runnable>> stream = p_140646_.pop();
         if (stream == null) {
            this.sleeping.add(p_140647_);
         } else {
            CompletableFuture.allOf(stream.map((p_143172_) -> {
               return p_143172_.map(p_140647_::ask, (p_143180_) -> {
                  p_143180_.run();
                  return CompletableFuture.completedFuture(Unit.INSTANCE);
               });
            }).toArray((p_212890_) -> {
               return new CompletableFuture[p_212890_];
            })).thenAccept((p_212894_) -> {
               this.pollTask(p_140646_, p_140647_);
            });
         }

      }));
   }

   private <T> ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> getQueue(ProcessorHandle<T> pProcessor) {
      ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>> chunktaskpriorityqueue = this.queues.get(pProcessor);
      if (chunktaskpriorityqueue == null) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("No queue for: " + pProcessor));
      } else {
         return (ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>>)chunktaskpriorityqueue;
      }
   }

   @VisibleForTesting
   public String getDebugStatus() {
      return (String)this.queues.entrySet().stream().map((p_212898_) -> {
         return p_212898_.getKey().name() + "=[" + (String)p_212898_.getValue().getAcquired().stream().map((p_212896_) -> {
            return p_212896_ + ":" + new ChunkPos(p_212896_);
         }).collect(Collectors.joining(",")) + "]";
      }).collect(Collectors.joining(",")) + ", s=" + this.sleeping.size();
   }

   public void close() {
      this.queues.keySet().forEach(ProcessorHandle::close);
   }

   public static final class Message<T> {
      final Function<ProcessorHandle<Unit>, T> task;
      final long pos;
      final IntSupplier level;

      Message(Function<ProcessorHandle<Unit>, T> pTask, long pPos, IntSupplier pLevel) {
         this.task = pTask;
         this.pos = pPos;
         this.level = pLevel;
      }
   }

   public static final class Release {
      final Runnable task;
      final long pos;
      final boolean clearQueue;

      Release(Runnable pTask, long pPos, boolean pClearQueue) {
         this.task = pTask;
         this.pos = pPos;
         this.clearQueue = pClearQueue;
      }
   }
}