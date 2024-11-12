package net.minecraft.client;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

@OnlyIn(Dist.CLIENT)
public class StringSplitter {
   final StringSplitter.WidthProvider widthProvider;

   public StringSplitter(StringSplitter.WidthProvider pWidthProvider) {
      this.widthProvider = pWidthProvider;
   }

   public float stringWidth(@Nullable String pContent) {
      if (pContent == null) {
         return 0.0F;
      } else {
         MutableFloat mutablefloat = new MutableFloat();
         StringDecomposer.iterateFormatted(pContent, Style.EMPTY, (p_92429_, p_92430_, p_92431_) -> {
            mutablefloat.add(this.widthProvider.getWidth(p_92431_, p_92430_));
            return true;
         });
         return mutablefloat.floatValue();
      }
   }

   public float stringWidth(FormattedText pContent) {
      MutableFloat mutablefloat = new MutableFloat();
      StringDecomposer.iterateFormatted(pContent, Style.EMPTY, (p_92420_, p_92421_, p_92422_) -> {
         mutablefloat.add(this.widthProvider.getWidth(p_92422_, p_92421_));
         return true;
      });
      return mutablefloat.floatValue();
   }

   public float stringWidth(FormattedCharSequence pContent) {
      MutableFloat mutablefloat = new MutableFloat();
      pContent.accept((p_92400_, p_92401_, p_92402_) -> {
         mutablefloat.add(this.widthProvider.getWidth(p_92402_, p_92401_));
         return true;
      });
      return mutablefloat.floatValue();
   }

   public int plainIndexAtWidth(String pContent, int pMaxWidth, Style pStyle) {
      StringSplitter.WidthLimitedCharSink stringsplitter$widthlimitedcharsink = new StringSplitter.WidthLimitedCharSink((float)pMaxWidth);
      StringDecomposer.iterate(pContent, pStyle, stringsplitter$widthlimitedcharsink);
      return stringsplitter$widthlimitedcharsink.getPosition();
   }

   public String plainHeadByWidth(String pContent, int pMaxWidth, Style pStyle) {
      return pContent.substring(0, this.plainIndexAtWidth(pContent, pMaxWidth, pStyle));
   }

   public String plainTailByWidth(String pContent, int pMaxWidth, Style pStyle) {
      MutableFloat mutablefloat = new MutableFloat();
      MutableInt mutableint = new MutableInt(pContent.length());
      StringDecomposer.iterateBackwards(pContent, pStyle, (p_92407_, p_92408_, p_92409_) -> {
         float f = mutablefloat.addAndGet(this.widthProvider.getWidth(p_92409_, p_92408_));
         if (f > (float)pMaxWidth) {
            return false;
         } else {
            mutableint.setValue(p_92407_);
            return true;
         }
      });
      return pContent.substring(mutableint.intValue());
   }

   public int formattedIndexByWidth(String pContent, int pMaxWidth, Style pStyle) {
      StringSplitter.WidthLimitedCharSink stringsplitter$widthlimitedcharsink = new StringSplitter.WidthLimitedCharSink((float)pMaxWidth);
      StringDecomposer.iterateFormatted(pContent, pStyle, stringsplitter$widthlimitedcharsink);
      return stringsplitter$widthlimitedcharsink.getPosition();
   }

   @Nullable
   public Style componentStyleAtWidth(FormattedText pContent, int pMaxWidth) {
      StringSplitter.WidthLimitedCharSink stringsplitter$widthlimitedcharsink = new StringSplitter.WidthLimitedCharSink((float)pMaxWidth);
      return pContent.visit((p_92343_, p_92344_) -> {
         return StringDecomposer.iterateFormatted(p_92344_, p_92343_, stringsplitter$widthlimitedcharsink) ? Optional.empty() : Optional.of(p_92343_);
      }, Style.EMPTY).orElse((Style)null);
   }

   @Nullable
   public Style componentStyleAtWidth(FormattedCharSequence pContent, int pMaxWidth) {
      StringSplitter.WidthLimitedCharSink stringsplitter$widthlimitedcharsink = new StringSplitter.WidthLimitedCharSink((float)pMaxWidth);
      MutableObject<Style> mutableobject = new MutableObject<>();
      pContent.accept((p_92348_, p_92349_, p_92350_) -> {
         if (!stringsplitter$widthlimitedcharsink.accept(p_92348_, p_92349_, p_92350_)) {
            mutableobject.setValue(p_92349_);
            return false;
         } else {
            return true;
         }
      });
      return mutableobject.getValue();
   }

   public String formattedHeadByWidth(String pContent, int pMaxWidth, Style pStyle) {
      return pContent.substring(0, this.formattedIndexByWidth(pContent, pMaxWidth, pStyle));
   }

   public FormattedText headByWidth(FormattedText pContent, int pMaxWidth, Style pStyle) {
      final StringSplitter.WidthLimitedCharSink stringsplitter$widthlimitedcharsink = new StringSplitter.WidthLimitedCharSink((float)pMaxWidth);
      return pContent.visit(new FormattedText.StyledContentConsumer<FormattedText>() {
         private final ComponentCollector collector = new ComponentCollector();

         public Optional<FormattedText> accept(Style p_92443_, String p_92444_) {
            stringsplitter$widthlimitedcharsink.resetPosition();
            if (!StringDecomposer.iterateFormatted(p_92444_, p_92443_, stringsplitter$widthlimitedcharsink)) {
               String s = p_92444_.substring(0, stringsplitter$widthlimitedcharsink.getPosition());
               if (!s.isEmpty()) {
                  this.collector.append(FormattedText.of(s, p_92443_));
               }

               return Optional.of(this.collector.getResultOrEmpty());
            } else {
               if (!p_92444_.isEmpty()) {
                  this.collector.append(FormattedText.of(p_92444_, p_92443_));
               }

               return Optional.empty();
            }
         }
      }, pStyle).orElse(pContent);
   }

   public int findLineBreak(String pContent, int pMaxWidth, Style pStyle) {
      StringSplitter.LineBreakFinder stringsplitter$linebreakfinder = new StringSplitter.LineBreakFinder((float)pMaxWidth);
      StringDecomposer.iterateFormatted(pContent, pStyle, stringsplitter$linebreakfinder);
      return stringsplitter$linebreakfinder.getSplitPosition();
   }

   public static int getWordPosition(String pContent, int pSkipCount, int pCursorPoint, boolean pIncludeWhitespace) {
      int i = pCursorPoint;
      boolean flag = pSkipCount < 0;
      int j = Math.abs(pSkipCount);

      for(int k = 0; k < j; ++k) {
         if (flag) {
            while(pIncludeWhitespace && i > 0 && (pContent.charAt(i - 1) == ' ' || pContent.charAt(i - 1) == '\n')) {
               --i;
            }

            while(i > 0 && pContent.charAt(i - 1) != ' ' && pContent.charAt(i - 1) != '\n') {
               --i;
            }
         } else {
            int l = pContent.length();
            int i1 = pContent.indexOf(32, i);
            int j1 = pContent.indexOf(10, i);
            if (i1 == -1 && j1 == -1) {
               i = -1;
            } else if (i1 != -1 && j1 != -1) {
               i = Math.min(i1, j1);
            } else if (i1 != -1) {
               i = i1;
            } else {
               i = j1;
            }

            if (i == -1) {
               i = l;
            } else {
               while(pIncludeWhitespace && i < l && (pContent.charAt(i) == ' ' || pContent.charAt(i) == '\n')) {
                  ++i;
               }
            }
         }
      }

      return i;
   }

   public void splitLines(String pContent, int pMaxWidth, Style pStyle, boolean pWithNewLines, StringSplitter.LinePosConsumer pLinePos) {
      int i = 0;
      int j = pContent.length();

      StringSplitter.LineBreakFinder stringsplitter$linebreakfinder;
      for(Style style = pStyle; i < j; style = stringsplitter$linebreakfinder.getSplitStyle()) {
         stringsplitter$linebreakfinder = new StringSplitter.LineBreakFinder((float)pMaxWidth);
         boolean flag = StringDecomposer.iterateFormatted(pContent, i, style, pStyle, stringsplitter$linebreakfinder);
         if (flag) {
            pLinePos.accept(style, i, j);
            break;
         }

         int k = stringsplitter$linebreakfinder.getSplitPosition();
         char c0 = pContent.charAt(k);
         int l = c0 != '\n' && c0 != ' ' ? k : k + 1;
         pLinePos.accept(style, i, pWithNewLines ? l : k);
         i = l;
      }

   }

   public List<FormattedText> splitLines(String pContent, int pMaxWidth, Style pStyle) {
      List<FormattedText> list = Lists.newArrayList();
      this.splitLines(pContent, pMaxWidth, pStyle, false, (p_92373_, p_92374_, p_92375_) -> {
         list.add(FormattedText.of(pContent.substring(p_92374_, p_92375_), p_92373_));
      });
      return list;
   }

   public List<FormattedText> splitLines(FormattedText pContent, int pMaxWidth, Style pStyle) {
      List<FormattedText> list = Lists.newArrayList();
      this.splitLines(pContent, pMaxWidth, pStyle, (p_92378_, p_92379_) -> {
         list.add(p_92378_);
      });
      return list;
   }

   public List<FormattedText> splitLines(FormattedText pContent, int pMaxWidth, Style pStyle, FormattedText pPrefix) {
      List<FormattedText> list = Lists.newArrayList();
      this.splitLines(pContent, pMaxWidth, pStyle, (p_168619_, p_168620_) -> {
         list.add(p_168620_ ? FormattedText.composite(pPrefix, p_168619_) : p_168619_);
      });
      return list;
   }

   public void splitLines(FormattedText pContent, int pMaxWidth, Style pStyle, BiConsumer<FormattedText, Boolean> pSplitifier) {
      List<StringSplitter.LineComponent> list = Lists.newArrayList();
      pContent.visit((p_92382_, p_92383_) -> {
         if (!p_92383_.isEmpty()) {
            list.add(new StringSplitter.LineComponent(p_92383_, p_92382_));
         }

         return Optional.empty();
      }, pStyle);
      StringSplitter.FlatComponents stringsplitter$flatcomponents = new StringSplitter.FlatComponents(list);
      boolean flag = true;
      boolean flag1 = false;
      boolean flag2 = false;

      while(flag) {
         flag = false;
         StringSplitter.LineBreakFinder stringsplitter$linebreakfinder = new StringSplitter.LineBreakFinder((float)pMaxWidth);

         for(StringSplitter.LineComponent stringsplitter$linecomponent : stringsplitter$flatcomponents.parts) {
            boolean flag3 = StringDecomposer.iterateFormatted(stringsplitter$linecomponent.contents, 0, stringsplitter$linecomponent.style, pStyle, stringsplitter$linebreakfinder);
            if (!flag3) {
               int i = stringsplitter$linebreakfinder.getSplitPosition();
               Style style = stringsplitter$linebreakfinder.getSplitStyle();
               char c0 = stringsplitter$flatcomponents.charAt(i);
               boolean flag4 = c0 == '\n';
               boolean flag5 = flag4 || c0 == ' ';
               flag1 = flag4;
               FormattedText formattedtext = stringsplitter$flatcomponents.splitAt(i, flag5 ? 1 : 0, style);
               pSplitifier.accept(formattedtext, flag2);
               flag2 = !flag4;
               flag = true;
               break;
            }

            stringsplitter$linebreakfinder.addToOffset(stringsplitter$linecomponent.contents.length());
         }
      }

      FormattedText formattedtext1 = stringsplitter$flatcomponents.getRemainder();
      if (formattedtext1 != null) {
         pSplitifier.accept(formattedtext1, flag2);
      } else if (flag1) {
         pSplitifier.accept(FormattedText.EMPTY, false);
      }

   }

   @OnlyIn(Dist.CLIENT)
   static class FlatComponents {
      final List<StringSplitter.LineComponent> parts;
      private String flatParts;

      public FlatComponents(List<StringSplitter.LineComponent> pParts) {
         this.parts = pParts;
         this.flatParts = pParts.stream().map((p_92459_) -> {
            return p_92459_.contents;
         }).collect(Collectors.joining());
      }

      public char charAt(int pCodePoint) {
         return this.flatParts.charAt(pCodePoint);
      }

      public FormattedText splitAt(int pBegin, int pEnd, Style pStyle) {
         ComponentCollector componentcollector = new ComponentCollector();
         ListIterator<StringSplitter.LineComponent> listiterator = this.parts.listIterator();
         int i = pBegin;
         boolean flag = false;

         while(listiterator.hasNext()) {
            StringSplitter.LineComponent stringsplitter$linecomponent = listiterator.next();
            String s = stringsplitter$linecomponent.contents;
            int j = s.length();
            if (!flag) {
               if (i > j) {
                  componentcollector.append(stringsplitter$linecomponent);
                  listiterator.remove();
                  i -= j;
               } else {
                  String s1 = s.substring(0, i);
                  if (!s1.isEmpty()) {
                     componentcollector.append(FormattedText.of(s1, stringsplitter$linecomponent.style));
                  }

                  i += pEnd;
                  flag = true;
               }
            }

            if (flag) {
               if (i <= j) {
                  String s2 = s.substring(i);
                  if (s2.isEmpty()) {
                     listiterator.remove();
                  } else {
                     listiterator.set(new StringSplitter.LineComponent(s2, pStyle));
                  }
                  break;
               }

               listiterator.remove();
               i -= j;
            }
         }

         this.flatParts = this.flatParts.substring(pBegin + pEnd);
         return componentcollector.getResultOrEmpty();
      }

      @Nullable
      public FormattedText getRemainder() {
         ComponentCollector componentcollector = new ComponentCollector();
         this.parts.forEach(componentcollector::append);
         this.parts.clear();
         return componentcollector.getResult();
      }
   }

   @OnlyIn(Dist.CLIENT)
   class LineBreakFinder implements FormattedCharSink {
      private final float maxWidth;
      private int lineBreak = -1;
      private Style lineBreakStyle = Style.EMPTY;
      private boolean hadNonZeroWidthChar;
      private float width;
      private int lastSpace = -1;
      private Style lastSpaceStyle = Style.EMPTY;
      private int nextChar;
      private int offset;

      public LineBreakFinder(float pMaxWidth) {
         this.maxWidth = Math.max(pMaxWidth, 1.0F);
      }

      /**
       * Accepts a single code point from a {@link net.minecraft.util.FormattedCharSequence}.
       * @return {@code true} to accept more characters, {@code false} to stop traversing the sequence.
       * @param pPositionInCurrentSequence Contains the relative position of the character in the current sub-sequence.
       * If multiple formatted char sequences have been combined, this value will reset to {@code 0} after each sequence
       * has been fully consumed.
       */
      public boolean accept(int pPositionInCurrentSequence, Style pStyle, int pCodePoint) {
         int i = pPositionInCurrentSequence + this.offset;
         switch (pCodePoint) {
            case 10:
               return this.finishIteration(i, pStyle);
            case 32:
               this.lastSpace = i;
               this.lastSpaceStyle = pStyle;
            default:
               float f = StringSplitter.this.widthProvider.getWidth(pCodePoint, pStyle);
               this.width += f;
               if (this.hadNonZeroWidthChar && this.width > this.maxWidth) {
                  return this.lastSpace != -1 ? this.finishIteration(this.lastSpace, this.lastSpaceStyle) : this.finishIteration(i, pStyle);
               } else {
                  this.hadNonZeroWidthChar |= f != 0.0F;
                  this.nextChar = i + Character.charCount(pCodePoint);
                  return true;
               }
         }
      }

      private boolean finishIteration(int pLineBreak, Style pLineBreakStyle) {
         this.lineBreak = pLineBreak;
         this.lineBreakStyle = pLineBreakStyle;
         return false;
      }

      private boolean lineBreakFound() {
         return this.lineBreak != -1;
      }

      public int getSplitPosition() {
         return this.lineBreakFound() ? this.lineBreak : this.nextChar;
      }

      public Style getSplitStyle() {
         return this.lineBreakStyle;
      }

      public void addToOffset(int pOffset) {
         this.offset += pOffset;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class LineComponent implements FormattedText {
      final String contents;
      final Style style;

      public LineComponent(String pContents, Style pStyle) {
         this.contents = pContents;
         this.style = pStyle;
      }

      public <T> Optional<T> visit(FormattedText.ContentConsumer<T> pAcceptor) {
         return pAcceptor.accept(this.contents);
      }

      public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pAcceptor, Style pStyle) {
         return pAcceptor.accept(this.style.applyTo(pStyle), this.contents);
      }
   }

   @FunctionalInterface
   @OnlyIn(Dist.CLIENT)
   public interface LinePosConsumer {
      void accept(Style pStyle, int pCurrentPos, int pContentWidth);
   }

   @OnlyIn(Dist.CLIENT)
   class WidthLimitedCharSink implements FormattedCharSink {
      private float maxWidth;
      private int position;

      public WidthLimitedCharSink(float pMaxWidth) {
         this.maxWidth = pMaxWidth;
      }

      /**
       * Accepts a single code point from a {@link net.minecraft.util.FormattedCharSequence}.
       * @return {@code true} to accept more characters, {@code false} to stop traversing the sequence.
       * @param pPositionInCurrentSequence Contains the relative position of the character in the current sub-sequence.
       * If multiple formatted char sequences have been combined, this value will reset to {@code 0} after each sequence
       * has been fully consumed.
       */
      public boolean accept(int pPositionInCurrentSequence, Style pStyle, int pCodePoint) {
         this.maxWidth -= StringSplitter.this.widthProvider.getWidth(pCodePoint, pStyle);
         if (this.maxWidth >= 0.0F) {
            this.position = pPositionInCurrentSequence + Character.charCount(pCodePoint);
            return true;
         } else {
            return false;
         }
      }

      public int getPosition() {
         return this.position;
      }

      public void resetPosition() {
         this.position = 0;
      }
   }

   @FunctionalInterface
   @OnlyIn(Dist.CLIENT)
   public interface WidthProvider {
      float getWidth(int pCodePoint, Style pStyle);
   }
}