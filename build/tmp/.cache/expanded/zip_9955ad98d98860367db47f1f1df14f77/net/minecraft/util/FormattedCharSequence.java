package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import net.minecraft.network.chat.Style;

@FunctionalInterface
public interface FormattedCharSequence {
   FormattedCharSequence EMPTY = (p_13704_) -> {
      return true;
   };

   boolean accept(FormattedCharSink pSink);

   static FormattedCharSequence codepoint(int pCodePoint, Style pStyle) {
      return (p_13730_) -> {
         return p_13730_.accept(0, pStyle, pCodePoint);
      };
   }

   static FormattedCharSequence forward(String pText, Style pStyle) {
      return pText.isEmpty() ? EMPTY : (p_13739_) -> {
         return StringDecomposer.iterate(pText, pStyle, p_13739_);
      };
   }

   static FormattedCharSequence forward(String pText, Style pStyle, Int2IntFunction pCodePointMapper) {
      return pText.isEmpty() ? EMPTY : (p_144730_) -> {
         return StringDecomposer.iterate(pText, pStyle, decorateOutput(p_144730_, pCodePointMapper));
      };
   }

   static FormattedCharSequence backward(String pText, Style pStyle) {
      return pText.isEmpty() ? EMPTY : (p_144716_) -> {
         return StringDecomposer.iterateBackwards(pText, pStyle, p_144716_);
      };
   }

   static FormattedCharSequence backward(String pText, Style pStyle, Int2IntFunction pCodePointMapper) {
      return pText.isEmpty() ? EMPTY : (p_13721_) -> {
         return StringDecomposer.iterateBackwards(pText, pStyle, decorateOutput(p_13721_, pCodePointMapper));
      };
   }

   static FormattedCharSink decorateOutput(FormattedCharSink pSink, Int2IntFunction pCodePointMapper) {
      return (p_13711_, p_13712_, p_13713_) -> {
         return pSink.accept(p_13711_, p_13712_, pCodePointMapper.apply(Integer.valueOf(p_13713_)));
      };
   }

   static FormattedCharSequence composite() {
      return EMPTY;
   }

   static FormattedCharSequence composite(FormattedCharSequence pSequence) {
      return pSequence;
   }

   static FormattedCharSequence composite(FormattedCharSequence pFirst, FormattedCharSequence pSecond) {
      return fromPair(pFirst, pSecond);
   }

   static FormattedCharSequence composite(FormattedCharSequence... pParts) {
      return fromList(ImmutableList.copyOf(pParts));
   }

   static FormattedCharSequence composite(List<FormattedCharSequence> pParts) {
      int i = pParts.size();
      switch (i) {
         case 0:
            return EMPTY;
         case 1:
            return pParts.get(0);
         case 2:
            return fromPair(pParts.get(0), pParts.get(1));
         default:
            return fromList(ImmutableList.copyOf(pParts));
      }
   }

   static FormattedCharSequence fromPair(FormattedCharSequence pFirst, FormattedCharSequence pSecond) {
      return (p_13702_) -> {
         return pFirst.accept(p_13702_) && pSecond.accept(p_13702_);
      };
   }

   static FormattedCharSequence fromList(List<FormattedCharSequence> pParts) {
      return (p_13726_) -> {
         for(FormattedCharSequence formattedcharsequence : pParts) {
            if (!formattedcharsequence.accept(p_13726_)) {
               return false;
            }
         }

         return true;
      };
   }
}