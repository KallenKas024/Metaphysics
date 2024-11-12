package net.minecraft.server.level;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.util.DebugBuffer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ChunkHolder {
   public static final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> UNLOADED_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
   public static final CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNK);
   public static final Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> UNLOADED_LEVEL_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
   private static final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> NOT_DONE_YET = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
   private static final CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_LEVEL_CHUNK);
   private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
   private final AtomicReferenceArray<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> futures = new AtomicReferenceArray<>(CHUNK_STATUSES.size());
   private final LevelHeightAccessor levelHeightAccessor;
   /**
    * A future that returns the chunk if it is a border chunk, {@link
    * net.minecraft.world.server.ChunkHolder.ChunkLoadingFailure#UNLOADED} otherwise.
    */
   private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
   /**
    * A future that returns the chunk if it is a ticking chunk, {@link
    * net.minecraft.world.server.ChunkHolder.ChunkLoadingFailure#UNLOADED} otherwise.
    */
   private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
   /**
    * A future that returns the chunk if it is an entity ticking chunk, {@link
    * net.minecraft.world.server.ChunkHolder.ChunkLoadingFailure#UNLOADED} otherwise.
    */
   private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
   private CompletableFuture<ChunkAccess> chunkToSave = CompletableFuture.completedFuture((ChunkAccess)null);
   @Nullable
   private final DebugBuffer<ChunkHolder.ChunkSaveDebug> chunkToSaveHistory = null;
   private int oldTicketLevel;
   private int ticketLevel;
   private int queueLevel;
   final ChunkPos pos;
   private boolean hasChangedSections;
   private final ShortSet[] changedBlocksPerSection;
   private final BitSet blockChangedLightSectionFilter = new BitSet();
   private final BitSet skyChangedLightSectionFilter = new BitSet();
   private final LevelLightEngine lightEngine;
   private final ChunkHolder.LevelChangeListener onLevelChange;
   private final ChunkHolder.PlayerProvider playerProvider;
   private boolean wasAccessibleSinceLastSave;
   LevelChunk currentlyLoading; // Forge: Used to bypass future chain when loading chunks.
   private CompletableFuture<Void> pendingFullStateConfirmation = CompletableFuture.completedFuture((Void)null);

   public ChunkHolder(ChunkPos pPos, int pTicketLevel, LevelHeightAccessor pLevelHeightAccessor, LevelLightEngine pLightEngine, ChunkHolder.LevelChangeListener pOnLevelChange, ChunkHolder.PlayerProvider pPlayerProvider) {
      this.pos = pPos;
      this.levelHeightAccessor = pLevelHeightAccessor;
      this.lightEngine = pLightEngine;
      this.onLevelChange = pOnLevelChange;
      this.playerProvider = pPlayerProvider;
      this.oldTicketLevel = ChunkLevel.MAX_LEVEL + 1;
      this.ticketLevel = this.oldTicketLevel;
      this.queueLevel = this.oldTicketLevel;
      this.setTicketLevel(pTicketLevel);
      this.changedBlocksPerSection = new ShortSet[pLevelHeightAccessor.getSectionsCount()];
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getFutureIfPresentUnchecked(ChunkStatus pChunkStatus) {
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.futures.get(pChunkStatus.getIndex());
      return completablefuture == null ? UNLOADED_CHUNK_FUTURE : completablefuture;
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getFutureIfPresent(ChunkStatus pChunkStatus) {
      return ChunkLevel.generationStatus(this.ticketLevel).isOrAfter(pChunkStatus) ? this.getFutureIfPresentUnchecked(pChunkStatus) : UNLOADED_CHUNK_FUTURE;
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getTickingChunkFuture() {
      return this.tickingChunkFuture;
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getEntityTickingChunkFuture() {
      return this.entityTickingChunkFuture;
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getFullChunkFuture() {
      return this.fullChunkFuture;
   }

   @Nullable
   public LevelChunk getTickingChunk() {
      CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getTickingChunkFuture();
      Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = completablefuture.getNow((Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)null);
      return either == null ? null : either.left().orElse((LevelChunk)null);
   }

   @Nullable
   public LevelChunk getFullChunk() {
      CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getFullChunkFuture();
      Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = completablefuture.getNow((Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)null);
      return either == null ? null : either.left().orElse((LevelChunk)null);
   }

   @Nullable
   public ChunkStatus getLastAvailableStatus() {
      for(int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
         ChunkStatus chunkstatus = CHUNK_STATUSES.get(i);
         CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getFutureIfPresentUnchecked(chunkstatus);
         if (completablefuture.getNow(UNLOADED_CHUNK).left().isPresent()) {
            return chunkstatus;
         }
      }

      return null;
   }

   @Nullable
   public ChunkAccess getLastAvailable() {
      for(int i = CHUNK_STATUSES.size() - 1; i >= 0; --i) {
         ChunkStatus chunkstatus = CHUNK_STATUSES.get(i);
         CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getFutureIfPresentUnchecked(chunkstatus);
         if (!completablefuture.isCompletedExceptionally()) {
            Optional<ChunkAccess> optional = completablefuture.getNow(UNLOADED_CHUNK).left();
            if (optional.isPresent()) {
               return optional.get();
            }
         }
      }

      return null;
   }

   public CompletableFuture<ChunkAccess> getChunkToSave() {
      return this.chunkToSave;
   }

   public void blockChanged(BlockPos pPos) {
      LevelChunk levelchunk = this.getTickingChunk();
      if (levelchunk != null) {
         int i = this.levelHeightAccessor.getSectionIndex(pPos.getY());
         if (this.changedBlocksPerSection[i] == null) {
            this.hasChangedSections = true;
            this.changedBlocksPerSection[i] = new ShortOpenHashSet();
         }

         this.changedBlocksPerSection[i].add(SectionPos.sectionRelativePos(pPos));
      }
   }

   public void sectionLightChanged(LightLayer pType, int pSectionY) {
      Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = this.getFutureIfPresent(ChunkStatus.INITIALIZE_LIGHT).getNow((Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>)null);
      if (either != null) {
         ChunkAccess chunkaccess = either.left().orElse((ChunkAccess)null);
         if (chunkaccess != null) {
            chunkaccess.setUnsaved(true);
            LevelChunk levelchunk = this.getTickingChunk();
            if (levelchunk != null) {
               int i = this.lightEngine.getMinLightSection();
               int j = this.lightEngine.getMaxLightSection();
               if (pSectionY >= i && pSectionY <= j) {
                  int k = pSectionY - i;
                  if (pType == LightLayer.SKY) {
                     this.skyChangedLightSectionFilter.set(k);
                  } else {
                     this.blockChangedLightSectionFilter.set(k);
                  }

               }
            }
         }
      }
   }

   public void broadcastChanges(LevelChunk pChunk) {
      if (this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
         Level level = pChunk.getLevel();
         if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            List<ServerPlayer> list = this.playerProvider.getPlayers(this.pos, true);
            if (!list.isEmpty()) {
               ClientboundLightUpdatePacket clientboundlightupdatepacket = new ClientboundLightUpdatePacket(pChunk.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter);
               this.broadcast(list, clientboundlightupdatepacket);
            }

            this.skyChangedLightSectionFilter.clear();
            this.blockChangedLightSectionFilter.clear();
         }

         if (this.hasChangedSections) {
            List<ServerPlayer> list1 = this.playerProvider.getPlayers(this.pos, false);

            for(int j = 0; j < this.changedBlocksPerSection.length; ++j) {
               ShortSet shortset = this.changedBlocksPerSection[j];
               if (shortset != null) {
                  this.changedBlocksPerSection[j] = null;
                  if (!list1.isEmpty()) {
                     int i = this.levelHeightAccessor.getSectionYFromSectionIndex(j);
                     SectionPos sectionpos = SectionPos.of(pChunk.getPos(), i);
                     if (shortset.size() == 1) {
                        BlockPos blockpos = sectionpos.relativeToBlockPos(shortset.iterator().nextShort());
                        BlockState blockstate = level.getBlockState(blockpos);
                        this.broadcast(list1, new ClientboundBlockUpdatePacket(blockpos, blockstate));
                        this.broadcastBlockEntityIfNeeded(list1, level, blockpos, blockstate);
                     } else {
                        LevelChunkSection levelchunksection = pChunk.getSection(j);
                        ClientboundSectionBlocksUpdatePacket clientboundsectionblocksupdatepacket = new ClientboundSectionBlocksUpdatePacket(sectionpos, shortset, levelchunksection);
                        this.broadcast(list1, clientboundsectionblocksupdatepacket);
                        clientboundsectionblocksupdatepacket.runUpdates((p_288761_, p_288762_) -> {
                           this.broadcastBlockEntityIfNeeded(list1, level, p_288761_, p_288762_);
                        });
                     }
                  }
               }
            }

            this.hasChangedSections = false;
         }
      }
   }

   private void broadcastBlockEntityIfNeeded(List<ServerPlayer> pPlayers, Level pLevel, BlockPos pPos, BlockState pState) {
      if (pState.hasBlockEntity()) {
         this.broadcastBlockEntity(pPlayers, pLevel, pPos);
      }

   }

   private void broadcastBlockEntity(List<ServerPlayer> pPlayers, Level pLevel, BlockPos pPox) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPox);
      if (blockentity != null) {
         Packet<?> packet = blockentity.getUpdatePacket();
         if (packet != null) {
            this.broadcast(pPlayers, packet);
         }
      }

   }

   private void broadcast(List<ServerPlayer> pPlayers, Packet<?> pPacket) {
      pPlayers.forEach((p_140062_) -> {
         p_140062_.connection.send(pPacket);
      });
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getOrScheduleFuture(ChunkStatus pStatus, ChunkMap pMap) {
      int i = pStatus.getIndex();
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.futures.get(i);
      if (completablefuture != null) {
         Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = completablefuture.getNow(NOT_DONE_YET);
         if (either == null) {
            String s = "value in future for status: " + pStatus + " was incorrectly set to null at chunk: " + this.pos;
            throw pMap.debugFuturesAndCreateReportedException(new IllegalStateException("null value previously set for chunk status"), s);
         }

         if (either == NOT_DONE_YET || either.right().isEmpty()) {
            return completablefuture;
         }
      }

      if (ChunkLevel.generationStatus(this.ticketLevel).isOrAfter(pStatus)) {
         CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture1 = pMap.schedule(this, pStatus);
         this.updateChunkToSave(completablefuture1, "schedule " + pStatus);
         this.futures.set(i, completablefuture1);
         return completablefuture1;
      } else {
         return completablefuture == null ? UNLOADED_CHUNK_FUTURE : completablefuture;
      }
   }

   protected void addSaveDependency(String pSource, CompletableFuture<?> pFuture) {
      if (this.chunkToSaveHistory != null) {
         this.chunkToSaveHistory.push(new ChunkHolder.ChunkSaveDebug(Thread.currentThread(), pFuture, pSource));
      }

      this.chunkToSave = this.chunkToSave.thenCombine(pFuture, (p_200414_, p_200415_) -> {
         return p_200414_;
      });
   }

   private void updateChunkToSave(CompletableFuture<? extends Either<? extends ChunkAccess, ChunkHolder.ChunkLoadingFailure>> pFeature, String pSource) {
      if (this.chunkToSaveHistory != null) {
         this.chunkToSaveHistory.push(new ChunkHolder.ChunkSaveDebug(Thread.currentThread(), pFeature, pSource));
      }

      this.chunkToSave = this.chunkToSave.thenCombine(pFeature, (p_200411_, p_200412_) -> {
         return p_200412_.map((p_200406_) -> {
            return p_200406_;
         }, (p_200409_) -> {
            return p_200411_;
         });
      });
   }

   public FullChunkStatus getFullStatus() {
      return ChunkLevel.fullStatus(this.ticketLevel);
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   public int getTicketLevel() {
      return this.ticketLevel;
   }

   public int getQueueLevel() {
      return this.queueLevel;
   }

   private void setQueueLevel(int p_140087_) {
      this.queueLevel = p_140087_;
   }

   public void setTicketLevel(int pLevel) {
      this.ticketLevel = pLevel;
   }

   private void scheduleFullChunkPromotion(ChunkMap pChunkMap, CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> pFuture, Executor pExecutor, FullChunkStatus pFullChunkStatus) {
      this.pendingFullStateConfirmation.cancel(false);
      CompletableFuture<Void> completablefuture = new CompletableFuture<>();
      completablefuture.thenRunAsync(() -> {
         pChunkMap.onFullChunkStatusChange(this.pos, pFullChunkStatus);
      }, pExecutor);
      this.pendingFullStateConfirmation = completablefuture;
      pFuture.thenAccept((p_200421_) -> {
         p_200421_.ifLeft((p_200424_) -> {
            completablefuture.complete((Void)null);
         });
      });
   }

   private void demoteFullChunk(ChunkMap pChunkMap, FullChunkStatus pFullChunkStatus) {
      this.pendingFullStateConfirmation.cancel(false);
      pChunkMap.onFullChunkStatusChange(this.pos, pFullChunkStatus);
   }

   protected void updateFutures(ChunkMap pChunkMap, Executor pExecutor) {
      ChunkStatus chunkstatus = ChunkLevel.generationStatus(this.oldTicketLevel);
      ChunkStatus chunkstatus1 = ChunkLevel.generationStatus(this.ticketLevel);
      boolean flag = ChunkLevel.isLoaded(this.oldTicketLevel);
      boolean flag1 = ChunkLevel.isLoaded(this.ticketLevel);
      FullChunkStatus fullchunkstatus = ChunkLevel.fullStatus(this.oldTicketLevel);
      FullChunkStatus fullchunkstatus1 = ChunkLevel.fullStatus(this.ticketLevel);
      if (flag) {
         Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = Either.right(new ChunkHolder.ChunkLoadingFailure() {
            public String toString() {
               return "Unloaded ticket level " + ChunkHolder.this.pos;
            }
         });

         for(int i = flag1 ? chunkstatus1.getIndex() + 1 : 0; i <= chunkstatus.getIndex(); ++i) {
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.futures.get(i);
            if (completablefuture == null) {
               this.futures.set(i, CompletableFuture.completedFuture(either));
            }
         }
      }

      boolean flag5 = fullchunkstatus.isOrAfter(FullChunkStatus.FULL);
      boolean flag6 = fullchunkstatus1.isOrAfter(FullChunkStatus.FULL);
      this.wasAccessibleSinceLastSave |= flag6;
      if (!flag5 && flag6) {
         this.fullChunkFuture = pChunkMap.prepareAccessibleChunk(this);
         this.scheduleFullChunkPromotion(pChunkMap, this.fullChunkFuture, pExecutor, FullChunkStatus.FULL);
         this.updateChunkToSave(this.fullChunkFuture, "full");
      }

      if (flag5 && !flag6) {
         this.fullChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
         this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
      }

      boolean flag7 = fullchunkstatus.isOrAfter(FullChunkStatus.BLOCK_TICKING);
      boolean flag2 = fullchunkstatus1.isOrAfter(FullChunkStatus.BLOCK_TICKING);
      if (!flag7 && flag2) {
         this.tickingChunkFuture = pChunkMap.prepareTickingChunk(this);
         this.scheduleFullChunkPromotion(pChunkMap, this.tickingChunkFuture, pExecutor, FullChunkStatus.BLOCK_TICKING);
         this.updateChunkToSave(this.tickingChunkFuture, "ticking");
      }

      if (flag7 && !flag2) {
         this.tickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
         this.tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
      }

      boolean flag3 = fullchunkstatus.isOrAfter(FullChunkStatus.ENTITY_TICKING);
      boolean flag4 = fullchunkstatus1.isOrAfter(FullChunkStatus.ENTITY_TICKING);
      if (!flag3 && flag4) {
         if (this.entityTickingChunkFuture != UNLOADED_LEVEL_CHUNK_FUTURE) {
            throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
         }

         this.entityTickingChunkFuture = pChunkMap.prepareEntityTickingChunk(this);
         this.scheduleFullChunkPromotion(pChunkMap, this.entityTickingChunkFuture, pExecutor, FullChunkStatus.ENTITY_TICKING);
         this.updateChunkToSave(this.entityTickingChunkFuture, "entity ticking");
      }

      if (flag3 && !flag4) {
         this.entityTickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
         this.entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
      }

      if (!fullchunkstatus1.isOrAfter(fullchunkstatus)) {
         this.demoteFullChunk(pChunkMap, fullchunkstatus1);
      }

      this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
      this.oldTicketLevel = this.ticketLevel;
   }

   public boolean wasAccessibleSinceLastSave() {
      return this.wasAccessibleSinceLastSave;
   }

   public void refreshAccessibility() {
      this.wasAccessibleSinceLastSave = ChunkLevel.fullStatus(this.ticketLevel).isOrAfter(FullChunkStatus.FULL);
   }

   public void replaceProtoChunk(ImposterProtoChunk pImposter) {
      for(int i = 0; i < this.futures.length(); ++i) {
         CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.futures.get(i);
         if (completablefuture != null) {
            Optional<ChunkAccess> optional = completablefuture.getNow(UNLOADED_CHUNK).left();
            if (!optional.isEmpty() && optional.get() instanceof ProtoChunk) {
               this.futures.set(i, CompletableFuture.completedFuture(Either.left(pImposter)));
            }
         }
      }

      this.updateChunkToSave(CompletableFuture.completedFuture(Either.left(pImposter.getWrapped())), "replaceProto");
   }

   public List<Pair<ChunkStatus, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>> getAllFutures() {
      List<Pair<ChunkStatus, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>> list = new ArrayList<>();

      for(int i = 0; i < CHUNK_STATUSES.size(); ++i) {
         list.add(Pair.of(CHUNK_STATUSES.get(i), this.futures.get(i)));
      }

      return list;
   }

   public interface ChunkLoadingFailure {
      ChunkHolder.ChunkLoadingFailure UNLOADED = new ChunkHolder.ChunkLoadingFailure() {
         public String toString() {
            return "UNLOADED";
         }
      };
   }

   static final class ChunkSaveDebug {
      private final Thread thread;
      private final CompletableFuture<?> future;
      private final String source;

      ChunkSaveDebug(Thread pThread, CompletableFuture<?> pFuture, String pSource) {
         this.thread = pThread;
         this.future = pFuture;
         this.source = pSource;
      }
   }

   @FunctionalInterface
   public interface LevelChangeListener {
      void onLevelChange(ChunkPos pChunkPos, IntSupplier p_140120_, int p_140121_, IntConsumer p_140122_);
   }

   public interface PlayerProvider {
      /**
       * Returns the players tracking the given chunk.
       */
      List<ServerPlayer> getPlayers(ChunkPos pPos, boolean pBoundaryOnly);
   }
}
