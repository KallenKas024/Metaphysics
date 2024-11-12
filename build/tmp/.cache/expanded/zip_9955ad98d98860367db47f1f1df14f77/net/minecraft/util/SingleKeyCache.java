package net.minecraft.util;

import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;

public class SingleKeyCache<K, V> {
   private final Function<K, V> computeValue;
   @Nullable
   private K cacheKey = (K)null;
   @Nullable
   private V cachedValue;

   public SingleKeyCache(Function<K, V> pComputeValue) {
      this.computeValue = pComputeValue;
   }

   public V getValue(K pCacheKey) {
      if (this.cachedValue == null || !Objects.equals(this.cacheKey, pCacheKey)) {
         this.cachedValue = this.computeValue.apply(pCacheKey);
         this.cacheKey = pCacheKey;
      }

      return this.cachedValue;
   }
}