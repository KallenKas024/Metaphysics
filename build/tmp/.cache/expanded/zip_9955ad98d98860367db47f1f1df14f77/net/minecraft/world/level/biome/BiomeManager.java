package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;

public class BiomeManager {
   public static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
   private static final int ZOOM_BITS = 2;
   private static final int ZOOM = 4;
   private static final int ZOOM_MASK = 3;
   private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
   private final long biomeZoomSeed;

   public BiomeManager(BiomeManager.NoiseBiomeSource pNoiseBiomeSource, long pBiomeZoomSeed) {
      this.noiseBiomeSource = pNoiseBiomeSource;
      this.biomeZoomSeed = pBiomeZoomSeed;
   }

   public static long obfuscateSeed(long pSeed) {
      return Hashing.sha256().hashLong(pSeed).asLong();
   }

   public BiomeManager withDifferentSource(BiomeManager.NoiseBiomeSource pNewSource) {
      return new BiomeManager(pNewSource, this.biomeZoomSeed);
   }

   public Holder<Biome> getBiome(BlockPos pPos) {
      int i = pPos.getX() - 2;
      int j = pPos.getY() - 2;
      int k = pPos.getZ() - 2;
      int l = i >> 2;
      int i1 = j >> 2;
      int j1 = k >> 2;
      double d0 = (double)(i & 3) / 4.0D;
      double d1 = (double)(j & 3) / 4.0D;
      double d2 = (double)(k & 3) / 4.0D;
      int k1 = 0;
      double d3 = Double.POSITIVE_INFINITY;

      for(int l1 = 0; l1 < 8; ++l1) {
         boolean flag = (l1 & 4) == 0;
         boolean flag1 = (l1 & 2) == 0;
         boolean flag2 = (l1 & 1) == 0;
         int i2 = flag ? l : l + 1;
         int j2 = flag1 ? i1 : i1 + 1;
         int k2 = flag2 ? j1 : j1 + 1;
         double d4 = flag ? d0 : d0 - 1.0D;
         double d5 = flag1 ? d1 : d1 - 1.0D;
         double d6 = flag2 ? d2 : d2 - 1.0D;
         double d7 = getFiddledDistance(this.biomeZoomSeed, i2, j2, k2, d4, d5, d6);
         if (d3 > d7) {
            k1 = l1;
            d3 = d7;
         }
      }

      int l2 = (k1 & 4) == 0 ? l : l + 1;
      int i3 = (k1 & 2) == 0 ? i1 : i1 + 1;
      int j3 = (k1 & 1) == 0 ? j1 : j1 + 1;
      return this.noiseBiomeSource.getNoiseBiome(l2, i3, j3);
   }

   public Holder<Biome> getNoiseBiomeAtPosition(double pX, double pY, double pZ) {
      int i = QuartPos.fromBlock(Mth.floor(pX));
      int j = QuartPos.fromBlock(Mth.floor(pY));
      int k = QuartPos.fromBlock(Mth.floor(pZ));
      return this.getNoiseBiomeAtQuart(i, j, k);
   }

   public Holder<Biome> getNoiseBiomeAtPosition(BlockPos pPos) {
      int i = QuartPos.fromBlock(pPos.getX());
      int j = QuartPos.fromBlock(pPos.getY());
      int k = QuartPos.fromBlock(pPos.getZ());
      return this.getNoiseBiomeAtQuart(i, j, k);
   }

   public Holder<Biome> getNoiseBiomeAtQuart(int pX, int pY, int pZ) {
      return this.noiseBiomeSource.getNoiseBiome(pX, pY, pZ);
   }

   private static double getFiddledDistance(long pSeed, int pX, int pY, int pZ, double pXNoise, double pYNoise, double pZNoise) {
      long $$7 = LinearCongruentialGenerator.next(pSeed, (long)pX);
      $$7 = LinearCongruentialGenerator.next($$7, (long)pY);
      $$7 = LinearCongruentialGenerator.next($$7, (long)pZ);
      $$7 = LinearCongruentialGenerator.next($$7, (long)pX);
      $$7 = LinearCongruentialGenerator.next($$7, (long)pY);
      $$7 = LinearCongruentialGenerator.next($$7, (long)pZ);
      double d0 = getFiddle($$7);
      $$7 = LinearCongruentialGenerator.next($$7, pSeed);
      double d1 = getFiddle($$7);
      $$7 = LinearCongruentialGenerator.next($$7, pSeed);
      double d2 = getFiddle($$7);
      return Mth.square(pZNoise + d2) + Mth.square(pYNoise + d1) + Mth.square(pXNoise + d0);
   }

   private static double getFiddle(long pSeed) {
      double d0 = (double)Math.floorMod(pSeed >> 24, 1024) / 1024.0D;
      return (d0 - 0.5D) * 0.9D;
   }

   public interface NoiseBiomeSource {
      /**
       * Gets the biome at the given quart positions.
       * Note that the coordinates passed into this method are 1/4 the scale of block coordinates.
       */
      Holder<Biome> getNoiseBiome(int pX, int pY, int pZ);
   }
}