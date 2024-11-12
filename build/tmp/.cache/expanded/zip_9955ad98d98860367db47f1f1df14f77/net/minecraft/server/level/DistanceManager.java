package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public abstract class DistanceManager {
   static final Logger LOGGER = LogUtils.getLogger();
   static final int PLAYER_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
   private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
   final Long2ObjectMap<ObjectSet<ServerPlayer>> playersPerChunk = new Long2ObjectOpenHashMap<>();
   final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap<>();
   private final DistanceManager.ChunkTicketTracker ticketTracker = new DistanceManager.ChunkTicketTracker();
   private final DistanceManager.FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter = new DistanceManager.FixedPlayerDistanceChunkTracker(8);
   private final TickingTracker tickingTicketsTracker = new TickingTracker();
   private final DistanceManager.PlayerTicketTracker playerTicketManager = new DistanceManager.PlayerTicketTracker(32);
   final Set<ChunkHolder> chunksToUpdateFutures = Sets.newHashSet();
   final ChunkTaskPriorityQueueSorter ticketThrottler;
   final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> ticketThrottlerInput;
   final ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> ticketThrottlerReleaser;
   final LongSet ticketsToRelease = new LongOpenHashSet();
   final Executor mainThreadExecutor;
   private long ticketTickCounter;
   private int simulationDistance = 10;

   private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> forcedTickets = new Long2ObjectOpenHashMap<>();

   protected DistanceManager(Executor pDispatcher, Executor pMainThreadExecutor) {
      ProcessorHandle<Runnable> processorhandle = ProcessorHandle.of("player ticket throttler", pMainThreadExecutor::execute);
      ChunkTaskPriorityQueueSorter chunktaskpriorityqueuesorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(processorhandle), pDispatcher, 4);
      this.ticketThrottler = chunktaskpriorityqueuesorter;
      this.ticketThrottlerInput = chunktaskpriorityqueuesorter.getProcessor(processorhandle, true);
      this.ticketThrottlerReleaser = chunktaskpriorityqueuesorter.getReleaseProcessor(processorhandle);
      this.mainThreadExecutor = pMainThreadExecutor;
   }

   protected void purgeStaleTickets() {
      ++this.ticketTickCounter;
      ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>>> objectiterator = this.tickets.long2ObjectEntrySet().fastIterator();

      while(objectiterator.hasNext()) {
         Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> entry = objectiterator.next();
         Iterator<Ticket<?>> iterator = entry.getValue().iterator();
         boolean flag = false;

         while(iterator.hasNext()) {
            Ticket<?> ticket = iterator.next();
            if (ticket.timedOut(this.ticketTickCounter)) {
               iterator.remove();
               flag = true;
               this.tickingTicketsTracker.removeTicket(entry.getLongKey(), ticket);
            }
         }

         if (flag) {
            this.ticketTracker.update(entry.getLongKey(), getTicketLevelAt(entry.getValue()), false);
         }

         if (entry.getValue().isEmpty()) {
            objectiterator.remove();
         }
      }

   }

   /**
    * Gets the {@linkplain net.minecraft.server.level.Ticket#getTicketLevel level} of the ticket.
    */
   private static int getTicketLevelAt(SortedArraySet<Ticket<?>> pTickets) {
      return !pTickets.isEmpty() ? pTickets.first().getTicketLevel() : ChunkLevel.MAX_LEVEL + 1;
   }

   protected abstract boolean isChunkToRemove(long pChunkPos);

   @Nullable
   protected abstract ChunkHolder getChunk(long pChunkPos);

   @Nullable
   protected abstract ChunkHolder updateChunkScheduling(long pChunkPos, int pNewLevel, @Nullable ChunkHolder pHolder, int pOldLevel);

   public boolean runAllUpdates(ChunkMap pChunkManager) {
      this.naturalSpawnChunkCounter.runAllUpdates();
      this.tickingTicketsTracker.runAllUpdates();
      this.playerTicketManager.runAllUpdates();
      int i = Integer.MAX_VALUE - this.ticketTracker.runDistanceUpdates(Integer.MAX_VALUE);
      boolean flag = i != 0;
      if (flag) {
      }

      if (!this.chunksToUpdateFutures.isEmpty()) {
         this.chunksToUpdateFutures.forEach((p_183908_) -> {
            p_183908_.updateFutures(pChunkManager, this.mainThreadExecutor);
         });
         this.chunksToUpdateFutures.clear();
         return true;
      } else {
         if (!this.ticketsToRelease.isEmpty()) {
            LongIterator longiterator = this.ticketsToRelease.iterator();

            while(longiterator.hasNext()) {
               long j = longiterator.nextLong();
               if (this.getTickets(j).stream().anyMatch((p_183910_) -> {
                  return p_183910_.getType() == TicketType.PLAYER;
               })) {
                  ChunkHolder chunkholder = pChunkManager.getUpdatingChunkIfPresent(j);
                  if (chunkholder == null) {
                     throw new IllegalStateException();
                  }

                  CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture = chunkholder.getEntityTickingChunkFuture();
                  completablefuture.thenAccept((p_183905_) -> {
                     this.mainThreadExecutor.execute(() -> {
                        this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                        }, j, false));
                     });
                  });
               }
            }

            this.ticketsToRelease.clear();
         }

         return flag;
      }
   }

   void addTicket(long pChunkPos, Ticket<?> pTicket) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.getTickets(pChunkPos);
      int i = getTicketLevelAt(sortedarrayset);
      Ticket<?> ticket = sortedarrayset.addOrGet(pTicket);
      ticket.setCreatedTick(this.ticketTickCounter);
      if (pTicket.getTicketLevel() < i) {
         this.ticketTracker.update(pChunkPos, pTicket.getTicketLevel(), true);
      }

      if (pTicket.isForceTicks()) {
          SortedArraySet<Ticket<?>> tickets = forcedTickets.computeIfAbsent(pChunkPos, e -> SortedArraySet.create(4));
          tickets.addOrGet(ticket);
      }
   }

   void removeTicket(long pChunkPos, Ticket<?> pTicket) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.getTickets(pChunkPos);
      if (sortedarrayset.remove(pTicket)) {
      }

      if (sortedarrayset.isEmpty()) {
         this.tickets.remove(pChunkPos);
      }

      this.ticketTracker.update(pChunkPos, getTicketLevelAt(sortedarrayset), false);

      if (pTicket.isForceTicks()) {
          SortedArraySet<Ticket<?>> tickets = forcedTickets.get(pChunkPos);
          if (tickets != null) {
              tickets.remove(pTicket);
          }
      }
   }

   public <T> void addTicket(TicketType<T> pType, ChunkPos pPos, int pLevel, T pValue) {
      this.addTicket(pPos.toLong(), new Ticket<>(pType, pLevel, pValue));
   }

   public <T> void removeTicket(TicketType<T> pType, ChunkPos pPos, int pLevel, T pValue) {
      Ticket<T> ticket = new Ticket<>(pType, pLevel, pValue);
      this.removeTicket(pPos.toLong(), ticket);
   }

   public <T> void addRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue) {
      addRegionTicket(pType, pPos, pDistance, pValue, false);
   }
   public <T> void addRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue, boolean forceTicks) {
      Ticket<T> ticket = new Ticket<>(pType, ChunkLevel.byStatus(FullChunkStatus.FULL) - pDistance, pValue, forceTicks);
      long i = pPos.toLong();
      this.addTicket(i, ticket);
      this.tickingTicketsTracker.addTicket(i, ticket);
   }

   public <T> void removeRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue) {
      removeRegionTicket(pType, pPos, pDistance, pValue, false);
   }
   public <T> void removeRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue, boolean forceTicks) {
      Ticket<T> ticket = new Ticket<>(pType, ChunkLevel.byStatus(FullChunkStatus.FULL) - pDistance, pValue, forceTicks);
      long i = pPos.toLong();
      this.removeTicket(i, ticket);
      this.tickingTicketsTracker.removeTicket(i, ticket);
   }

   private SortedArraySet<Ticket<?>> getTickets(long pChunkPos) {
      return this.tickets.computeIfAbsent(pChunkPos, (p_183923_) -> {
         return SortedArraySet.create(4);
      });
   }

   protected void updateChunkForced(ChunkPos pPos, boolean pAdd) {
      Ticket<ChunkPos> ticket = new Ticket<>(TicketType.FORCED, ChunkMap.FORCED_TICKET_LEVEL, pPos);
      long i = pPos.toLong();
      if (pAdd) {
         this.addTicket(i, ticket);
         this.tickingTicketsTracker.addTicket(i, ticket);
      } else {
         this.removeTicket(i, ticket);
         this.tickingTicketsTracker.removeTicket(i, ticket);
      }

   }

   public void addPlayer(SectionPos pSectionPos, ServerPlayer pPlayer) {
      ChunkPos chunkpos = pSectionPos.chunk();
      long i = chunkpos.toLong();
      this.playersPerChunk.computeIfAbsent(i, (p_183921_) -> {
         return new ObjectOpenHashSet();
      }).add(pPlayer);
      this.naturalSpawnChunkCounter.update(i, 0, true);
      this.playerTicketManager.update(i, 0, true);
      this.tickingTicketsTracker.addTicket(TicketType.PLAYER, chunkpos, this.getPlayerTicketLevel(), chunkpos);
   }

   public void removePlayer(SectionPos pSectionPos, ServerPlayer pPlayer) {
      ChunkPos chunkpos = pSectionPos.chunk();
      long i = chunkpos.toLong();
      ObjectSet<ServerPlayer> objectset = this.playersPerChunk.get(i);
      objectset.remove(pPlayer);
      if (objectset.isEmpty()) {
         this.playersPerChunk.remove(i);
         this.naturalSpawnChunkCounter.update(i, Integer.MAX_VALUE, false);
         this.playerTicketManager.update(i, Integer.MAX_VALUE, false);
         this.tickingTicketsTracker.removeTicket(TicketType.PLAYER, chunkpos, this.getPlayerTicketLevel(), chunkpos);
      }

   }

   private int getPlayerTicketLevel() {
      return Math.max(0, ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - this.simulationDistance);
   }

   public boolean inEntityTickingRange(long pChunkPos) {
      return ChunkLevel.isEntityTicking(this.tickingTicketsTracker.getLevel(pChunkPos));
   }

   public boolean inBlockTickingRange(long pChunkPos) {
      return ChunkLevel.isBlockTicking(this.tickingTicketsTracker.getLevel(pChunkPos));
   }

   protected String getTicketDebugString(long pChunkPos) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.tickets.get(pChunkPos);
      return sortedarrayset != null && !sortedarrayset.isEmpty() ? sortedarrayset.first().toString() : "no_ticket";
   }

   protected void updatePlayerTickets(int pViewDistance) {
      this.playerTicketManager.updateViewDistance(pViewDistance);
   }

   public void updateSimulationDistance(int pSimulationDistance) {
      if (pSimulationDistance != this.simulationDistance) {
         this.simulationDistance = pSimulationDistance;
         this.tickingTicketsTracker.replacePlayerTicketsLevel(this.getPlayerTicketLevel());
      }

   }

   /**
    * Returns the number of chunks taken into account when calculating the mob cap
    */
   public int getNaturalSpawnChunkCount() {
      this.naturalSpawnChunkCounter.runAllUpdates();
      return this.naturalSpawnChunkCounter.chunks.size();
   }

   public boolean hasPlayersNearby(long pChunkPos) {
      this.naturalSpawnChunkCounter.runAllUpdates();
      return this.naturalSpawnChunkCounter.chunks.containsKey(pChunkPos);
   }

   public String getDebugStatus() {
      return this.ticketThrottler.getDebugStatus();
   }

   public boolean shouldForceTicks(long chunkPos) {
       SortedArraySet<Ticket<?>> tickets = forcedTickets.get(chunkPos);
       return tickets != null && !tickets.isEmpty();
   }

   private void dumpTickets(String pFilename) {
      try (FileOutputStream fileoutputstream = new FileOutputStream(new File(pFilename))) {
         for(Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> entry : this.tickets.long2ObjectEntrySet()) {
            ChunkPos chunkpos = new ChunkPos(entry.getLongKey());

            for(Ticket<?> ticket : entry.getValue()) {
               fileoutputstream.write((chunkpos.x + "\t" + chunkpos.z + "\t" + ticket.getType() + "\t" + ticket.getTicketLevel() + "\t\n").getBytes(StandardCharsets.UTF_8));
            }
         }
      } catch (IOException ioexception) {
         LOGGER.error("Failed to dump tickets to {}", pFilename, ioexception);
      }

   }

   @VisibleForTesting
   TickingTracker tickingTracker() {
      return this.tickingTicketsTracker;
   }

   public void removeTicketsOnClosing() {
      ImmutableSet<TicketType<?>> immutableset = ImmutableSet.of(TicketType.UNKNOWN, TicketType.POST_TELEPORT, TicketType.LIGHT);
      ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>>> objectiterator = this.tickets.long2ObjectEntrySet().fastIterator();

      while(objectiterator.hasNext()) {
         Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> entry = objectiterator.next();
         Iterator<Ticket<?>> iterator = entry.getValue().iterator();
         boolean flag = false;

         while(iterator.hasNext()) {
            Ticket<?> ticket = iterator.next();
            if (!immutableset.contains(ticket.getType())) {
               iterator.remove();
               flag = true;
               this.tickingTicketsTracker.removeTicket(entry.getLongKey(), ticket);
            }
         }

         if (flag) {
            this.ticketTracker.update(entry.getLongKey(), getTicketLevelAt(entry.getValue()), false);
         }

         if (entry.getValue().isEmpty()) {
            objectiterator.remove();
         }
      }

   }

   public boolean hasTickets() {
      return !this.tickets.isEmpty();
   }

   class ChunkTicketTracker extends ChunkTracker {
      private static final int MAX_LEVEL = ChunkLevel.MAX_LEVEL + 1;

      public ChunkTicketTracker() {
         super(MAX_LEVEL + 1, 16, 256);
      }

      protected int getLevelFromSource(long pPos) {
         SortedArraySet<Ticket<?>> sortedarrayset = DistanceManager.this.tickets.get(pPos);
         if (sortedarrayset == null) {
            return Integer.MAX_VALUE;
         } else {
            return sortedarrayset.isEmpty() ? Integer.MAX_VALUE : sortedarrayset.first().getTicketLevel();
         }
      }

      protected int getLevel(long pSectionPos) {
         if (!DistanceManager.this.isChunkToRemove(pSectionPos)) {
            ChunkHolder chunkholder = DistanceManager.this.getChunk(pSectionPos);
            if (chunkholder != null) {
               return chunkholder.getTicketLevel();
            }
         }

         return MAX_LEVEL;
      }

      protected void setLevel(long pSectionPos, int pLevel) {
         ChunkHolder chunkholder = DistanceManager.this.getChunk(pSectionPos);
         int i = chunkholder == null ? MAX_LEVEL : chunkholder.getTicketLevel();
         if (i != pLevel) {
            chunkholder = DistanceManager.this.updateChunkScheduling(pSectionPos, pLevel, chunkholder, i);
            if (chunkholder != null) {
               DistanceManager.this.chunksToUpdateFutures.add(chunkholder);
            }

         }
      }

      public int runDistanceUpdates(int pToUpdateCount) {
         return this.runUpdates(pToUpdateCount);
      }
   }

   class FixedPlayerDistanceChunkTracker extends ChunkTracker {
      /** Chunks that are at most {@link #range} chunks away from the closest player. */
      protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
      protected final int maxDistance;

      protected FixedPlayerDistanceChunkTracker(int pMaxDistance) {
         super(pMaxDistance + 2, 16, 256);
         this.maxDistance = pMaxDistance;
         this.chunks.defaultReturnValue((byte)(pMaxDistance + 2));
      }

      protected int getLevel(long pSectionPos) {
         return this.chunks.get(pSectionPos);
      }

      protected void setLevel(long pSectionPos, int pLevel) {
         byte b0;
         if (pLevel > this.maxDistance) {
            b0 = this.chunks.remove(pSectionPos);
         } else {
            b0 = this.chunks.put(pSectionPos, (byte)pLevel);
         }

         this.onLevelChange(pSectionPos, b0, pLevel);
      }

      /**
       * Called after {@link PlayerChunkTracker#setLevel(long, int)} puts/removes chunk into/from {@link
       * #chunksInRange}.
       * @param pOldLevel Previous level of the chunk if it was smaller than {@link #range}, {@code range + 2}
       * otherwise.
       */
      protected void onLevelChange(long pChunkPos, int pOldLevel, int pNewLevel) {
      }

      protected int getLevelFromSource(long pPos) {
         return this.havePlayer(pPos) ? 0 : Integer.MAX_VALUE;
      }

      private boolean havePlayer(long pChunkPos) {
         ObjectSet<ServerPlayer> objectset = DistanceManager.this.playersPerChunk.get(pChunkPos);
         return objectset != null && !objectset.isEmpty();
      }

      public void runAllUpdates() {
         this.runUpdates(Integer.MAX_VALUE);
      }

      private void dumpChunks(String pFilename) {
         try (FileOutputStream fileoutputstream = new FileOutputStream(new File(pFilename))) {
            for(Long2ByteMap.Entry entry : this.chunks.long2ByteEntrySet()) {
               ChunkPos chunkpos = new ChunkPos(entry.getLongKey());
               String s = Byte.toString(entry.getByteValue());
               fileoutputstream.write((chunkpos.x + "\t" + chunkpos.z + "\t" + s + "\n").getBytes(StandardCharsets.UTF_8));
            }
         } catch (IOException ioexception) {
            DistanceManager.LOGGER.error("Failed to dump chunks to {}", pFilename, ioexception);
         }

      }
   }

   class PlayerTicketTracker extends DistanceManager.FixedPlayerDistanceChunkTracker {
      private int viewDistance;
      private final Long2IntMap queueLevels = Long2IntMaps.synchronize(new Long2IntOpenHashMap());
      private final LongSet toUpdate = new LongOpenHashSet();

      protected PlayerTicketTracker(int pMaxDistance) {
         super(pMaxDistance);
         this.viewDistance = 0;
         this.queueLevels.defaultReturnValue(pMaxDistance + 2);
      }

      /**
       * Called after {@link PlayerChunkTracker#setLevel(long, int)} puts/removes chunk into/from {@link
       * #chunksInRange}.
       * @param pOldLevel Previous level of the chunk if it was smaller than {@link #range}, {@code range + 2}
       * otherwise.
       */
      protected void onLevelChange(long pChunkPos, int pOldLevel, int pNewLevel) {
         this.toUpdate.add(pChunkPos);
      }

      public void updateViewDistance(int pViewDistance) {
         for(Long2ByteMap.Entry entry : this.chunks.long2ByteEntrySet()) {
            byte b0 = entry.getByteValue();
            long i = entry.getLongKey();
            this.onLevelChange(i, b0, this.haveTicketFor(b0), b0 <= pViewDistance);
         }

         this.viewDistance = pViewDistance;
      }

      private void onLevelChange(long pChunkPos, int p_140920_, boolean p_140921_, boolean p_140922_) {
         if (p_140921_ != p_140922_) {
            Ticket<?> ticket = new Ticket<>(TicketType.PLAYER, DistanceManager.PLAYER_TICKET_LEVEL, new ChunkPos(pChunkPos));
            if (p_140922_) {
               DistanceManager.this.ticketThrottlerInput.tell(ChunkTaskPriorityQueueSorter.message(() -> {
                  DistanceManager.this.mainThreadExecutor.execute(() -> {
                     if (this.haveTicketFor(this.getLevel(pChunkPos))) {
                        DistanceManager.this.addTicket(pChunkPos, ticket);
                        DistanceManager.this.ticketsToRelease.add(pChunkPos);
                     } else {
                        DistanceManager.this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                        }, pChunkPos, false));
                     }

                  });
               }, pChunkPos, () -> {
                  return p_140920_;
               }));
            } else {
               DistanceManager.this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                  DistanceManager.this.mainThreadExecutor.execute(() -> {
                     DistanceManager.this.removeTicket(pChunkPos, ticket);
                  });
               }, pChunkPos, true));
            }
         }

      }

      public void runAllUpdates() {
         super.runAllUpdates();
         if (!this.toUpdate.isEmpty()) {
            LongIterator longiterator = this.toUpdate.iterator();

            while(longiterator.hasNext()) {
               long i = longiterator.nextLong();
               int j = this.queueLevels.get(i);
               int k = this.getLevel(i);
               if (j != k) {
                  DistanceManager.this.ticketThrottler.onLevelChange(new ChunkPos(i), () -> {
                     return this.queueLevels.get(i);
                  }, k, (p_140928_) -> {
                     if (p_140928_ >= this.queueLevels.defaultReturnValue()) {
                        this.queueLevels.remove(i);
                     } else {
                        this.queueLevels.put(i, p_140928_);
                     }

                  });
                  this.onLevelChange(i, k, this.haveTicketFor(j), this.haveTicketFor(k));
               }
            }

            this.toUpdate.clear();
         }

      }

      private boolean haveTicketFor(int pLevel) {
         return pLevel <= this.viewDistance;
      }
   }
}
