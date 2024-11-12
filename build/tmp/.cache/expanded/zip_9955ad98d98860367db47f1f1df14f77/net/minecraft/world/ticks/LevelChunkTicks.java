package net.minecraft.world.ticks;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;

public class LevelChunkTicks<T> implements SerializableTickContainer<T>, TickContainerAccess<T> {
   private final Queue<ScheduledTick<T>> tickQueue = new PriorityQueue<>(ScheduledTick.DRAIN_ORDER);
   @Nullable
   private List<SavedTick<T>> pendingTicks;
   private final Set<ScheduledTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet<>(ScheduledTick.UNIQUE_TICK_HASH);
   @Nullable
   private BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> onTickAdded;

   public LevelChunkTicks() {
   }

   public LevelChunkTicks(List<SavedTick<T>> pPendingTicks) {
      this.pendingTicks = pPendingTicks;

      for(SavedTick<T> savedtick : pPendingTicks) {
         this.ticksPerPosition.add(ScheduledTick.probe(savedtick.type(), savedtick.pos()));
      }

   }

   public void setOnTickAdded(@Nullable BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> pOnTickAdded) {
      this.onTickAdded = pOnTickAdded;
   }

   @Nullable
   public ScheduledTick<T> peek() {
      return this.tickQueue.peek();
   }

   @Nullable
   public ScheduledTick<T> poll() {
      ScheduledTick<T> scheduledtick = this.tickQueue.poll();
      if (scheduledtick != null) {
         this.ticksPerPosition.remove(scheduledtick);
      }

      return scheduledtick;
   }

   public void schedule(ScheduledTick<T> pTick) {
      if (this.ticksPerPosition.add(pTick)) {
         this.scheduleUnchecked(pTick);
      }

   }

   private void scheduleUnchecked(ScheduledTick<T> pTick) {
      this.tickQueue.add(pTick);
      if (this.onTickAdded != null) {
         this.onTickAdded.accept(this, pTick);
      }

   }

   public boolean hasScheduledTick(BlockPos pPos, T pType) {
      return this.ticksPerPosition.contains(ScheduledTick.probe(pType, pPos));
   }

   public void removeIf(Predicate<ScheduledTick<T>> pPredicate) {
      Iterator<ScheduledTick<T>> iterator = this.tickQueue.iterator();

      while(iterator.hasNext()) {
         ScheduledTick<T> scheduledtick = iterator.next();
         if (pPredicate.test(scheduledtick)) {
            iterator.remove();
            this.ticksPerPosition.remove(scheduledtick);
         }
      }

   }

   public Stream<ScheduledTick<T>> getAll() {
      return this.tickQueue.stream();
   }

   public int count() {
      return this.tickQueue.size() + (this.pendingTicks != null ? this.pendingTicks.size() : 0);
   }

   public ListTag save(long pGameTime, Function<T, String> pIdGetter) {
      ListTag listtag = new ListTag();
      if (this.pendingTicks != null) {
         for(SavedTick<T> savedtick : this.pendingTicks) {
            listtag.add(savedtick.save(pIdGetter));
         }
      }

      for(ScheduledTick<T> scheduledtick : this.tickQueue) {
         listtag.add(SavedTick.saveTick(scheduledtick, pIdGetter, pGameTime));
      }

      return listtag;
   }

   public void unpack(long pGameTime) {
      if (this.pendingTicks != null) {
         int i = -this.pendingTicks.size();

         for(SavedTick<T> savedtick : this.pendingTicks) {
            this.scheduleUnchecked(savedtick.unpack(pGameTime, (long)(i++)));
         }
      }

      this.pendingTicks = null;
   }

   public static <T> LevelChunkTicks<T> load(ListTag pTag, Function<String, Optional<T>> pIsParser, ChunkPos pPos) {
      ImmutableList.Builder<SavedTick<T>> builder = ImmutableList.builder();
      SavedTick.loadTickList(pTag, pIsParser, pPos, builder::add);
      return new LevelChunkTicks<>(builder.build());
   }
}