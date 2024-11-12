package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.function.LongPredicate;
import net.minecraft.util.Mth;

public abstract class DynamicGraphMinFixedPoint {
   public static final long SOURCE = Long.MAX_VALUE;
   private static final int NO_COMPUTED_LEVEL = 255;
   protected final int levelCount;
   private final LeveledPriorityQueue priorityQueue;
   private final Long2ByteMap computedLevels;
   private volatile boolean hasWork;

   protected DynamicGraphMinFixedPoint(int pFirstQueuedLevel, int pWidth, final int pHeight) {
      if (pFirstQueuedLevel >= 254) {
         throw new IllegalArgumentException("Level count must be < 254.");
      } else {
         this.levelCount = pFirstQueuedLevel;
         this.priorityQueue = new LeveledPriorityQueue(pFirstQueuedLevel, pWidth);
         this.computedLevels = new Long2ByteOpenHashMap(pHeight, 0.5F) {
            protected void rehash(int p_75611_) {
               if (p_75611_ > pHeight) {
                  super.rehash(p_75611_);
               }

            }
         };
         this.computedLevels.defaultReturnValue((byte)-1);
      }
   }

   protected void removeFromQueue(long p_75601_) {
      int i = this.computedLevels.remove(p_75601_) & 255;
      if (i != 255) {
         int j = this.getLevel(p_75601_);
         int k = this.calculatePriority(j, i);
         this.priorityQueue.dequeue(p_75601_, k, this.levelCount);
         this.hasWork = !this.priorityQueue.isEmpty();
      }
   }

   public void removeIf(LongPredicate pPredicate) {
      LongList longlist = new LongArrayList();
      this.computedLevels.keySet().forEach((long p_75586_) -> {
         if (pPredicate.test(p_75586_)) {
            longlist.add(p_75586_);
         }

      });
      longlist.forEach((java.util.function.LongConsumer)this::removeFromQueue);
   }

   private int calculatePriority(int pOldLevel, int pNewLevel) {
      return Math.min(Math.min(pOldLevel, pNewLevel), this.levelCount - 1);
   }

   protected void checkNode(long pLevelPos) {
      this.checkEdge(pLevelPos, pLevelPos, this.levelCount - 1, false);
   }

   protected void checkEdge(long pFromPos, long pToPos, int pNewLevel, boolean pIsDecreasing) {
      this.checkEdge(pFromPos, pToPos, pNewLevel, this.getLevel(pToPos), this.computedLevels.get(pToPos) & 255, pIsDecreasing);
      this.hasWork = !this.priorityQueue.isEmpty();
   }

   private void checkEdge(long pFromPos, long pToPos, int pNewLevel, int pPreviousLevel, int pPropagationLevel, boolean pIsDecreasing) {
      if (!this.isSource(pToPos)) {
         pNewLevel = Mth.clamp(pNewLevel, 0, this.levelCount - 1);
         pPreviousLevel = Mth.clamp(pPreviousLevel, 0, this.levelCount - 1);
         boolean flag = pPropagationLevel == 255;
         if (flag) {
            pPropagationLevel = pPreviousLevel;
         }

         int i;
         if (pIsDecreasing) {
            i = Math.min(pPropagationLevel, pNewLevel);
         } else {
            i = Mth.clamp(this.getComputedLevel(pToPos, pFromPos, pNewLevel), 0, this.levelCount - 1);
         }

         int j = this.calculatePriority(pPreviousLevel, pPropagationLevel);
         if (pPreviousLevel != i) {
            int k = this.calculatePriority(pPreviousLevel, i);
            if (j != k && !flag) {
               this.priorityQueue.dequeue(pToPos, j, k);
            }

            this.priorityQueue.enqueue(pToPos, k);
            this.computedLevels.put(pToPos, (byte)i);
         } else if (!flag) {
            this.priorityQueue.dequeue(pToPos, j, this.levelCount);
            this.computedLevels.remove(pToPos);
         }

      }
   }

   protected final void checkNeighbor(long pFromPos, long pToPos, int pSourceLevel, boolean pIsDecreasing) {
      int i = this.computedLevels.get(pToPos) & 255;
      int j = Mth.clamp(this.computeLevelFromNeighbor(pFromPos, pToPos, pSourceLevel), 0, this.levelCount - 1);
      if (pIsDecreasing) {
         this.checkEdge(pFromPos, pToPos, j, this.getLevel(pToPos), i, pIsDecreasing);
      } else {
         boolean flag = i == 255;
         int k;
         if (flag) {
            k = Mth.clamp(this.getLevel(pToPos), 0, this.levelCount - 1);
         } else {
            k = i;
         }

         if (j == k) {
            this.checkEdge(pFromPos, pToPos, this.levelCount - 1, flag ? k : this.getLevel(pToPos), i, pIsDecreasing);
         }
      }

   }

   protected final boolean hasWork() {
      return this.hasWork;
   }

   protected final int runUpdates(int pToUpdateCount) {
      if (this.priorityQueue.isEmpty()) {
         return pToUpdateCount;
      } else {
         while(!this.priorityQueue.isEmpty() && pToUpdateCount > 0) {
            --pToUpdateCount;
            long i = this.priorityQueue.removeFirstLong();
            int j = Mth.clamp(this.getLevel(i), 0, this.levelCount - 1);
            int k = this.computedLevels.remove(i) & 255;
            if (k < j) {
               this.setLevel(i, k);
               this.checkNeighborsAfterUpdate(i, k, true);
            } else if (k > j) {
               this.setLevel(i, this.levelCount - 1);
               if (k != this.levelCount - 1) {
                  this.priorityQueue.enqueue(i, this.calculatePriority(this.levelCount - 1, k));
                  this.computedLevels.put(i, (byte)k);
               }

               this.checkNeighborsAfterUpdate(i, j, false);
            }
         }

         this.hasWork = !this.priorityQueue.isEmpty();
         return pToUpdateCount;
      }
   }

   public int getQueueSize() {
      return this.computedLevels.size();
   }

   protected boolean isSource(long pPos) {
      return pPos == Long.MAX_VALUE;
   }

   /**
    * Computes level propagated from neighbors of specified position with given existing level, excluding the given
    * source position.
    */
   protected abstract int getComputedLevel(long pPos, long pExcludedSourcePos, int pLevel);

   protected abstract void checkNeighborsAfterUpdate(long pPos, int pLevel, boolean pIsDecreasing);

   protected abstract int getLevel(long pChunkPos);

   protected abstract void setLevel(long pChunkPos, int pLevel);

   /**
    * Returns level propagated from start position with specified level to the neighboring end position.
    */
   protected abstract int computeLevelFromNeighbor(long pStartPos, long pEndPos, int pStartLevel);
}