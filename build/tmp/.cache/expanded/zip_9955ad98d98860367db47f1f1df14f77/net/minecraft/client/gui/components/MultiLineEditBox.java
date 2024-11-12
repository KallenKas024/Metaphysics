package net.minecraft.client.gui.components;

import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiLineEditBox extends AbstractScrollWidget {
   private static final int CURSOR_INSERT_WIDTH = 1;
   private static final int CURSOR_INSERT_COLOR = -3092272;
   private static final String CURSOR_APPEND_CHARACTER = "_";
   private static final int TEXT_COLOR = -2039584;
   private static final int PLACEHOLDER_TEXT_COLOR = -857677600;
   private final Font font;
   private final Component placeholder;
   private final MultilineTextField textField;
   private int frame;

   public MultiLineEditBox(Font pFont, int pX, int pY, int pWidth, int pHeight, Component pPlaceholder, Component pMessage) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.font = pFont;
      this.placeholder = pPlaceholder;
      this.textField = new MultilineTextField(pFont, pWidth - this.totalInnerPadding());
      this.textField.setCursorListener(this::scrollToCursor);
   }

   public void setCharacterLimit(int pCharacterLimit) {
      this.textField.setCharacterLimit(pCharacterLimit);
   }

   public void setValueListener(Consumer<String> pValueListener) {
      this.textField.setValueListener(pValueListener);
   }

   public void setValue(String pFullText) {
      this.textField.setValue(pFullText);
   }

   public String getValue() {
      return this.textField.value();
   }

   public void tick() {
      ++this.frame;
   }

   public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
      pNarrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.editBox", this.getMessage(), this.getValue()));
   }

   /**
    * Called when a mouse button is clicked within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was clicked.
    */
   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
         return true;
      } else if (this.withinContentAreaPoint(pMouseX, pMouseY) && pButton == 0) {
         this.textField.setSelecting(Screen.hasShiftDown());
         this.seekCursorScreen(pMouseX, pMouseY);
         return true;
      } else {
         return false;
      }
   }

   /**
    * Called when the mouse is dragged within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that is being dragged.
    * @param pDragX the X distance of the drag.
    * @param pDragY the Y distance of the drag.
    */
   public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
         return true;
      } else if (this.withinContentAreaPoint(pMouseX, pMouseY) && pButton == 0) {
         this.textField.setSelecting(true);
         this.seekCursorScreen(pMouseX, pMouseY);
         this.textField.setSelecting(Screen.hasShiftDown());
         return true;
      } else {
         return false;
      }
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      return this.textField.keyPressed(pKeyCode);
   }

   /**
    * Called when a character is typed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pCodePoint the code point of the typed character.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean charTyped(char pCodePoint, int pModifiers) {
      if (this.visible && this.isFocused() && SharedConstants.isAllowedChatCharacter(pCodePoint)) {
         this.textField.insertText(Character.toString(pCodePoint));
         return true;
      } else {
         return false;
      }
   }

   protected void renderContents(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      String s = this.textField.value();
      if (s.isEmpty() && !this.isFocused()) {
         pGuiGraphics.drawWordWrap(this.font, this.placeholder, this.getX() + this.innerPadding(), this.getY() + this.innerPadding(), this.width - this.totalInnerPadding(), -857677600);
      } else {
         int i = this.textField.cursor();
         boolean flag = this.isFocused() && this.frame / 6 % 2 == 0;
         boolean flag1 = i < s.length();
         int j = 0;
         int k = 0;
         int l = this.getY() + this.innerPadding();

         for(MultilineTextField.StringView multilinetextfield$stringview : this.textField.iterateLines()) {
            boolean flag2 = this.withinContentAreaTopBottom(l, l + 9);
            if (flag && flag1 && i >= multilinetextfield$stringview.beginIndex() && i <= multilinetextfield$stringview.endIndex()) {
               if (flag2) {
                  j = pGuiGraphics.drawString(this.font, s.substring(multilinetextfield$stringview.beginIndex(), i), this.getX() + this.innerPadding(), l, -2039584) - 1;
                  pGuiGraphics.fill(j, l - 1, j + 1, l + 1 + 9, -3092272);
                  pGuiGraphics.drawString(this.font, s.substring(i, multilinetextfield$stringview.endIndex()), j, l, -2039584);
               }
            } else {
               if (flag2) {
                  j = pGuiGraphics.drawString(this.font, s.substring(multilinetextfield$stringview.beginIndex(), multilinetextfield$stringview.endIndex()), this.getX() + this.innerPadding(), l, -2039584) - 1;
               }

               k = l;
            }

            l += 9;
         }

         if (flag && !flag1 && this.withinContentAreaTopBottom(k, k + 9)) {
            pGuiGraphics.drawString(this.font, "_", j, k, -3092272);
         }

         if (this.textField.hasSelection()) {
            MultilineTextField.StringView multilinetextfield$stringview2 = this.textField.getSelected();
            int k1 = this.getX() + this.innerPadding();
            l = this.getY() + this.innerPadding();

            for(MultilineTextField.StringView multilinetextfield$stringview1 : this.textField.iterateLines()) {
               if (multilinetextfield$stringview2.beginIndex() > multilinetextfield$stringview1.endIndex()) {
                  l += 9;
               } else {
                  if (multilinetextfield$stringview1.beginIndex() > multilinetextfield$stringview2.endIndex()) {
                     break;
                  }

                  if (this.withinContentAreaTopBottom(l, l + 9)) {
                     int i1 = this.font.width(s.substring(multilinetextfield$stringview1.beginIndex(), Math.max(multilinetextfield$stringview2.beginIndex(), multilinetextfield$stringview1.beginIndex())));
                     int j1;
                     if (multilinetextfield$stringview2.endIndex() > multilinetextfield$stringview1.endIndex()) {
                        j1 = this.width - this.innerPadding();
                     } else {
                        j1 = this.font.width(s.substring(multilinetextfield$stringview1.beginIndex(), multilinetextfield$stringview2.endIndex()));
                     }

                     this.renderHighlight(pGuiGraphics, k1 + i1, l, k1 + j1, l + 9);
                  }

                  l += 9;
               }
            }
         }

      }
   }

   protected void renderDecorations(GuiGraphics pGuiGraphics) {
      super.renderDecorations(pGuiGraphics);
      if (this.textField.hasCharacterLimit()) {
         int i = this.textField.characterLimit();
         Component component = Component.translatable("gui.multiLineEditBox.character_limit", this.textField.value().length(), i);
         pGuiGraphics.drawString(this.font, component, this.getX() + this.width - this.font.width(component), this.getY() + this.height + 4, 10526880);
      }

   }

   public int getInnerHeight() {
      return 9 * this.textField.getLineCount();
   }

   protected boolean scrollbarVisible() {
      return (double)this.textField.getLineCount() > this.getDisplayableLineCount();
   }

   protected double scrollRate() {
      return 9.0D / 2.0D;
   }

   private void renderHighlight(GuiGraphics pGuiGraphics, int pMinX, int pMinY, int pMaxX, int pMaxY) {
      pGuiGraphics.fill(RenderType.guiTextHighlight(), pMinX, pMinY, pMaxX, pMaxY, -16776961);
   }

   private void scrollToCursor() {
      double d0 = this.scrollAmount();
      MultilineTextField.StringView multilinetextfield$stringview = this.textField.getLineView((int)(d0 / 9.0D));
      if (this.textField.cursor() <= multilinetextfield$stringview.beginIndex()) {
         d0 = (double)(this.textField.getLineAtCursor() * 9);
      } else {
         MultilineTextField.StringView multilinetextfield$stringview1 = this.textField.getLineView((int)((d0 + (double)this.height) / 9.0D) - 1);
         if (this.textField.cursor() > multilinetextfield$stringview1.endIndex()) {
            d0 = (double)(this.textField.getLineAtCursor() * 9 - this.height + 9 + this.totalInnerPadding());
         }
      }

      this.setScrollAmount(d0);
   }

   private double getDisplayableLineCount() {
      return (double)(this.height - this.totalInnerPadding()) / 9.0D;
   }

   private void seekCursorScreen(double pMouseX, double pMouseY) {
      double d0 = pMouseX - (double)this.getX() - (double)this.innerPadding();
      double d1 = pMouseY - (double)this.getY() - (double)this.innerPadding() + this.scrollAmount();
      this.textField.seekCursorToPoint(d0, d1);
   }
}