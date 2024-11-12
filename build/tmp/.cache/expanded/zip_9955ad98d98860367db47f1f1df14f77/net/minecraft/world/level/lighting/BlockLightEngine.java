package net.minecraft.world.level.lighting;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;

public final class BlockLightEngine extends LightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
   private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

   public BlockLightEngine(LightChunkGetter pChunkSource) {
      this(pChunkSource, new BlockLightSectionStorage(pChunkSource));
   }

   @VisibleForTesting
   public BlockLightEngine(LightChunkGetter pChunkSource, BlockLightSectionStorage pStorage) {
      super(pChunkSource, pStorage);
   }

   protected void checkNode(long pPackedPos) {
      long i = SectionPos.blockToSection(pPackedPos);
      if (this.storage.storingLightForSection(i)) {
         BlockState blockstate = this.getState(this.mutablePos.set(pPackedPos));
         int j = this.getEmission(pPackedPos, blockstate);
         int k = this.storage.getStoredLevel(pPackedPos);
         if (j < k) {
            this.storage.setStoredLevel(pPackedPos, 0);
            this.enqueueDecrease(pPackedPos, LightEngine.QueueEntry.decreaseAllDirections(k));
         } else {
            this.enqueueDecrease(pPackedPos, PULL_LIGHT_IN_ENTRY);
         }

         if (j > 0) {
            this.enqueueIncrease(pPackedPos, LightEngine.QueueEntry.increaseLightFromEmission(j, isEmptyShape(blockstate)));
         }

      }
   }

   protected void propagateIncrease(long pPackedPos, long pQueueEntry, int pLightLevel) {
      BlockState blockstate = null;

      for(Direction direction : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection(pQueueEntry, direction)) {
            long i = BlockPos.offset(pPackedPos, direction);
            if (this.storage.storingLightForSection(SectionPos.blockToSection(i))) {
               int j = this.storage.getStoredLevel(i);
               int k = pLightLevel - 1;
               if (k > j) {
                  this.mutablePos.set(i);
                  BlockState blockstate1 = this.getState(this.mutablePos);
                  int l = pLightLevel - this.getOpacity(blockstate1, this.mutablePos);
                  if (l > j) {
                     if (blockstate == null) {
                        blockstate = LightEngine.QueueEntry.isFromEmptyShape(pQueueEntry) ? Blocks.AIR.defaultBlockState() : this.getState(this.mutablePos.set(pPackedPos));
                     }

                     if (!this.shapeOccludes(pPackedPos, blockstate, i, blockstate1, direction)) {
                        this.storage.setStoredLevel(i, l);
                        if (l > 1) {
                           this.enqueueIncrease(i, LightEngine.QueueEntry.increaseSkipOneDirection(l, isEmptyShape(blockstate1), direction.getOpposite()));
                        }
                     }
                  }
               }
            }
         }
      }

   }

   protected void propagateDecrease(long pPackedPos, long pLightLevel) {
      int i = LightEngine.QueueEntry.getFromLevel(pLightLevel);

      for(Direction direction : PROPAGATION_DIRECTIONS) {
         if (LightEngine.QueueEntry.shouldPropagateInDirection(pLightLevel, direction)) {
            long j = BlockPos.offset(pPackedPos, direction);
            if (this.storage.storingLightForSection(SectionPos.blockToSection(j))) {
               int k = this.storage.getStoredLevel(j);
               if (k != 0) {
                  if (k <= i - 1) {
                     BlockState blockstate = this.getState(this.mutablePos.set(j));
                     int l = this.getEmission(j, blockstate);
                     this.storage.setStoredLevel(j, 0);
                     if (l < k) {
                        this.enqueueDecrease(j, LightEngine.QueueEntry.decreaseSkipOneDirection(k, direction.getOpposite()));
                     }

                     if (l > 0) {
                        this.enqueueIncrease(j, LightEngine.QueueEntry.increaseLightFromEmission(l, isEmptyShape(blockstate)));
                     }
                  } else {
                     this.enqueueIncrease(j, LightEngine.QueueEntry.increaseOnlyOneDirection(k, false, direction.getOpposite()));
                  }
               }
            }
         }
      }

   }

   private int getEmission(long pPackedPos, BlockState pState) {
      int i = pState.getLightEmission(chunkSource.getLevel(), mutablePos);
      return i > 0 && this.storage.lightOnInSection(SectionPos.blockToSection(pPackedPos)) ? i : 0;
   }

   public void propagateLightSources(ChunkPos pChunkPos) {
      this.setLightEnabled(pChunkPos, true);
      LightChunk lightchunk = this.chunkSource.getChunkForLighting(pChunkPos.x, pChunkPos.z);
      if (lightchunk != null) {
         lightchunk.findBlockLightSources((p_285266_, p_285452_) -> {
            int i = p_285452_.getLightEmission(chunkSource.getLevel(), p_285266_);
            this.enqueueIncrease(p_285266_.asLong(), LightEngine.QueueEntry.increaseLightFromEmission(i, isEmptyShape(p_285452_)));
         });
      }

   }
}
