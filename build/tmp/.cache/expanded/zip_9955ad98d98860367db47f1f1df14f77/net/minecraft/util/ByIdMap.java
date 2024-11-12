package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public class ByIdMap {
   private static <T> IntFunction<T> createMap(ToIntFunction<T> pKeyExtractor, T[] pValues) {
      if (pValues.length == 0) {
         throw new IllegalArgumentException("Empty value list");
      } else {
         Int2ObjectMap<T> int2objectmap = new Int2ObjectOpenHashMap<>();

         for(T t : pValues) {
            int i = pKeyExtractor.applyAsInt(t);
            T t1 = int2objectmap.put(i, t);
            if (t1 != null) {
               throw new IllegalArgumentException("Duplicate entry on id " + i + ": current=" + t + ", previous=" + t1);
            }
         }

         return int2objectmap;
      }
   }

   public static <T> IntFunction<T> sparse(ToIntFunction<T> pKeyExtractor, T[] pValues, T pFallback) {
      IntFunction<T> intfunction = createMap(pKeyExtractor, pValues);
      return (p_262932_) -> {
         return Objects.requireNonNullElse(intfunction.apply(p_262932_), pFallback);
      };
   }

   private static <T> T[] createSortedArray(ToIntFunction<T> pKeyExtractor, T[] pValues) {
      int i = pValues.length;
      if (i == 0) {
         throw new IllegalArgumentException("Empty value list");
      } else {
         T[] at = (T[])((Object[])pValues.clone());
         Arrays.fill(at, (Object)null);

         for(T t : pValues) {
            int j = pKeyExtractor.applyAsInt(t);
            if (j < 0 || j >= i) {
               throw new IllegalArgumentException("Values are not continous, found index " + j + " for value " + t);
            }

            T t1 = at[j];
            if (t1 != null) {
               throw new IllegalArgumentException("Duplicate entry on id " + j + ": current=" + t + ", previous=" + t1);
            }

            at[j] = t;
         }

         for(int k = 0; k < i; ++k) {
            if (at[k] == null) {
               throw new IllegalArgumentException("Missing value at index: " + k);
            }
         }

         return at;
      }
   }

   public static <T> IntFunction<T> continuous(ToIntFunction<T> pKeyExtractor, T[] pValues, ByIdMap.OutOfBoundsStrategy pOutOfBoundsStrategy) {
      T[] at = createSortedArray(pKeyExtractor, pValues);
      int i = at.length;
      IntFunction intfunction;
      switch (pOutOfBoundsStrategy) {
         case ZERO:
            T t = at[0];
            intfunction = (p_262927_) -> {
               return p_262927_ >= 0 && p_262927_ < i ? at[p_262927_] : t;
            };
            break;
         case WRAP:
            intfunction = (p_262977_) -> {
               return at[Mth.positiveModulo(p_262977_, i)];
            };
            break;
         case CLAMP:
            intfunction = (p_263013_) -> {
               return at[Mth.clamp(p_263013_, 0, i - 1)];
            };
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return intfunction;
   }

   public static enum OutOfBoundsStrategy {
      ZERO,
      WRAP,
      CLAMP;
   }
}