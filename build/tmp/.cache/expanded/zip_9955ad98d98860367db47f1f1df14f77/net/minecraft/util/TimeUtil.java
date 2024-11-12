package net.minecraft.util;

import java.util.concurrent.TimeUnit;
import net.minecraft.util.valueproviders.UniformInt;

public class TimeUtil {
   public static final long NANOSECONDS_PER_SECOND = TimeUnit.SECONDS.toNanos(1L);
   public static final long NANOSECONDS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1L);

   public static UniformInt rangeOfSeconds(int pMinInclusive, int pMaxInclusive) {
      return UniformInt.of(pMinInclusive * 20, pMaxInclusive * 20);
   }
}