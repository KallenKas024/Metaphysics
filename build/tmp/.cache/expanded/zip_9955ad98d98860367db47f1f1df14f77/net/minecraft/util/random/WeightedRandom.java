package net.minecraft.util.random;

import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

public class WeightedRandom {
   private WeightedRandom() {
   }

   public static int getTotalWeight(List<? extends WeightedEntry> pEntries) {
      long i = 0L;

      for(WeightedEntry weightedentry : pEntries) {
         i += (long)weightedentry.getWeight().asInt();
      }

      if (i > 2147483647L) {
         throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
      } else {
         return (int)i;
      }
   }

   public static <T extends WeightedEntry> Optional<T> getRandomItem(RandomSource pRandom, List<T> pEntries, int pTotalWeight) {
      if (pTotalWeight < 0) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
      } else if (pTotalWeight == 0) {
         return Optional.empty();
      } else {
         int i = pRandom.nextInt(pTotalWeight);
         return getWeightedItem(pEntries, i);
      }
   }

   public static <T extends WeightedEntry> Optional<T> getWeightedItem(List<T> pEntries, int pWeightedIndex) {
      for(T t : pEntries) {
         pWeightedIndex -= t.getWeight().asInt();
         if (pWeightedIndex < 0) {
            return Optional.of(t);
         }
      }

      return Optional.empty();
   }

   public static <T extends WeightedEntry> Optional<T> getRandomItem(RandomSource pRandom, List<T> pEntries) {
      return getRandomItem(pRandom, pEntries, getTotalWeight(pEntries));
   }
}