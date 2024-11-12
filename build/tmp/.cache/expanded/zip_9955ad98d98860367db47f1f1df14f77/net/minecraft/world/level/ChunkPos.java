package net.minecraft.world.level;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public class ChunkPos {
   private static final int SAFETY_MARGIN = 1056;
   /** Value representing an absent or invalid chunkpos */
   public static final long INVALID_CHUNK_POS = asLong(1875066, 1875066);
   public static final ChunkPos ZERO = new ChunkPos(0, 0);
   private static final long COORD_BITS = 32L;
   private static final long COORD_MASK = 4294967295L;
   private static final int REGION_BITS = 5;
   public static final int REGION_SIZE = 32;
   private static final int REGION_MASK = 31;
   public static final int REGION_MAX_INDEX = 31;
   public final int x;
   public final int z;
   private static final int HASH_A = 1664525;
   private static final int HASH_C = 1013904223;
   private static final int HASH_Z_XOR = -559038737;

   public ChunkPos(int pX, int pY) {
      this.x = pX;
      this.z = pY;
   }

   public ChunkPos(BlockPos pPos) {
      this.x = SectionPos.blockToSectionCoord(pPos.getX());
      this.z = SectionPos.blockToSectionCoord(pPos.getZ());
   }

   public ChunkPos(long pPackedPos) {
      this.x = (int)pPackedPos;
      this.z = (int)(pPackedPos >> 32);
   }

   public static ChunkPos minFromRegion(int p_220338_, int p_220339_) {
      return new ChunkPos(p_220338_ << 5, p_220339_ << 5);
   }

   public static ChunkPos maxFromRegion(int p_220341_, int p_220342_) {
      return new ChunkPos((p_220341_ << 5) + 31, (p_220342_ << 5) + 31);
   }

   public long toLong() {
      return asLong(this.x, this.z);
   }

   /**
    * Converts the chunk coordinate pair to a long
    */
   public static long asLong(int pX, int pZ) {
      return (long)pX & 4294967295L | ((long)pZ & 4294967295L) << 32;
   }

   public static long asLong(BlockPos pPos) {
      return asLong(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()));
   }

   public static int getX(long pChunkAsLong) {
      return (int)(pChunkAsLong & 4294967295L);
   }

   public static int getZ(long pChunkAsLong) {
      return (int)(pChunkAsLong >>> 32 & 4294967295L);
   }

   public int hashCode() {
      return hash(this.x, this.z);
   }

   public static int hash(int pX, int pZ) {
      int i = 1664525 * pX + 1013904223;
      int j = 1664525 * (pZ ^ -559038737) + 1013904223;
      return i ^ j;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof ChunkPos)) {
         return false;
      } else {
         ChunkPos chunkpos = (ChunkPos)pOther;
         return this.x == chunkpos.x && this.z == chunkpos.z;
      }
   }

   public int getMiddleBlockX() {
      return this.getBlockX(8);
   }

   public int getMiddleBlockZ() {
      return this.getBlockZ(8);
   }

   /**
    * Get the first world X coordinate that belongs to this Chunk
    */
   public int getMinBlockX() {
      return SectionPos.sectionToBlockCoord(this.x);
   }

   /**
    * Get the first world Z coordinate that belongs to this Chunk
    */
   public int getMinBlockZ() {
      return SectionPos.sectionToBlockCoord(this.z);
   }

   /**
    * Get the last world X coordinate that belongs to this Chunk
    */
   public int getMaxBlockX() {
      return this.getBlockX(15);
   }

   /**
    * Get the last world Z coordinate that belongs to this Chunk
    */
   public int getMaxBlockZ() {
      return this.getBlockZ(15);
   }

   /**
    * Gets the x-coordinate of the region file containing this chunk.
    */
   public int getRegionX() {
      return this.x >> 5;
   }

   /**
    * Gets the z-coordinate of the region file containing this chunk.
    */
   public int getRegionZ() {
      return this.z >> 5;
   }

   /**
    * Gets the x-coordinate of this chunk within the region file that contains it.
    */
   public int getRegionLocalX() {
      return this.x & 31;
   }

   /**
    * Gets the z-coordinate of this chunk within the region file that contains it.
    */
   public int getRegionLocalZ() {
      return this.z & 31;
   }

   public BlockPos getBlockAt(int pXSection, int pY, int pZSection) {
      return new BlockPos(this.getBlockX(pXSection), pY, this.getBlockZ(pZSection));
   }

   public int getBlockX(int pX) {
      return SectionPos.sectionToBlockCoord(this.x, pX);
   }

   public int getBlockZ(int pZ) {
      return SectionPos.sectionToBlockCoord(this.z, pZ);
   }

   public BlockPos getMiddleBlockPosition(int pY) {
      return new BlockPos(this.getMiddleBlockX(), pY, this.getMiddleBlockZ());
   }

   public String toString() {
      return "[" + this.x + ", " + this.z + "]";
   }

   public BlockPos getWorldPosition() {
      return new BlockPos(this.getMinBlockX(), 0, this.getMinBlockZ());
   }

   public int getChessboardDistance(ChunkPos pChunkPos) {
      return Math.max(Math.abs(this.x - pChunkPos.x), Math.abs(this.z - pChunkPos.z));
   }

   public static Stream<ChunkPos> rangeClosed(ChunkPos pCenter, int pRadius) {
      return rangeClosed(new ChunkPos(pCenter.x - pRadius, pCenter.z - pRadius), new ChunkPos(pCenter.x + pRadius, pCenter.z + pRadius));
   }

   public static Stream<ChunkPos> rangeClosed(final ChunkPos pStart, final ChunkPos pEnd) {
      int i = Math.abs(pStart.x - pEnd.x) + 1;
      int j = Math.abs(pStart.z - pEnd.z) + 1;
      final int k = pStart.x < pEnd.x ? 1 : -1;
      final int l = pStart.z < pEnd.z ? 1 : -1;
      return StreamSupport.stream(new Spliterators.AbstractSpliterator<ChunkPos>((long)(i * j), 64) {
         @Nullable
         private ChunkPos pos;

         public boolean tryAdvance(Consumer<? super ChunkPos> p_45630_) {
            if (this.pos == null) {
               this.pos = pStart;
            } else {
               int i1 = this.pos.x;
               int j1 = this.pos.z;
               if (i1 == pEnd.x) {
                  if (j1 == pEnd.z) {
                     return false;
                  }

                  this.pos = new ChunkPos(pStart.x, j1 + l);
               } else {
                  this.pos = new ChunkPos(i1 + k, j1);
               }
            }

            p_45630_.accept(this.pos);
            return true;
         }
      }, false);
   }
}