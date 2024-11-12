package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.function.Function;

public interface ToFloatFunction<C> {
   ToFloatFunction<Float> IDENTITY = createUnlimited((p_216474_) -> {
      return p_216474_;
   });

   float apply(C pObject);

   float minValue();

   float maxValue();

   static ToFloatFunction<Float> createUnlimited(final Float2FloatFunction pWrapped) {
      return new ToFloatFunction<Float>() {
         public float apply(Float p_216483_) {
            return pWrapped.apply(p_216483_);
         }

         public float minValue() {
            return Float.NEGATIVE_INFINITY;
         }

         public float maxValue() {
            return Float.POSITIVE_INFINITY;
         }
      };
   }

   default <C2> ToFloatFunction<C2> comap(final Function<C2, C> pConverter) {
      final ToFloatFunction<C> tofloatfunction = this;
      return new ToFloatFunction<C2>() {
         public float apply(C2 p_216496_) {
            return tofloatfunction.apply(pConverter.apply(p_216496_));
         }

         public float minValue() {
            return tofloatfunction.minValue();
         }

         public float maxValue() {
            return tofloatfunction.maxValue();
         }
      };
   }
}