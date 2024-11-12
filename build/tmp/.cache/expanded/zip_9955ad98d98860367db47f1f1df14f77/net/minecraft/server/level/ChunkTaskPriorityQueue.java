package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;

public class ChunkTaskPriorityQueue<T> {
   public static final int PRIORITY_LEVEL_COUNT = ChunkLevel.MAX_LEVEL + 2;
   private final List<Long2ObjectLinkedOpenHashMap<List<Optional<T>>>> taskQueue = IntStream.range(0, PRIORITY_LEVEL_COUNT).mapToObj((p_140520_) -> {
      return new Long2ObjectLinkedOpenHashMap<List<Optional<T>>>();
   }).collect(Collectors.toList());
   private volatile int firstQueue = PRIORITY_LEVEL_COUNT;
   private final String name;
   private final LongSet acquired = new LongOpenHashSet();
   private final int maxTasks;

   public ChunkTaskPriorityQueue(String pName, int pMaxTasks) {
      this.name = pName;
      this.maxTasks = pMaxTasks;
   }

   protected void resortChunkTasks(int p_140522_, ChunkPos pChunkPos, int p_140524_) {
      if (p_140522_ < PRIORITY_LEVEL_COUNT) {
         Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2objectlinkedopenhashmap = this.taskQueue.get(p_140522_);
         List<Optional<T>> list = long2objectlinkedopenhashmap.remove(pChunkPos.toLong());
         if (p_140522_ == this.firstQueue) {
            while(this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty()) {
               ++this.firstQueue;
            }
         }

         if (list != null && !list.isEmpty()) {
            this.taskQueue.get(p_140524_).computeIfAbsent(pChunkPos.toLong(), (p_140547_) -> {
               return Lists.newArrayList();
            }).addAll(list);
            this.firstQueue = Math.min(this.firstQueue, p_140524_);
         }

      }
   }

   protected void submit(Optional<T> pTask, long pChunkPos, int pChunkLevel) {
      this.taskQueue.get(pChunkLevel).computeIfAbsent(pChunkPos, (p_140545_) -> {
         return Lists.newArrayList();
      }).add(pTask);
      this.firstQueue = Math.min(this.firstQueue, pChunkLevel);
   }

   protected void release(long pChunkPos, boolean pFullClear) {
      for(Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2objectlinkedopenhashmap : this.taskQueue) {
         List<Optional<T>> list = long2objectlinkedopenhashmap.get(pChunkPos);
         if (list != null) {
            if (pFullClear) {
               list.clear();
            } else {
               list.removeIf((p_140534_) -> {
                  return !p_140534_.isPresent();
               });
            }

            if (list.isEmpty()) {
               long2objectlinkedopenhashmap.remove(pChunkPos);
            }
         }
      }

      while(this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty()) {
         ++this.firstQueue;
      }

      this.acquired.remove(pChunkPos);
   }

   private Runnable acquire(long p_140526_) {
      return () -> {
         this.acquired.add(p_140526_);
      };
   }

   @Nullable
   public Stream<Either<T, Runnable>> pop() {
      if (this.acquired.size() >= this.maxTasks) {
         return null;
      } else if (!this.hasWork()) {
         return null;
      } else {
         int i = this.firstQueue;
         Long2ObjectLinkedOpenHashMap<List<Optional<T>>> long2objectlinkedopenhashmap = this.taskQueue.get(i);
         long j = long2objectlinkedopenhashmap.firstLongKey();

         List<Optional<T>> list;
         for(list = long2objectlinkedopenhashmap.removeFirst(); this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty(); ++this.firstQueue) {
         }

         return list.stream().map((p_140529_) -> {
            return p_140529_.<Either<T,Runnable>>map(Either::left).orElseGet(() -> {
               return Either.right(this.acquire(j));
            });
         });
      }
   }

   public boolean hasWork() {
      return this.firstQueue < PRIORITY_LEVEL_COUNT;
   }

   public String toString() {
      return this.name + " " + this.firstQueue + "...";
   }

   @VisibleForTesting
   LongSet getAcquired() {
      return new LongOpenHashSet(this.acquired);
   }
}