package com.mojang.realmsclient.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextRenderingUtils {
   private TextRenderingUtils() {
   }

   @VisibleForTesting
   protected static List<String> lineBreak(String pText) {
      return Arrays.asList(pText.split("\\n"));
   }

   public static List<TextRenderingUtils.Line> decompose(String pText, TextRenderingUtils.LineSegment... pSegments) {
      return decompose(pText, Arrays.asList(pSegments));
   }

   private static List<TextRenderingUtils.Line> decompose(String pText, List<TextRenderingUtils.LineSegment> pSegments) {
      List<String> list = lineBreak(pText);
      return insertLinks(list, pSegments);
   }

   private static List<TextRenderingUtils.Line> insertLinks(List<String> pLines, List<TextRenderingUtils.LineSegment> pSegments) {
      int i = 0;
      List<TextRenderingUtils.Line> list = Lists.newArrayList();

      for(String s : pLines) {
         List<TextRenderingUtils.LineSegment> list1 = Lists.newArrayList();

         for(String s1 : split(s, "%link")) {
            if ("%link".equals(s1)) {
               list1.add(pSegments.get(i++));
            } else {
               list1.add(TextRenderingUtils.LineSegment.text(s1));
            }
         }

         list.add(new TextRenderingUtils.Line(list1));
      }

      return list;
   }

   public static List<String> split(String pToSplit, String pDelimiter) {
      if (pDelimiter.isEmpty()) {
         throw new IllegalArgumentException("Delimiter cannot be the empty string");
      } else {
         List<String> list = Lists.newArrayList();

         int i;
         int j;
         for(i = 0; (j = pToSplit.indexOf(pDelimiter, i)) != -1; i = j + pDelimiter.length()) {
            if (j > i) {
               list.add(pToSplit.substring(i, j));
            }

            list.add(pDelimiter);
         }

         if (i < pToSplit.length()) {
            list.add(pToSplit.substring(i));
         }

         return list;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Line {
      public final List<TextRenderingUtils.LineSegment> segments;

      Line(TextRenderingUtils.LineSegment... pSegments) {
         this(Arrays.asList(pSegments));
      }

      Line(List<TextRenderingUtils.LineSegment> pSegments) {
         this.segments = pSegments;
      }

      public String toString() {
         return "Line{segments=" + this.segments + "}";
      }

      public boolean equals(Object pOther) {
         if (this == pOther) {
            return true;
         } else if (pOther != null && this.getClass() == pOther.getClass()) {
            TextRenderingUtils.Line textrenderingutils$line = (TextRenderingUtils.Line)pOther;
            return Objects.equals(this.segments, textrenderingutils$line.segments);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.segments);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class LineSegment {
      private final String fullText;
      @Nullable
      private final String linkTitle;
      @Nullable
      private final String linkUrl;

      private LineSegment(String pFullText) {
         this.fullText = pFullText;
         this.linkTitle = null;
         this.linkUrl = null;
      }

      private LineSegment(String pFullText, @Nullable String pLinkTitle, @Nullable String pLinkUrl) {
         this.fullText = pFullText;
         this.linkTitle = pLinkTitle;
         this.linkUrl = pLinkUrl;
      }

      public boolean equals(Object pOther) {
         if (this == pOther) {
            return true;
         } else if (pOther != null && this.getClass() == pOther.getClass()) {
            TextRenderingUtils.LineSegment textrenderingutils$linesegment = (TextRenderingUtils.LineSegment)pOther;
            return Objects.equals(this.fullText, textrenderingutils$linesegment.fullText) && Objects.equals(this.linkTitle, textrenderingutils$linesegment.linkTitle) && Objects.equals(this.linkUrl, textrenderingutils$linesegment.linkUrl);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.fullText, this.linkTitle, this.linkUrl);
      }

      public String toString() {
         return "Segment{fullText='" + this.fullText + "', linkTitle='" + this.linkTitle + "', linkUrl='" + this.linkUrl + "'}";
      }

      public String renderedText() {
         return this.isLink() ? this.linkTitle : this.fullText;
      }

      public boolean isLink() {
         return this.linkTitle != null;
      }

      public String getLinkUrl() {
         if (!this.isLink()) {
            throw new IllegalStateException("Not a link: " + this);
         } else {
            return this.linkUrl;
         }
      }

      public static TextRenderingUtils.LineSegment link(String pLinkTitle, String pLinkUrl) {
         return new TextRenderingUtils.LineSegment((String)null, pLinkTitle, pLinkUrl);
      }

      @VisibleForTesting
      protected static TextRenderingUtils.LineSegment text(String pFullText) {
         return new TextRenderingUtils.LineSegment(pFullText);
      }
   }
}