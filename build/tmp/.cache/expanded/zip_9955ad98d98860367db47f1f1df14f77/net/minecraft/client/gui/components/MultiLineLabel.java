package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MultiLineLabel {
   MultiLineLabel EMPTY = new MultiLineLabel() {
      public int renderCentered(GuiGraphics p_283287_, int p_94383_, int p_94384_) {
         return p_94384_;
      }

      public int renderCentered(GuiGraphics p_283384_, int p_94395_, int p_94396_, int p_94397_, int p_94398_) {
         return p_94396_;
      }

      public int renderLeftAligned(GuiGraphics p_283077_, int p_94379_, int p_94380_, int p_282157_, int p_282742_) {
         return p_94380_;
      }

      public int renderLeftAlignedNoShadow(GuiGraphics p_283645_, int p_94389_, int p_94390_, int p_94391_, int p_94392_) {
         return p_94390_;
      }

      public void renderBackgroundCentered(GuiGraphics p_283208_, int p_210825_, int p_210826_, int p_210827_, int p_210828_, int p_210829_) {
      }

      public int getLineCount() {
         return 0;
      }

      public int getWidth() {
         return 0;
      }
   };

   static MultiLineLabel create(Font pFont, FormattedText pFormattedText, int pMaxWidth) {
      return createFixed(pFont, pFont.split(pFormattedText, pMaxWidth).stream().map((p_94374_) -> {
         return new MultiLineLabel.TextWithWidth(p_94374_, pFont.width(p_94374_));
      }).collect(ImmutableList.toImmutableList()));
   }

   static MultiLineLabel create(Font pFont, FormattedText pFormattedText, int pMaxWidth, int pMaxLines) {
      return createFixed(pFont, pFont.split(pFormattedText, pMaxWidth).stream().limit((long)pMaxLines).map((p_94371_) -> {
         return new MultiLineLabel.TextWithWidth(p_94371_, pFont.width(p_94371_));
      }).collect(ImmutableList.toImmutableList()));
   }

   static MultiLineLabel create(Font pFont, Component... pComponents) {
      return createFixed(pFont, Arrays.stream(pComponents).map(Component::getVisualOrderText).map((p_94360_) -> {
         return new MultiLineLabel.TextWithWidth(p_94360_, pFont.width(p_94360_));
      }).collect(ImmutableList.toImmutableList()));
   }

   static MultiLineLabel create(Font pFont, List<Component> pComponents) {
      return createFixed(pFont, pComponents.stream().map(Component::getVisualOrderText).map((p_169035_) -> {
         return new MultiLineLabel.TextWithWidth(p_169035_, pFont.width(p_169035_));
      }).collect(ImmutableList.toImmutableList()));
   }

   static MultiLineLabel createFixed(final Font pFont, final List<MultiLineLabel.TextWithWidth> pTextList) {
      return pTextList.isEmpty() ? EMPTY : new MultiLineLabel() {
         private final int width = pTextList.stream().mapToInt((p_232527_) -> {
            return p_232527_.width;
         }).max().orElse(0);

         public int renderCentered(GuiGraphics p_283492_, int p_283184_, int p_282078_) {
            return this.renderCentered(p_283492_, p_283184_, p_282078_, 9, 16777215);
         }

         public int renderCentered(GuiGraphics p_281603_, int p_281267_, int p_281819_, int p_281545_, int p_282780_) {
            int i = p_281819_;

            for(MultiLineLabel.TextWithWidth multilinelabel$textwithwidth : pTextList) {
               p_281603_.drawString(pFont, multilinelabel$textwithwidth.text, p_281267_ - multilinelabel$textwithwidth.width / 2, i, p_282780_);
               i += p_281545_;
            }

            return i;
         }

         public int renderLeftAligned(GuiGraphics p_282318_, int p_283665_, int p_283416_, int p_281919_, int p_281686_) {
            int i = p_283416_;

            for(MultiLineLabel.TextWithWidth multilinelabel$textwithwidth : pTextList) {
               p_282318_.drawString(pFont, multilinelabel$textwithwidth.text, p_283665_, i, p_281686_);
               i += p_281919_;
            }

            return i;
         }

         public int renderLeftAlignedNoShadow(GuiGraphics p_281782_, int p_282841_, int p_283554_, int p_282768_, int p_283499_) {
            int i = p_283554_;

            for(MultiLineLabel.TextWithWidth multilinelabel$textwithwidth : pTextList) {
               p_281782_.drawString(pFont, multilinelabel$textwithwidth.text, p_282841_, i, p_283499_, false);
               i += p_282768_;
            }

            return i;
         }

         public void renderBackgroundCentered(GuiGraphics p_281633_, int p_210832_, int p_210833_, int p_210834_, int p_210835_, int p_210836_) {
            int i = pTextList.stream().mapToInt((p_232524_) -> {
               return p_232524_.width;
            }).max().orElse(0);
            if (i > 0) {
               p_281633_.fill(p_210832_ - i / 2 - p_210835_, p_210833_ - p_210835_, p_210832_ + i / 2 + p_210835_, p_210833_ + pTextList.size() * p_210834_ + p_210835_, p_210836_);
            }

         }

         public int getLineCount() {
            return pTextList.size();
         }

         public int getWidth() {
            return this.width;
         }
      };
   }

   int renderCentered(GuiGraphics pGuiGraphics, int pWidth, int pTextWithWidthList);

   int renderCentered(GuiGraphics pGuiGraphics, int pWidth, int pX, int pY, int pColor);

   int renderLeftAligned(GuiGraphics pGuiGraphics, int pX, int pY, int pLineHeight, int pColor);

   int renderLeftAlignedNoShadow(GuiGraphics pGuiGraphics, int pX, int pY, int pLineHeight, int pColor);

   void renderBackgroundCentered(GuiGraphics pGuiGraphics, int p_210818_, int p_210819_, int p_210820_, int p_210821_, int pColor);

   int getLineCount();

   int getWidth();

   @OnlyIn(Dist.CLIENT)
   public static class TextWithWidth {
      final FormattedCharSequence text;
      final int width;

      TextWithWidth(FormattedCharSequence pText, int pWidth) {
         this.text = pText;
         this.width = pWidth;
      }
   }
}