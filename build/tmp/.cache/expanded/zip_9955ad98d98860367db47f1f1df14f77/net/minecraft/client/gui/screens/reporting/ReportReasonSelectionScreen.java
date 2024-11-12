package net.minecraft.client.gui.screens.reporting;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReportReasonSelectionScreen extends Screen {
   private static final Component REASON_TITLE = Component.translatable("gui.abuseReport.reason.title");
   private static final Component REASON_DESCRIPTION = Component.translatable("gui.abuseReport.reason.description");
   private static final Component READ_INFO_LABEL = Component.translatable("gui.chatReport.read_info");
   private static final int FOOTER_HEIGHT = 95;
   private static final int BUTTON_WIDTH = 150;
   private static final int BUTTON_HEIGHT = 20;
   private static final int CONTENT_WIDTH = 320;
   private static final int PADDING = 4;
   @Nullable
   private final Screen lastScreen;
   @Nullable
   private ReportReasonSelectionScreen.ReasonSelectionList reasonSelectionList;
   @Nullable
   ReportReason currentlySelectedReason;
   private final Consumer<ReportReason> onSelectedReason;

   public ReportReasonSelectionScreen(@Nullable Screen pLastScreen, @Nullable ReportReason pCurrentlySelectedReason, Consumer<ReportReason> pOnSelectedReason) {
      super(REASON_TITLE);
      this.lastScreen = pLastScreen;
      this.currentlySelectedReason = pCurrentlySelectedReason;
      this.onSelectedReason = pOnSelectedReason;
   }

   protected void init() {
      this.reasonSelectionList = new ReportReasonSelectionScreen.ReasonSelectionList(this.minecraft);
      this.reasonSelectionList.setRenderBackground(false);
      this.addWidget(this.reasonSelectionList);
      ReportReasonSelectionScreen.ReasonSelectionList.Entry reportreasonselectionscreen$reasonselectionlist$entry = Optionull.map(this.currentlySelectedReason, this.reasonSelectionList::findEntry);
      this.reasonSelectionList.setSelected(reportreasonselectionscreen$reasonselectionlist$entry);
      int i = this.width / 2 - 150 - 5;
      this.addRenderableWidget(Button.builder(READ_INFO_LABEL, (p_280887_) -> {
         this.minecraft.setScreen(new ConfirmLinkScreen((p_280888_) -> {
            if (p_280888_) {
               Util.getPlatform().openUri("https://aka.ms/aboutjavareporting");
            }

            this.minecraft.setScreen(this);
         }, "https://aka.ms/aboutjavareporting", true));
      }).bounds(i, this.buttonTop(), 150, 20).build());
      int j = this.width / 2 + 5;
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280889_) -> {
         ReportReasonSelectionScreen.ReasonSelectionList.Entry reportreasonselectionscreen$reasonselectionlist$entry1 = this.reasonSelectionList.getSelected();
         if (reportreasonselectionscreen$reasonselectionlist$entry1 != null) {
            this.onSelectedReason.accept(reportreasonselectionscreen$reasonselectionlist$entry1.getReason());
         }

         this.minecraft.setScreen(this.lastScreen);
      }).bounds(j, this.buttonTop(), 150, 20).build());
      super.init();
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
      this.reasonSelectionList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.fill(this.contentLeft(), this.descriptionTop(), this.contentRight(), this.descriptionBottom(), 2130706432);
      pGuiGraphics.drawString(this.font, REASON_DESCRIPTION, this.contentLeft() + 4, this.descriptionTop() + 4, -8421505);
      ReportReasonSelectionScreen.ReasonSelectionList.Entry reportreasonselectionscreen$reasonselectionlist$entry = this.reasonSelectionList.getSelected();
      if (reportreasonselectionscreen$reasonselectionlist$entry != null) {
         int i = this.contentLeft() + 4 + 16;
         int j = this.contentRight() - 4;
         int k = this.descriptionTop() + 4 + 9 + 2;
         int l = this.descriptionBottom() - 4;
         int i1 = j - i;
         int j1 = l - k;
         int k1 = this.font.wordWrapHeight(reportreasonselectionscreen$reasonselectionlist$entry.reason.description(), i1);
         pGuiGraphics.drawWordWrap(this.font, reportreasonselectionscreen$reasonselectionlist$entry.reason.description(), i, k + (j1 - k1) / 2, i1, -1);
      }

   }

   private int buttonTop() {
      return this.height - 20 - 4;
   }

   private int contentLeft() {
      return (this.width - 320) / 2;
   }

   private int contentRight() {
      return (this.width + 320) / 2;
   }

   private int descriptionTop() {
      return this.height - 95 + 4;
   }

   private int descriptionBottom() {
      return this.buttonTop() - 4;
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   @OnlyIn(Dist.CLIENT)
   public class ReasonSelectionList extends ObjectSelectionList<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
      public ReasonSelectionList(Minecraft pMinecraft) {
         super(pMinecraft, ReportReasonSelectionScreen.this.width, ReportReasonSelectionScreen.this.height, 40, ReportReasonSelectionScreen.this.height - 95, 18);

         for(ReportReason reportreason : ReportReason.values()) {
            this.addEntry(new ReportReasonSelectionScreen.ReasonSelectionList.Entry(reportreason));
         }

      }

      @Nullable
      public ReportReasonSelectionScreen.ReasonSelectionList.Entry findEntry(ReportReason pReason) {
         return this.children().stream().filter((p_239293_) -> {
            return p_239293_.reason == pReason;
         }).findFirst().orElse((ReportReasonSelectionScreen.ReasonSelectionList.Entry)null);
      }

      public int getRowWidth() {
         return 320;
      }

      protected int getScrollbarPosition() {
         return this.getRowRight() - 2;
      }

      public void setSelected(@Nullable ReportReasonSelectionScreen.ReasonSelectionList.Entry pSelected) {
         super.setSelected(pSelected);
         ReportReasonSelectionScreen.this.currentlySelectedReason = pSelected != null ? pSelected.getReason() : null;
      }

      @OnlyIn(Dist.CLIENT)
      public class Entry extends ObjectSelectionList.Entry<ReportReasonSelectionScreen.ReasonSelectionList.Entry> {
         final ReportReason reason;

         public Entry(ReportReason pReason) {
            this.reason = pReason;
         }

         public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            int i = pLeft + 1;
            int j = pTop + (pHeight - 9) / 2 + 1;
            pGuiGraphics.drawString(ReportReasonSelectionScreen.this.font, this.reason.title(), i, j, -1);
         }

         public Component getNarration() {
            return Component.translatable("gui.abuseReport.reason.narration", this.reason.title(), this.reason.description());
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
               ReasonSelectionList.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }

         public ReportReason getReason() {
            return this.reason;
         }
      }
   }
}