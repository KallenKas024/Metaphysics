package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ThreadingDetector;

public class LegacyRandomSource implements BitRandomSource {
   private static final int MODULUS_BITS = 48;
   private static final long MODULUS_MASK = 281474976710655L;
   private static final long MULTIPLIER = 25214903917L;
   private static final long INCREMENT = 11L;
   private final AtomicLong seed = new AtomicLong();
   private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

   public LegacyRandomSource(long pSeed) {
      this.setSeed(pSeed);
   }

   public RandomSource fork() {
      return new LegacyRandomSource(this.nextLong());
   }

   public PositionalRandomFactory forkPositional() {
      return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
   }

   public void setSeed(long pSeed) {
      if (!this.seed.compareAndSet(this.seed.get(), (pSeed ^ 25214903917L) & 281474976710655L)) {
         throw ThreadingDetector.makeThreadingException("LegacyRandomSource", (Thread)null);
      } else {
         this.gaussianSource.reset();
      }
   }

   public int next(int pSize) {
      long i = this.seed.get();
      long j = i * 25214903917L + 11L & 281474976710655L;
      if (!this.seed.compareAndSet(i, j)) {
         throw ThreadingDetector.makeThreadingException("LegacyRandomSource", (Thread)null);
      } else {
         return (int)(j >> 48 - pSize);
      }
   }

   public double nextGaussian() {
      return this.gaussianSource.nextGaussian();
   }

   public static class LegacyPositionalRandomFactory implements PositionalRandomFactory {
      private final long seed;

      public LegacyPositionalRandomFactory(long pSeed) {
         this.seed = pSeed;
      }

      public RandomSource at(int pX, int pY, int pZ) {
         long i = Mth.getSeed(pX, pY, pZ);
         long j = i ^ this.seed;
         return new LegacyRandomSource(j);
      }

      public RandomSource fromHashOf(String pName) {
         int i = pName.hashCode();
         return new LegacyRandomSource((long)i ^ this.seed);
      }

      @VisibleForTesting
      public void parityConfigString(StringBuilder pBuilder) {
         pBuilder.append("LegacyPositionalRandomFactory{").append(this.seed).append("}");
      }
   }
}