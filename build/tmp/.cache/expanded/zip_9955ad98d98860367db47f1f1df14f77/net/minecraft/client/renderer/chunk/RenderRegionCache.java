package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderRegionCache {
   private final Long2ObjectMap<RenderRegionCache.ChunkInfo> chunkInfoCache = new Long2ObjectOpenHashMap<>();

   @Nullable
   public RenderChunkRegion createRegion(Level pLevel, BlockPos pStart, BlockPos pEnd, int pPadding) {
      int i = SectionPos.blockToSectionCoord(pStart.getX() - pPadding);
      int j = SectionPos.blockToSectionCoord(pStart.getZ() - pPadding);
      int k = SectionPos.blockToSectionCoord(pEnd.getX() + pPadding);
      int l = SectionPos.blockToSectionCoord(pEnd.getZ() + pPadding);
      RenderRegionCache.ChunkInfo[][] arenderregioncache$chunkinfo = new RenderRegionCache.ChunkInfo[k - i + 1][l - j + 1];

      for(int i1 = i; i1 <= k; ++i1) {
         for(int j1 = j; j1 <= l; ++j1) {
            arenderregioncache$chunkinfo[i1 - i][j1 - j] = this.chunkInfoCache.computeIfAbsent(ChunkPos.asLong(i1, j1), (p_200464_) -> {
               return new RenderRegionCache.ChunkInfo(pLevel.getChunk(ChunkPos.getX(p_200464_), ChunkPos.getZ(p_200464_)));
            });
         }
      }

      if (isAllEmpty(pStart, pEnd, i, j, arenderregioncache$chunkinfo)) {
         return null;
      } else {
         RenderChunk[][] arenderchunk = new RenderChunk[k - i + 1][l - j + 1];

         for(int l1 = i; l1 <= k; ++l1) {
            for(int k1 = j; k1 <= l; ++k1) {
               arenderchunk[l1 - i][k1 - j] = arenderregioncache$chunkinfo[l1 - i][k1 - j].renderChunk();
            }
         }

         return new RenderChunkRegion(pLevel, i, j, arenderchunk);
      }
   }

   private static boolean isAllEmpty(BlockPos pStart, BlockPos pEnd, int pStartX, int pStartZ, RenderRegionCache.ChunkInfo[][] pInfos) {
      int i = SectionPos.blockToSectionCoord(pStart.getX());
      int j = SectionPos.blockToSectionCoord(pStart.getZ());
      int k = SectionPos.blockToSectionCoord(pEnd.getX());
      int l = SectionPos.blockToSectionCoord(pEnd.getZ());

      for(int i1 = i; i1 <= k; ++i1) {
         for(int j1 = j; j1 <= l; ++j1) {
            LevelChunk levelchunk = pInfos[i1 - pStartX][j1 - pStartZ].chunk();
            if (!levelchunk.isYSpaceEmpty(pStart.getY(), pEnd.getY())) {
               return false;
            }
         }
      }

      return true;
   }

   @OnlyIn(Dist.CLIENT)
   static final class ChunkInfo {
      private final LevelChunk chunk;
      @Nullable
      private RenderChunk renderChunk;

      ChunkInfo(LevelChunk pChunk) {
         this.chunk = pChunk;
      }

      public LevelChunk chunk() {
         return this.chunk;
      }

      public RenderChunk renderChunk() {
         if (this.renderChunk == null) {
            this.renderChunk = new RenderChunk(this.chunk);
         }

         return this.renderChunk;
      }
   }
}