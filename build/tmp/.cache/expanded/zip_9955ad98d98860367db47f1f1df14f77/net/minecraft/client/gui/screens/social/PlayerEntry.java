package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerEntry extends ContainerObjectSelectionList.Entry<PlayerEntry> {
   private static final ResourceLocation REPORT_BUTTON_LOCATION = new ResourceLocation("textures/gui/report_button.png");
   private static final int TOOLTIP_DELAY = 10;
   private final Minecraft minecraft;
   private final List<AbstractWidget> children;
   private final UUID id;
   private final String playerName;
   private final Supplier<ResourceLocation> skinGetter;
   private boolean isRemoved;
   private boolean hasRecentMessages;
   private final boolean reportingEnabled;
   private final boolean playerReportable;
   private final boolean hasDraftReport;
   @Nullable
   private Button hideButton;
   @Nullable
   private Button showButton;
   @Nullable
   private Button reportButton;
   private float tooltipHoverTime;
   private static final Component HIDDEN = Component.translatable("gui.socialInteractions.status_hidden").withStyle(ChatFormatting.ITALIC);
   private static final Component BLOCKED = Component.translatable("gui.socialInteractions.status_blocked").withStyle(ChatFormatting.ITALIC);
   private static final Component OFFLINE = Component.translatable("gui.socialInteractions.status_offline").withStyle(ChatFormatting.ITALIC);
   private static final Component HIDDEN_OFFLINE = Component.translatable("gui.socialInteractions.status_hidden_offline").withStyle(ChatFormatting.ITALIC);
   private static final Component BLOCKED_OFFLINE = Component.translatable("gui.socialInteractions.status_blocked_offline").withStyle(ChatFormatting.ITALIC);
   private static final Component REPORT_DISABLED_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report.disabled");
   private static final Component NOT_REPORTABLE_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report.not_reportable");
   private static final Component HIDE_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.hide");
   private static final Component SHOW_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.show");
   private static final Component REPORT_PLAYER_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report");
   private static final int SKIN_SIZE = 24;
   private static final int PADDING = 4;
   private static final int CHAT_TOGGLE_ICON_SIZE = 20;
   private static final int CHAT_TOGGLE_ICON_X = 0;
   private static final int CHAT_TOGGLE_ICON_Y = 38;
   public static final int SKIN_SHADE = FastColor.ARGB32.color(190, 0, 0, 0);
   public static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
   public static final int BG_FILL_REMOVED = FastColor.ARGB32.color(255, 48, 48, 48);
   public static final int PLAYERNAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);
   public static final int PLAYER_STATUS_COLOR = FastColor.ARGB32.color(140, 255, 255, 255);

   public PlayerEntry(Minecraft pMinecraft, SocialInteractionsScreen pSocialInteractionsScreen, UUID pId, String pPlayerName, Supplier<ResourceLocation> pSkinGetter, boolean pPlayerReportable) {
      this.minecraft = pMinecraft;
      this.id = pId;
      this.playerName = pPlayerName;
      this.skinGetter = pSkinGetter;
      ReportingContext reportingcontext = pMinecraft.getReportingContext();
      this.reportingEnabled = reportingcontext.sender().isEnabled();
      this.playerReportable = pPlayerReportable;
      this.hasDraftReport = reportingcontext.hasDraftReportFor(pId);
      Component component = Component.translatable("gui.socialInteractions.narration.hide", pPlayerName);
      Component component1 = Component.translatable("gui.socialInteractions.narration.show", pPlayerName);
      PlayerSocialManager playersocialmanager = pMinecraft.getPlayerSocialManager();
      boolean flag = pMinecraft.getChatStatus().isChatAllowed(pMinecraft.isLocalServer());
      boolean flag1 = !pMinecraft.player.getUUID().equals(pId);
      if (flag1 && flag && !playersocialmanager.isBlocked(pId)) {
         this.reportButton = new ImageButton(0, 0, 20, 20, 0, 0, 20, REPORT_BUTTON_LOCATION, 64, 64, (p_238875_) -> {
            reportingcontext.draftReportHandled(pMinecraft, pSocialInteractionsScreen, () -> {
               pMinecraft.setScreen(new ChatReportScreen(pSocialInteractionsScreen, reportingcontext, pId));
            }, false);
         }, Component.translatable("gui.socialInteractions.report")) {
            protected MutableComponent createNarrationMessage() {
               return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
            }
         };
         this.reportButton.setTooltip(this.createReportButtonTooltip());
         this.reportButton.setTooltipDelay(10);
         this.hideButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, (p_100612_) -> {
            playersocialmanager.hidePlayer(pId);
            this.onHiddenOrShown(true, Component.translatable("gui.socialInteractions.hidden_in_chat", pPlayerName));
         }, Component.translatable("gui.socialInteractions.hide")) {
            protected MutableComponent createNarrationMessage() {
               return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
            }
         };
         this.hideButton.setTooltip(Tooltip.create(HIDE_TEXT_TOOLTIP, component));
         this.hideButton.setTooltipDelay(10);
         this.showButton = new ImageButton(0, 0, 20, 20, 20, 38, 20, SocialInteractionsScreen.SOCIAL_INTERACTIONS_LOCATION, 256, 256, (p_170074_) -> {
            playersocialmanager.showPlayer(pId);
            this.onHiddenOrShown(false, Component.translatable("gui.socialInteractions.shown_in_chat", pPlayerName));
         }, Component.translatable("gui.socialInteractions.show")) {
            protected MutableComponent createNarrationMessage() {
               return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
            }
         };
         this.showButton.setTooltip(Tooltip.create(SHOW_TEXT_TOOLTIP, component1));
         this.showButton.setTooltipDelay(10);
         this.reportButton.active = false;
         this.children = new ArrayList<>();
         this.children.add(this.hideButton);
         this.children.add(this.reportButton);
         this.updateHideAndShowButton(playersocialmanager.isHidden(this.id));
      } else {
         this.children = ImmutableList.of();
      }

   }

   private Tooltip createReportButtonTooltip() {
      if (!this.playerReportable) {
         return Tooltip.create(NOT_REPORTABLE_TOOLTIP);
      } else if (!this.reportingEnabled) {
         return Tooltip.create(REPORT_DISABLED_TOOLTIP);
      } else {
         return !this.hasRecentMessages ? Tooltip.create(Component.translatable("gui.socialInteractions.tooltip.report.no_messages", this.playerName)) : Tooltip.create(REPORT_PLAYER_TOOLTIP, Component.translatable("gui.socialInteractions.narration.report", this.playerName));
      }
   }

   public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
      int i = pLeft + 4;
      int j = pTop + (pHeight - 24) / 2;
      int k = i + 24 + 4;
      Component component = this.getStatusComponent();
      int l;
      if (component == CommonComponents.EMPTY) {
         pGuiGraphics.fill(pLeft, pTop, pLeft + pWidth, pTop + pHeight, BG_FILL);
         l = pTop + (pHeight - 9) / 2;
      } else {
         pGuiGraphics.fill(pLeft, pTop, pLeft + pWidth, pTop + pHeight, BG_FILL_REMOVED);
         l = pTop + (pHeight - (9 + 9)) / 2;
         pGuiGraphics.drawString(this.minecraft.font, component, k, l + 12, PLAYER_STATUS_COLOR, false);
      }

      PlayerFaceRenderer.draw(pGuiGraphics, this.skinGetter.get(), i, j, 24);
      pGuiGraphics.drawString(this.minecraft.font, this.playerName, k, l, PLAYERNAME_COLOR, false);
      if (this.isRemoved) {
         pGuiGraphics.fill(i, j, i + 24, j + 24, SKIN_SHADE);
      }

      if (this.hideButton != null && this.showButton != null && this.reportButton != null) {
         float f = this.tooltipHoverTime;
         this.hideButton.setX(pLeft + (pWidth - this.hideButton.getWidth() - 4) - 20 - 4);
         this.hideButton.setY(pTop + (pHeight - this.hideButton.getHeight()) / 2);
         this.hideButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         this.showButton.setX(pLeft + (pWidth - this.showButton.getWidth() - 4) - 20 - 4);
         this.showButton.setY(pTop + (pHeight - this.showButton.getHeight()) / 2);
         this.showButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         this.reportButton.setX(pLeft + (pWidth - this.showButton.getWidth() - 4));
         this.reportButton.setY(pTop + (pHeight - this.showButton.getHeight()) / 2);
         this.reportButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         if (f == this.tooltipHoverTime) {
            this.tooltipHoverTime = 0.0F;
         }
      }

      if (this.hasDraftReport && this.reportButton != null) {
         pGuiGraphics.blit(AbstractWidget.WIDGETS_LOCATION, this.reportButton.getX() + 5, this.reportButton.getY() + 1, 182.0F, 24.0F, 15, 15, 256, 256);
      }

   }

   /**
    * {@return a List containing all GUI element children of this GUI element}
    */
   public List<? extends GuiEventListener> children() {
      return this.children;
   }

   public List<? extends NarratableEntry> narratables() {
      return this.children;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public UUID getPlayerId() {
      return this.id;
   }

   public void setRemoved(boolean pIsRemoved) {
      this.isRemoved = pIsRemoved;
   }

   public boolean isRemoved() {
      return this.isRemoved;
   }

   public void setHasRecentMessages(boolean pHasRecentMessages) {
      this.hasRecentMessages = pHasRecentMessages;
      if (this.reportButton != null) {
         this.reportButton.active = this.reportingEnabled && this.playerReportable && pHasRecentMessages;
         this.reportButton.setTooltip(this.createReportButtonTooltip());
      }

   }

   public boolean hasRecentMessages() {
      return this.hasRecentMessages;
   }

   private void onHiddenOrShown(boolean pVisible, Component pMessage) {
      this.updateHideAndShowButton(pVisible);
      this.minecraft.gui.getChat().addMessage(pMessage);
      this.minecraft.getNarrator().sayNow(pMessage);
   }

   private void updateHideAndShowButton(boolean pVisible) {
      this.showButton.visible = pVisible;
      this.hideButton.visible = !pVisible;
      this.children.set(0, pVisible ? this.showButton : this.hideButton);
   }

   MutableComponent getEntryNarationMessage(MutableComponent pComponent) {
      Component component = this.getStatusComponent();
      return component == CommonComponents.EMPTY ? Component.literal(this.playerName).append(", ").append(pComponent) : Component.literal(this.playerName).append(", ").append(component).append(", ").append(pComponent);
   }

   private Component getStatusComponent() {
      boolean flag = this.minecraft.getPlayerSocialManager().isHidden(this.id);
      boolean flag1 = this.minecraft.getPlayerSocialManager().isBlocked(this.id);
      if (flag1 && this.isRemoved) {
         return BLOCKED_OFFLINE;
      } else if (flag && this.isRemoved) {
         return HIDDEN_OFFLINE;
      } else if (flag1) {
         return BLOCKED;
      } else if (flag) {
         return HIDDEN;
      } else {
         return this.isRemoved ? OFFLINE : CommonComponents.EMPTY;
      }
   }
}