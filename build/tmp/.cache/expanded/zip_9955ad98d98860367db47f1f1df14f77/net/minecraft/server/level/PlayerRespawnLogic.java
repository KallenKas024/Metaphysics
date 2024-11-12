package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class PlayerRespawnLogic {
   @Nullable
   protected static BlockPos getOverworldRespawnPos(ServerLevel pLevel, int pX, int pZ) {
      boolean flag = pLevel.dimensionType().hasCeiling();
      LevelChunk levelchunk = pLevel.getChunk(SectionPos.blockToSectionCoord(pX), SectionPos.blockToSectionCoord(pZ));
      int i = flag ? pLevel.getChunkSource().getGenerator().getSpawnHeight(pLevel) : levelchunk.getHeight(Heightmap.Types.MOTION_BLOCKING, pX & 15, pZ & 15);
      if (i < pLevel.getMinBuildHeight()) {
         return null;
      } else {
         int j = levelchunk.getHeight(Heightmap.Types.WORLD_SURFACE, pX & 15, pZ & 15);
         if (j <= i && j > levelchunk.getHeight(Heightmap.Types.OCEAN_FLOOR, pX & 15, pZ & 15)) {
            return null;
         } else {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for(int k = i + 1; k >= pLevel.getMinBuildHeight(); --k) {
               blockpos$mutableblockpos.set(pX, k, pZ);
               BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
               if (!blockstate.getFluidState().isEmpty()) {
                  break;
               }

               if (Block.isFaceFull(blockstate.getCollisionShape(pLevel, blockpos$mutableblockpos), Direction.UP)) {
                  return blockpos$mutableblockpos.above().immutable();
               }
            }

            return null;
         }
      }
   }

   @Nullable
   public static BlockPos getSpawnPosInChunk(ServerLevel pLevel, ChunkPos pChunkPos) {
      if (SharedConstants.debugVoidTerrain(pChunkPos)) {
         return null;
      } else {
         for(int i = pChunkPos.getMinBlockX(); i <= pChunkPos.getMaxBlockX(); ++i) {
            for(int j = pChunkPos.getMinBlockZ(); j <= pChunkPos.getMaxBlockZ(); ++j) {
               BlockPos blockpos = getOverworldRespawnPos(pLevel, i, j);
               if (blockpos != null) {
                  return blockpos;
               }
            }
         }

         return null;
      }
   }
}