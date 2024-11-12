package net.minecraft.world.level.levelgen;

import java.util.function.LongFunction;
import net.minecraft.util.RandomSource;

public class WorldgenRandom extends LegacyRandomSource {
   private final RandomSource randomSource;
   private int count;

   public WorldgenRandom(RandomSource pRandomSource) {
      super(0L);
      this.randomSource = pRandomSource;
   }

   public int getCount() {
      return this.count;
   }

   public RandomSource fork() {
      return this.randomSource.fork();
   }

   public PositionalRandomFactory forkPositional() {
      return this.randomSource.forkPositional();
   }

   public int next(int pBits) {
      ++this.count;
      RandomSource randomsource = this.randomSource;
      if (randomsource instanceof LegacyRandomSource legacyrandomsource) {
         return legacyrandomsource.next(pBits);
      } else {
         return (int)(this.randomSource.nextLong() >>> 64 - pBits);
      }
   }

   public synchronized void setSeed(long pSeed) {
      if (this.randomSource != null) {
         this.randomSource.setSeed(pSeed);
      }
   }

   /**
    * Seeds the current random for chunk decoration, including spawning mobs and for use in feature placement.
    * The coordinates correspond to the minimum block position within a given chunk.
    */
   public long setDecorationSeed(long pLevelSeed, int pMinChunkBlockX, int pMinChunkBlockZ) {
      this.setSeed(pLevelSeed);
      long i = this.nextLong() | 1L;
      long j = this.nextLong() | 1L;
      long k = (long)pMinChunkBlockX * i + (long)pMinChunkBlockZ * j ^ pLevelSeed;
      this.setSeed(k);
      return k;
   }

   /**
    * Seeds the current random for placing features.
    * Each feature is seeded differently in order to seem more random. However, it does not do a good job of this, and
    * issues can arise from the salt being small with features that have the same decoration step and are close together
    * in the feature lists.
    * @param pDecorationSeed The seed computed by {@link #setDecorationSeed(long, int, int)}
    * @param pIndex The cumulative index of the generating feature within the biome's list of features.
    * @param pDecorationStep The ordinal of the {@link net.minecraft.world.level.levelgen.GenerationStep.Decoration} of
    * the generating feature.
    */
   public void setFeatureSeed(long pDecorationSeed, int pIndex, int pDecorationStep) {
      long i = pDecorationSeed + (long)pIndex + (long)(10000 * pDecorationStep);
      this.setSeed(i);
   }

   /**
    * Seeds the current random for placing large features such as caves, strongholds, and mineshafts.
    * @param pBaseSeed This is passed in as the level seed, or in some cases such as carvers, as an offset from the
    * level seed unique to each carver.
    */
   public void setLargeFeatureSeed(long pBaseSeed, int pChunkX, int pChunkZ) {
      this.setSeed(pBaseSeed);
      long i = this.nextLong();
      long j = this.nextLong();
      long k = (long)pChunkX * i ^ (long)pChunkZ * j ^ pBaseSeed;
      this.setSeed(k);
   }

   /**
    * Seeds the current random for placing the starts of structure features.
    * The region coordinates are the region which the target chunk lies in. For example, witch hut regions are 32x32
    * chunks, so all chunks within that region would be seeded identically.
    * The size of the regions themselves are determined by the {@code spacing} of the structure settings.
    * @param pSalt A salt unique to each structure.
    */
   public void setLargeFeatureWithSalt(long pLevelSeed, int pRegionX, int pRegionZ, int pSalt) {
      long i = (long)pRegionX * 341873128712L + (long)pRegionZ * 132897987541L + pLevelSeed + (long)pSalt;
      this.setSeed(i);
   }

   /**
    * Creates a new {@code RandomSource}, seeded for determining whether a chunk is a slime chunk or not.
    * @param pSalt For vanilla slimes, this is always {@code 987234911L}
    */
   public static RandomSource seedSlimeChunk(int pChunkX, int pChunkZ, long pLevelSeed, long pSalt) {
      return RandomSource.create(pLevelSeed + (long)(pChunkX * pChunkX * 4987142) + (long)(pChunkX * 5947611) + (long)(pChunkZ * pChunkZ) * 4392871L + (long)(pChunkZ * 389711) ^ pSalt);
   }

   public static enum Algorithm {
      LEGACY(LegacyRandomSource::new),
      XOROSHIRO(XoroshiroRandomSource::new);

      private final LongFunction<RandomSource> constructor;

      private Algorithm(LongFunction<RandomSource> pConstructor) {
         this.constructor = pConstructor;
      }

      public RandomSource newInstance(long pSeed) {
         return this.constructor.apply(pSeed);
      }
   }
}