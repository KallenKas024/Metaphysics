package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.slf4j.Logger;

public class ThreadedLevelLightEngine extends LevelLightEngine implements AutoCloseable {
   public static final int DEFAULT_BATCH_SIZE = 1000;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ProcessorMailbox<Runnable> taskMailbox;
   private final ObjectList<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> lightTasks = new ObjectArrayList<>();
   private final ChunkMap chunkMap;
   private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> sorterMailbox;
   private final int taskPerBatch = 1000;
   private final AtomicBoolean scheduled = new AtomicBoolean();

   public ThreadedLevelLightEngine(LightChunkGetter pLightChunk, ChunkMap pChunkMap, boolean pSkyLight, ProcessorMailbox<Runnable> pTaskMailbox, ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> pSorterMailbox) {
      super(pLightChunk, true, pSkyLight);
      this.chunkMap = pChunkMap;
      this.sorterMailbox = pSorterMailbox;
      this.taskMailbox = pTaskMailbox;
   }

   public void close() {
   }

   public int runLightUpdates() {
      throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Ran automatically on a different thread!"));
   }

   public void checkBlock(BlockPos pPos) {
      BlockPos blockpos = pPos.immutable();
      this.addTask(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()), ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.checkBlock(blockpos);
      }, () -> {
         return "checkBlock " + blockpos;
      }));
   }

   protected void updateChunkStatus(ChunkPos pChunkPos) {
      this.addTask(pChunkPos.x, pChunkPos.z, () -> {
         return 0;
      }, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.retainData(pChunkPos, false);
         super.setLightEnabled(pChunkPos, false);

         for(int i = this.getMinLightSection(); i < this.getMaxLightSection(); ++i) {
            super.queueSectionData(LightLayer.BLOCK, SectionPos.of(pChunkPos, i), (DataLayer)null);
            super.queueSectionData(LightLayer.SKY, SectionPos.of(pChunkPos, i), (DataLayer)null);
         }

         for(int j = this.levelHeightAccessor.getMinSection(); j < this.levelHeightAccessor.getMaxSection(); ++j) {
            super.updateSectionStatus(SectionPos.of(pChunkPos, j), true);
         }

      }, () -> {
         return "updateChunkStatus " + pChunkPos + " true";
      }));
   }

   public void updateSectionStatus(SectionPos pPos, boolean pIsEmpty) {
      this.addTask(pPos.x(), pPos.z(), () -> {
         return 0;
      }, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.updateSectionStatus(pPos, pIsEmpty);
      }, () -> {
         return "updateSectionStatus " + pPos + " " + pIsEmpty;
      }));
   }

   public void propagateLightSources(ChunkPos pChunkPos) {
      this.addTask(pChunkPos.x, pChunkPos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.propagateLightSources(pChunkPos);
      }, () -> {
         return "propagateLight " + pChunkPos;
      }));
   }

   public void setLightEnabled(ChunkPos pChunkPos, boolean pLightEnabled) {
      this.addTask(pChunkPos.x, pChunkPos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.setLightEnabled(pChunkPos, pLightEnabled);
      }, () -> {
         return "enableLight " + pChunkPos + " " + pLightEnabled;
      }));
   }

   public void queueSectionData(LightLayer pLightLayer, SectionPos pSectionPos, @Nullable DataLayer pDataLayer) {
      this.addTask(pSectionPos.x(), pSectionPos.z(), () -> {
         return 0;
      }, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.queueSectionData(pLightLayer, pSectionPos, pDataLayer);
      }, () -> {
         return "queueData " + pSectionPos;
      }));
   }

   private void addTask(int pChunkX, int pChunkZ, ThreadedLevelLightEngine.TaskType pType, Runnable pTask) {
      this.addTask(pChunkX, pChunkZ, this.chunkMap.getChunkQueueLevel(ChunkPos.asLong(pChunkX, pChunkZ)), pType, pTask);
   }

   private void addTask(int pChunkX, int pChunkZ, IntSupplier pQueueLevelSupplier, ThreadedLevelLightEngine.TaskType pType, Runnable pTask) {
      this.sorterMailbox.tell(ChunkTaskPriorityQueueSorter.message(() -> {
         this.lightTasks.add(Pair.of(pType, pTask));
         if (this.lightTasks.size() >= 1000) {
            this.runUpdate();
         }

      }, ChunkPos.asLong(pChunkX, pChunkZ), pQueueLevelSupplier));
   }

   public void retainData(ChunkPos pPos, boolean pRetain) {
      this.addTask(pPos.x, pPos.z, () -> {
         return 0;
      }, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         super.retainData(pPos, pRetain);
      }, () -> {
         return "retainData " + pPos;
      }));
   }

   public CompletableFuture<ChunkAccess> initializeLight(ChunkAccess pChunk, boolean pLightEnabled) {
      ChunkPos chunkpos = pChunk.getPos();
      this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         LevelChunkSection[] alevelchunksection = pChunk.getSections();

         for(int i = 0; i < pChunk.getSectionsCount(); ++i) {
            LevelChunkSection levelchunksection = alevelchunksection[i];
            if (!levelchunksection.hasOnlyAir()) {
               int j = this.levelHeightAccessor.getSectionYFromSectionIndex(i);
               super.updateSectionStatus(SectionPos.of(chunkpos, j), false);
            }
         }

      }, () -> {
         return "initializeLight: " + chunkpos;
      }));
      return CompletableFuture.supplyAsync(() -> {
         super.setLightEnabled(chunkpos, pLightEnabled);
         super.retainData(chunkpos, false);
         return pChunk;
      }, (p_215135_) -> {
         this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.POST_UPDATE, p_215135_);
      });
   }

   public CompletableFuture<ChunkAccess> lightChunk(ChunkAccess pChunk, boolean pIsLighted) {
      ChunkPos chunkpos = pChunk.getPos();
      pChunk.setLightCorrect(false);
      this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
         if (!pIsLighted) {
            super.propagateLightSources(chunkpos);
         }

      }, () -> {
         return "lightChunk " + chunkpos + " " + pIsLighted;
      }));
      return CompletableFuture.supplyAsync(() -> {
         pChunk.setLightCorrect(true);
         this.chunkMap.releaseLightTicket(chunkpos);
         return pChunk;
      }, (p_280982_) -> {
         this.addTask(chunkpos.x, chunkpos.z, ThreadedLevelLightEngine.TaskType.POST_UPDATE, p_280982_);
      });
   }

   public void tryScheduleUpdate() {
      if ((!this.lightTasks.isEmpty() || super.hasLightWork()) && this.scheduled.compareAndSet(false, true)) {
         this.taskMailbox.tell(() -> {
            this.runUpdate();
            this.scheduled.set(false);
         });
      }

   }

   private void runUpdate() {
      int i = Math.min(this.lightTasks.size(), 1000);
      ObjectListIterator<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> objectlistiterator = this.lightTasks.iterator();

      int j;
      for(j = 0; objectlistiterator.hasNext() && j < i; ++j) {
         Pair<ThreadedLevelLightEngine.TaskType, Runnable> pair = objectlistiterator.next();
         if (pair.getFirst() == ThreadedLevelLightEngine.TaskType.PRE_UPDATE) {
            pair.getSecond().run();
         }
      }

      objectlistiterator.back(j);
      super.runLightUpdates();

      for(int k = 0; objectlistiterator.hasNext() && k < i; ++k) {
         Pair<ThreadedLevelLightEngine.TaskType, Runnable> pair1 = objectlistiterator.next();
         if (pair1.getFirst() == ThreadedLevelLightEngine.TaskType.POST_UPDATE) {
            pair1.getSecond().run();
         }

         objectlistiterator.remove();
      }

   }

   static enum TaskType {
      PRE_UPDATE,
      POST_UPDATE;
   }
}