package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatSelectionScreen extends Screen {
   private static final Component TITLE = Component.translatable("gui.chatSelection.title");
   private static final Component CONTEXT_INFO = Component.translatable("gui.chatSelection.context").withStyle(ChatFormatting.GRAY);
   @Nullable
   private final Screen lastScreen;
   private final ReportingContext reportingContext;
   private Button confirmSelectedButton;
   private MultiLineLabel contextInfoLabel;
   @Nullable
   private ChatSelectionScreen.ChatSelectionList chatSelectionList;
   final ChatReportBuilder report;
   private final Consumer<ChatReportBuilder> onSelected;
   private ChatSelectionLogFiller chatLogFiller;

   public ChatSelectionScreen(@Nullable Screen pLastScreen, ReportingContext pReportingContext, ChatReportBuilder pReport, Consumer<ChatReportBuilder> pOnSelected) {
      super(TITLE);
      this.lastScreen = pLastScreen;
      this.reportingContext = pReportingContext;
      this.report = pReport.copy();
      this.onSelected = pOnSelected;
   }

   protected void init() {
      this.chatLogFiller = new ChatSelectionLogFiller(this.reportingContext, this::canReport);
      this.contextInfoLabel = MultiLineLabel.create(this.font, CONTEXT_INFO, this.width - 16);
      this.chatSelectionList = new ChatSelectionScreen.ChatSelectionList(this.minecraft, (this.contextInfoLabel.getLineCount() + 1) * 9);
      this.chatSelectionList.setRenderBackground(false);
      this.addWidget(this.chatSelectionList);
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_239860_) -> {
         this.onClose();
      }).bounds(this.width / 2 - 155, this.height - 32, 150, 20).build());
      this.confirmSelectedButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_239591_) -> {
         this.onSelected.accept(this.report);
         this.onClose();
      }).bounds(this.width / 2 - 155 + 160, this.height - 32, 150, 20).build());
      this.updateConfirmSelectedButton();
      this.extendLog();
      this.chatSelectionList.setScrollAmount((double)this.chatSelectionList.getMaxScroll());
   }

   private boolean canReport(LoggedChatMessage p_242240_) {
      return p_242240_.canReport(this.report.reportedProfileId());
   }

   private void extendLog() {
      int i = this.chatSelectionList.getMaxVisibleEntries();
      this.chatLogFiller.fillNextPage(i, this.chatSelectionList);
   }

   void onReachedScrollTop() {
      this.extendLog();
   }

   void updateConfirmSelectedButton() {
      this.confirmSelectedButton.active = !this.report.reportedMessages().isEmpty();
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
      this.chatSelectionList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
      AbuseReportLimits abusereportlimits = this.reportingContext.sender().reportLimits();
      int i = this.report.reportedMessages().size();
      int j = abusereportlimits.maxReportedMessageCount();
      Component component = Component.translatable("gui.chatSelection.selected", i, j);
      pGuiGraphics.drawCenteredString(this.font, component, this.width / 2, 16 + 9 * 3 / 2, 10526880);
      this.contextInfoLabel.renderCentered(pGuiGraphics, this.width / 2, this.chatSelectionList.getFooterTop());
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), CONTEXT_INFO);
   }

   @OnlyIn(Dist.CLIENT)
   public class ChatSelectionList extends ObjectSelectionList<ChatSelectionScreen.ChatSelectionList.Entry> implements ChatSelectionLogFiller.Output {
      @Nullable
      private ChatSelectionScreen.ChatSelectionList.Heading previousHeading;

      public ChatSelectionList(Minecraft pMinecraft, int p_239061_) {
         super(pMinecraft, ChatSelectionScreen.this.width, ChatSelectionScreen.this.height, 40, ChatSelectionScreen.this.height - 40 - p_239061_, 16);
      }

      public void setScrollAmount(double pScroll) {
         double d0 = this.getScrollAmount();
         super.setScrollAmount(pScroll);
         if ((float)this.getMaxScroll() > 1.0E-5F && pScroll <= (double)1.0E-5F && !Mth.equal(pScroll, d0)) {
            ChatSelectionScreen.this.onReachedScrollTop();
         }

      }

      public void acceptMessage(int pChatId, LoggedChatMessage.Player pPlayerMessage) {
         boolean flag = pPlayerMessage.canReport(ChatSelectionScreen.this.report.reportedProfileId());
         ChatTrustLevel chattrustlevel = pPlayerMessage.trustLevel();
         GuiMessageTag guimessagetag = chattrustlevel.createTag(pPlayerMessage.message());
         ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen$chatselectionlist$entry = new ChatSelectionScreen.ChatSelectionList.MessageEntry(pChatId, pPlayerMessage.toContentComponent(), pPlayerMessage.toNarrationComponent(), guimessagetag, flag, true);
         this.addEntryToTop(chatselectionscreen$chatselectionlist$entry);
         this.updateHeading(pPlayerMessage, flag);
      }

      private void updateHeading(LoggedChatMessage.Player pLoggedPlayerChatMessage, boolean pCanReport) {
         ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen$chatselectionlist$entry = new ChatSelectionScreen.ChatSelectionList.MessageHeadingEntry(pLoggedPlayerChatMessage.profile(), pLoggedPlayerChatMessage.toHeadingComponent(), pCanReport);
         this.addEntryToTop(chatselectionscreen$chatselectionlist$entry);
         ChatSelectionScreen.ChatSelectionList.Heading chatselectionscreen$chatselectionlist$heading = new ChatSelectionScreen.ChatSelectionList.Heading(pLoggedPlayerChatMessage.profileId(), chatselectionscreen$chatselectionlist$entry);
         if (this.previousHeading != null && this.previousHeading.canCombine(chatselectionscreen$chatselectionlist$heading)) {
            this.removeEntryFromTop(this.previousHeading.entry());
         }

         this.previousHeading = chatselectionscreen$chatselectionlist$heading;
      }

      public void acceptDivider(Component pText) {
         this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.PaddingEntry());
         this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.DividerEntry(pText));
         this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.PaddingEntry());
         this.previousHeading = null;
      }

      protected int getScrollbarPosition() {
         return (this.width + this.getRowWidth()) / 2;
      }

      public int getRowWidth() {
         return Math.min(350, this.width - 50);
      }

      public int getMaxVisibleEntries() {
         return Mth.positiveCeilDiv(this.y1 - this.y0, this.itemHeight);
      }

      protected void renderItem(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick, int pIndex, int pLeft, int pTop, int pWidth, int pHeight) {
         ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen$chatselectionlist$entry = this.getEntry(pIndex);
         if (this.shouldHighlightEntry(chatselectionscreen$chatselectionlist$entry)) {
            boolean flag = this.getSelected() == chatselectionscreen$chatselectionlist$entry;
            int i = this.isFocused() && flag ? -1 : -8355712;
            this.renderSelection(pGuiGraphics, pTop, pWidth, pHeight, i, -16777216);
         }

         chatselectionscreen$chatselectionlist$entry.render(pGuiGraphics, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, this.getHovered() == chatselectionscreen$chatselectionlist$entry, pPartialTick);
      }

      private boolean shouldHighlightEntry(ChatSelectionScreen.ChatSelectionList.Entry pEntry) {
         if (pEntry.canSelect()) {
            boolean flag = this.getSelected() == pEntry;
            boolean flag1 = this.getSelected() == null;
            boolean flag2 = this.getHovered() == pEntry;
            return flag || flag1 && flag2 && pEntry.canReport();
         } else {
            return false;
         }
      }

      @Nullable
      protected ChatSelectionScreen.ChatSelectionList.Entry nextEntry(ScreenDirection pDirection) {
         return this.nextEntry(pDirection, ChatSelectionScreen.ChatSelectionList.Entry::canSelect);
      }

      public void setSelected(@Nullable ChatSelectionScreen.ChatSelectionList.Entry pSelected) {
         super.setSelected(pSelected);
         ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen$chatselectionlist$entry = this.nextEntry(ScreenDirection.UP);
         if (chatselectionscreen$chatselectionlist$entry == null) {
            ChatSelectionScreen.this.onReachedScrollTop();
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
         ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen$chatselectionlist$entry = this.getSelected();
         return chatselectionscreen$chatselectionlist$entry != null && chatselectionscreen$chatselectionlist$entry.keyPressed(pKeyCode, pScanCode, pModifiers) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }

      public int getFooterTop() {
         return this.y1 + 9;
      }

      @OnlyIn(Dist.CLIENT)
      public class DividerEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
         private static final int COLOR = -6250336;
         private final Component text;

         public DividerEntry(Component pText) {
            this.text = pText;
         }

         public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            int i = pTop + pHeight / 2;
            int j = pLeft + pWidth - 8;
            int k = ChatSelectionScreen.this.font.width(this.text);
            int l = (pLeft + j - k) / 2;
            int i1 = i - 9 / 2;
            pGuiGraphics.drawString(ChatSelectionScreen.this.font, this.text, l, i1, -6250336);
         }

         public Component getNarration() {
            return this.text;
         }
      }

      @OnlyIn(Dist.CLIENT)
      public abstract class Entry extends ObjectSelectionList.Entry<ChatSelectionScreen.ChatSelectionList.Entry> {
         public Component getNarration() {
            return CommonComponents.EMPTY;
         }

         public boolean isSelected() {
            return false;
         }

         public boolean canSelect() {
            return false;
         }

         public boolean canReport() {
            return this.canSelect();
         }
      }

      @OnlyIn(Dist.CLIENT)
      static record Heading(UUID sender, ChatSelectionScreen.ChatSelectionList.Entry entry) {
         public boolean canCombine(ChatSelectionScreen.ChatSelectionList.Heading pOther) {
            return pOther.sender.equals(this.sender);
         }
      }

      @OnlyIn(Dist.CLIENT)
      public class MessageEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
         private static final ResourceLocation CHECKMARK_TEXTURE = new ResourceLocation("minecraft", "textures/gui/checkmark.png");
         private static final int CHECKMARK_WIDTH = 9;
         private static final int CHECKMARK_HEIGHT = 8;
         private static final int INDENT_AMOUNT = 11;
         private static final int TAG_MARGIN_LEFT = 4;
         private final int chatId;
         private final FormattedText text;
         private final Component narration;
         @Nullable
         private final List<FormattedCharSequence> hoverText;
         @Nullable
         private final GuiMessageTag.Icon tagIcon;
         @Nullable
         private final List<FormattedCharSequence> tagHoverText;
         private final boolean canReport;
         private final boolean playerMessage;

         public MessageEntry(int pChatId, Component pText, Component pNarration, @Nullable GuiMessageTag pTagIcon, boolean pCanReport, boolean pPlayerMessage) {
            this.chatId = pChatId;
            this.tagIcon = Optionull.map(pTagIcon, GuiMessageTag::icon);
            this.tagHoverText = pTagIcon != null && pTagIcon.text() != null ? ChatSelectionScreen.this.font.split(pTagIcon.text(), ChatSelectionList.this.getRowWidth()) : null;
            this.canReport = pCanReport;
            this.playerMessage = pPlayerMessage;
            FormattedText formattedtext = ChatSelectionScreen.this.font.substrByWidth(pText, this.getMaximumTextWidth() - ChatSelectionScreen.this.font.width(CommonComponents.ELLIPSIS));
            if (pText != formattedtext) {
               this.text = FormattedText.composite(formattedtext, CommonComponents.ELLIPSIS);
               this.hoverText = ChatSelectionScreen.this.font.split(pText, ChatSelectionList.this.getRowWidth());
            } else {
               this.text = pText;
               this.hoverText = null;
            }

            this.narration = pNarration;
         }

         public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            if (this.isSelected() && this.canReport) {
               this.renderSelectedCheckmark(pGuiGraphics, pTop, pLeft, pHeight);
            }

            int i = pLeft + this.getTextIndent();
            int j = pTop + 1 + (pHeight - 9) / 2;
            pGuiGraphics.drawString(ChatSelectionScreen.this.font, Language.getInstance().getVisualOrder(this.text), i, j, this.canReport ? -1 : -1593835521);
            if (this.hoverText != null && pHovering) {
               ChatSelectionScreen.this.setTooltipForNextRenderPass(this.hoverText);
            }

            int k = ChatSelectionScreen.this.font.width(this.text);
            this.renderTag(pGuiGraphics, i + k + 4, pTop, pHeight, pMouseX, pMouseY);
         }

         private void renderTag(GuiGraphics pGuiGraphics, int pX, int pY, int pHeight, int pMouseX, int pMouseY) {
            if (this.tagIcon != null) {
               int i = pY + (pHeight - this.tagIcon.height) / 2;
               this.tagIcon.draw(pGuiGraphics, pX, i);
               if (this.tagHoverText != null && pMouseX >= pX && pMouseX <= pX + this.tagIcon.width && pMouseY >= i && pMouseY <= i + this.tagIcon.height) {
                  ChatSelectionScreen.this.setTooltipForNextRenderPass(this.tagHoverText);
               }
            }

         }

         private void renderSelectedCheckmark(GuiGraphics pGuiGraphics, int pTop, int pLeft, int pHeight) {
            int i = pTop + (pHeight - 8) / 2;
            RenderSystem.enableBlend();
            pGuiGraphics.blit(CHECKMARK_TEXTURE, pLeft, i, 0.0F, 0.0F, 9, 8, 9, 8);
            RenderSystem.disableBlend();
         }

         private int getMaximumTextWidth() {
            int i = this.tagIcon != null ? this.tagIcon.width + 4 : 0;
            return ChatSelectionList.this.getRowWidth() - this.getTextIndent() - 4 - i;
         }

         private int getTextIndent() {
            return this.playerMessage ? 11 : 0;
         }

         public Component getNarration() {
            return (Component)(this.isSelected() ? Component.translatable("narrator.select", this.narration) : this.narration);
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
            if (pButton == 0) {
               ChatSelectionList.this.setSelected((ChatSelectionScreen.ChatSelectionList.Entry)null);
               return this.toggleReport();
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
            return CommonInputs.selected(pKeyCode) ? this.toggleReport() : false;
         }

         public boolean isSelected() {
            return ChatSelectionScreen.this.report.isReported(this.chatId);
         }

         public boolean canSelect() {
            return true;
         }

         public boolean canReport() {
            return this.canReport;
         }

         private boolean toggleReport() {
            if (this.canReport) {
               ChatSelectionScreen.this.report.toggleReported(this.chatId);
               ChatSelectionScreen.this.updateConfirmSelectedButton();
               return true;
            } else {
               return false;
            }
         }
      }

      @OnlyIn(Dist.CLIENT)
      public class MessageHeadingEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
         private static final int FACE_SIZE = 12;
         private final Component heading;
         private final ResourceLocation skin;
         private final boolean canReport;

         public MessageHeadingEntry(GameProfile pProfile, Component pHeading, boolean pCanReport) {
            this.heading = pHeading;
            this.canReport = pCanReport;
            this.skin = ChatSelectionList.this.minecraft.getSkinManager().getInsecureSkinLocation(pProfile);
         }

         public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            int i = pLeft - 12 - 4;
            int j = pTop + (pHeight - 12) / 2;
            PlayerFaceRenderer.draw(pGuiGraphics, this.skin, i, j, 12);
            int k = pTop + 1 + (pHeight - 9) / 2;
            pGuiGraphics.drawString(ChatSelectionScreen.this.font, this.heading, pLeft, k, this.canReport ? -1 : -1593835521);
         }
      }

      @OnlyIn(Dist.CLIENT)
      public class PaddingEntry extends ChatSelectionScreen.ChatSelectionList.Entry {
         public void render(GuiGraphics p_282007_, int p_240110_, int p_240111_, int p_240112_, int p_240113_, int p_240114_, int p_240115_, int p_240116_, boolean p_240117_, float p_240118_) {
         }
      }
   }
}