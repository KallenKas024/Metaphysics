package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

public class SubStringSource {
   private final String plainText;
   private final List<Style> charStyles;
   private final Int2IntFunction reverseCharModifier;

   private SubStringSource(String pPlainText, List<Style> pCharStyles, Int2IntFunction pReverseCharModifier) {
      this.plainText = pPlainText;
      this.charStyles = ImmutableList.copyOf(pCharStyles);
      this.reverseCharModifier = pReverseCharModifier;
   }

   public String getPlainText() {
      return this.plainText;
   }

   public List<FormattedCharSequence> substring(int pFromIndex, int pToIndex, boolean pReversed) {
      if (pToIndex == 0) {
         return ImmutableList.of();
      } else {
         List<FormattedCharSequence> list = Lists.newArrayList();
         Style style = this.charStyles.get(pFromIndex);
         int i = pFromIndex;

         for(int j = 1; j < pToIndex; ++j) {
            int k = pFromIndex + j;
            Style style1 = this.charStyles.get(k);
            if (!style1.equals(style)) {
               String s = this.plainText.substring(i, k);
               list.add(pReversed ? FormattedCharSequence.backward(s, style, this.reverseCharModifier) : FormattedCharSequence.forward(s, style));
               style = style1;
               i = k;
            }
         }

         if (i < pFromIndex + pToIndex) {
            String s1 = this.plainText.substring(i, pFromIndex + pToIndex);
            list.add(pReversed ? FormattedCharSequence.backward(s1, style, this.reverseCharModifier) : FormattedCharSequence.forward(s1, style));
         }

         return pReversed ? Lists.reverse(list) : list;
      }
   }

   public static SubStringSource create(FormattedText pFormattedText) {
      return create(pFormattedText, (p_178527_) -> {
         return p_178527_;
      }, (p_178529_) -> {
         return p_178529_;
      });
   }

   public static SubStringSource create(FormattedText pFormattedText, Int2IntFunction pReverseCharModifier, UnaryOperator<String> pTextTransformer) {
      StringBuilder stringbuilder = new StringBuilder();
      List<Style> list = Lists.newArrayList();
      pFormattedText.visit((p_131249_, p_131250_) -> {
         StringDecomposer.iterateFormatted(p_131250_, p_131249_, (p_178533_, p_178534_, p_178535_) -> {
            stringbuilder.appendCodePoint(p_178535_);
            int i = Character.charCount(p_178535_);

            for(int j = 0; j < i; ++j) {
               list.add(p_178534_);
            }

            return true;
         });
         return Optional.empty();
      }, Style.EMPTY);
      return new SubStringSource(pTextTransformer.apply(stringbuilder.toString()), list, pReverseCharModifier);
   }
}