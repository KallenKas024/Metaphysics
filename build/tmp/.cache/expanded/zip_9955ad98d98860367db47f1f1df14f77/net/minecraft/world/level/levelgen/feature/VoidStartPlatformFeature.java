package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VoidStartPlatformFeature extends Feature<NoneFeatureConfiguration> {
   private static final BlockPos PLATFORM_OFFSET = new BlockPos(8, 3, 8);
   private static final ChunkPos PLATFORM_ORIGIN_CHUNK = new ChunkPos(PLATFORM_OFFSET);
   private static final int PLATFORM_RADIUS = 16;
   private static final int PLATFORM_RADIUS_CHUNKS = 1;

   public VoidStartPlatformFeature(Codec<NoneFeatureConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Returns the Manhattan distance between the two points.
    */
   private static int checkerboardDistance(int pFirstX, int pFirstZ, int pSecondX, int pSecondZ) {
      return Math.max(Math.abs(pFirstX - pSecondX), Math.abs(pFirstZ - pSecondZ));
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
      WorldGenLevel worldgenlevel = pContext.level();
      ChunkPos chunkpos = new ChunkPos(pContext.origin());
      if (checkerboardDistance(chunkpos.x, chunkpos.z, PLATFORM_ORIGIN_CHUNK.x, PLATFORM_ORIGIN_CHUNK.z) > 1) {
         return true;
      } else {
         BlockPos blockpos = PLATFORM_OFFSET.atY(pContext.origin().getY() + PLATFORM_OFFSET.getY());
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         for(int i = chunkpos.getMinBlockZ(); i <= chunkpos.getMaxBlockZ(); ++i) {
            for(int j = chunkpos.getMinBlockX(); j <= chunkpos.getMaxBlockX(); ++j) {
               if (checkerboardDistance(blockpos.getX(), blockpos.getZ(), j, i) <= 16) {
                  blockpos$mutableblockpos.set(j, blockpos.getY(), i);
                  if (blockpos$mutableblockpos.equals(blockpos)) {
                     worldgenlevel.setBlock(blockpos$mutableblockpos, Blocks.COBBLESTONE.defaultBlockState(), 2);
                  } else {
                     worldgenlevel.setBlock(blockpos$mutableblockpos, Blocks.STONE.defaultBlockState(), 2);
                  }
               }
            }
         }

         return true;
      }
   }
}