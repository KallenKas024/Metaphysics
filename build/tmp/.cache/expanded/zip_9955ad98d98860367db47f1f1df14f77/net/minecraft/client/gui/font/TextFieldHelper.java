package net.minecraft.client.gui.font;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextFieldHelper {
   private final Supplier<String> getMessageFn;
   private final Consumer<String> setMessageFn;
   private final Supplier<String> getClipboardFn;
   private final Consumer<String> setClipboardFn;
   private final Predicate<String> stringValidator;
   private int cursorPos;
   private int selectionPos;

   public TextFieldHelper(Supplier<String> pGetMessage, Consumer<String> pSetMessage, Supplier<String> pGetClipboard, Consumer<String> pSetClipboard, Predicate<String> pStringValidator) {
      this.getMessageFn = pGetMessage;
      this.setMessageFn = pSetMessage;
      this.getClipboardFn = pGetClipboard;
      this.setClipboardFn = pSetClipboard;
      this.stringValidator = pStringValidator;
      this.setCursorToEnd();
   }

   public static Supplier<String> createClipboardGetter(Minecraft pMinecraft) {
      return () -> {
         return getClipboardContents(pMinecraft);
      };
   }

   public static String getClipboardContents(Minecraft pMinecraft) {
      return ChatFormatting.stripFormatting(pMinecraft.keyboardHandler.getClipboard().replaceAll("\\r", ""));
   }

   public static Consumer<String> createClipboardSetter(Minecraft pMinecraft) {
      return (p_95173_) -> {
         setClipboardContents(pMinecraft, p_95173_);
      };
   }

   public static void setClipboardContents(Minecraft pMinecraft, String pText) {
      pMinecraft.keyboardHandler.setClipboard(pText);
   }

   public boolean charTyped(char pCharacter) {
      if (SharedConstants.isAllowedChatCharacter(pCharacter)) {
         this.insertText(this.getMessageFn.get(), Character.toString(pCharacter));
      }

      return true;
   }

   public boolean keyPressed(int pKey) {
      if (Screen.isSelectAll(pKey)) {
         this.selectAll();
         return true;
      } else if (Screen.isCopy(pKey)) {
         this.copy();
         return true;
      } else if (Screen.isPaste(pKey)) {
         this.paste();
         return true;
      } else if (Screen.isCut(pKey)) {
         this.cut();
         return true;
      } else {
         TextFieldHelper.CursorStep textfieldhelper$cursorstep = Screen.hasControlDown() ? TextFieldHelper.CursorStep.WORD : TextFieldHelper.CursorStep.CHARACTER;
         if (pKey == 259) {
            this.removeFromCursor(-1, textfieldhelper$cursorstep);
            return true;
         } else {
            if (pKey == 261) {
               this.removeFromCursor(1, textfieldhelper$cursorstep);
            } else {
               if (pKey == 263) {
                  this.moveBy(-1, Screen.hasShiftDown(), textfieldhelper$cursorstep);
                  return true;
               }

               if (pKey == 262) {
                  this.moveBy(1, Screen.hasShiftDown(), textfieldhelper$cursorstep);
                  return true;
               }

               if (pKey == 268) {
                  this.setCursorToStart(Screen.hasShiftDown());
                  return true;
               }

               if (pKey == 269) {
                  this.setCursorToEnd(Screen.hasShiftDown());
                  return true;
               }
            }

            return false;
         }
      }
   }

   private int clampToMsgLength(int pTextIndex) {
      return Mth.clamp(pTextIndex, 0, this.getMessageFn.get().length());
   }

   private void insertText(String pText, String pClipboardText) {
      if (this.selectionPos != this.cursorPos) {
         pText = this.deleteSelection(pText);
      }

      this.cursorPos = Mth.clamp(this.cursorPos, 0, pText.length());
      String s = (new StringBuilder(pText)).insert(this.cursorPos, pClipboardText).toString();
      if (this.stringValidator.test(s)) {
         this.setMessageFn.accept(s);
         this.selectionPos = this.cursorPos = Math.min(s.length(), this.cursorPos + pClipboardText.length());
      }

   }

   public void insertText(String pText) {
      this.insertText(this.getMessageFn.get(), pText);
   }

   private void resetSelectionIfNeeded(boolean pKeepSelection) {
      if (!pKeepSelection) {
         this.selectionPos = this.cursorPos;
      }

   }

   public void moveBy(int pDirection, boolean pKeepSelection, TextFieldHelper.CursorStep pCursorStep) {
      switch (pCursorStep) {
         case CHARACTER:
            this.moveByChars(pDirection, pKeepSelection);
            break;
         case WORD:
            this.moveByWords(pDirection, pKeepSelection);
      }

   }

   public void moveByChars(int pDirection) {
      this.moveByChars(pDirection, false);
   }

   public void moveByChars(int pDirection, boolean pKeepSelection) {
      this.cursorPos = Util.offsetByCodepoints(this.getMessageFn.get(), this.cursorPos, pDirection);
      this.resetSelectionIfNeeded(pKeepSelection);
   }

   public void moveByWords(int pDirection) {
      this.moveByWords(pDirection, false);
   }

   public void moveByWords(int pDirection, boolean pKeepSelection) {
      this.cursorPos = StringSplitter.getWordPosition(this.getMessageFn.get(), pDirection, this.cursorPos, true);
      this.resetSelectionIfNeeded(pKeepSelection);
   }

   public void removeFromCursor(int pDirection, TextFieldHelper.CursorStep pStep) {
      switch (pStep) {
         case CHARACTER:
            this.removeCharsFromCursor(pDirection);
            break;
         case WORD:
            this.removeWordsFromCursor(pDirection);
      }

   }

   public void removeWordsFromCursor(int pDirection) {
      int i = StringSplitter.getWordPosition(this.getMessageFn.get(), pDirection, this.cursorPos, true);
      this.removeCharsFromCursor(i - this.cursorPos);
   }

   public void removeCharsFromCursor(int pDirection) {
      String s = this.getMessageFn.get();
      if (!s.isEmpty()) {
         String s1;
         if (this.selectionPos != this.cursorPos) {
            s1 = this.deleteSelection(s);
         } else {
            int i = Util.offsetByCodepoints(s, this.cursorPos, pDirection);
            int j = Math.min(i, this.cursorPos);
            int k = Math.max(i, this.cursorPos);
            s1 = (new StringBuilder(s)).delete(j, k).toString();
            if (pDirection < 0) {
               this.selectionPos = this.cursorPos = j;
            }
         }

         this.setMessageFn.accept(s1);
      }

   }

   public void cut() {
      String s = this.getMessageFn.get();
      this.setClipboardFn.accept(this.getSelected(s));
      this.setMessageFn.accept(this.deleteSelection(s));
   }

   public void paste() {
      this.insertText(this.getMessageFn.get(), this.getClipboardFn.get());
      this.selectionPos = this.cursorPos;
   }

   public void copy() {
      this.setClipboardFn.accept(this.getSelected(this.getMessageFn.get()));
   }

   public void selectAll() {
      this.selectionPos = 0;
      this.cursorPos = this.getMessageFn.get().length();
   }

   private String getSelected(String pText) {
      int i = Math.min(this.cursorPos, this.selectionPos);
      int j = Math.max(this.cursorPos, this.selectionPos);
      return pText.substring(i, j);
   }

   private String deleteSelection(String pText) {
      if (this.selectionPos == this.cursorPos) {
         return pText;
      } else {
         int i = Math.min(this.cursorPos, this.selectionPos);
         int j = Math.max(this.cursorPos, this.selectionPos);
         String s = pText.substring(0, i) + pText.substring(j);
         this.selectionPos = this.cursorPos = i;
         return s;
      }
   }

   public void setCursorToStart() {
      this.setCursorToStart(false);
   }

   public void setCursorToStart(boolean pKeepSelection) {
      this.cursorPos = 0;
      this.resetSelectionIfNeeded(pKeepSelection);
   }

   public void setCursorToEnd() {
      this.setCursorToEnd(false);
   }

   public void setCursorToEnd(boolean pKeepSelection) {
      this.cursorPos = this.getMessageFn.get().length();
      this.resetSelectionIfNeeded(pKeepSelection);
   }

   public int getCursorPos() {
      return this.cursorPos;
   }

   public void setCursorPos(int pTextIndex) {
      this.setCursorPos(pTextIndex, true);
   }

   public void setCursorPos(int pTextIndex, boolean pKeepSelection) {
      this.cursorPos = this.clampToMsgLength(pTextIndex);
      this.resetSelectionIfNeeded(pKeepSelection);
   }

   public int getSelectionPos() {
      return this.selectionPos;
   }

   public void setSelectionPos(int pTextIndex) {
      this.selectionPos = this.clampToMsgLength(pTextIndex);
   }

   public void setSelectionRange(int pSelectionStart, int pSelectionEnd) {
      int i = this.getMessageFn.get().length();
      this.cursorPos = Mth.clamp(pSelectionStart, 0, i);
      this.selectionPos = Mth.clamp(pSelectionEnd, 0, i);
   }

   public boolean isSelecting() {
      return this.cursorPos != this.selectionPos;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum CursorStep {
      CHARACTER,
      WORD;
   }
}