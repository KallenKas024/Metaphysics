package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record KeyDispatchDataCodec<A>(Codec<A> codec) {
   public static <A> KeyDispatchDataCodec<A> of(Codec<A> pCodec) {
      return new KeyDispatchDataCodec<>(pCodec);
   }

   public static <A> KeyDispatchDataCodec<A> of(MapCodec<A> pCodec) {
      return new KeyDispatchDataCodec<>(pCodec.codec());
   }
}