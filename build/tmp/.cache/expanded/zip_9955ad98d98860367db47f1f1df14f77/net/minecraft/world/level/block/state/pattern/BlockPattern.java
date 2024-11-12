package net.minecraft.world.level.block.state.pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;

public class BlockPattern {
   private final Predicate<BlockInWorld>[][][] pattern;
   private final int depth;
   private final int height;
   private final int width;

   public BlockPattern(Predicate<BlockInWorld>[][][] pPattern) {
      this.pattern = pPattern;
      this.depth = pPattern.length;
      if (this.depth > 0) {
         this.height = pPattern[0].length;
         if (this.height > 0) {
            this.width = pPattern[0][0].length;
         } else {
            this.width = 0;
         }
      } else {
         this.height = 0;
         this.width = 0;
      }

   }

   public int getDepth() {
      return this.depth;
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidth() {
      return this.width;
   }

   @VisibleForTesting
   public Predicate<BlockInWorld>[][][] getPattern() {
      return this.pattern;
   }

   @Nullable
   @VisibleForTesting
   public BlockPattern.BlockPatternMatch matches(LevelReader pLevel, BlockPos pPos, Direction pFinger, Direction pThumb) {
      LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(pLevel, false);
      return this.matches(pPos, pFinger, pThumb, loadingcache);
   }

   /**
    * Checks that the given pattern & rotation is at the block coordinates.
    */
   @Nullable
   private BlockPattern.BlockPatternMatch matches(BlockPos pPos, Direction pFinger, Direction pThumb, LoadingCache<BlockPos, BlockInWorld> pCache) {
      for(int i = 0; i < this.width; ++i) {
         for(int j = 0; j < this.height; ++j) {
            for(int k = 0; k < this.depth; ++k) {
               if (!this.pattern[k][j][i].test(pCache.getUnchecked(translateAndRotate(pPos, pFinger, pThumb, i, j, k)))) {
                  return null;
               }
            }
         }
      }

      return new BlockPattern.BlockPatternMatch(pPos, pFinger, pThumb, pCache, this.width, this.height, this.depth);
   }

   /**
    * Calculates whether the given world position matches the pattern. Warning, fairly heavy function.
    * @return a BlockPatternMatch if found, null otherwise.
    */
   @Nullable
   public BlockPattern.BlockPatternMatch find(LevelReader pLevel, BlockPos pPos) {
      LoadingCache<BlockPos, BlockInWorld> loadingcache = createLevelCache(pLevel, false);
      int i = Math.max(Math.max(this.width, this.height), this.depth);

      for(BlockPos blockpos : BlockPos.betweenClosed(pPos, pPos.offset(i - 1, i - 1, i - 1))) {
         for(Direction direction : Direction.values()) {
            for(Direction direction1 : Direction.values()) {
               if (direction1 != direction && direction1 != direction.getOpposite()) {
                  BlockPattern.BlockPatternMatch blockpattern$blockpatternmatch = this.matches(blockpos, direction, direction1, loadingcache);
                  if (blockpattern$blockpatternmatch != null) {
                     return blockpattern$blockpatternmatch;
                  }
               }
            }
         }
      }

      return null;
   }

   public static LoadingCache<BlockPos, BlockInWorld> createLevelCache(LevelReader pLevel, boolean pForceLoad) {
      return CacheBuilder.newBuilder().build(new BlockPattern.BlockCacheLoader(pLevel, pForceLoad));
   }

   /**
    * Offsets the position of pos in the direction of finger and thumb facing by offset amounts, follows the right-hand
    * rule for cross products (finger, thumb, palm)
    * 
    * @return a new BlockPos offset in the facing directions
    */
   protected static BlockPos translateAndRotate(BlockPos pPos, Direction pFinger, Direction pThumb, int pPalmOffset, int pThumbOffset, int pFingerOffset) {
      if (pFinger != pThumb && pFinger != pThumb.getOpposite()) {
         Vec3i vec3i = new Vec3i(pFinger.getStepX(), pFinger.getStepY(), pFinger.getStepZ());
         Vec3i vec3i1 = new Vec3i(pThumb.getStepX(), pThumb.getStepY(), pThumb.getStepZ());
         Vec3i vec3i2 = vec3i.cross(vec3i1);
         return pPos.offset(vec3i1.getX() * -pThumbOffset + vec3i2.getX() * pPalmOffset + vec3i.getX() * pFingerOffset, vec3i1.getY() * -pThumbOffset + vec3i2.getY() * pPalmOffset + vec3i.getY() * pFingerOffset, vec3i1.getZ() * -pThumbOffset + vec3i2.getZ() * pPalmOffset + vec3i.getZ() * pFingerOffset);
      } else {
         throw new IllegalArgumentException("Invalid forwards & up combination");
      }
   }

   static class BlockCacheLoader extends CacheLoader<BlockPos, BlockInWorld> {
      private final LevelReader level;
      private final boolean loadChunks;

      public BlockCacheLoader(LevelReader pLevel, boolean pLoadChunks) {
         this.level = pLevel;
         this.loadChunks = pLoadChunks;
      }

      public BlockInWorld load(BlockPos pPos) {
         return new BlockInWorld(this.level, pPos, this.loadChunks);
      }
   }

   public static class BlockPatternMatch {
      private final BlockPos frontTopLeft;
      private final Direction forwards;
      private final Direction up;
      private final LoadingCache<BlockPos, BlockInWorld> cache;
      private final int width;
      private final int height;
      private final int depth;

      public BlockPatternMatch(BlockPos pFrontTopLeft, Direction pForwards, Direction pUp, LoadingCache<BlockPos, BlockInWorld> pCache, int pWidth, int pHeight, int pDepth) {
         this.frontTopLeft = pFrontTopLeft;
         this.forwards = pForwards;
         this.up = pUp;
         this.cache = pCache;
         this.width = pWidth;
         this.height = pHeight;
         this.depth = pDepth;
      }

      public BlockPos getFrontTopLeft() {
         return this.frontTopLeft;
      }

      public Direction getForwards() {
         return this.forwards;
      }

      public Direction getUp() {
         return this.up;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }

      public int getDepth() {
         return this.depth;
      }

      public BlockInWorld getBlock(int pPalmOffset, int pThumbOffset, int pFingerOffset) {
         return this.cache.getUnchecked(BlockPattern.translateAndRotate(this.frontTopLeft, this.getForwards(), this.getUp(), pPalmOffset, pThumbOffset, pFingerOffset));
      }

      public String toString() {
         return MoreObjects.toStringHelper(this).add("up", this.up).add("forwards", this.forwards).add("frontTopLeft", this.frontTopLeft).toString();
      }
   }
}