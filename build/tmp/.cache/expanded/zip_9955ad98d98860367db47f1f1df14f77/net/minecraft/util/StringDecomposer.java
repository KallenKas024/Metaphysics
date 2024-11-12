package net.minecraft.util;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class StringDecomposer {
   private static final char REPLACEMENT_CHAR = '\ufffd';
   private static final Optional<Object> STOP_ITERATION = Optional.of(Unit.INSTANCE);

   private static boolean feedChar(Style pStyle, FormattedCharSink pSink, int pPosition, char pCharacter) {
      return Character.isSurrogate(pCharacter) ? pSink.accept(pPosition, pStyle, 65533) : pSink.accept(pPosition, pStyle, pCharacter);
   }

   public static boolean iterate(String pText, Style pStyle, FormattedCharSink pSink) {
      int i = pText.length();

      for(int j = 0; j < i; ++j) {
         char c0 = pText.charAt(j);
         if (Character.isHighSurrogate(c0)) {
            if (j + 1 >= i) {
               if (!pSink.accept(j, pStyle, 65533)) {
                  return false;
               }
               break;
            }

            char c1 = pText.charAt(j + 1);
            if (Character.isLowSurrogate(c1)) {
               if (!pSink.accept(j, pStyle, Character.toCodePoint(c0, c1))) {
                  return false;
               }

               ++j;
            } else if (!pSink.accept(j, pStyle, 65533)) {
               return false;
            }
         } else if (!feedChar(pStyle, pSink, j, c0)) {
            return false;
         }
      }

      return true;
   }

   public static boolean iterateBackwards(String pText, Style pStyle, FormattedCharSink pSink) {
      int i = pText.length();

      for(int j = i - 1; j >= 0; --j) {
         char c0 = pText.charAt(j);
         if (Character.isLowSurrogate(c0)) {
            if (j - 1 < 0) {
               if (!pSink.accept(0, pStyle, 65533)) {
                  return false;
               }
               break;
            }

            char c1 = pText.charAt(j - 1);
            if (Character.isHighSurrogate(c1)) {
               --j;
               if (!pSink.accept(j, pStyle, Character.toCodePoint(c1, c0))) {
                  return false;
               }
            } else if (!pSink.accept(j, pStyle, 65533)) {
               return false;
            }
         } else if (!feedChar(pStyle, pSink, j, c0)) {
            return false;
         }
      }

      return true;
   }

   /**
    * Iterate a String while applying legacy formatting codes starting with a {@code 鎼倉 sign.
    */
   public static boolean iterateFormatted(String pText, Style pStyle, FormattedCharSink pSink) {
      return iterateFormatted(pText, 0, pStyle, pSink);
   }

   /**
    * Iterate a String while applying legacy formatting codes starting with a {@code 鎼倉 sign.
    * @param pSkip The amount of characters to skip from the beginning.
    */
   public static boolean iterateFormatted(String pText, int pSkip, Style pStyle, FormattedCharSink pSink) {
      return iterateFormatted(pText, pSkip, pStyle, pStyle, pSink);
   }

   /**
    * Iterate a String while applying legacy formatting codes starting with a {@code 鎼倉 sign.
    * @param pSkip The amount of character to skip from the beginning.
    * @param pCurrentStyle The current style at the starting position after the skip.
    * @param pDefaultStyle The default style for the sequence that should be applied after a reset format code ({@code
    * 鎼俽})
    */
   public static boolean iterateFormatted(String pText, int pSkip, Style pCurrentStyle, Style pDefaultStyle, FormattedCharSink pSink) {
      int i = pText.length();
      Style style = pCurrentStyle;

      for(int j = pSkip; j < i; ++j) {
         char c0 = pText.charAt(j);
         if (c0 == 167) {
            if (j + 1 >= i) {
               break;
            }

            char c1 = pText.charAt(j + 1);
            ChatFormatting chatformatting = ChatFormatting.getByCode(c1);
            if (chatformatting != null) {
               style = chatformatting == ChatFormatting.RESET ? pDefaultStyle : style.applyLegacyFormat(chatformatting);
            }

            ++j;
         } else if (Character.isHighSurrogate(c0)) {
            if (j + 1 >= i) {
               if (!pSink.accept(j, style, 65533)) {
                  return false;
               }
               break;
            }

            char c2 = pText.charAt(j + 1);
            if (Character.isLowSurrogate(c2)) {
               if (!pSink.accept(j, style, Character.toCodePoint(c0, c2))) {
                  return false;
               }

               ++j;
            } else if (!pSink.accept(j, style, 65533)) {
               return false;
            }
         } else if (!feedChar(style, pSink, j, c0)) {
            return false;
         }
      }

      return true;
   }

   public static boolean iterateFormatted(FormattedText pText, Style pStyle, FormattedCharSink pSink) {
      return !pText.visit((p_14302_, p_14303_) -> {
         return iterateFormatted(p_14303_, 0, p_14302_, pSink) ? Optional.empty() : STOP_ITERATION;
      }, pStyle).isPresent();
   }

   public static String filterBrokenSurrogates(String pText) {
      StringBuilder stringbuilder = new StringBuilder();
      iterate(pText, Style.EMPTY, (p_14343_, p_14344_, p_14345_) -> {
         stringbuilder.appendCodePoint(p_14345_);
         return true;
      });
      return stringbuilder.toString();
   }

   public static String getPlainText(FormattedText pText) {
      StringBuilder stringbuilder = new StringBuilder();
      iterateFormatted(pText, Style.EMPTY, (p_14323_, p_14324_, p_14325_) -> {
         stringbuilder.appendCodePoint(p_14325_);
         return true;
      });
      return stringbuilder.toString();
   }
}