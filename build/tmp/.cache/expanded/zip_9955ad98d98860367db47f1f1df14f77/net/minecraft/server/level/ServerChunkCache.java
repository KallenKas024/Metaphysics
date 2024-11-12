package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;

public class ServerChunkCache extends ChunkSource {
   private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
   private final DistanceManager distanceManager;
   public final ServerLevel level;
   final Thread mainThread;
   final ThreadedLevelLightEngine lightEngine;
   private final ServerChunkCache.MainThreadExecutor mainThreadProcessor;
   public final ChunkMap chunkMap;
   private final DimensionDataStorage dataStorage;
   private long lastInhabitedUpdate;
   private boolean spawnEnemies = true;
   private boolean spawnFriendlies = true;
   private static final int CACHE_SIZE = 4;
   private final long[] lastChunkPos = new long[4];
   private final ChunkStatus[] lastChunkStatus = new ChunkStatus[4];
   private final ChunkAccess[] lastChunk = new ChunkAccess[4];
   @Nullable
   @VisibleForDebug
   private NaturalSpawner.SpawnState lastSpawnState;

   public ServerChunkCache(ServerLevel pLevel, LevelStorageSource.LevelStorageAccess pLevelStorageAccess, DataFixer pFixerUpper, StructureTemplateManager pStructureManager, Executor pDispatcher, ChunkGenerator pGenerator, int pViewDistance, int pSimulationDistance, boolean pSync, ChunkProgressListener pProgressListener, ChunkStatusUpdateListener pChunkStatusListener, Supplier<DimensionDataStorage> pOverworldDataStorage) {
      this.level = pLevel;
      this.mainThreadProcessor = new ServerChunkCache.MainThreadExecutor(pLevel);
      this.mainThread = Thread.currentThread();
      File file1 = pLevelStorageAccess.getDimensionPath(pLevel.dimension()).resolve("data").toFile();
      file1.mkdirs();
      this.dataStorage = new DimensionDataStorage(file1, pFixerUpper);
      this.chunkMap = new ChunkMap(pLevel, pLevelStorageAccess, pFixerUpper, pStructureManager, pDispatcher, this.mainThreadProcessor, this, pGenerator, pProgressListener, pChunkStatusListener, pOverworldDataStorage, pViewDistance, pSync);
      this.lightEngine = this.chunkMap.getLightEngine();
      this.distanceManager = this.chunkMap.getDistanceManager();
      this.distanceManager.updateSimulationDistance(pSimulationDistance);
      this.clearCache();
   }

   public ThreadedLevelLightEngine getLightEngine() {
      return this.lightEngine;
   }

   @Nullable
   private ChunkHolder getVisibleChunkIfPresent(long pChunkPos) {
      return this.chunkMap.getVisibleChunkIfPresent(pChunkPos);
   }

   public int getTickingGenerated() {
      return this.chunkMap.getTickingGenerated();
   }

   private void storeInCache(long pChunkPos, ChunkAccess pChunk, ChunkStatus pChunkStatus) {
      for(int i = 3; i > 0; --i) {
         this.lastChunkPos[i] = this.lastChunkPos[i - 1];
         this.lastChunkStatus[i] = this.lastChunkStatus[i - 1];
         this.lastChunk[i] = this.lastChunk[i - 1];
      }

      this.lastChunkPos[0] = pChunkPos;
      this.lastChunkStatus[0] = pChunkStatus;
      this.lastChunk[0] = pChunk;
   }

   /**
    * Gets the chunk at the provided position, if it exists.
    * Note: This method <strong>can deadlock</strong> when called from within an existing chunk load, as it will be
    * stuck waiting for the current chunk to load!
    * @param pLoad If this should force a chunk load. When {@code false}, this will return null if the chunk is not
    * loaded.
    */
   @Nullable
   public ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
      if (Thread.currentThread() != this.mainThread) {
         return CompletableFuture.supplyAsync(() -> {
            return this.getChunk(pChunkX, pChunkZ, pRequiredStatus, pLoad);
         }, this.mainThreadProcessor).join();
      } else {
         ProfilerFiller profilerfiller = this.level.getProfiler();
         profilerfiller.incrementCounter("getChunk");
         long i = ChunkPos.asLong(pChunkX, pChunkZ);

         for(int j = 0; j < 4; ++j) {
            if (i == this.lastChunkPos[j] && pRequiredStatus == this.lastChunkStatus[j]) {
               ChunkAccess chunkaccess = this.lastChunk[j];
               if (chunkaccess != null || !pLoad) {
                  return chunkaccess;
               }
            }
         }

         profilerfiller.incrementCounter("getChunkCacheMiss");
         CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getChunkFutureMainThread(pChunkX, pChunkZ, pRequiredStatus, pLoad);
         this.mainThreadProcessor.managedBlock(completablefuture::isDone);
         ChunkAccess chunkaccess1 = completablefuture.join().map((p_8406_) -> {
            return p_8406_;
         }, (p_8423_) -> {
            if (pLoad) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + p_8423_));
            } else {
               return null;
            }
         });
         this.storeInCache(i, chunkaccess1, pRequiredStatus);
         return chunkaccess1;
      }
   }

   @Nullable
   public LevelChunk getChunkNow(int pChunkX, int pChunkZ) {
      if (Thread.currentThread() != this.mainThread) {
         return null;
      } else {
         this.level.getProfiler().incrementCounter("getChunkNow");
         long i = ChunkPos.asLong(pChunkX, pChunkZ);

         for(int j = 0; j < 4; ++j) {
            if (i == this.lastChunkPos[j] && this.lastChunkStatus[j] == ChunkStatus.FULL) {
               ChunkAccess chunkaccess = this.lastChunk[j];
               return chunkaccess instanceof LevelChunk ? (LevelChunk)chunkaccess : null;
            }
         }

         ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);
         if (chunkholder == null) {
            return null;
         } else {
            if (chunkholder.currentlyLoading != null) return chunkholder.currentlyLoading; // Forge: If the requested chunk is loading, bypass the future chain to prevent a deadlock.
            Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = chunkholder.getFutureIfPresent(ChunkStatus.FULL).getNow((Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>)null);
            if (either == null) {
               return null;
            } else {
               ChunkAccess chunkaccess1 = either.left().orElse((ChunkAccess)null);
               if (chunkaccess1 != null) {
                  this.storeInCache(i, chunkaccess1, ChunkStatus.FULL);
                  if (chunkaccess1 instanceof LevelChunk) {
                     return (LevelChunk)chunkaccess1;
                  }
               }

               return null;
            }
         }
      }
   }

   private void clearCache() {
      Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
      Arrays.fill(this.lastChunkStatus, (Object)null);
      Arrays.fill(this.lastChunk, (Object)null);
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture(int pX, int pY, ChunkStatus pChunkStatus, boolean pLoad) {
      boolean flag = Thread.currentThread() == this.mainThread;
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture;
      if (flag) {
         completablefuture = this.getChunkFutureMainThread(pX, pY, pChunkStatus, pLoad);
         this.mainThreadProcessor.managedBlock(completablefuture::isDone);
      } else {
         completablefuture = CompletableFuture.supplyAsync(() -> {
            return this.getChunkFutureMainThread(pX, pY, pChunkStatus, pLoad);
         }, this.mainThreadProcessor).thenCompose((p_8413_) -> {
            return p_8413_;
         });
      }

      return completablefuture;
   }

   private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFutureMainThread(int pX, int pY, ChunkStatus pChunkStatus, boolean pLoad) {
      ChunkPos chunkpos = new ChunkPos(pX, pY);
      long i = chunkpos.toLong();
      int j = ChunkLevel.byStatus(pChunkStatus);
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);
      if (pLoad) {
         this.distanceManager.addTicket(TicketType.UNKNOWN, chunkpos, j, chunkpos);
         if (this.chunkAbsent(chunkholder, j)) {
            ProfilerFiller profilerfiller = this.level.getProfiler();
            profilerfiller.push("chunkLoad");
            this.runDistanceManagerUpdates();
            chunkholder = this.getVisibleChunkIfPresent(i);
            profilerfiller.pop();
            if (this.chunkAbsent(chunkholder, j)) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
            }
         }
      }

      return this.chunkAbsent(chunkholder, j) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : chunkholder.getOrScheduleFuture(pChunkStatus, this.chunkMap);
   }

   private boolean chunkAbsent(@Nullable ChunkHolder pChunkHolder, int pStatus) {
      return pChunkHolder == null || pChunkHolder.getTicketLevel() > pStatus;
   }

   /**
    * @return {@code true} if a chunk is loaded at the provided position, without forcing a chunk load.
    */
   public boolean hasChunk(int pX, int pZ) {
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent((new ChunkPos(pX, pZ)).toLong());
      int i = ChunkLevel.byStatus(ChunkStatus.FULL);
      return !this.chunkAbsent(chunkholder, i);
   }

   @Nullable
   public LightChunk getChunkForLighting(int pChunkX, int pChunkZ) {
      long i = ChunkPos.asLong(pChunkX, pChunkZ);
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);
      if (chunkholder == null) {
         return null;
      } else {
         int j = CHUNK_STATUSES.size() - 1;

         while(true) {
            ChunkStatus chunkstatus = CHUNK_STATUSES.get(j);
            Optional<ChunkAccess> optional = chunkholder.getFutureIfPresentUnchecked(chunkstatus).getNow(ChunkHolder.UNLOADED_CHUNK).left();
            if (optional.isPresent()) {
               return optional.get();
            }

            if (chunkstatus == ChunkStatus.INITIALIZE_LIGHT.getParent()) {
               return null;
            }

            --j;
         }
      }
   }

   public Level getLevel() {
      return this.level;
   }

   public boolean pollTask() {
      return this.mainThreadProcessor.pollTask();
   }

   boolean runDistanceManagerUpdates() {
      boolean flag = this.distanceManager.runAllUpdates(this.chunkMap);
      boolean flag1 = this.chunkMap.promoteChunkMap();
      if (!flag && !flag1) {
         return false;
      } else {
         this.clearCache();
         return true;
      }
   }

   public boolean isPositionTicking(long pChunkPos) {
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(pChunkPos);
      if (chunkholder == null) {
         return false;
      } else if (!this.level.shouldTickBlocksAt(pChunkPos)) {
         return false;
      } else {
         Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = chunkholder.getTickingChunkFuture().getNow((Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)null);
         return either != null && either.left().isPresent();
      }
   }

   public void save(boolean pFlush) {
      this.runDistanceManagerUpdates();
      this.chunkMap.saveAllChunks(pFlush);
   }

   public void close() throws IOException {
      this.save(true);
      this.lightEngine.close();
      this.chunkMap.close();
   }

   public void tick(BooleanSupplier pHasTimeLeft, boolean pTickChunks) {
      this.level.getProfiler().push("purge");
      this.distanceManager.purgeStaleTickets();
      this.runDistanceManagerUpdates();
      this.level.getProfiler().popPush("chunks");
      if (pTickChunks) {
         this.tickChunks();
      }

      this.level.getProfiler().popPush("unload");
      this.chunkMap.tick(pHasTimeLeft);
      this.level.getProfiler().pop();
      this.clearCache();
   }

   private void tickChunks() {
      long i = this.level.getGameTime();
      long j = i - this.lastInhabitedUpdate;
      this.lastInhabitedUpdate = i;
      boolean flag = this.level.isDebug();
      if (flag) {
         this.chunkMap.tick();
      } else {
         LevelData leveldata = this.level.getLevelData();
         ProfilerFiller profilerfiller = this.level.getProfiler();
         profilerfiller.push("pollingChunks");
         int k = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
         boolean flag1 = leveldata.getGameTime() % 400L == 0L;
         profilerfiller.push("naturalSpawnCount");
         int l = this.distanceManager.getNaturalSpawnChunkCount();
         NaturalSpawner.SpawnState naturalspawner$spawnstate = NaturalSpawner.createState(l, this.level.getAllEntities(), this::getFullChunk, new LocalMobCapCalculator(this.chunkMap));
         this.lastSpawnState = naturalspawner$spawnstate;
         profilerfiller.popPush("filteringLoadedChunks");
         List<ServerChunkCache.ChunkAndHolder> list = Lists.newArrayListWithCapacity(l);

         for(ChunkHolder chunkholder : this.chunkMap.getChunks()) {
            LevelChunk levelchunk = chunkholder.getTickingChunk();
            if (levelchunk != null) {
               list.add(new ServerChunkCache.ChunkAndHolder(levelchunk, chunkholder));
            }
         }

         profilerfiller.popPush("spawnAndTick");
         boolean flag2 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
         Collections.shuffle(list);

         for(ServerChunkCache.ChunkAndHolder serverchunkcache$chunkandholder : list) {
            LevelChunk levelchunk1 = serverchunkcache$chunkandholder.chunk;
            ChunkPos chunkpos = levelchunk1.getPos();
            if ((this.level.isNaturalSpawningAllowed(chunkpos) && this.chunkMap.anyPlayerCloseEnoughForSpawning(chunkpos)) || this.distanceManager.shouldForceTicks(chunkpos.toLong())) {
               levelchunk1.incrementInhabitedTime(j);
               if (flag2 && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(chunkpos)) {
                  NaturalSpawner.spawnForChunk(this.level, levelchunk1, naturalspawner$spawnstate, this.spawnFriendlies, this.spawnEnemies, flag1);
               }

               if (this.level.shouldTickBlocksAt(chunkpos.toLong())) {
                  this.level.tickChunk(levelchunk1, k);
               }
            }
         }

         profilerfiller.popPush("customSpawners");
         if (flag2) {
            this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
         }

         profilerfiller.popPush("broadcast");
         list.forEach((p_184022_) -> {
            p_184022_.holder.broadcastChanges(p_184022_.chunk);
         });
         profilerfiller.pop();
         profilerfiller.pop();
         this.chunkMap.tick();
      }
   }

   private void getFullChunk(long p_8371_, Consumer<LevelChunk> p_8372_) {
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_8371_);
      if (chunkholder != null) {
         chunkholder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().ifPresent(p_8372_);
      }

   }

   /**
    * @return A human-readable string representing data about this chunk source.
    */
   public String gatherStats() {
      return Integer.toString(this.getLoadedChunksCount());
   }

   @VisibleForTesting
   public int getPendingTasksCount() {
      return this.mainThreadProcessor.getPendingTasksCount();
   }

   public ChunkGenerator getGenerator() {
      return this.chunkMap.generator();
   }

   public ChunkGeneratorStructureState getGeneratorState() {
      return this.chunkMap.generatorState();
   }

   public RandomState randomState() {
      return this.chunkMap.randomState();
   }

   public int getLoadedChunksCount() {
      return this.chunkMap.size();
   }

   public void blockChanged(BlockPos pPos) {
      int i = SectionPos.blockToSectionCoord(pPos.getX());
      int j = SectionPos.blockToSectionCoord(pPos.getZ());
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(ChunkPos.asLong(i, j));
      if (chunkholder != null) {
         chunkholder.blockChanged(pPos);
      }

   }

   public void onLightUpdate(LightLayer pType, SectionPos pPos) {
      this.mainThreadProcessor.execute(() -> {
         ChunkHolder chunkholder = this.getVisibleChunkIfPresent(pPos.chunk().toLong());
         if (chunkholder != null) {
            chunkholder.sectionLightChanged(pType, pPos.y());
         }

      });
   }

   public <T> void addRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue) {
      addRegionTicket(pType, pPos, pDistance, pValue, false);
   }
   public <T> void addRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue, boolean forceTicks) {
      this.distanceManager.addRegionTicket(pType, pPos, pDistance, pValue, forceTicks);
   }

   public <T> void removeRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue) {
      removeRegionTicket(pType, pPos, pDistance, pValue, false);
   }
   public <T> void removeRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue, boolean forceTicks) {
      this.distanceManager.removeRegionTicket(pType, pPos, pDistance, pValue, forceTicks);
   }

   public void updateChunkForced(ChunkPos pPos, boolean pAdd) {
      this.distanceManager.updateChunkForced(pPos, pAdd);
   }

   public void move(ServerPlayer pPlayer) {
      if (!pPlayer.isRemoved()) {
         this.chunkMap.move(pPlayer);
      }

   }

   public void removeEntity(Entity pEntity) {
      this.chunkMap.removeEntity(pEntity);
   }

   public void addEntity(Entity pEntity) {
      this.chunkMap.addEntity(pEntity);
   }

   public void broadcastAndSend(Entity pEntity, Packet<?> pPacket) {
      this.chunkMap.broadcastAndSend(pEntity, pPacket);
   }

   public void broadcast(Entity pEntity, Packet<?> pPacket) {
      this.chunkMap.broadcast(pEntity, pPacket);
   }

   public void setViewDistance(int pViewDistance) {
      this.chunkMap.setViewDistance(pViewDistance);
   }

   public void setSimulationDistance(int pSimulationDistance) {
      this.distanceManager.updateSimulationDistance(pSimulationDistance);
   }

   public void setSpawnSettings(boolean pHostile, boolean pPeaceful) {
      this.spawnEnemies = pHostile;
      this.spawnFriendlies = pPeaceful;
   }

   public String getChunkDebugData(ChunkPos pChunkPos) {
      return this.chunkMap.getChunkDebugData(pChunkPos);
   }

   public DimensionDataStorage getDataStorage() {
      return this.dataStorage;
   }

   public PoiManager getPoiManager() {
      return this.chunkMap.getPoiManager();
   }

   public ChunkScanAccess chunkScanner() {
      return this.chunkMap.chunkScanner();
   }

   @Nullable
   @VisibleForDebug
   public NaturalSpawner.SpawnState getLastSpawnState() {
      return this.lastSpawnState;
   }

   public void removeTicketsOnClosing() {
      this.distanceManager.removeTicketsOnClosing();
   }

   static record ChunkAndHolder(LevelChunk chunk, ChunkHolder holder) {
   }

   final class MainThreadExecutor extends BlockableEventLoop<Runnable> {
      MainThreadExecutor(Level pLevel) {
         super("Chunk source main thread executor for " + pLevel.dimension().location());
      }

      /**
       * Wraps the given runnable task. In this case, the original runnable is returned as-is.
       * <p>
       * @return The wrapped runnable task
       * @param pRunnable The original runnable task
       */
      protected Runnable wrapRunnable(Runnable pRunnable) {
         return pRunnable;
      }

      /**
       * Determines whether the given runnable task should be run or not.
       * It depends on the shutdown state of the SoundEngineExecutor.
       * <p>
       * @return true if the task should run, false otherwise
       * @param pRunnable The runnable task
       */
      protected boolean shouldRun(Runnable pRunnable) {
         return true;
      }

      protected boolean scheduleExecutables() {
         return true;
      }

      /**
       * {@return The currently running thread}
       */
      protected Thread getRunningThread() {
         return ServerChunkCache.this.mainThread;
      }

      protected void doRunTask(Runnable pTask) {
         ServerChunkCache.this.level.getProfiler().incrementCounter("runTask");
         super.doRunTask(pTask);
      }

      public boolean pollTask() {
         if (ServerChunkCache.this.runDistanceManagerUpdates()) {
            return true;
         } else {
            ServerChunkCache.this.lightEngine.tryScheduleUpdate();
            return super.pollTask();
         }
      }
   }
}
