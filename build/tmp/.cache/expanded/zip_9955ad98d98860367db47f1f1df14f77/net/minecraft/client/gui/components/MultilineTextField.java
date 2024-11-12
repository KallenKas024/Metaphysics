package net.minecraft.client.gui.components;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultilineTextField {
   public static final int NO_CHARACTER_LIMIT = Integer.MAX_VALUE;
   private static final int LINE_SEEK_PIXEL_BIAS = 2;
   private final Font font;
   private final List<MultilineTextField.StringView> displayLines = Lists.newArrayList();
   private String value;
   private int cursor;
   private int selectCursor;
   private boolean selecting;
   private int characterLimit = Integer.MAX_VALUE;
   private final int width;
   private Consumer<String> valueListener = (p_239235_) -> {
   };
   private Runnable cursorListener = () -> {
   };

   public MultilineTextField(Font pFont, int pWidth) {
      this.font = pFont;
      this.width = pWidth;
      this.setValue("");
   }

   public int characterLimit() {
      return this.characterLimit;
   }

   public void setCharacterLimit(int pCharacterLimit) {
      if (pCharacterLimit < 0) {
         throw new IllegalArgumentException("Character limit cannot be negative");
      } else {
         this.characterLimit = pCharacterLimit;
      }
   }

   public boolean hasCharacterLimit() {
      return this.characterLimit != Integer.MAX_VALUE;
   }

   public void setValueListener(Consumer<String> pValueListener) {
      this.valueListener = pValueListener;
   }

   public void setCursorListener(Runnable pCursorListener) {
      this.cursorListener = pCursorListener;
   }

   public void setValue(String pFullText) {
      this.value = this.truncateFullText(pFullText);
      this.cursor = this.value.length();
      this.selectCursor = this.cursor;
      this.onValueChange();
   }

   public String value() {
      return this.value;
   }

   public void insertText(String pText) {
      if (!pText.isEmpty() || this.hasSelection()) {
         String s = this.truncateInsertionText(SharedConstants.filterText(pText, true));
         MultilineTextField.StringView multilinetextfield$stringview = this.getSelected();
         this.value = (new StringBuilder(this.value)).replace(multilinetextfield$stringview.beginIndex, multilinetextfield$stringview.endIndex, s).toString();
         this.cursor = multilinetextfield$stringview.beginIndex + s.length();
         this.selectCursor = this.cursor;
         this.onValueChange();
      }
   }

   public void deleteText(int pLength) {
      if (!this.hasSelection()) {
         this.selectCursor = Mth.clamp(this.cursor + pLength, 0, this.value.length());
      }

      this.insertText("");
   }

   public int cursor() {
      return this.cursor;
   }

   public void setSelecting(boolean pSelecting) {
      this.selecting = pSelecting;
   }

   public MultilineTextField.StringView getSelected() {
      return new MultilineTextField.StringView(Math.min(this.selectCursor, this.cursor), Math.max(this.selectCursor, this.cursor));
   }

   public int getLineCount() {
      return this.displayLines.size();
   }

   public int getLineAtCursor() {
      for(int i = 0; i < this.displayLines.size(); ++i) {
         MultilineTextField.StringView multilinetextfield$stringview = this.displayLines.get(i);
         if (this.cursor >= multilinetextfield$stringview.beginIndex && this.cursor <= multilinetextfield$stringview.endIndex) {
            return i;
         }
      }

      return -1;
   }

   public MultilineTextField.StringView getLineView(int pLineNumber) {
      return this.displayLines.get(Mth.clamp(pLineNumber, 0, this.displayLines.size() - 1));
   }

   public void seekCursor(Whence pWhence, int pPosition) {
      switch (pWhence) {
         case ABSOLUTE:
            this.cursor = pPosition;
            break;
         case RELATIVE:
            this.cursor += pPosition;
            break;
         case END:
            this.cursor = this.value.length() + pPosition;
      }

      this.cursor = Mth.clamp(this.cursor, 0, this.value.length());
      this.cursorListener.run();
      if (!this.selecting) {
         this.selectCursor = this.cursor;
      }

   }

   public void seekCursorLine(int pOffset) {
      if (pOffset != 0) {
         int i = this.font.width(this.value.substring(this.getCursorLineView().beginIndex, this.cursor)) + 2;
         MultilineTextField.StringView multilinetextfield$stringview = this.getCursorLineView(pOffset);
         int j = this.font.plainSubstrByWidth(this.value.substring(multilinetextfield$stringview.beginIndex, multilinetextfield$stringview.endIndex), i).length();
         this.seekCursor(Whence.ABSOLUTE, multilinetextfield$stringview.beginIndex + j);
      }
   }

   public void seekCursorToPoint(double pX, double pY) {
      int i = Mth.floor(pX);
      int j = Mth.floor(pY / 9.0D);
      MultilineTextField.StringView multilinetextfield$stringview = this.displayLines.get(Mth.clamp(j, 0, this.displayLines.size() - 1));
      int k = this.font.plainSubstrByWidth(this.value.substring(multilinetextfield$stringview.beginIndex, multilinetextfield$stringview.endIndex), i).length();
      this.seekCursor(Whence.ABSOLUTE, multilinetextfield$stringview.beginIndex + k);
   }

   public boolean keyPressed(int pKeyCode) {
      this.selecting = Screen.hasShiftDown();
      if (Screen.isSelectAll(pKeyCode)) {
         this.cursor = this.value.length();
         this.selectCursor = 0;
         return true;
      } else if (Screen.isCopy(pKeyCode)) {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
         return true;
      } else if (Screen.isPaste(pKeyCode)) {
         this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
         return true;
      } else if (Screen.isCut(pKeyCode)) {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
         this.insertText("");
         return true;
      } else {
         switch (pKeyCode) {
            case 257:
            case 335:
               this.insertText("\n");
               return true;
            case 259:
               if (Screen.hasControlDown()) {
                  MultilineTextField.StringView multilinetextfield$stringview3 = this.getPreviousWord();
                  this.deleteText(multilinetextfield$stringview3.beginIndex - this.cursor);
               } else {
                  this.deleteText(-1);
               }

               return true;
            case 261:
               if (Screen.hasControlDown()) {
                  MultilineTextField.StringView multilinetextfield$stringview2 = this.getNextWord();
                  this.deleteText(multilinetextfield$stringview2.beginIndex - this.cursor);
               } else {
                  this.deleteText(1);
               }

               return true;
            case 262:
               if (Screen.hasControlDown()) {
                  MultilineTextField.StringView multilinetextfield$stringview1 = this.getNextWord();
                  this.seekCursor(Whence.ABSOLUTE, multilinetextfield$stringview1.beginIndex);
               } else {
                  this.seekCursor(Whence.RELATIVE, 1);
               }

               return true;
            case 263:
               if (Screen.hasControlDown()) {
                  MultilineTextField.StringView multilinetextfield$stringview = this.getPreviousWord();
                  this.seekCursor(Whence.ABSOLUTE, multilinetextfield$stringview.beginIndex);
               } else {
                  this.seekCursor(Whence.RELATIVE, -1);
               }

               return true;
            case 264:
               if (!Screen.hasControlDown()) {
                  this.seekCursorLine(1);
               }

               return true;
            case 265:
               if (!Screen.hasControlDown()) {
                  this.seekCursorLine(-1);
               }

               return true;
            case 266:
               this.seekCursor(Whence.ABSOLUTE, 0);
               return true;
            case 267:
               this.seekCursor(Whence.END, 0);
               return true;
            case 268:
               if (Screen.hasControlDown()) {
                  this.seekCursor(Whence.ABSOLUTE, 0);
               } else {
                  this.seekCursor(Whence.ABSOLUTE, this.getCursorLineView().beginIndex);
               }

               return true;
            case 269:
               if (Screen.hasControlDown()) {
                  this.seekCursor(Whence.END, 0);
               } else {
                  this.seekCursor(Whence.ABSOLUTE, this.getCursorLineView().endIndex);
               }

               return true;
            default:
               return false;
         }
      }
   }

   public Iterable<MultilineTextField.StringView> iterateLines() {
      return this.displayLines;
   }

   public boolean hasSelection() {
      return this.selectCursor != this.cursor;
   }

   @VisibleForTesting
   public String getSelectedText() {
      MultilineTextField.StringView multilinetextfield$stringview = this.getSelected();
      return this.value.substring(multilinetextfield$stringview.beginIndex, multilinetextfield$stringview.endIndex);
   }

   private MultilineTextField.StringView getCursorLineView() {
      return this.getCursorLineView(0);
   }

   private MultilineTextField.StringView getCursorLineView(int pOffset) {
      int i = this.getLineAtCursor();
      if (i < 0) {
         throw new IllegalStateException("Cursor is not within text (cursor = " + this.cursor + ", length = " + this.value.length() + ")");
      } else {
         return this.displayLines.get(Mth.clamp(i + pOffset, 0, this.displayLines.size() - 1));
      }
   }

   @VisibleForTesting
   public MultilineTextField.StringView getPreviousWord() {
      if (this.value.isEmpty()) {
         return MultilineTextField.StringView.EMPTY;
      } else {
         int i;
         for(i = Mth.clamp(this.cursor, 0, this.value.length() - 1); i > 0 && Character.isWhitespace(this.value.charAt(i - 1)); --i) {
         }

         while(i > 0 && !Character.isWhitespace(this.value.charAt(i - 1))) {
            --i;
         }

         return new MultilineTextField.StringView(i, this.getWordEndPosition(i));
      }
   }

   @VisibleForTesting
   public MultilineTextField.StringView getNextWord() {
      if (this.value.isEmpty()) {
         return MultilineTextField.StringView.EMPTY;
      } else {
         int i;
         for(i = Mth.clamp(this.cursor, 0, this.value.length() - 1); i < this.value.length() && !Character.isWhitespace(this.value.charAt(i)); ++i) {
         }

         while(i < this.value.length() && Character.isWhitespace(this.value.charAt(i))) {
            ++i;
         }

         return new MultilineTextField.StringView(i, this.getWordEndPosition(i));
      }
   }

   private int getWordEndPosition(int pCursor) {
      int i;
      for(i = pCursor; i < this.value.length() && !Character.isWhitespace(this.value.charAt(i)); ++i) {
      }

      return i;
   }

   private void onValueChange() {
      this.reflowDisplayLines();
      this.valueListener.accept(this.value);
      this.cursorListener.run();
   }

   private void reflowDisplayLines() {
      this.displayLines.clear();
      if (this.value.isEmpty()) {
         this.displayLines.add(MultilineTextField.StringView.EMPTY);
      } else {
         this.font.getSplitter().splitLines(this.value, this.width, Style.EMPTY, false, (p_239846_, p_239847_, p_239848_) -> {
            this.displayLines.add(new MultilineTextField.StringView(p_239847_, p_239848_));
         });
         if (this.value.charAt(this.value.length() - 1) == '\n') {
            this.displayLines.add(new MultilineTextField.StringView(this.value.length(), this.value.length()));
         }

      }
   }

   private String truncateFullText(String pFullText) {
      return this.hasCharacterLimit() ? StringUtil.truncateStringIfNecessary(pFullText, this.characterLimit, false) : pFullText;
   }

   private String truncateInsertionText(String pText) {
      if (this.hasCharacterLimit()) {
         int i = this.characterLimit - this.value.length();
         return StringUtil.truncateStringIfNecessary(pText, i, false);
      } else {
         return pText;
      }
   }

   @OnlyIn(Dist.CLIENT)
   protected static record StringView(int beginIndex, int endIndex) {
      static final MultilineTextField.StringView EMPTY = new MultilineTextField.StringView(0, 0);
   }
}