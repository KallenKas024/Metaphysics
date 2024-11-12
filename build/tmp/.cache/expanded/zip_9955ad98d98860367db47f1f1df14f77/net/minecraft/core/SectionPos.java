package net.minecraft.core;

import it.unimi.dsi.fastutil.longs.LongConsumer;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.entity.EntityAccess;

public class SectionPos extends Vec3i {
   public static final int SECTION_BITS = 4;
   public static final int SECTION_SIZE = 16;
   public static final int SECTION_MASK = 15;
   public static final int SECTION_HALF_SIZE = 8;
   public static final int SECTION_MAX_INDEX = 15;
   private static final int PACKED_X_LENGTH = 22;
   private static final int PACKED_Y_LENGTH = 20;
   private static final int PACKED_Z_LENGTH = 22;
   private static final long PACKED_X_MASK = 4194303L;
   private static final long PACKED_Y_MASK = 1048575L;
   private static final long PACKED_Z_MASK = 4194303L;
   private static final int Y_OFFSET = 0;
   private static final int Z_OFFSET = 20;
   private static final int X_OFFSET = 42;
   private static final int RELATIVE_X_SHIFT = 8;
   private static final int RELATIVE_Y_SHIFT = 0;
   private static final int RELATIVE_Z_SHIFT = 4;

   SectionPos(int pX, int pY, int pZ) {
      super(pX, pY, pZ);
   }

   public static SectionPos of(int pChunkX, int pChunkY, int pChunkZ) {
      return new SectionPos(pChunkX, pChunkY, pChunkZ);
   }

   public static SectionPos of(BlockPos pPos) {
      return new SectionPos(blockToSectionCoord(pPos.getX()), blockToSectionCoord(pPos.getY()), blockToSectionCoord(pPos.getZ()));
   }

   public static SectionPos of(ChunkPos pChunkPos, int pY) {
      return new SectionPos(pChunkPos.x, pY, pChunkPos.z);
   }

   public static SectionPos of(EntityAccess pEntity) {
      return of(pEntity.blockPosition());
   }

   public static SectionPos of(Position pPosition) {
      return new SectionPos(blockToSectionCoord(pPosition.x()), blockToSectionCoord(pPosition.y()), blockToSectionCoord(pPosition.z()));
   }

   public static SectionPos of(long pPacked) {
      return new SectionPos(x(pPacked), y(pPacked), z(pPacked));
   }

   public static SectionPos bottomOf(ChunkAccess pChunk) {
      return of(pChunk.getPos(), pChunk.getMinSection());
   }

   public static long offset(long pPacked, Direction pDirection) {
      return offset(pPacked, pDirection.getStepX(), pDirection.getStepY(), pDirection.getStepZ());
   }

   public static long offset(long pPacked, int pDx, int pDy, int pDz) {
      return asLong(x(pPacked) + pDx, y(pPacked) + pDy, z(pPacked) + pDz);
   }

   public static int posToSectionCoord(double pPos) {
      return blockToSectionCoord(Mth.floor(pPos));
   }

   public static int blockToSectionCoord(int pBlockCoord) {
      return pBlockCoord >> 4;
   }

   public static int blockToSectionCoord(double pCoord) {
      return Mth.floor(pCoord) >> 4;
   }

   public static int sectionRelative(int pRel) {
      return pRel & 15;
   }

   public static short sectionRelativePos(BlockPos pPos) {
      int i = sectionRelative(pPos.getX());
      int j = sectionRelative(pPos.getY());
      int k = sectionRelative(pPos.getZ());
      return (short)(i << 8 | k << 4 | j << 0);
   }

   public static int sectionRelativeX(short pX) {
      return pX >>> 8 & 15;
   }

   public static int sectionRelativeY(short pY) {
      return pY >>> 0 & 15;
   }

   public static int sectionRelativeZ(short pZ) {
      return pZ >>> 4 & 15;
   }

   public int relativeToBlockX(short pX) {
      return this.minBlockX() + sectionRelativeX(pX);
   }

   public int relativeToBlockY(short pY) {
      return this.minBlockY() + sectionRelativeY(pY);
   }

   public int relativeToBlockZ(short pZ) {
      return this.minBlockZ() + sectionRelativeZ(pZ);
   }

   public BlockPos relativeToBlockPos(short pPos) {
      return new BlockPos(this.relativeToBlockX(pPos), this.relativeToBlockY(pPos), this.relativeToBlockZ(pPos));
   }

   public static int sectionToBlockCoord(int pSectionCoord) {
      return pSectionCoord << 4;
   }

   public static int sectionToBlockCoord(int pPos, int pOffset) {
      return sectionToBlockCoord(pPos) + pOffset;
   }

   public static int x(long pPacked) {
      return (int)(pPacked << 0 >> 42);
   }

   public static int y(long pPacked) {
      return (int)(pPacked << 44 >> 44);
   }

   public static int z(long pPacked) {
      return (int)(pPacked << 22 >> 42);
   }

   public int x() {
      return this.getX();
   }

   public int y() {
      return this.getY();
   }

   public int z() {
      return this.getZ();
   }

   public int minBlockX() {
      return sectionToBlockCoord(this.x());
   }

   public int minBlockY() {
      return sectionToBlockCoord(this.y());
   }

   public int minBlockZ() {
      return sectionToBlockCoord(this.z());
   }

   public int maxBlockX() {
      return sectionToBlockCoord(this.x(), 15);
   }

   public int maxBlockY() {
      return sectionToBlockCoord(this.y(), 15);
   }

   public int maxBlockZ() {
      return sectionToBlockCoord(this.z(), 15);
   }

   public static long blockToSection(long pLevelPos) {
      return asLong(blockToSectionCoord(BlockPos.getX(pLevelPos)), blockToSectionCoord(BlockPos.getY(pLevelPos)), blockToSectionCoord(BlockPos.getZ(pLevelPos)));
   }

   public static long getZeroNode(int pX, int pZ) {
      return getZeroNode(asLong(pX, 0, pZ));
   }

   public static long getZeroNode(long pPos) {
      return pPos & -1048576L;
   }

   public BlockPos origin() {
      return new BlockPos(sectionToBlockCoord(this.x()), sectionToBlockCoord(this.y()), sectionToBlockCoord(this.z()));
   }

   public BlockPos center() {
      int i = 8;
      return this.origin().offset(8, 8, 8);
   }

   public ChunkPos chunk() {
      return new ChunkPos(this.x(), this.z());
   }

   public static long asLong(BlockPos pBlockPos) {
      return asLong(blockToSectionCoord(pBlockPos.getX()), blockToSectionCoord(pBlockPos.getY()), blockToSectionCoord(pBlockPos.getZ()));
   }

   public static long asLong(int pX, int pY, int pZ) {
      long i = 0L;
      i |= ((long)pX & 4194303L) << 42;
      i |= ((long)pY & 1048575L) << 0;
      return i | ((long)pZ & 4194303L) << 20;
   }

   public long asLong() {
      return asLong(this.x(), this.y(), this.z());
   }

   public SectionPos offset(int pDx, int pDy, int pDz) {
      return pDx == 0 && pDy == 0 && pDz == 0 ? this : new SectionPos(this.x() + pDx, this.y() + pDy, this.z() + pDz);
   }

   public Stream<BlockPos> blocksInside() {
      return BlockPos.betweenClosedStream(this.minBlockX(), this.minBlockY(), this.minBlockZ(), this.maxBlockX(), this.maxBlockY(), this.maxBlockZ());
   }

   public static Stream<SectionPos> cube(SectionPos pCenter, int pRadius) {
      int i = pCenter.x();
      int j = pCenter.y();
      int k = pCenter.z();
      return betweenClosedStream(i - pRadius, j - pRadius, k - pRadius, i + pRadius, j + pRadius, k + pRadius);
   }

   public static Stream<SectionPos> aroundChunk(ChunkPos pChunkPos, int pX, int pY, int pZ) {
      int i = pChunkPos.x;
      int j = pChunkPos.z;
      return betweenClosedStream(i - pX, pY, j - pX, i + pX, pZ - 1, j + pX);
   }

   public static Stream<SectionPos> betweenClosedStream(final int pX1, final int pY1, final int pZ1, final int pX2, final int pY2, final int pZ2) {
      return StreamSupport.stream(new Spliterators.AbstractSpliterator<SectionPos>((long)((pX2 - pX1 + 1) * (pY2 - pY1 + 1) * (pZ2 - pZ1 + 1)), 64) {
         final Cursor3D cursor = new Cursor3D(pX1, pY1, pZ1, pX2, pY2, pZ2);

         public boolean tryAdvance(Consumer<? super SectionPos> p_123271_) {
            if (this.cursor.advance()) {
               p_123271_.accept(new SectionPos(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
               return true;
            } else {
               return false;
            }
         }
      }, false);
   }

   public static void aroundAndAtBlockPos(BlockPos pPos, LongConsumer pConsumer) {
      aroundAndAtBlockPos(pPos.getX(), pPos.getY(), pPos.getZ(), pConsumer);
   }

   public static void aroundAndAtBlockPos(long pPos, LongConsumer pConsumer) {
      aroundAndAtBlockPos(BlockPos.getX(pPos), BlockPos.getY(pPos), BlockPos.getZ(pPos), pConsumer);
   }

   public static void aroundAndAtBlockPos(int pX, int pY, int pZ, LongConsumer pConsumer) {
      int i = blockToSectionCoord(pX - 1);
      int j = blockToSectionCoord(pX + 1);
      int k = blockToSectionCoord(pY - 1);
      int l = blockToSectionCoord(pY + 1);
      int i1 = blockToSectionCoord(pZ - 1);
      int j1 = blockToSectionCoord(pZ + 1);
      if (i == j && k == l && i1 == j1) {
         pConsumer.accept(asLong(i, k, i1));
      } else {
         for(int k1 = i; k1 <= j; ++k1) {
            for(int l1 = k; l1 <= l; ++l1) {
               for(int i2 = i1; i2 <= j1; ++i2) {
                  pConsumer.accept(asLong(k1, l1, i2));
               }
            }
         }
      }

   }
}