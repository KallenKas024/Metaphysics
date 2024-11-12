package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ViewArea {
   protected final LevelRenderer levelRenderer;
   protected final Level level;
   protected int chunkGridSizeY;
   protected int chunkGridSizeX;
   protected int chunkGridSizeZ;
   public ChunkRenderDispatcher.RenderChunk[] chunks;

   public ViewArea(ChunkRenderDispatcher pChunkRenderDispatcher, Level pLevel, int pViewDistance, LevelRenderer pLevelRenderer) {
      this.levelRenderer = pLevelRenderer;
      this.level = pLevel;
      this.setViewDistance(pViewDistance);
      this.createChunks(pChunkRenderDispatcher);
   }

   protected void createChunks(ChunkRenderDispatcher pRenderChunkFactory) {
      if (!Minecraft.getInstance().isSameThread()) {
         throw new IllegalStateException("createChunks called from wrong thread: " + Thread.currentThread().getName());
      } else {
         int i = this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ;
         this.chunks = new ChunkRenderDispatcher.RenderChunk[i];

         for(int j = 0; j < this.chunkGridSizeX; ++j) {
            for(int k = 0; k < this.chunkGridSizeY; ++k) {
               for(int l = 0; l < this.chunkGridSizeZ; ++l) {
                  int i1 = this.getChunkIndex(j, k, l);
                  this.chunks[i1] = pRenderChunkFactory.new RenderChunk(i1, j * 16, k * 16, l * 16);
               }
            }
         }

      }
   }

   public void releaseAllBuffers() {
      for(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk : this.chunks) {
         chunkrenderdispatcher$renderchunk.releaseBuffers();
      }

   }

   private int getChunkIndex(int pX, int pY, int pZ) {
      return (pZ * this.chunkGridSizeY + pY) * this.chunkGridSizeX + pX;
   }

   protected void setViewDistance(int pRenderDistanceChunks) {
      int i = pRenderDistanceChunks * 2 + 1;
      this.chunkGridSizeX = i;
      this.chunkGridSizeY = this.level.getSectionsCount();
      this.chunkGridSizeZ = i;
   }

   public void repositionCamera(double pViewEntityX, double pViewEntityZ) {
      int i = Mth.ceil(pViewEntityX);
      int j = Mth.ceil(pViewEntityZ);

      for(int k = 0; k < this.chunkGridSizeX; ++k) {
         int l = this.chunkGridSizeX * 16;
         int i1 = i - 8 - l / 2;
         int j1 = i1 + Math.floorMod(k * 16 - i1, l);

         for(int k1 = 0; k1 < this.chunkGridSizeZ; ++k1) {
            int l1 = this.chunkGridSizeZ * 16;
            int i2 = j - 8 - l1 / 2;
            int j2 = i2 + Math.floorMod(k1 * 16 - i2, l1);

            for(int k2 = 0; k2 < this.chunkGridSizeY; ++k2) {
               int l2 = this.level.getMinBuildHeight() + k2 * 16;
               ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.chunks[this.getChunkIndex(k, k2, k1)];
               BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
               if (j1 != blockpos.getX() || l2 != blockpos.getY() || j2 != blockpos.getZ()) {
                  chunkrenderdispatcher$renderchunk.setOrigin(j1, l2, j2);
               }
            }
         }
      }

   }

   public void setDirty(int pSectionX, int pSectionY, int pSectionZ, boolean pReRenderOnMainThread) {
      int i = Math.floorMod(pSectionX, this.chunkGridSizeX);
      int j = Math.floorMod(pSectionY - this.level.getMinSection(), this.chunkGridSizeY);
      int k = Math.floorMod(pSectionZ, this.chunkGridSizeZ);
      ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.chunks[this.getChunkIndex(i, j, k)];
      chunkrenderdispatcher$renderchunk.setDirty(pReRenderOnMainThread);
   }

   @Nullable
   protected ChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos pPos) {
      int i = Mth.floorDiv(pPos.getX(), 16);
      int j = Mth.floorDiv(pPos.getY() - this.level.getMinBuildHeight(), 16);
      int k = Mth.floorDiv(pPos.getZ(), 16);
      if (j >= 0 && j < this.chunkGridSizeY) {
         i = Mth.positiveModulo(i, this.chunkGridSizeX);
         k = Mth.positiveModulo(k, this.chunkGridSizeZ);
         return this.chunks[this.getChunkIndex(i, j, k)];
      } else {
         return null;
      }
   }
}