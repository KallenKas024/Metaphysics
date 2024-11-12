package net.minecraft.world.level.chunk;

import java.util.BitSet;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CarvingMask {
   private final int minY;
   private final BitSet mask;
   private CarvingMask.Mask additionalMask = (p_196713_, p_196714_, p_196715_) -> {
      return false;
   };

   public CarvingMask(int pMask, int pMinY) {
      this.minY = pMinY;
      this.mask = new BitSet(256 * pMask);
   }

   public void setAdditionalMask(CarvingMask.Mask pAdditionalMask) {
      this.additionalMask = pAdditionalMask;
   }

   public CarvingMask(long[] pMask, int pMinY) {
      this.minY = pMinY;
      this.mask = BitSet.valueOf(pMask);
   }

   private int getIndex(int pX, int pY, int pZ) {
      return pX & 15 | (pZ & 15) << 4 | pY - this.minY << 8;
   }

   public void set(int pX, int pY, int pZ) {
      this.mask.set(this.getIndex(pX, pY, pZ));
   }

   public boolean get(int pX, int pY, int pZ) {
      return this.additionalMask.test(pX, pY, pZ) || this.mask.get(this.getIndex(pX, pY, pZ));
   }

   public Stream<BlockPos> stream(ChunkPos pPos) {
      return this.mask.stream().mapToObj((p_196709_) -> {
         int i = p_196709_ & 15;
         int j = p_196709_ >> 4 & 15;
         int k = p_196709_ >> 8;
         return pPos.getBlockAt(i, k + this.minY, j);
      });
   }

   public long[] toArray() {
      return this.mask.toLongArray();
   }

   public interface Mask {
      boolean test(int pX, int pY, int pZ);
   }
}