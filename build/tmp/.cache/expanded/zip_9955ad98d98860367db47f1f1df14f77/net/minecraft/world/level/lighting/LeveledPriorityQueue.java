package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

public class LeveledPriorityQueue {
   private final int levelCount;
   private final LongLinkedOpenHashSet[] queues;
   private int firstQueuedLevel;

   public LeveledPriorityQueue(int pLevelCount, final int pExpectedSize) {
      this.levelCount = pLevelCount;
      this.queues = new LongLinkedOpenHashSet[pLevelCount];

      for(int i = 0; i < pLevelCount; ++i) {
         this.queues[i] = new LongLinkedOpenHashSet(pExpectedSize, 0.5F) {
            protected void rehash(int p_278313_) {
               if (p_278313_ > pExpectedSize) {
                  super.rehash(p_278313_);
               }

            }
         };
      }

      this.firstQueuedLevel = pLevelCount;
   }

   public long removeFirstLong() {
      LongLinkedOpenHashSet longlinkedopenhashset = this.queues[this.firstQueuedLevel];
      long i = longlinkedopenhashset.removeFirstLong();
      if (longlinkedopenhashset.isEmpty()) {
         this.checkFirstQueuedLevel(this.levelCount);
      }

      return i;
   }

   public boolean isEmpty() {
      return this.firstQueuedLevel >= this.levelCount;
   }

   public void dequeue(long pValue, int pLevelIndex, int pEndIndex) {
      LongLinkedOpenHashSet longlinkedopenhashset = this.queues[pLevelIndex];
      longlinkedopenhashset.remove(pValue);
      if (longlinkedopenhashset.isEmpty() && this.firstQueuedLevel == pLevelIndex) {
         this.checkFirstQueuedLevel(pEndIndex);
      }

   }

   public void enqueue(long pValue, int pLevelIndex) {
      this.queues[pLevelIndex].add(pValue);
      if (this.firstQueuedLevel > pLevelIndex) {
         this.firstQueuedLevel = pLevelIndex;
      }

   }

   private void checkFirstQueuedLevel(int pEndLevelIndex) {
      int i = this.firstQueuedLevel;
      this.firstQueuedLevel = pEndLevelIndex;

      for(int j = i + 1; j < pEndLevelIndex; ++j) {
         if (!this.queues[j].isEmpty()) {
            this.firstQueuedLevel = j;
            break;
         }
      }

   }
}