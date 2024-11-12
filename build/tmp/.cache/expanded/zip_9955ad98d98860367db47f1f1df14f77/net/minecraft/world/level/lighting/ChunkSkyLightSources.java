package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChunkSkyLightSources {
   private static final int SIZE = 16;
   public static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
   private final int minY;
   private final BitStorage heightmap;
   private final BlockPos.MutableBlockPos mutablePos1 = new BlockPos.MutableBlockPos();
   private final BlockPos.MutableBlockPos mutablePos2 = new BlockPos.MutableBlockPos();

   public ChunkSkyLightSources(LevelHeightAccessor pLevel) {
      this.minY = pLevel.getMinBuildHeight() - 1;
      int i = pLevel.getMaxBuildHeight();
      int j = Mth.ceillog2(i - this.minY + 1);
      this.heightmap = new SimpleBitStorage(j, 256);
   }

   public void fillFrom(ChunkAccess pChunk) {
      int i = pChunk.getHighestFilledSectionIndex();
      if (i == -1) {
         this.fill(this.minY);
      } else {
         for(int j = 0; j < 16; ++j) {
            for(int k = 0; k < 16; ++k) {
               int l = Math.max(this.findLowestSourceY(pChunk, i, k, j), this.minY);
               this.set(index(k, j), l);
            }
         }

      }
   }

   private int findLowestSourceY(ChunkAccess pChunk, int pSectionIndex, int pX, int pZ) {
      int i = SectionPos.sectionToBlockCoord(pChunk.getSectionYFromSectionIndex(pSectionIndex) + 1);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = this.mutablePos1.set(pX, i, pZ);
      BlockPos.MutableBlockPos blockpos$mutableblockpos1 = this.mutablePos2.setWithOffset(blockpos$mutableblockpos, Direction.DOWN);
      BlockState blockstate = Blocks.AIR.defaultBlockState();

      for(int j = pSectionIndex; j >= 0; --j) {
         LevelChunkSection levelchunksection = pChunk.getSection(j);
         if (levelchunksection.hasOnlyAir()) {
            blockstate = Blocks.AIR.defaultBlockState();
            int l = pChunk.getSectionYFromSectionIndex(j);
            blockpos$mutableblockpos.setY(SectionPos.sectionToBlockCoord(l));
            blockpos$mutableblockpos1.setY(blockpos$mutableblockpos.getY() - 1);
         } else {
            for(int k = 15; k >= 0; --k) {
               BlockState blockstate1 = levelchunksection.getBlockState(pX, k, pZ);
               if (isEdgeOccluded(pChunk, blockpos$mutableblockpos, blockstate, blockpos$mutableblockpos1, blockstate1)) {
                  return blockpos$mutableblockpos.getY();
               }

               blockstate = blockstate1;
               blockpos$mutableblockpos.set(blockpos$mutableblockpos1);
               blockpos$mutableblockpos1.move(Direction.DOWN);
            }
         }
      }

      return this.minY;
   }

   public boolean update(BlockGetter pLevel, int pX, int pY, int pZ) {
      int i = pY + 1;
      int j = index(pX, pZ);
      int k = this.get(j);
      if (i < k) {
         return false;
      } else {
         BlockPos blockpos = this.mutablePos1.set(pX, pY + 1, pZ);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         BlockPos blockpos1 = this.mutablePos2.set(pX, pY, pZ);
         BlockState blockstate1 = pLevel.getBlockState(blockpos1);
         if (this.updateEdge(pLevel, j, k, blockpos, blockstate, blockpos1, blockstate1)) {
            return true;
         } else {
            BlockPos blockpos2 = this.mutablePos1.set(pX, pY - 1, pZ);
            BlockState blockstate2 = pLevel.getBlockState(blockpos2);
            return this.updateEdge(pLevel, j, k, blockpos1, blockstate1, blockpos2, blockstate2);
         }
      }
   }

   private boolean updateEdge(BlockGetter pLevel, int pIndex, int pMinY, BlockPos pPos1, BlockState pState1, BlockPos pPos2, BlockState pState2) {
      int i = pPos1.getY();
      if (isEdgeOccluded(pLevel, pPos1, pState1, pPos2, pState2)) {
         if (i > pMinY) {
            this.set(pIndex, i);
            return true;
         }
      } else if (i == pMinY) {
         this.set(pIndex, this.findLowestSourceBelow(pLevel, pPos2, pState2));
         return true;
      }

      return false;
   }

   private int findLowestSourceBelow(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = this.mutablePos1.set(pPos);
      BlockPos.MutableBlockPos blockpos$mutableblockpos1 = this.mutablePos2.setWithOffset(pPos, Direction.DOWN);
      BlockState blockstate = pState;

      while(blockpos$mutableblockpos1.getY() >= this.minY) {
         BlockState blockstate1 = pLevel.getBlockState(blockpos$mutableblockpos1);
         if (isEdgeOccluded(pLevel, blockpos$mutableblockpos, blockstate, blockpos$mutableblockpos1, blockstate1)) {
            return blockpos$mutableblockpos.getY();
         }

         blockstate = blockstate1;
         blockpos$mutableblockpos.set(blockpos$mutableblockpos1);
         blockpos$mutableblockpos1.move(Direction.DOWN);
      }

      return this.minY;
   }

   private static boolean isEdgeOccluded(BlockGetter pLevel, BlockPos pPos1, BlockState pState1, BlockPos pPos2, BlockState pState2) {
      if (pState2.getLightBlock(pLevel, pPos2) != 0) {
         return true;
      } else {
         VoxelShape voxelshape = LightEngine.getOcclusionShape(pLevel, pPos1, pState1, Direction.DOWN);
         VoxelShape voxelshape1 = LightEngine.getOcclusionShape(pLevel, pPos2, pState2, Direction.UP);
         return Shapes.faceShapeOccludes(voxelshape, voxelshape1);
      }
   }

   public int getLowestSourceY(int pX, int pZ) {
      int i = this.get(index(pX, pZ));
      return this.extendSourcesBelowWorld(i);
   }

   public int getHighestLowestSourceY() {
      int i = Integer.MIN_VALUE;

      for(int j = 0; j < this.heightmap.getSize(); ++j) {
         int k = this.heightmap.get(j);
         if (k > i) {
            i = k;
         }
      }

      return this.extendSourcesBelowWorld(i + this.minY);
   }

   private void fill(int pValue) {
      int i = pValue - this.minY;

      for(int j = 0; j < this.heightmap.getSize(); ++j) {
         this.heightmap.set(j, i);
      }

   }

   private void set(int pIndex, int pValue) {
      this.heightmap.set(pIndex, pValue - this.minY);
   }

   private int get(int pIndex) {
      return this.heightmap.get(pIndex) + this.minY;
   }

   private int extendSourcesBelowWorld(int pY) {
      return pY == this.minY ? Integer.MIN_VALUE : pY;
   }

   private static int index(int pX, int pZ) {
      return pX + pZ * 16;
   }
}