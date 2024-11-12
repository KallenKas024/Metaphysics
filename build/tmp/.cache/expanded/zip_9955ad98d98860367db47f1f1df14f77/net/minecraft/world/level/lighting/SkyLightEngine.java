package net.minecraft.world.level.lighting;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.jetbrains.annotations.VisibleForTesting;

public final class SkyLightEngine extends LightEngine<SkyLightSectionStorage.SkyDataLayerStorageMap, SkyLightSectionStorage> {
   private static final long REMOVE_TOP_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(15);
   private static final long REMOVE_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.decreaseSkipOneDirection(15, Direction.UP);
   private static final long ADD_SKY_SOURCE_ENTRY = LightEngine.QueueEntry.increaseSkipOneDirection(15, false, Direction.UP);
   private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
   private final ChunkSkyLightSources emptyChunkSources;

   public SkyLightEngine(LightChunkGetter pChunkSource) {
      this(pChunkSource, new SkyLightSectionStorage(pChunkSource));
   }

   @VisibleForTesting
   protected SkyLightEngine(LightChunkGetter pChunkSource, SkyLightSectionStorage pSectionStorage) {
      super(pChunkSource, pSectionStorage);
      this.emptyChunkSources = new ChunkSkyLightSources(pChunkSource.getLevel());
   }

   private static boolean isSourceLevel(int pLevel) {
      return pLevel == 15;
   }

   private int getLowestSourceY(int pX, int pZ, int pDefaultReturnValue) {
      ChunkSkyLightSources chunkskylightsources = this.getChunkSources(SectionPos.blockToSectionCoord(pX), SectionPos.blockToSectionCoord(pZ));
      return chunkskylightsources == null ? pDefaultReturnValue : chunkskylightsources.getLowestSourceY(SectionPos.sectionRelative(pX), SectionPos.sectionRelative(pZ));
   }

   @Nullable
   private ChunkSkyLightSources getChunkSources(int pChunkX, int pChunkZ) {
      LightChunk lightchunk = this.chunkSource.getChunkForLighting(pChunkX, pChunkZ);
      return lightchunk != null ? lightchunk.getSkyLightSources() : null;
   }

   protected void checkNode(long pLevelPos) {
      int i = BlockPos.getX(pLevelPos);
      int j = BlockPos.getY(pLevelPos);
      int k = BlockPos.getZ(pLevelPos);
      long l = SectionPos.blockToSection(pLevelPos);
      int i1 = this.storage.lightOnInSection(l) ? this.getLowestSourceY(i, k, Integer.MAX_VALUE) : Integer.MAX_VALUE;
      if (i1 != Integer.MAX_VALUE) {
         this.updateSourcesInColumn(i, k, i1);
      }

      if (this.storage.storingLightForSection(l)) {
         boolean flag = j >= i1;
         if (flag) {
            this.enqueueDecrease(pLevelPos, REMOVE_SKY_SOURCE_ENTRY);
            this.enqueueIncrease(pLevelPos, ADD_SKY_SOURCE_ENTRY);
         } else {
            int j1 = this.storage.getStoredLevel(pLevelPos);
            if (j1 > 0) {
               this.storage.setStoredLevel(pLevelPos, 0);
               this.enqueueDecrease(pLevelPos, LightEngine.QueueEntry.decreaseAllDirections(j1));
            } else {
               this.enqueueDecrease(pLevelPos, PULL_LIGHT_IN_ENTRY);
            }
         }

      }
   }

   private void updateSourcesInColumn(int pX, int pZ, int pLowestY) {
      int i = SectionPos.sectionToBlockCoord(this.storage.getBottomSectionY());
      this.removeSourcesBelow(pX, pZ, pLowestY, i);
      this.addSourcesAbove(pX, pZ, pLowestY, i);
   }

   private void removeSourcesBelow(int pX, int pZ, int pMinY, int pBottomSectionY) {
      if (pMinY > pBottomSectionY) {
         int i = SectionPos.blockToSectionCoord(pX);
         int j = SectionPos.blockToSectionCoord(pZ);
         int k = pMinY - 1;

         for(int l = SectionPos.blockToSectionCoord(k); this.storage.hasLightDataAtOrBelow(l); --l) {
            if (this.storage.storingLightForSection(SectionPos.asLong(i, l, j))) {
               int i1 = SectionPos.sectionToBlockCoord(l);
               int j1 = i1 + 15;

               for(int k1 = Math.min(j1, k); k1 >= i1; --k1) {
                  long l1 = BlockPos.asLong(pX, k1, pZ);
                  if (!isSourceLevel(this.storage.getStoredLevel(l1))) {
                     return;
                  }

                  this.storage.setStoredLevel(l1, 0);
                  this.enqueueDecrease(l1, k1 == pMinY - 1 ? REMOVE_TOP_SKY_SOURCE_ENTRY : REMOVE_SKY_SOURCE_ENTRY);
               }
            }
         }

      }
   }

   private void addSourcesAbove(int pX, int pZ, int pMaxY, int pBottomSectionY) {
      int i = SectionPos.blockToSectionCoord(pX);
      int j = SectionPos.blockToSectionCoord(pZ);
      int k = Math.max(Math.max(this.getLowestSourceY(pX - 1, pZ, Integer.MIN_VALUE), this.getLowestSourceY(pX + 1, pZ, Integer.MIN_VALUE)), Math.max(this.getLowestSourceY(pX, pZ - 1, Integer.MIN_VALUE), this.getLowestSourceY(pX, pZ + 1, Integer.MIN_VALUE)));
      int l = Math.max(pMaxY, pBottomSectionY);

      for(long i1 = SectionPos.asLong(i, SectionPos.blockToSectionCoord(l), j); !this.storage.isAboveData(i1); i1 = SectionPos.offset(i1, Direction.UP)) {
         if (this.storage.storingLightForSection(i1)) {
            int j1 = SectionPos.sectionToBlockCoord(SectionPos.y(i1));
            int k1 = j1 + 15;

            for(int l1 = Math.max(j1, l); l1 <= k1; ++l1) {
               long i2 = BlockPos.asLong(pX, l1, pZ);
               if (isSourceLevel(this.storage.getStoredLevel(i2))) {
                  return;
               }

               this.storage.setStoredLevel(i2, 15);
               if (l1 < k || l1 == pMaxY) {
                  this.enqueueIncrease(i2, ADD_SKY_SOURCE_ENTRY);
               }
            }
         }
      }

   }

   protected void propagateIncrease(long pPackedPos, long pQueueEntry, int pLightLevel) {
      BlockState blockstate = null;
      int i = this.countEmptySectionsBelowIfAtBorder(pPackedPos);

      for(Direction direction : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection(pQueueEntry, direction)) {
            long j = BlockPos.offset(pPackedPos, direction);
            if (this.storage.storingLightForSection(SectionPos.blockToSection(j))) {
               int k = this.storage.getStoredLevel(j);
               int l = pLightLevel - 1;
               if (l > k) {
                  this.mutablePos.set(j);
                  BlockState blockstate1 = this.getState(this.mutablePos);
                  int i1 = pLightLevel - this.getOpacity(blockstate1, this.mutablePos);
                  if (i1 > k) {
                     if (blockstate == null) {
                        blockstate = LightEngine.QueueEntry.isFromEmptyShape(pQueueEntry) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(pPackedPos));
                     }

                     if (!this.shapeOccludes(pPackedPos, blockstate, j, blockstate1, direction)) {
                        this.storage.setStoredLevel(j, i1);
                        if (i1 > 1) {
                           this.enqueueIncrease(j, LightEngine.QueueEntry.increaseSkipOneDirection(i1, isEmptyShape(blockstate1), direction.getOpposite()));
                        }

                        this.propagateFromEmptySections(j, direction, i1, true, i);
                     }
                  }
               }
            }
         }
      }

   }

   protected void propagateDecrease(long pPackedPos, long pLightLevel) {
      int i = this.countEmptySectionsBelowIfAtBorder(pPackedPos);
      int j = LightEngine.QueueEntry.getFromLevel(pLightLevel);

      for(Direction direction : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection(pLightLevel, direction)) {
            long k = BlockPos.offset(pPackedPos, direction);
            if (this.storage.storingLightForSection(SectionPos.blockToSection(k))) {
               int l = this.storage.getStoredLevel(k);
               if (l != 0) {
                  if (l <= j - 1) {
                     this.storage.setStoredLevel(k, 0);
                     this.enqueueDecrease(k, LightEngine.QueueEntry.decreaseSkipOneDirection(l, direction.getOpposite()));
                     this.propagateFromEmptySections(k, direction, l, false, i);
                  } else {
                     this.enqueueIncrease(k, LightEngine.QueueEntry.increaseOnlyOneDirection(l, false, direction.getOpposite()));
                  }
               }
            }
         }
      }

   }

   private int countEmptySectionsBelowIfAtBorder(long pPackedPos) {
      int i = BlockPos.getY(pPackedPos);
      int j = SectionPos.sectionRelative(i);
      if (j != 0) {
         return 0;
      } else {
         int k = BlockPos.getX(pPackedPos);
         int l = BlockPos.getZ(pPackedPos);
         int i1 = SectionPos.sectionRelative(k);
         int j1 = SectionPos.sectionRelative(l);
         if (i1 != 0 && i1 != 15 && j1 != 0 && j1 != 15) {
            return 0;
         } else {
            int k1 = SectionPos.blockToSectionCoord(k);
            int l1 = SectionPos.blockToSectionCoord(i);
            int i2 = SectionPos.blockToSectionCoord(l);

            int j2;
            for(j2 = 0; !this.storage.storingLightForSection(SectionPos.asLong(k1, l1 - j2 - 1, i2)) && this.storage.hasLightDataAtOrBelow(l1 - j2 - 1); ++j2) {
            }

            return j2;
         }
      }
   }

   private void propagateFromEmptySections(long pPackedPos, Direction pDirection, int pLevel, boolean pShouldIncrease, int pEmptySections) {
      if (pEmptySections != 0) {
         int i = BlockPos.getX(pPackedPos);
         int j = BlockPos.getZ(pPackedPos);
         if (crossedSectionEdge(pDirection, SectionPos.sectionRelative(i), SectionPos.sectionRelative(j))) {
            int k = BlockPos.getY(pPackedPos);
            int l = SectionPos.blockToSectionCoord(i);
            int i1 = SectionPos.blockToSectionCoord(j);
            int j1 = SectionPos.blockToSectionCoord(k) - 1;
            int k1 = j1 - pEmptySections + 1;

            while(j1 >= k1) {
               if (!this.storage.storingLightForSection(SectionPos.asLong(l, j1, i1))) {
                  --j1;
               } else {
                  int l1 = SectionPos.sectionToBlockCoord(j1);

                  for(int i2 = 15; i2 >= 0; --i2) {
                     long j2 = BlockPos.asLong(i, l1 + i2, j);
                     if (pShouldIncrease) {
                        this.storage.setStoredLevel(j2, pLevel);
                        if (pLevel > 1) {
                           this.enqueueIncrease(j2, LightEngine.QueueEntry.increaseSkipOneDirection(pLevel, true, pDirection.getOpposite()));
                        }
                     } else {
                        this.storage.setStoredLevel(j2, 0);
                        this.enqueueDecrease(j2, LightEngine.QueueEntry.decreaseSkipOneDirection(pLevel, pDirection.getOpposite()));
                     }
                  }

                  --j1;
               }
            }

         }
      }
   }

   private static boolean crossedSectionEdge(Direction pDirection, int pX, int pZ) {
      boolean flag;
      switch (pDirection) {
         case NORTH:
            flag = pZ == 15;
            break;
         case SOUTH:
            flag = pZ == 0;
            break;
         case WEST:
            flag = pX == 15;
            break;
         case EAST:
            flag = pX == 0;
            break;
         default:
            flag = false;
      }

      return flag;
   }

   public void setLightEnabled(ChunkPos pChunkPos, boolean pLightEnabled) {
      super.setLightEnabled(pChunkPos, pLightEnabled);
      if (pLightEnabled) {
         ChunkSkyLightSources chunkskylightsources = Objects.requireNonNullElse(this.getChunkSources(pChunkPos.x, pChunkPos.z), this.emptyChunkSources);
         int i = chunkskylightsources.getHighestLowestSourceY() - 1;
         int j = SectionPos.blockToSectionCoord(i) + 1;
         long k = SectionPos.getZeroNode(pChunkPos.x, pChunkPos.z);
         int l = this.storage.getTopSectionY(k);
         int i1 = Math.max(this.storage.getBottomSectionY(), j);

         for(int j1 = l - 1; j1 >= i1; --j1) {
            DataLayer datalayer = this.storage.getDataLayerToWrite(SectionPos.asLong(pChunkPos.x, j1, pChunkPos.z));
            if (datalayer != null && datalayer.isEmpty()) {
               datalayer.fill(15);
            }
         }
      }

   }

   public void propagateLightSources(ChunkPos pChunkPos) {
      long i = SectionPos.getZeroNode(pChunkPos.x, pChunkPos.z);
      this.storage.setLightEnabled(i, true);
      ChunkSkyLightSources chunkskylightsources = Objects.requireNonNullElse(this.getChunkSources(pChunkPos.x, pChunkPos.z), this.emptyChunkSources);
      ChunkSkyLightSources chunkskylightsources1 = Objects.requireNonNullElse(this.getChunkSources(pChunkPos.x, pChunkPos.z - 1), this.emptyChunkSources);
      ChunkSkyLightSources chunkskylightsources2 = Objects.requireNonNullElse(this.getChunkSources(pChunkPos.x, pChunkPos.z + 1), this.emptyChunkSources);
      ChunkSkyLightSources chunkskylightsources3 = Objects.requireNonNullElse(this.getChunkSources(pChunkPos.x - 1, pChunkPos.z), this.emptyChunkSources);
      ChunkSkyLightSources chunkskylightsources4 = Objects.requireNonNullElse(this.getChunkSources(pChunkPos.x + 1, pChunkPos.z), this.emptyChunkSources);
      int j = this.storage.getTopSectionY(i);
      int k = this.storage.getBottomSectionY();
      int l = SectionPos.sectionToBlockCoord(pChunkPos.x);
      int i1 = SectionPos.sectionToBlockCoord(pChunkPos.z);

      for(int j1 = j - 1; j1 >= k; --j1) {
         long k1 = SectionPos.asLong(pChunkPos.x, j1, pChunkPos.z);
         DataLayer datalayer = this.storage.getDataLayerToWrite(k1);
         if (datalayer != null) {
            int l1 = SectionPos.sectionToBlockCoord(j1);
            int i2 = l1 + 15;
            boolean flag = false;

            for(int j2 = 0; j2 < 16; ++j2) {
               for(int k2 = 0; k2 < 16; ++k2) {
                  int l2 = chunkskylightsources.getLowestSourceY(k2, j2);
                  if (l2 <= i2) {
                     int i3 = j2 == 0 ? chunkskylightsources1.getLowestSourceY(k2, 15) : chunkskylightsources.getLowestSourceY(k2, j2 - 1);
                     int j3 = j2 == 15 ? chunkskylightsources2.getLowestSourceY(k2, 0) : chunkskylightsources.getLowestSourceY(k2, j2 + 1);
                     int k3 = k2 == 0 ? chunkskylightsources3.getLowestSourceY(15, j2) : chunkskylightsources.getLowestSourceY(k2 - 1, j2);
                     int l3 = k2 == 15 ? chunkskylightsources4.getLowestSourceY(0, j2) : chunkskylightsources.getLowestSourceY(k2 + 1, j2);
                     int i4 = Math.max(Math.max(i3, j3), Math.max(k3, l3));

                     for(int j4 = i2; j4 >= Math.max(l1, l2); --j4) {
                        datalayer.set(k2, SectionPos.sectionRelative(j4), j2, 15);
                        if (j4 == l2 || j4 < i4) {
                           long k4 = BlockPos.asLong(l + k2, j4, i1 + j2);
                           this.enqueueIncrease(k4, LightEngine.QueueEntry.increaseSkySourceInDirections(j4 == l2, j4 < i3, j4 < j3, j4 < k3, j4 < l3));
                        }
                     }

                     if (l2 < l1) {
                        flag = true;
                     }
                  }
               }
            }

            if (!flag) {
               break;
            }
         }
      }

   }
}