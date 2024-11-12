package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public record InclusiveRange<T extends Comparable<T>>(T minInclusive, T maxInclusive) {
   public static final Codec<InclusiveRange<Integer>> INT = codec(Codec.INT);

   public InclusiveRange {
      if (minInclusive.compareTo(maxInclusive) > 0) {
         throw new IllegalArgumentException("min_inclusive must be less than or equal to max_inclusive");
      }
   }

   public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> pCodec) {
      return ExtraCodecs.intervalCodec(pCodec, "min_inclusive", "max_inclusive", InclusiveRange::create, InclusiveRange::minInclusive, InclusiveRange::maxInclusive);
   }

   public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> pCodec, T pMin, T pMax) {
      return ExtraCodecs.validate(codec(pCodec), (p_274898_) -> {
         if (p_274898_.minInclusive().compareTo(pMin) < 0) {
            return DataResult.error(() -> {
               return "Range limit too low, expected at least " + pMin + " [" + p_274898_.minInclusive() + "-" + p_274898_.maxInclusive() + "]";
            });
         } else {
            return p_274898_.maxInclusive().compareTo(pMax) > 0 ? DataResult.error(() -> {
               return "Range limit too high, expected at most " + pMax + " [" + p_274898_.minInclusive() + "-" + p_274898_.maxInclusive() + "]";
            }) : DataResult.success(p_274898_);
         }
      });
   }

   public static <T extends Comparable<T>> DataResult<InclusiveRange<T>> create(T p_184581_, T p_184582_) {
      return p_184581_.compareTo(p_184582_) <= 0 ? DataResult.success(new InclusiveRange<>(p_184581_, p_184582_)) : DataResult.error(() -> {
         return "min_inclusive must be less than or equal to max_inclusive";
      });
   }

   public boolean isValueInRange(T pValue) {
      return pValue.compareTo(this.minInclusive) >= 0 && pValue.compareTo(this.maxInclusive) <= 0;
   }

   public boolean contains(InclusiveRange<T> pValue) {
      return pValue.minInclusive().compareTo(this.minInclusive) >= 0 && pValue.maxInclusive.compareTo(this.maxInclusive) <= 0;
   }

   public String toString() {
      return "[" + this.minInclusive + ", " + this.maxInclusive + "]";
   }
}