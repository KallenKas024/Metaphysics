package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class ChatScreen extends Screen {
   public static final double MOUSE_SCROLL_SPEED = 7.0D;
   private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
   private static final int TOOLTIP_MAX_WIDTH = 210;
   private String historyBuffer = "";
   /**
    * keeps position of which chat message you will select when you press up, (does not increase for duplicated messages
    * sent immediately after each other)
    */
   private int historyPos = -1;
   /** Chat entry field */
   protected EditBox input;
   /** is the text that appears when you press the chat key and the input box appears pre-filled */
   private String initial;
   CommandSuggestions commandSuggestions;

   public ChatScreen(String pInitial) {
      super(Component.translatable("chat_screen.title"));
      this.initial = pInitial;
   }

   protected void init() {
      this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
      this.input = new EditBox(this.minecraft.fontFilterFishy, 4, this.height - 12, this.width - 4, 12, Component.translatable("chat.editBox")) {
         protected MutableComponent createNarrationMessage() {
            return super.createNarrationMessage().append(ChatScreen.this.commandSuggestions.getNarrationMessage());
         }
      };
      this.input.setMaxLength(256);
      this.input.setBordered(false);
      this.input.setValue(this.initial);
      this.input.setResponder(this::onEdited);
      this.input.setCanLoseFocus(false);
      this.addWidget(this.input);
      this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
      this.commandSuggestions.updateCommandInfo();
      this.setInitialFocus(this.input);
   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      String s = this.input.getValue();
      this.init(pMinecraft, pWidth, pHeight);
      this.setChatLine(s);
      this.commandSuggestions.updateCommandInfo();
   }

   public void removed() {
      this.minecraft.gui.getChat().resetChatScroll();
   }

   public void tick() {
      this.input.tick();
   }

   private void onEdited(String p_95611_) {
      String s = this.input.getValue();
      this.commandSuggestions.setAllowSuggestions(!s.equals(this.initial));
      this.commandSuggestions.updateCommandInfo();
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
      if (this.commandSuggestions.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (pKeyCode == 256) {
         this.minecraft.setScreen((Screen)null);
         return true;
      } else if (pKeyCode != 257 && pKeyCode != 335) {
         if (pKeyCode == 265) {
            this.moveInHistory(-1);
            return true;
         } else if (pKeyCode == 264) {
            this.moveInHistory(1);
            return true;
         } else if (pKeyCode == 266) {
            this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
            return true;
         } else if (pKeyCode == 267) {
            this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
            return true;
         } else {
            return false;
         }
      } else {
         if (this.handleChatInput(this.input.getValue(), true)) {
            this.minecraft.setScreen((Screen)null);
         }

         return true;
      }
   }

   /**
    * Called when the mouse wheel is scrolled within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pDelta the scrolling delta.
    */
   public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      pDelta = Mth.clamp(pDelta, -1.0D, 1.0D);
      if (this.commandSuggestions.mouseScrolled(pDelta)) {
         return true;
      } else {
         if (!hasShiftDown()) {
            pDelta *= 7.0D;
         }

         this.minecraft.gui.getChat().scrollChat((int)pDelta);
         return true;
      }
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
      if (this.commandSuggestions.mouseClicked((double)((int)pMouseX), (double)((int)pMouseY), pButton)) {
         return true;
      } else {
         if (pButton == 0) {
            ChatComponent chatcomponent = this.minecraft.gui.getChat();
            if (chatcomponent.handleChatQueueClicked(pMouseX, pMouseY)) {
               return true;
            }

            Style style = this.getComponentStyleAt(pMouseX, pMouseY);
            if (style != null && this.handleComponentClicked(style)) {
               this.initial = this.input.getValue();
               return true;
            }
         }

         return this.input.mouseClicked(pMouseX, pMouseY, pButton) ? true : super.mouseClicked(pMouseX, pMouseY, pButton);
      }
   }

   protected void insertText(String pText, boolean pOverwrite) {
      if (pOverwrite) {
         this.input.setValue(pText);
      } else {
         this.input.insertText(pText);
      }

   }

   /**
    * Input is relative and is applied directly to the sentHistoryCursor so -1 is the previous message, 1 is the next
    * message from the current cursor position.
    */
   public void moveInHistory(int pMsgPos) {
      int i = this.historyPos + pMsgPos;
      int j = this.minecraft.gui.getChat().getRecentChat().size();
      i = Mth.clamp(i, 0, j);
      if (i != this.historyPos) {
         if (i == j) {
            this.historyPos = j;
            this.input.setValue(this.historyBuffer);
         } else {
            if (this.historyPos == j) {
               this.historyBuffer = this.input.getValue();
            }

            this.input.setValue(this.minecraft.gui.getChat().getRecentChat().get(i));
            this.commandSuggestions.setAllowSuggestions(false);
            this.historyPos = i;
         }
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
      pGuiGraphics.fill(2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
      this.input.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.commandSuggestions.render(pGuiGraphics, pMouseX, pMouseY);
      GuiMessageTag guimessagetag = this.minecraft.gui.getChat().getMessageTagAt((double)pMouseX, (double)pMouseY);
      if (guimessagetag != null && guimessagetag.text() != null) {
         pGuiGraphics.renderTooltip(this.font, this.font.split(guimessagetag.text(), 210), pMouseX, pMouseY);
      } else {
         Style style = this.getComponentStyleAt((double)pMouseX, (double)pMouseY);
         if (style != null && style.getHoverEvent() != null) {
            pGuiGraphics.renderComponentHoverEffect(this.font, style, pMouseX, pMouseY);
         }
      }

   }

   public boolean isPauseScreen() {
      return false;
   }

   private void setChatLine(String pChatLine) {
      this.input.setValue(pChatLine);
   }

   protected void updateNarrationState(NarrationElementOutput pOutput) {
      pOutput.add(NarratedElementType.TITLE, this.getTitle());
      pOutput.add(NarratedElementType.USAGE, USAGE_TEXT);
      String s = this.input.getValue();
      if (!s.isEmpty()) {
         pOutput.nest().add(NarratedElementType.TITLE, Component.translatable("chat_screen.message", s));
      }

   }

   @Nullable
   private Style getComponentStyleAt(double pMouseX, double pMouseY) {
      return this.minecraft.gui.getChat().getClickedComponentStyleAt(pMouseX, pMouseY);
   }

   public boolean handleChatInput(String pInput, boolean pAddToRecentChat) {
      pInput = this.normalizeChatMessage(pInput);
      if (pInput.isEmpty()) {
         return true;
      } else {
         if (pAddToRecentChat) {
            this.minecraft.gui.getChat().addRecentChat(pInput);
         }

         if (pInput.startsWith("/")) {
            this.minecraft.player.connection.sendCommand(pInput.substring(1));
         } else {
            this.minecraft.player.connection.sendChat(pInput);
         }

         return minecraft.screen == this; // FORGE: Prevent closing the screen if another screen has been opened.
      }
   }

   public String normalizeChatMessage(String pMessage) {
      return StringUtil.trimChatMessage(StringUtils.normalizeSpace(pMessage.trim()));
   }
}
