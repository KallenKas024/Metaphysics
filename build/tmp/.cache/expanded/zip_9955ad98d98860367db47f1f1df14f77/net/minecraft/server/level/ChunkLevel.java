package net.minecraft.server.level;

import net.minecraft.world.level.chunk.ChunkStatus;

public class ChunkLevel {
   private static final int FULL_CHUNK_LEVEL = 33;
   private static final int BLOCK_TICKING_LEVEL = 32;
   private static final int ENTITY_TICKING_LEVEL = 31;
   public static final int MAX_LEVEL = 33 + ChunkStatus.maxDistance();

   public static ChunkStatus generationStatus(int pLevel) {
      return pLevel < 33 ? ChunkStatus.FULL : ChunkStatus.getStatusAroundFullChunk(pLevel - 33);
   }

   public static int byStatus(ChunkStatus pStatus) {
      return 33 + ChunkStatus.getDistance(pStatus);
   }

   public static FullChunkStatus fullStatus(int pLevel) {
      if (pLevel <= 31) {
         return FullChunkStatus.ENTITY_TICKING;
      } else if (pLevel <= 32) {
         return FullChunkStatus.BLOCK_TICKING;
      } else {
         return pLevel <= 33 ? FullChunkStatus.FULL : FullChunkStatus.INACCESSIBLE;
      }
   }

   public static int byStatus(FullChunkStatus pStatus) {
      int i;
      switch (pStatus) {
         case INACCESSIBLE:
            i = MAX_LEVEL;
            break;
         case FULL:
            i = 33;
            break;
         case BLOCK_TICKING:
            i = 32;
            break;
         case ENTITY_TICKING:
            i = 31;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return i;
   }

   public static boolean isEntityTicking(int pLevel) {
      return pLevel <= 31;
   }

   public static boolean isBlockTicking(int pLevel) {
      return pLevel <= 32;
   }

   public static boolean isLoaded(int pLevel) {
      return pLevel <= MAX_LEVEL;
   }
}