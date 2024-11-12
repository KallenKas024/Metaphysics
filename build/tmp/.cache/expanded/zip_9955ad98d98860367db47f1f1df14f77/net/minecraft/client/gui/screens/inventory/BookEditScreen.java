package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

@OnlyIn(Dist.CLIENT)
public class BookEditScreen extends Screen {
   private static final int TEXT_WIDTH = 114;
   private static final int TEXT_HEIGHT = 128;
   private static final int IMAGE_WIDTH = 192;
   private static final int IMAGE_HEIGHT = 192;
   private static final Component EDIT_TITLE_LABEL = Component.translatable("book.editTitle");
   private static final Component FINALIZE_WARNING_LABEL = Component.translatable("book.finalizeWarning");
   private static final FormattedCharSequence BLACK_CURSOR = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.BLACK));
   private static final FormattedCharSequence GRAY_CURSOR = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.GRAY));
   private final Player owner;
   private final ItemStack book;
   /** Whether the book's title or contents has been modified since being opened */
   private boolean isModified;
   /** Determines if the signing screen is open */
   private boolean isSigning;
   /** Update ticks since the gui was opened */
   private int frameTick;
   private int currentPage;
   private final List<String> pages = Lists.newArrayList();
   private String title = "";
   private final TextFieldHelper pageEdit = new TextFieldHelper(this::getCurrentPageText, this::setCurrentPageText, this::getClipboard, this::setClipboard, (p_280853_) -> {
      return p_280853_.length() < 1024 && this.font.wordWrapHeight(p_280853_, 114) <= 128;
   });
   private final TextFieldHelper titleEdit = new TextFieldHelper(() -> {
      return this.title;
   }, (p_98175_) -> {
      this.title = p_98175_;
   }, this::getClipboard, this::setClipboard, (p_98170_) -> {
      return p_98170_.length() < 16;
   });
   /** In milliseconds */
   private long lastClickTime;
   private int lastIndex = -1;
   private PageButton forwardButton;
   private PageButton backButton;
   private Button doneButton;
   private Button signButton;
   private Button finalizeButton;
   private Button cancelButton;
   private final InteractionHand hand;
   @Nullable
   private BookEditScreen.DisplayCache displayCache = BookEditScreen.DisplayCache.EMPTY;
   private Component pageMsg = CommonComponents.EMPTY;
   private final Component ownerText;

   public BookEditScreen(Player pOwner, ItemStack pBook, InteractionHand pHand) {
      super(GameNarrator.NO_TITLE);
      this.owner = pOwner;
      this.book = pBook;
      this.hand = pHand;
      CompoundTag compoundtag = pBook.getTag();
      if (compoundtag != null) {
         BookViewScreen.loadPages(compoundtag, this.pages::add);
      }

      if (this.pages.isEmpty()) {
         this.pages.add("");
      }

      this.ownerText = Component.translatable("book.byAuthor", pOwner.getName()).withStyle(ChatFormatting.DARK_GRAY);
   }

   private void setClipboard(String p_98148_) {
      if (this.minecraft != null) {
         TextFieldHelper.setClipboardContents(this.minecraft, p_98148_);
      }

   }

   private String getClipboard() {
      return this.minecraft != null ? TextFieldHelper.getClipboardContents(this.minecraft) : "";
   }

   /**
    * Returns the number of pages in the book
    */
   private int getNumPages() {
      return this.pages.size();
   }

   public void tick() {
      super.tick();
      ++this.frameTick;
   }

   protected void init() {
      this.clearDisplayCache();
      this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), (p_98177_) -> {
         this.isSigning = true;
         this.updateButtonVisibility();
      }).bounds(this.width / 2 - 100, 196, 98, 20).build());
      this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280851_) -> {
         this.minecraft.setScreen((Screen)null);
         this.saveChanges(false);
      }).bounds(this.width / 2 + 2, 196, 98, 20).build());
      this.finalizeButton = this.addRenderableWidget(Button.builder(Component.translatable("book.finalizeButton"), (p_280852_) -> {
         if (this.isSigning) {
            this.saveChanges(true);
            this.minecraft.setScreen((Screen)null);
         }

      }).bounds(this.width / 2 - 100, 196, 98, 20).build());
      this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_98157_) -> {
         if (this.isSigning) {
            this.isSigning = false;
         }

         this.updateButtonVisibility();
      }).bounds(this.width / 2 + 2, 196, 98, 20).build());
      int i = (this.width - 192) / 2;
      int j = 2;
      this.forwardButton = this.addRenderableWidget(new PageButton(i + 116, 159, true, (p_98144_) -> {
         this.pageForward();
      }, true));
      this.backButton = this.addRenderableWidget(new PageButton(i + 43, 159, false, (p_98113_) -> {
         this.pageBack();
      }, true));
      this.updateButtonVisibility();
   }

   /**
    * Displays the previous page
    */
   private void pageBack() {
      if (this.currentPage > 0) {
         --this.currentPage;
      }

      this.updateButtonVisibility();
      this.clearDisplayCacheAfterPageChange();
   }

   /**
    * Displays the next page (creating it if necessary)
    */
   private void pageForward() {
      if (this.currentPage < this.getNumPages() - 1) {
         ++this.currentPage;
      } else {
         this.appendPageToBook();
         if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
         }
      }

      this.updateButtonVisibility();
      this.clearDisplayCacheAfterPageChange();
   }

   /**
    * Sets visibility for book buttons
    */
   private void updateButtonVisibility() {
      this.backButton.visible = !this.isSigning && this.currentPage > 0;
      this.forwardButton.visible = !this.isSigning;
      this.doneButton.visible = !this.isSigning;
      this.signButton.visible = !this.isSigning;
      this.cancelButton.visible = this.isSigning;
      this.finalizeButton.visible = this.isSigning;
      this.finalizeButton.active = !this.title.trim().isEmpty();
   }

   private void eraseEmptyTrailingPages() {
      ListIterator<String> listiterator = this.pages.listIterator(this.pages.size());

      while(listiterator.hasPrevious() && listiterator.previous().isEmpty()) {
         listiterator.remove();
      }

   }

   private void saveChanges(boolean pPublish) {
      if (this.isModified) {
         this.eraseEmptyTrailingPages();
         this.updateLocalCopy(pPublish);
         int i = this.hand == InteractionHand.MAIN_HAND ? this.owner.getInventory().selected : 40;
         this.minecraft.getConnection().send(new ServerboundEditBookPacket(i, this.pages, pPublish ? Optional.of(this.title.trim()) : Optional.empty()));
      }
   }

   private void updateLocalCopy(boolean pSign) {
      ListTag listtag = new ListTag();
      this.pages.stream().map(StringTag::valueOf).forEach(listtag::add);
      if (!this.pages.isEmpty()) {
         this.book.addTagElement("pages", listtag);
      }

      if (pSign) {
         this.book.addTagElement("author", StringTag.valueOf(this.owner.getGameProfile().getName()));
         this.book.addTagElement("title", StringTag.valueOf(this.title.trim()));
      }

   }

   /**
    * Adds a new page to the book (capped at 100 pages)
    */
   private void appendPageToBook() {
      if (this.getNumPages() < 100) {
         this.pages.add("");
         this.isModified = true;
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
      if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (this.isSigning) {
         return this.titleKeyPressed(pKeyCode, pScanCode, pModifiers);
      } else {
         boolean flag = this.bookKeyPressed(pKeyCode, pScanCode, pModifiers);
         if (flag) {
            this.clearDisplayCache();
            return true;
         } else {
            return false;
         }
      }
   }

   /**
    * Called when a character is typed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pCodePoint the code point of the typed character.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean charTyped(char pCodePoint, int pModifiers) {
      if (super.charTyped(pCodePoint, pModifiers)) {
         return true;
      } else if (this.isSigning) {
         boolean flag = this.titleEdit.charTyped(pCodePoint);
         if (flag) {
            this.updateButtonVisibility();
            this.isModified = true;
            return true;
         } else {
            return false;
         }
      } else if (SharedConstants.isAllowedChatCharacter(pCodePoint)) {
         this.pageEdit.insertText(Character.toString(pCodePoint));
         this.clearDisplayCache();
         return true;
      } else {
         return false;
      }
   }

   /**
    * Handles keypresses, clipboard functions, and page turning
    */
   private boolean bookKeyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (Screen.isSelectAll(pKeyCode)) {
         this.pageEdit.selectAll();
         return true;
      } else if (Screen.isCopy(pKeyCode)) {
         this.pageEdit.copy();
         return true;
      } else if (Screen.isPaste(pKeyCode)) {
         this.pageEdit.paste();
         return true;
      } else if (Screen.isCut(pKeyCode)) {
         this.pageEdit.cut();
         return true;
      } else {
         TextFieldHelper.CursorStep textfieldhelper$cursorstep = Screen.hasControlDown() ? TextFieldHelper.CursorStep.WORD : TextFieldHelper.CursorStep.CHARACTER;
         switch (pKeyCode) {
            case 257:
            case 335:
               this.pageEdit.insertText("\n");
               return true;
            case 259:
               this.pageEdit.removeFromCursor(-1, textfieldhelper$cursorstep);
               return true;
            case 261:
               this.pageEdit.removeFromCursor(1, textfieldhelper$cursorstep);
               return true;
            case 262:
               this.pageEdit.moveBy(1, Screen.hasShiftDown(), textfieldhelper$cursorstep);
               return true;
            case 263:
               this.pageEdit.moveBy(-1, Screen.hasShiftDown(), textfieldhelper$cursorstep);
               return true;
            case 264:
               this.keyDown();
               return true;
            case 265:
               this.keyUp();
               return true;
            case 266:
               this.backButton.onPress();
               return true;
            case 267:
               this.forwardButton.onPress();
               return true;
            case 268:
               this.keyHome();
               return true;
            case 269:
               this.keyEnd();
               return true;
            default:
               return false;
         }
      }
   }

   private void keyUp() {
      this.changeLine(-1);
   }

   private void keyDown() {
      this.changeLine(1);
   }

   private void changeLine(int pYChange) {
      int i = this.pageEdit.getCursorPos();
      int j = this.getDisplayCache().changeLine(i, pYChange);
      this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
   }

   private void keyHome() {
      if (Screen.hasControlDown()) {
         this.pageEdit.setCursorToStart(Screen.hasShiftDown());
      } else {
         int i = this.pageEdit.getCursorPos();
         int j = this.getDisplayCache().findLineStart(i);
         this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
      }

   }

   private void keyEnd() {
      if (Screen.hasControlDown()) {
         this.pageEdit.setCursorToEnd(Screen.hasShiftDown());
      } else {
         BookEditScreen.DisplayCache bookeditscreen$displaycache = this.getDisplayCache();
         int i = this.pageEdit.getCursorPos();
         int j = bookeditscreen$displaycache.findLineEnd(i);
         this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
      }

   }

   /**
    * Handles special keys pressed while editing the book's title
    */
   private boolean titleKeyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      switch (pKeyCode) {
         case 257:
         case 335:
            if (!this.title.isEmpty()) {
               this.saveChanges(true);
               this.minecraft.setScreen((Screen)null);
            }

            return true;
         case 259:
            this.titleEdit.removeCharsFromCursor(-1);
            this.updateButtonVisibility();
            this.isModified = true;
            return true;
         default:
            return false;
      }
   }

   /**
    * Returns the contents of the current page as a string (or an empty string if the currPage isn't a valid page index)
    */
   private String getCurrentPageText() {
      return this.currentPage >= 0 && this.currentPage < this.pages.size() ? this.pages.get(this.currentPage) : "";
   }

   private void setCurrentPageText(String p_98159_) {
      if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
         this.pages.set(this.currentPage, p_98159_);
         this.isModified = true;
         this.clearDisplayCache();
      }

   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      this.setFocused((GuiEventListener)null);
      int i = (this.width - 192) / 2;
      int j = 2;
      pGuiGraphics.blit(BookViewScreen.BOOK_LOCATION, i, 2, 0, 0, 192, 192);
      if (this.isSigning) {
         boolean flag = this.frameTick / 6 % 2 == 0;
         FormattedCharSequence formattedcharsequence = FormattedCharSequence.composite(FormattedCharSequence.forward(this.title, Style.EMPTY), flag ? BLACK_CURSOR : GRAY_CURSOR);
         int k = this.font.width(EDIT_TITLE_LABEL);
         pGuiGraphics.drawString(this.font, EDIT_TITLE_LABEL, i + 36 + (114 - k) / 2, 34, 0, false);
         int l = this.font.width(formattedcharsequence);
         pGuiGraphics.drawString(this.font, formattedcharsequence, i + 36 + (114 - l) / 2, 50, 0, false);
         int i1 = this.font.width(this.ownerText);
         pGuiGraphics.drawString(this.font, this.ownerText, i + 36 + (114 - i1) / 2, 60, 0, false);
         pGuiGraphics.drawWordWrap(this.font, FINALIZE_WARNING_LABEL, i + 36, 82, 114, 0);
      } else {
         int j1 = this.font.width(this.pageMsg);
         pGuiGraphics.drawString(this.font, this.pageMsg, i - j1 + 192 - 44, 18, 0, false);
         BookEditScreen.DisplayCache bookeditscreen$displaycache = this.getDisplayCache();

         for(BookEditScreen.LineInfo bookeditscreen$lineinfo : bookeditscreen$displaycache.lines) {
            pGuiGraphics.drawString(this.font, bookeditscreen$lineinfo.asComponent, bookeditscreen$lineinfo.x, bookeditscreen$lineinfo.y, -16777216, false);
         }

         this.renderHighlight(pGuiGraphics, bookeditscreen$displaycache.selection);
         this.renderCursor(pGuiGraphics, bookeditscreen$displaycache.cursor, bookeditscreen$displaycache.cursorAtEnd);
      }

      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   private void renderCursor(GuiGraphics pGuiGraphics, BookEditScreen.Pos2i pCursorPos, boolean pIsEndOfText) {
      if (this.frameTick / 6 % 2 == 0) {
         pCursorPos = this.convertLocalToScreen(pCursorPos);
         if (!pIsEndOfText) {
            pGuiGraphics.fill(pCursorPos.x, pCursorPos.y - 1, pCursorPos.x + 1, pCursorPos.y + 9, -16777216);
         } else {
            pGuiGraphics.drawString(this.font, "_", pCursorPos.x, pCursorPos.y, 0, false);
         }
      }

   }

   private void renderHighlight(GuiGraphics pGuiGraphics, Rect2i[] pHighlightAreas) {
      for(Rect2i rect2i : pHighlightAreas) {
         int i = rect2i.getX();
         int j = rect2i.getY();
         int k = i + rect2i.getWidth();
         int l = j + rect2i.getHeight();
         pGuiGraphics.fill(RenderType.guiTextHighlight(), i, j, k, l, -16776961);
      }

   }

   private BookEditScreen.Pos2i convertScreenToLocal(BookEditScreen.Pos2i pScreenPos) {
      return new BookEditScreen.Pos2i(pScreenPos.x - (this.width - 192) / 2 - 36, pScreenPos.y - 32);
   }

   private BookEditScreen.Pos2i convertLocalToScreen(BookEditScreen.Pos2i pLocalScreenPos) {
      return new BookEditScreen.Pos2i(pLocalScreenPos.x + (this.width - 192) / 2 + 36, pLocalScreenPos.y + 32);
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
      } else {
         if (pButton == 0) {
            long i = Util.getMillis();
            BookEditScreen.DisplayCache bookeditscreen$displaycache = this.getDisplayCache();
            int j = bookeditscreen$displaycache.getIndexAtPosition(this.font, this.convertScreenToLocal(new BookEditScreen.Pos2i((int)pMouseX, (int)pMouseY)));
            if (j >= 0) {
               if (j == this.lastIndex && i - this.lastClickTime < 250L) {
                  if (!this.pageEdit.isSelecting()) {
                     this.selectWord(j);
                  } else {
                     this.pageEdit.selectAll();
                  }
               } else {
                  this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
               }

               this.clearDisplayCache();
            }

            this.lastIndex = j;
            this.lastClickTime = i;
         }

         return true;
      }
   }

   private void selectWord(int pIndex) {
      String s = this.getCurrentPageText();
      this.pageEdit.setSelectionRange(StringSplitter.getWordPosition(s, -1, pIndex, false), StringSplitter.getWordPosition(s, 1, pIndex, false));
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
      } else {
         if (pButton == 0) {
            BookEditScreen.DisplayCache bookeditscreen$displaycache = this.getDisplayCache();
            int i = bookeditscreen$displaycache.getIndexAtPosition(this.font, this.convertScreenToLocal(new BookEditScreen.Pos2i((int)pMouseX, (int)pMouseY)));
            this.pageEdit.setCursorPos(i, true);
            this.clearDisplayCache();
         }

         return true;
      }
   }

   private BookEditScreen.DisplayCache getDisplayCache() {
      if (this.displayCache == null) {
         this.displayCache = this.rebuildDisplayCache();
         this.pageMsg = Component.translatable("book.pageIndicator", this.currentPage + 1, this.getNumPages());
      }

      return this.displayCache;
   }

   private void clearDisplayCache() {
      this.displayCache = null;
   }

   private void clearDisplayCacheAfterPageChange() {
      this.pageEdit.setCursorToEnd();
      this.clearDisplayCache();
   }

   private BookEditScreen.DisplayCache rebuildDisplayCache() {
      String s = this.getCurrentPageText();
      if (s.isEmpty()) {
         return BookEditScreen.DisplayCache.EMPTY;
      } else {
         int i = this.pageEdit.getCursorPos();
         int j = this.pageEdit.getSelectionPos();
         IntList intlist = new IntArrayList();
         List<BookEditScreen.LineInfo> list = Lists.newArrayList();
         MutableInt mutableint = new MutableInt();
         MutableBoolean mutableboolean = new MutableBoolean();
         StringSplitter stringsplitter = this.font.getSplitter();
         stringsplitter.splitLines(s, 114, Style.EMPTY, true, (p_98132_, p_98133_, p_98134_) -> {
            int k3 = mutableint.getAndIncrement();
            String s2 = s.substring(p_98133_, p_98134_);
            mutableboolean.setValue(s2.endsWith("\n"));
            String s3 = StringUtils.stripEnd(s2, " \n");
            int l3 = k3 * 9;
            BookEditScreen.Pos2i bookeditscreen$pos2i1 = this.convertLocalToScreen(new BookEditScreen.Pos2i(0, l3));
            intlist.add(p_98133_);
            list.add(new BookEditScreen.LineInfo(p_98132_, s3, bookeditscreen$pos2i1.x, bookeditscreen$pos2i1.y));
         });
         int[] aint = intlist.toIntArray();
         boolean flag = i == s.length();
         BookEditScreen.Pos2i bookeditscreen$pos2i;
         if (flag && mutableboolean.isTrue()) {
            bookeditscreen$pos2i = new BookEditScreen.Pos2i(0, list.size() * 9);
         } else {
            int k = findLineFromPos(aint, i);
            int l = this.font.width(s.substring(aint[k], i));
            bookeditscreen$pos2i = new BookEditScreen.Pos2i(l, k * 9);
         }

         List<Rect2i> list1 = Lists.newArrayList();
         if (i != j) {
            int l2 = Math.min(i, j);
            int i1 = Math.max(i, j);
            int j1 = findLineFromPos(aint, l2);
            int k1 = findLineFromPos(aint, i1);
            if (j1 == k1) {
               int l1 = j1 * 9;
               int i2 = aint[j1];
               list1.add(this.createPartialLineSelection(s, stringsplitter, l2, i1, l1, i2));
            } else {
               int i3 = j1 + 1 > aint.length ? s.length() : aint[j1 + 1];
               list1.add(this.createPartialLineSelection(s, stringsplitter, l2, i3, j1 * 9, aint[j1]));

               for(int j3 = j1 + 1; j3 < k1; ++j3) {
                  int j2 = j3 * 9;
                  String s1 = s.substring(aint[j3], aint[j3 + 1]);
                  int k2 = (int)stringsplitter.stringWidth(s1);
                  list1.add(this.createSelection(new BookEditScreen.Pos2i(0, j2), new BookEditScreen.Pos2i(k2, j2 + 9)));
               }

               list1.add(this.createPartialLineSelection(s, stringsplitter, aint[k1], i1, k1 * 9, aint[k1]));
            }
         }

         return new BookEditScreen.DisplayCache(s, bookeditscreen$pos2i, flag, aint, list.toArray(new BookEditScreen.LineInfo[0]), list1.toArray(new Rect2i[0]));
      }
   }

   static int findLineFromPos(int[] pLineStarts, int pFind) {
      int i = Arrays.binarySearch(pLineStarts, pFind);
      return i < 0 ? -(i + 2) : i;
   }

   private Rect2i createPartialLineSelection(String pInput, StringSplitter pSplitter, int pStartPos, int pEndPos, int pY, int pLineStart) {
      String s = pInput.substring(pLineStart, pStartPos);
      String s1 = pInput.substring(pLineStart, pEndPos);
      BookEditScreen.Pos2i bookeditscreen$pos2i = new BookEditScreen.Pos2i((int)pSplitter.stringWidth(s), pY);
      BookEditScreen.Pos2i bookeditscreen$pos2i1 = new BookEditScreen.Pos2i((int)pSplitter.stringWidth(s1), pY + 9);
      return this.createSelection(bookeditscreen$pos2i, bookeditscreen$pos2i1);
   }

   private Rect2i createSelection(BookEditScreen.Pos2i pCorner1, BookEditScreen.Pos2i pCorner2) {
      BookEditScreen.Pos2i bookeditscreen$pos2i = this.convertLocalToScreen(pCorner1);
      BookEditScreen.Pos2i bookeditscreen$pos2i1 = this.convertLocalToScreen(pCorner2);
      int i = Math.min(bookeditscreen$pos2i.x, bookeditscreen$pos2i1.x);
      int j = Math.max(bookeditscreen$pos2i.x, bookeditscreen$pos2i1.x);
      int k = Math.min(bookeditscreen$pos2i.y, bookeditscreen$pos2i1.y);
      int l = Math.max(bookeditscreen$pos2i.y, bookeditscreen$pos2i1.y);
      return new Rect2i(i, k, j - i, l - k);
   }

   @OnlyIn(Dist.CLIENT)
   static class DisplayCache {
      static final BookEditScreen.DisplayCache EMPTY = new BookEditScreen.DisplayCache("", new BookEditScreen.Pos2i(0, 0), true, new int[]{0}, new BookEditScreen.LineInfo[]{new BookEditScreen.LineInfo(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
      private final String fullText;
      final BookEditScreen.Pos2i cursor;
      final boolean cursorAtEnd;
      private final int[] lineStarts;
      final BookEditScreen.LineInfo[] lines;
      final Rect2i[] selection;

      public DisplayCache(String pFullText, BookEditScreen.Pos2i pCursor, boolean pCursorAtEnd, int[] pLineStarts, BookEditScreen.LineInfo[] pLines, Rect2i[] pSelection) {
         this.fullText = pFullText;
         this.cursor = pCursor;
         this.cursorAtEnd = pCursorAtEnd;
         this.lineStarts = pLineStarts;
         this.lines = pLines;
         this.selection = pSelection;
      }

      public int getIndexAtPosition(Font pFont, BookEditScreen.Pos2i pCursorPosition) {
         int i = pCursorPosition.y / 9;
         if (i < 0) {
            return 0;
         } else if (i >= this.lines.length) {
            return this.fullText.length();
         } else {
            BookEditScreen.LineInfo bookeditscreen$lineinfo = this.lines[i];
            return this.lineStarts[i] + pFont.getSplitter().plainIndexAtWidth(bookeditscreen$lineinfo.contents, pCursorPosition.x, bookeditscreen$lineinfo.style);
         }
      }

      public int changeLine(int pXChange, int pYChange) {
         int i = BookEditScreen.findLineFromPos(this.lineStarts, pXChange);
         int j = i + pYChange;
         int k;
         if (0 <= j && j < this.lineStarts.length) {
            int l = pXChange - this.lineStarts[i];
            int i1 = this.lines[j].contents.length();
            k = this.lineStarts[j] + Math.min(l, i1);
         } else {
            k = pXChange;
         }

         return k;
      }

      public int findLineStart(int pLine) {
         int i = BookEditScreen.findLineFromPos(this.lineStarts, pLine);
         return this.lineStarts[i];
      }

      public int findLineEnd(int pLine) {
         int i = BookEditScreen.findLineFromPos(this.lineStarts, pLine);
         return this.lineStarts[i] + this.lines[i].contents.length();
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class LineInfo {
      final Style style;
      final String contents;
      final Component asComponent;
      final int x;
      final int y;

      public LineInfo(Style pStyle, String pContents, int pX, int pY) {
         this.style = pStyle;
         this.contents = pContents;
         this.x = pX;
         this.y = pY;
         this.asComponent = Component.literal(pContents).setStyle(pStyle);
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class Pos2i {
      public final int x;
      public final int y;

      Pos2i(int pX, int pY) {
         this.x = pX;
         this.y = pY;
      }
   }
}