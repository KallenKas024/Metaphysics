package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RealmsNewsManager;
import com.mojang.realmsclient.gui.RealmsServerList;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.gui.task.DataFetcher;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
   private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
   private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
   private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
   static final ResourceLocation INVITATION_ICONS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invitation_icons.png");
   static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
   static final ResourceLocation WORLDICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/world_icon.png");
   private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("realms", "textures/gui/title/realms.png");
   private static final ResourceLocation NEWS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_icon.png");
   private static final ResourceLocation POPUP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/popup.png");
   private static final ResourceLocation DARKEN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/darken.png");
   static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_icon.png");
   private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
   static final ResourceLocation INFO_ICON_LOCATION = new ResourceLocation("minecraft", "textures/gui/info_icon.png");
   static final List<Component> TRIAL_MESSAGE_LINES = ImmutableList.of(Component.translatable("mco.trial.message.line1"), Component.translatable("mco.trial.message.line2"));
   static final Component SERVER_UNITIALIZED_TEXT = Component.translatable("mco.selectServer.uninitialized");
   static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredList");
   private static final Component SUBSCRIPTION_RENEW_TEXT = Component.translatable("mco.selectServer.expiredRenew");
   static final Component TRIAL_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredTrial");
   static final Component SELECT_MINIGAME_PREFIX = Component.translatable("mco.selectServer.minigame").append(CommonComponents.SPACE);
   private static final Component POPUP_TEXT = Component.translatable("mco.selectServer.popup");
   private static final Component PLAY_TEXT = Component.translatable("mco.selectServer.play");
   private static final Component LEAVE_SERVER_TEXT = Component.translatable("mco.selectServer.leave");
   private static final Component CONFIGURE_SERVER_TEXT = Component.translatable("mco.selectServer.configure");
   private static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
   private static final Component SERVER_EXPIRES_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
   private static final Component SERVER_EXPIRES_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
   private static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
   private static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
   private static final Component NEWS_TOOLTIP = Component.translatable("mco.news");
   static final Component UNITIALIZED_WORLD_NARRATION = Component.translatable("gui.narrate.button", SERVER_UNITIALIZED_TEXT);
   static final Component TRIAL_TEXT = CommonComponents.joinLines(TRIAL_MESSAGE_LINES);
   private static final int BUTTON_WIDTH = 100;
   private static final int BUTTON_TOP_ROW_WIDTH = 308;
   private static final int BUTTON_BOTTOM_ROW_WIDTH = 204;
   private static final int FOOTER_HEIGHT = 64;
   private static final int LOGO_WIDTH = 128;
   private static final int LOGO_HEIGHT = 34;
   private static final int LOGO_TEXTURE_WIDTH = 128;
   private static final int LOGO_TEXTURE_HEIGHT = 64;
   private static final int LOGO_PADDING = 5;
   private static final int HEADER_HEIGHT = 44;
   private static List<ResourceLocation> teaserImages = ImmutableList.of();
   @Nullable
   private DataFetcher.Subscription dataSubscription;
   private RealmsServerList serverList;
   private final Set<UUID> handledSeenNotifications = new HashSet<>();
   private static boolean overrideConfigure;
   private static int lastScrollYPosition = -1;
   static volatile boolean hasParentalConsent;
   static volatile boolean checkedParentalConsent;
   static volatile boolean checkedClientCompatability;
   @Nullable
   static Screen realmsGenericErrorScreen;
   private static boolean regionsPinged;
   private final RateLimiter inviteNarrationLimiter;
   private boolean dontSetConnectedToRealms;
   final Screen lastScreen;
   RealmsMainScreen.RealmSelectionList realmSelectionList;
   private boolean realmsSelectionListAdded;
   private Button playButton;
   private Button backButton;
   private Button renewButton;
   private Button configureButton;
   private Button leaveButton;
   private List<RealmsServer> realmsServers = ImmutableList.of();
   volatile int numberOfPendingInvites;
   int animTick;
   private boolean hasFetchedServers;
   boolean popupOpenedByUser;
   private boolean justClosedPopup;
   private volatile boolean trialsAvailable;
   private volatile boolean createdTrial;
   private volatile boolean showingPopup;
   volatile boolean hasUnreadNews;
   @Nullable
   volatile String newsLink;
   private int carouselIndex;
   private int carouselTick;
   private boolean hasSwitchedCarouselImage;
   private List<KeyCombo> keyCombos;
   long lastClickTime;
   private ReentrantLock connectLock = new ReentrantLock();
   private MultiLineLabel formattedPopup = MultiLineLabel.EMPTY;
   private final List<RealmsNotification> notifications = new ArrayList<>();
   private Button showPopupButton;
   private RealmsMainScreen.PendingInvitesButton pendingInvitesButton;
   private Button newsButton;
   private Button createTrialButton;
   private Button buyARealmButton;
   private Button closeButton;

   public RealmsMainScreen(Screen pLastScreen) {
      super(GameNarrator.NO_TITLE);
      this.lastScreen = pLastScreen;
      this.inviteNarrationLimiter = RateLimiter.create((double)0.016666668F);
   }

   private boolean shouldShowMessageInList() {
      if (hasParentalConsent() && this.hasFetchedServers) {
         if (this.trialsAvailable && !this.createdTrial) {
            return true;
         } else {
            for(RealmsServer realmsserver : this.realmsServers) {
               if (realmsserver.ownerUUID.equals(this.minecraft.getUser().getUuid())) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean shouldShowPopup() {
      if (hasParentalConsent() && this.hasFetchedServers) {
         return this.popupOpenedByUser ? true : this.realmsServers.isEmpty();
      } else {
         return false;
      }
   }

   public void init() {
      this.keyCombos = Lists.newArrayList(new KeyCombo(new char[]{'3', '2', '1', '4', '5', '6'}, () -> {
         overrideConfigure = !overrideConfigure;
      }), new KeyCombo(new char[]{'9', '8', '7', '1', '2', '3'}, () -> {
         if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
            this.switchToProd();
         } else {
            this.switchToStage();
         }

      }), new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
         if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
            this.switchToProd();
         } else {
            this.switchToLocal();
         }

      }));
      if (realmsGenericErrorScreen != null) {
         this.minecraft.setScreen(realmsGenericErrorScreen);
      } else {
         this.connectLock = new ReentrantLock();
         if (checkedClientCompatability && !hasParentalConsent()) {
            this.checkParentalConsent();
         }

         this.checkClientCompatability();
         if (!this.dontSetConnectedToRealms) {
            this.minecraft.setConnectedToRealms(false);
         }

         this.showingPopup = false;
         this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
         if (lastScrollYPosition != -1) {
            this.realmSelectionList.setScrollAmount((double)lastScrollYPosition);
         }

         this.addWidget(this.realmSelectionList);
         this.realmsSelectionListAdded = true;
         this.setInitialFocus(this.realmSelectionList);
         this.addMiddleButtons();
         this.addFooterButtons();
         this.addTopButtons();
         this.updateButtonStates((RealmsServer)null);
         this.formattedPopup = MultiLineLabel.create(this.font, POPUP_TEXT, 100);
         RealmsNewsManager realmsnewsmanager = this.minecraft.realmsDataFetcher().newsManager;
         this.hasUnreadNews = realmsnewsmanager.hasUnreadNews();
         this.newsLink = realmsnewsmanager.newsLink();
         if (this.serverList == null) {
            this.serverList = new RealmsServerList(this.minecraft);
         }

         if (this.dataSubscription != null) {
            this.dataSubscription.forceUpdate();
         }

      }
   }

   private static boolean hasParentalConsent() {
      return checkedParentalConsent && hasParentalConsent;
   }

   public void addTopButtons() {
      this.pendingInvitesButton = this.addRenderableWidget(new RealmsMainScreen.PendingInvitesButton());
      this.newsButton = this.addRenderableWidget(new RealmsMainScreen.NewsButton());
      this.showPopupButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.selectServer.purchase"), (p_86597_) -> {
         this.popupOpenedByUser = !this.popupOpenedByUser;
      }).bounds(this.width - 90, 12, 80, 20).build());
   }

   public void addMiddleButtons() {
      this.createTrialButton = this.addWidget(Button.builder(Component.translatable("mco.selectServer.trial"), (p_280681_) -> {
         if (this.trialsAvailable && !this.createdTrial) {
            Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
            this.minecraft.setScreen(this.lastScreen);
         }
      }).bounds(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20).build());
      this.buyARealmButton = this.addWidget(Button.builder(Component.translatable("mco.selectServer.buy"), (p_231255_) -> {
         Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms");
      }).bounds(this.width / 2 + 52, this.popupY0() + 160 - 20, 98, 20).build());
      this.closeButton = this.addWidget(new RealmsMainScreen.CloseButton());
   }

   public void addFooterButtons() {
      this.playButton = Button.builder(PLAY_TEXT, (p_86659_) -> {
         this.play(this.getSelectedServer(), this);
      }).width(100).build();
      this.configureButton = Button.builder(CONFIGURE_SERVER_TEXT, (p_86672_) -> {
         this.configureClicked(this.getSelectedServer());
      }).width(100).build();
      this.renewButton = Button.builder(SUBSCRIPTION_RENEW_TEXT, (p_86622_) -> {
         this.onRenew(this.getSelectedServer());
      }).width(100).build();
      this.leaveButton = Button.builder(LEAVE_SERVER_TEXT, (p_86679_) -> {
         this.leaveClicked(this.getSelectedServer());
      }).width(100).build();
      this.backButton = Button.builder(CommonComponents.GUI_BACK, (p_280683_) -> {
         if (!this.justClosedPopup) {
            this.minecraft.setScreen(this.lastScreen);
         }

      }).width(100).build();
      GridLayout gridlayout = new GridLayout();
      GridLayout.RowHelper gridlayout$rowhelper = gridlayout.createRowHelper(1);
      LinearLayout linearlayout = gridlayout$rowhelper.addChild(new LinearLayout(308, 20, LinearLayout.Orientation.HORIZONTAL), gridlayout$rowhelper.newCellSettings().paddingBottom(4));
      linearlayout.addChild(this.playButton);
      linearlayout.addChild(this.configureButton);
      linearlayout.addChild(this.renewButton);
      LinearLayout linearlayout1 = gridlayout$rowhelper.addChild(new LinearLayout(204, 20, LinearLayout.Orientation.HORIZONTAL), gridlayout$rowhelper.newCellSettings().alignHorizontallyCenter());
      linearlayout1.addChild(this.leaveButton);
      linearlayout1.addChild(this.backButton);
      gridlayout.visitWidgets((p_272289_) -> {
         AbstractWidget abstractwidget = this.addRenderableWidget(p_272289_);
      });
      gridlayout.arrangeElements();
      FrameLayout.centerInRectangle(gridlayout, 0, this.height - 64, this.width, 64);
   }

   void updateButtonStates(@Nullable RealmsServer pRealmsServer) {
      this.backButton.active = true;
      if (hasParentalConsent() && this.hasFetchedServers) {
         boolean flag = this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial;
         this.createTrialButton.visible = flag;
         this.createTrialButton.active = flag;
         this.buyARealmButton.visible = this.shouldShowPopup();
         this.closeButton.visible = this.shouldShowPopup();
         this.newsButton.active = true;
         this.newsButton.visible = this.newsLink != null;
         this.pendingInvitesButton.active = true;
         this.pendingInvitesButton.visible = true;
         this.showPopupButton.active = !this.shouldShowPopup();
         this.playButton.visible = !this.shouldShowPopup();
         this.renewButton.visible = !this.shouldShowPopup();
         this.leaveButton.visible = !this.shouldShowPopup();
         this.configureButton.visible = !this.shouldShowPopup();
         this.backButton.visible = !this.shouldShowPopup();
         this.playButton.active = this.shouldPlayButtonBeActive(pRealmsServer);
         this.renewButton.active = this.shouldRenewButtonBeActive(pRealmsServer);
         this.leaveButton.active = this.shouldLeaveButtonBeActive(pRealmsServer);
         this.configureButton.active = this.shouldConfigureButtonBeActive(pRealmsServer);
      } else {
         hideWidgets(new AbstractWidget[]{this.playButton, this.renewButton, this.configureButton, this.createTrialButton, this.buyARealmButton, this.closeButton, this.newsButton, this.pendingInvitesButton, this.showPopupButton, this.leaveButton});
      }
   }

   private boolean shouldShowPopupButton() {
      return (!this.shouldShowPopup() || this.popupOpenedByUser) && hasParentalConsent() && this.hasFetchedServers;
   }

   boolean shouldPlayButtonBeActive(@Nullable RealmsServer pRealmsServer) {
      return pRealmsServer != null && !pRealmsServer.expired && pRealmsServer.state == RealmsServer.State.OPEN;
   }

   private boolean shouldRenewButtonBeActive(@Nullable RealmsServer pRealmsServer) {
      return pRealmsServer != null && pRealmsServer.expired && this.isSelfOwnedServer(pRealmsServer);
   }

   private boolean shouldConfigureButtonBeActive(@Nullable RealmsServer pRealmsServer) {
      return pRealmsServer != null && this.isSelfOwnedServer(pRealmsServer);
   }

   private boolean shouldLeaveButtonBeActive(@Nullable RealmsServer pRealmsServer) {
      return pRealmsServer != null && !this.isSelfOwnedServer(pRealmsServer);
   }

   public void tick() {
      super.tick();
      if (this.pendingInvitesButton != null) {
         this.pendingInvitesButton.tick();
      }

      this.justClosedPopup = false;
      ++this.animTick;
      boolean flag = hasParentalConsent();
      if (this.dataSubscription == null && flag) {
         this.dataSubscription = this.initDataFetcher(this.minecraft.realmsDataFetcher());
      } else if (this.dataSubscription != null && !flag) {
         this.dataSubscription = null;
      }

      if (this.dataSubscription != null) {
         this.dataSubscription.tick();
      }

      if (this.shouldShowPopup()) {
         ++this.carouselTick;
      }

      if (this.showPopupButton != null) {
         this.showPopupButton.visible = this.shouldShowPopupButton();
         this.showPopupButton.active = this.showPopupButton.visible;
      }

   }

   private DataFetcher.Subscription initDataFetcher(RealmsDataFetcher pDataFetcher) {
      DataFetcher.Subscription datafetcher$subscription = pDataFetcher.dataFetcher.createSubscription();
      datafetcher$subscription.subscribe(pDataFetcher.serverListUpdateTask, (p_275856_) -> {
         List<RealmsServer> list = this.serverList.updateServersList(p_275856_);
         boolean flag = false;

         for(RealmsServer realmsserver : list) {
            if (this.isSelfOwnedNonExpiredServer(realmsserver)) {
               flag = true;
            }
         }

         this.realmsServers = list;
         this.hasFetchedServers = true;
         this.refreshRealmsSelectionList();
         if (!regionsPinged && flag) {
            regionsPinged = true;
            this.pingRegions();
         }

      });
      callRealmsClient(RealmsClient::getNotifications, (p_274622_) -> {
         this.notifications.clear();
         this.notifications.addAll(p_274622_);
         this.refreshRealmsSelectionList();
      });
      datafetcher$subscription.subscribe(pDataFetcher.pendingInvitesTask, (p_280682_) -> {
         this.numberOfPendingInvites = p_280682_;
         if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
            this.minecraft.getNarrator().sayNow(Component.translatable("mco.configure.world.invite.narration", this.numberOfPendingInvites));
         }

      });
      datafetcher$subscription.subscribe(pDataFetcher.trialAvailabilityTask, (p_238839_) -> {
         if (!this.createdTrial) {
            if (p_238839_ != this.trialsAvailable && this.shouldShowPopup()) {
               this.trialsAvailable = p_238839_;
               this.showingPopup = false;
            } else {
               this.trialsAvailable = p_238839_;
            }

         }
      });
      datafetcher$subscription.subscribe(pDataFetcher.liveStatsTask, (p_238847_) -> {
         for(RealmsServerPlayerList realmsserverplayerlist : p_238847_.servers) {
            for(RealmsServer realmsserver : this.realmsServers) {
               if (realmsserver.id == realmsserverplayerlist.serverId) {
                  realmsserver.updateServerPing(realmsserverplayerlist);
                  break;
               }
            }
         }

      });
      datafetcher$subscription.subscribe(pDataFetcher.newsTask, (p_231355_) -> {
         pDataFetcher.newsManager.updateUnreadNews(p_231355_);
         this.hasUnreadNews = pDataFetcher.newsManager.hasUnreadNews();
         this.newsLink = pDataFetcher.newsManager.newsLink();
         this.updateButtonStates((RealmsServer)null);
      });
      return datafetcher$subscription;
   }

   private static <T> void callRealmsClient(RealmsMainScreen.RealmsCall<T> pCall, Consumer<T> pOnFinish) {
      Minecraft minecraft = Minecraft.getInstance();
      CompletableFuture.supplyAsync(() -> {
         try {
            return pCall.request(RealmsClient.create(minecraft));
         } catch (RealmsServiceException realmsserviceexception) {
            throw new RuntimeException(realmsserviceexception);
         }
      }).thenAcceptAsync(pOnFinish, minecraft).exceptionally((p_274626_) -> {
         LOGGER.error("Failed to execute call to Realms Service", p_274626_);
         return null;
      });
   }

   private void refreshRealmsSelectionList() {
      boolean flag = !this.hasFetchedServers;
      this.realmSelectionList.clear();
      List<UUID> list = new ArrayList<>();

      for(RealmsNotification realmsnotification : this.notifications) {
         this.addEntriesForNotification(this.realmSelectionList, realmsnotification);
         if (!realmsnotification.seen() && !this.handledSeenNotifications.contains(realmsnotification.uuid())) {
            list.add(realmsnotification.uuid());
         }
      }

      if (!list.isEmpty()) {
         callRealmsClient((p_274625_) -> {
            p_274625_.notificationsSeen(list);
            return null;
         }, (p_274630_) -> {
            this.handledSeenNotifications.addAll(list);
         });
      }

      if (this.shouldShowMessageInList()) {
         this.realmSelectionList.addEntry(new RealmsMainScreen.TrialEntry());
      }

      RealmsMainScreen.Entry realmsmainscreen$entry = null;
      RealmsServer realmsserver1 = this.getSelectedServer();

      for(RealmsServer realmsserver : this.realmsServers) {
         RealmsMainScreen.ServerEntry realmsmainscreen$serverentry = new RealmsMainScreen.ServerEntry(realmsserver);
         this.realmSelectionList.addEntry(realmsmainscreen$serverentry);
         if (realmsserver1 != null && realmsserver1.id == realmsserver.id) {
            realmsmainscreen$entry = realmsmainscreen$serverentry;
         }
      }

      if (flag) {
         this.updateButtonStates((RealmsServer)null);
      } else {
         this.realmSelectionList.setSelected(realmsmainscreen$entry);
      }

   }

   private void addEntriesForNotification(RealmsMainScreen.RealmSelectionList pSelectionList, RealmsNotification pNotification) {
      if (pNotification instanceof RealmsNotification.VisitUrl realmsnotification$visiturl) {
         pSelectionList.addEntry(new RealmsMainScreen.NotificationMessageEntry(realmsnotification$visiturl.getMessage(), realmsnotification$visiturl));
         pSelectionList.addEntry(new RealmsMainScreen.ButtonEntry(realmsnotification$visiturl.buildOpenLinkButton(this)));
      }

   }

   void refreshFetcher() {
      if (this.dataSubscription != null) {
         this.dataSubscription.reset();
      }

   }

   private void pingRegions() {
      (new Thread(() -> {
         List<RegionPingResult> list = Ping.pingAllRegions();
         RealmsClient realmsclient = RealmsClient.create();
         PingResult pingresult = new PingResult();
         pingresult.pingResults = list;
         pingresult.worldIds = this.getOwnedNonExpiredWorldIds();

         try {
            realmsclient.sendPingResults(pingresult);
         } catch (Throwable throwable) {
            LOGGER.warn("Could not send ping result to Realms: ", throwable);
         }

      })).start();
   }

   private List<Long> getOwnedNonExpiredWorldIds() {
      List<Long> list = Lists.newArrayList();

      for(RealmsServer realmsserver : this.realmsServers) {
         if (this.isSelfOwnedNonExpiredServer(realmsserver)) {
            list.add(realmsserver.id);
         }
      }

      return list;
   }

   public void setCreatedTrial(boolean pCreatedTrial) {
      this.createdTrial = pCreatedTrial;
   }

   private void onRenew(@Nullable RealmsServer pRealmsServer) {
      if (pRealmsServer != null) {
         String s = CommonLinks.extendRealms(pRealmsServer.remoteSubscriptionId, this.minecraft.getUser().getUuid(), pRealmsServer.expiredTrial);
         this.minecraft.keyboardHandler.setClipboard(s);
         Util.getPlatform().openUri(s);
      }

   }

   private void checkClientCompatability() {
      if (!checkedClientCompatability) {
         checkedClientCompatability = true;
         (new Thread("MCO Compatability Checker #1") {
            public void run() {
               RealmsClient realmsclient = RealmsClient.create();

               try {
                  RealmsClient.CompatibleVersionResponse realmsclient$compatibleversionresponse = realmsclient.clientCompatible();
                  if (realmsclient$compatibleversionresponse != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen);
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                     return;
                  }

                  RealmsMainScreen.this.checkParentalConsent();
               } catch (RealmsServiceException realmsserviceexception) {
                  RealmsMainScreen.checkedClientCompatability = false;
                  RealmsMainScreen.LOGGER.error("Couldn't connect to realms", (Throwable)realmsserviceexception);
                  if (realmsserviceexception.httpResultCode == 401) {
                     RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(Component.translatable("mco.error.invalid.session.title"), Component.translatable("mco.error.invalid.session.message"), RealmsMainScreen.this.lastScreen);
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                     });
                  } else {
                     RealmsMainScreen.this.minecraft.execute(() -> {
                        RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsserviceexception, RealmsMainScreen.this.lastScreen));
                     });
                  }
               }

            }
         }).start();
      }

   }

   void checkParentalConsent() {
      (new Thread("MCO Compatability Checker #1") {
         public void run() {
            RealmsClient realmsclient = RealmsClient.create();

            try {
               Boolean obool = realmsclient.mcoEnabled();
               if (obool) {
                  RealmsMainScreen.LOGGER.info("Realms is available for this user");
                  RealmsMainScreen.hasParentalConsent = true;
               } else {
                  RealmsMainScreen.LOGGER.info("Realms is not available for this user");
                  RealmsMainScreen.hasParentalConsent = false;
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.minecraft.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen));
                  });
               }

               RealmsMainScreen.checkedParentalConsent = true;
            } catch (RealmsServiceException realmsserviceexception) {
               RealmsMainScreen.LOGGER.error("Couldn't connect to realms", (Throwable)realmsserviceexception);
               RealmsMainScreen.this.minecraft.execute(() -> {
                  RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsserviceexception, RealmsMainScreen.this.lastScreen));
               });
            }

         }
      }).start();
   }

   private void switchToStage() {
      if (RealmsClient.currentEnvironment != RealmsClient.Environment.STAGE) {
         (new Thread("MCO Stage Availability Checker #1") {
            public void run() {
               RealmsClient realmsclient = RealmsClient.create();

               try {
                  Boolean obool = realmsclient.stageAvailable();
                  if (obool) {
                     RealmsClient.switchToStage();
                     RealmsMainScreen.LOGGER.info("Switched to stage");
                     RealmsMainScreen.this.refreshFetcher();
                  }
               } catch (RealmsServiceException realmsserviceexception) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", (Object)realmsserviceexception.toString());
               }

            }
         }).start();
      }

   }

   private void switchToLocal() {
      if (RealmsClient.currentEnvironment != RealmsClient.Environment.LOCAL) {
         (new Thread("MCO Local Availability Checker #1") {
            public void run() {
               RealmsClient realmsclient = RealmsClient.create();

               try {
                  Boolean obool = realmsclient.stageAvailable();
                  if (obool) {
                     RealmsClient.switchToLocal();
                     RealmsMainScreen.LOGGER.info("Switched to local");
                     RealmsMainScreen.this.refreshFetcher();
                  }
               } catch (RealmsServiceException realmsserviceexception) {
                  RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", (Object)realmsserviceexception.toString());
               }

            }
         }).start();
      }

   }

   private void switchToProd() {
      RealmsClient.switchToProd();
      this.refreshFetcher();
   }

   private void configureClicked(@Nullable RealmsServer pRealmsServer) {
      if (pRealmsServer != null && (this.minecraft.getUser().getUuid().equals(pRealmsServer.ownerUUID) || overrideConfigure)) {
         this.saveListScrollPosition();
         this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, pRealmsServer.id));
      }

   }

   private void leaveClicked(@Nullable RealmsServer pRealmsServer) {
      if (pRealmsServer != null && !this.minecraft.getUser().getUuid().equals(pRealmsServer.ownerUUID)) {
         this.saveListScrollPosition();
         Component component = Component.translatable("mco.configure.world.leave.question.line1");
         Component component1 = Component.translatable("mco.configure.world.leave.question.line2");
         this.minecraft.setScreen(new RealmsLongConfirmationScreen((p_231253_) -> {
            this.leaveServer(p_231253_, pRealmsServer);
         }, RealmsLongConfirmationScreen.Type.INFO, component, component1, true));
      }

   }

   private void saveListScrollPosition() {
      lastScrollYPosition = (int)this.realmSelectionList.getScrollAmount();
   }

   @Nullable
   private RealmsServer getSelectedServer() {
      if (this.realmSelectionList == null) {
         return null;
      } else {
         RealmsMainScreen.Entry realmsmainscreen$entry = this.realmSelectionList.getSelected();
         return realmsmainscreen$entry != null ? realmsmainscreen$entry.getServer() : null;
      }
   }

   private void leaveServer(boolean pConfirmed, final RealmsServer pServer) {
      if (pConfirmed) {
         (new Thread("Realms-leave-server") {
            public void run() {
               try {
                  RealmsClient realmsclient = RealmsClient.create();
                  realmsclient.uninviteMyselfFrom(pServer.id);
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.removeServer(pServer);
                  });
               } catch (RealmsServiceException realmsserviceexception) {
                  RealmsMainScreen.LOGGER.error("Couldn't configure world");
                  RealmsMainScreen.this.minecraft.execute(() -> {
                     RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsserviceexception, RealmsMainScreen.this));
                  });
               }

            }
         }).start();
      }

      this.minecraft.setScreen(this);
   }

   void removeServer(RealmsServer pServer) {
      this.realmsServers = this.serverList.removeItem(pServer);
      this.realmSelectionList.children().removeIf((p_231250_) -> {
         RealmsServer realmsserver = p_231250_.getServer();
         return realmsserver != null && realmsserver.id == pServer.id;
      });
      this.realmSelectionList.setSelected((RealmsMainScreen.Entry)null);
      this.updateButtonStates((RealmsServer)null);
      this.playButton.active = false;
   }

   void dismissNotification(UUID pUuid) {
      callRealmsClient((p_274628_) -> {
         p_274628_.notificationsDismiss(List.of(pUuid));
         return null;
      }, (p_274632_) -> {
         this.notifications.removeIf((p_274621_) -> {
            return p_274621_.dismissable() && pUuid.equals(p_274621_.uuid());
         });
         this.refreshRealmsSelectionList();
      });
   }

   public void resetScreen() {
      if (this.realmSelectionList != null) {
         this.realmSelectionList.setSelected((RealmsMainScreen.Entry)null);
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
      if (pKeyCode == 256) {
         this.keyCombos.forEach(KeyCombo::reset);
         this.onClosePopup();
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   void onClosePopup() {
      if (this.shouldShowPopup() && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
      } else {
         this.minecraft.setScreen(this.lastScreen);
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
      this.keyCombos.forEach((p_231245_) -> {
         p_231245_.keyPressed(pCodePoint);
      });
      return true;
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
      this.realmSelectionList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.blit(LOGO_LOCATION, this.width / 2 - 64, 5, 0.0F, 0.0F, 128, 34, 128, 64);
      if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
         this.renderStage(pGuiGraphics);
      }

      if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
         this.renderLocal(pGuiGraphics);
      }

      if (this.shouldShowPopup()) {
         pGuiGraphics.pose().pushPose();
         pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
         this.drawPopup(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         pGuiGraphics.pose().popPose();
      } else {
         if (this.showingPopup) {
            this.updateButtonStates((RealmsServer)null);
            if (!this.realmsSelectionListAdded) {
               this.addWidget(this.realmSelectionList);
               this.realmsSelectionListAdded = true;
            }

            this.playButton.active = this.shouldPlayButtonBeActive(this.getSelectedServer());
         }

         this.showingPopup = false;
      }

      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
         int i = 8;
         int j = 8;
         int k = 0;
         if ((Util.getMillis() / 800L & 1L) == 1L) {
            k = 8;
         }

         pGuiGraphics.pose().pushPose();
         pGuiGraphics.pose().translate(0.0F, 0.0F, 110.0F);
         pGuiGraphics.blit(TRIAL_ICON_LOCATION, this.createTrialButton.getX() + this.createTrialButton.getWidth() - 8 - 4, this.createTrialButton.getY() + this.createTrialButton.getHeight() / 2 - 4, 0.0F, (float)k, 8, 8, 8, 16);
         pGuiGraphics.pose().popPose();
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
      if (this.isOutsidePopup(pMouseX, pMouseY) && this.popupOpenedByUser) {
         this.popupOpenedByUser = false;
         this.justClosedPopup = true;
         return true;
      } else {
         return super.mouseClicked(pMouseX, pMouseY, pButton);
      }
   }

   private boolean isOutsidePopup(double pMouseX, double pMouseY) {
      int i = this.popupX0();
      int j = this.popupY0();
      return pMouseX < (double)(i - 5) || pMouseX > (double)(i + 315) || pMouseY < (double)(j - 5) || pMouseY > (double)(j + 171);
   }

   private void drawPopup(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      int i = this.popupX0();
      int j = this.popupY0();
      if (!this.showingPopup) {
         this.carouselIndex = 0;
         this.carouselTick = 0;
         this.hasSwitchedCarouselImage = true;
         this.updateButtonStates((RealmsServer)null);
         if (this.realmsSelectionListAdded) {
            this.removeWidget(this.realmSelectionList);
            this.realmsSelectionListAdded = false;
         }

         this.minecraft.getNarrator().sayNow(POPUP_TEXT);
      }

      if (this.hasFetchedServers) {
         this.showingPopup = true;
      }

      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.7F);
      RenderSystem.enableBlend();
      pGuiGraphics.blit(DARKEN_LOCATION, 0, 44, 0.0F, 0.0F, this.width, this.height - 44, 310, 166);
      RenderSystem.disableBlend();
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      pGuiGraphics.blit(POPUP_LOCATION, i, j, 0.0F, 0.0F, 310, 166, 310, 166);
      if (!teaserImages.isEmpty()) {
         pGuiGraphics.blit(teaserImages.get(this.carouselIndex), i + 7, j + 7, 0.0F, 0.0F, 195, 152, 195, 152);
         if (this.carouselTick % 95 < 5) {
            if (!this.hasSwitchedCarouselImage) {
               this.carouselIndex = (this.carouselIndex + 1) % teaserImages.size();
               this.hasSwitchedCarouselImage = true;
            }
         } else {
            this.hasSwitchedCarouselImage = false;
         }
      }

      this.formattedPopup.renderLeftAlignedNoShadow(pGuiGraphics, this.width / 2 + 52, j + 7, 10, 16777215);
      this.createTrialButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.buyARealmButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.closeButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   int popupX0() {
      return (this.width - 310) / 2;
   }

   int popupY0() {
      return this.height / 2 - 80;
   }

   public void play(@Nullable RealmsServer pRealmsServer, Screen pLastScreen) {
      if (pRealmsServer != null) {
         try {
            if (!this.connectLock.tryLock(1L, TimeUnit.SECONDS)) {
               return;
            }

            if (this.connectLock.getHoldCount() > 1) {
               return;
            }
         } catch (InterruptedException interruptedexception) {
            return;
         }

         this.dontSetConnectedToRealms = true;
         this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(pLastScreen, new GetServerDetailsTask(this, pLastScreen, pRealmsServer, this.connectLock)));
      }

   }

   boolean isSelfOwnedServer(RealmsServer pServer) {
      return pServer.ownerUUID != null && pServer.ownerUUID.equals(this.minecraft.getUser().getUuid());
   }

   private boolean isSelfOwnedNonExpiredServer(RealmsServer pServer) {
      return this.isSelfOwnedServer(pServer) && !pServer.expired;
   }

   void drawExpired(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
      pGuiGraphics.blit(EXPIRED_ICON_LOCATION, pX, pY, 0.0F, 0.0F, 10, 28, 10, 28);
      if (pMouseX >= pX && pMouseX <= pX + 9 && pMouseY >= pY && pMouseY <= pY + 27 && pMouseY < this.height - 40 && pMouseY > 32 && !this.shouldShowPopup()) {
         this.setTooltipForNextRenderPass(SERVER_EXPIRED_TOOLTIP);
      }

   }

   void drawExpiring(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, int pDaysLeft) {
      if (this.animTick % 20 < 10) {
         pGuiGraphics.blit(EXPIRES_SOON_ICON_LOCATION, pX, pY, 0.0F, 0.0F, 10, 28, 20, 28);
      } else {
         pGuiGraphics.blit(EXPIRES_SOON_ICON_LOCATION, pX, pY, 10.0F, 0.0F, 10, 28, 20, 28);
      }

      if (pMouseX >= pX && pMouseX <= pX + 9 && pMouseY >= pY && pMouseY <= pY + 27 && pMouseY < this.height - 40 && pMouseY > 32 && !this.shouldShowPopup()) {
         if (pDaysLeft <= 0) {
            this.setTooltipForNextRenderPass(SERVER_EXPIRES_SOON_TOOLTIP);
         } else if (pDaysLeft == 1) {
            this.setTooltipForNextRenderPass(SERVER_EXPIRES_IN_DAY_TOOLTIP);
         } else {
            this.setTooltipForNextRenderPass(Component.translatable("mco.selectServer.expires.days", pDaysLeft));
         }
      }

   }

   void drawOpen(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
      pGuiGraphics.blit(ON_ICON_LOCATION, pX, pY, 0.0F, 0.0F, 10, 28, 10, 28);
      if (pMouseX >= pX && pMouseX <= pX + 9 && pMouseY >= pY && pMouseY <= pY + 27 && pMouseY < this.height - 40 && pMouseY > 32 && !this.shouldShowPopup()) {
         this.setTooltipForNextRenderPass(SERVER_OPEN_TOOLTIP);
      }

   }

   void drawClose(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
      pGuiGraphics.blit(OFF_ICON_LOCATION, pX, pY, 0.0F, 0.0F, 10, 28, 10, 28);
      if (pMouseX >= pX && pMouseX <= pX + 9 && pMouseY >= pY && pMouseY <= pY + 27 && pMouseY < this.height - 40 && pMouseY > 32 && !this.shouldShowPopup()) {
         this.setTooltipForNextRenderPass(SERVER_CLOSED_TOOLTIP);
      }

   }

   void renderNews(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, boolean pHasUnreadNews, int pX, int pY, boolean pIsHoveredOrFocused, boolean pActive) {
      boolean flag = false;
      if (pMouseX >= pX && pMouseX <= pX + 20 && pMouseY >= pY && pMouseY <= pY + 20) {
         flag = true;
      }

      if (!pActive) {
         pGuiGraphics.setColor(0.5F, 0.5F, 0.5F, 1.0F);
      }

      boolean flag1 = pActive && pIsHoveredOrFocused;
      float f = flag1 ? 20.0F : 0.0F;
      pGuiGraphics.blit(NEWS_LOCATION, pX, pY, f, 0.0F, 20, 20, 40, 20);
      if (flag && pActive) {
         this.setTooltipForNextRenderPass(NEWS_TOOLTIP);
      }

      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (pHasUnreadNews && pActive) {
         int i = flag ? 0 : (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
         pGuiGraphics.blit(INVITATION_ICONS_LOCATION, pX + 10, pY + 2 + i, 40.0F, 0.0F, 8, 8, 48, 16);
      }

   }

   private void renderLocal(GuiGraphics pGuiGraphics) {
      String s = "LOCAL!";
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
      pGuiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
      pGuiGraphics.pose().scale(1.5F, 1.5F, 1.5F);
      pGuiGraphics.drawString(this.font, "LOCAL!", 0, 0, 8388479, false);
      pGuiGraphics.pose().popPose();
   }

   private void renderStage(GuiGraphics pGuiGraphics) {
      String s = "STAGE!";
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
      pGuiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
      pGuiGraphics.pose().scale(1.5F, 1.5F, 1.5F);
      pGuiGraphics.drawString(this.font, "STAGE!", 0, 0, -256, false);
      pGuiGraphics.pose().popPose();
   }

   public RealmsMainScreen newScreen() {
      RealmsMainScreen realmsmainscreen = new RealmsMainScreen(this.lastScreen);
      realmsmainscreen.init(this.minecraft, this.width, this.height);
      return realmsmainscreen;
   }

   public static void updateTeaserImages(ResourceManager pResourceManager) {
      Collection<ResourceLocation> collection = pResourceManager.listResources("textures/gui/images", (p_193492_) -> {
         return p_193492_.getPath().endsWith(".png");
      }).keySet();
      teaserImages = collection.stream().filter((p_231247_) -> {
         return p_231247_.getNamespace().equals("realms");
      }).toList();
   }

   @OnlyIn(Dist.CLIENT)
   class ButtonEntry extends RealmsMainScreen.Entry {
      private final Button button;
      private final int xPos = RealmsMainScreen.this.width / 2 - 75;

      public ButtonEntry(Button pButton) {
         this.button = pButton;
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
         this.button.mouseClicked(pMouseX, pMouseY, pButton);
         return true;
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
         return this.button.keyPressed(pKeyCode, pScanCode, pModifiers) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.button.setPosition(this.xPos, pTop + 4);
         this.button.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      }

      public Component getNarration() {
         return this.button.getMessage();
      }
   }

   @OnlyIn(Dist.CLIENT)
   class CloseButton extends RealmsMainScreen.CrossButton {
      public CloseButton() {
         super(RealmsMainScreen.this.popupX0() + 4, RealmsMainScreen.this.popupY0() + 4, (p_86775_) -> {
            RealmsMainScreen.this.onClosePopup();
         }, Component.translatable("mco.selectServer.close"));
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class CrossButton extends Button {
      protected CrossButton(Button.OnPress pOnPress, Component pMessage) {
         this(0, 0, pOnPress, pMessage);
      }

      protected CrossButton(int pX, int pY, Button.OnPress pOnPress, Component pMessage) {
         super(pX, pY, 14, 14, pMessage, pOnPress, DEFAULT_NARRATION);
         this.setTooltip(Tooltip.create(pMessage));
      }

      public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
         float f = this.isHoveredOrFocused() ? 14.0F : 0.0F;
         pGuiGraphics.blit(RealmsMainScreen.CROSS_ICON_LOCATION, this.getX(), this.getY(), 0.0F, f, 14, 14, 14, 28);
      }
   }

   @OnlyIn(Dist.CLIENT)
   abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
      @Nullable
      public RealmsServer getServer() {
         return null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   class NewsButton extends Button {
      private static final int SIDE = 20;

      public NewsButton() {
         super(RealmsMainScreen.this.width - 115, 12, 20, 20, Component.translatable("mco.news"), (p_274636_) -> {
            if (RealmsMainScreen.this.newsLink != null) {
               ConfirmLinkScreen.confirmLinkNow(RealmsMainScreen.this.newsLink, RealmsMainScreen.this, true);
               if (RealmsMainScreen.this.hasUnreadNews) {
                  RealmsPersistence.RealmsPersistenceData realmspersistence$realmspersistencedata = RealmsPersistence.readFile();
                  realmspersistence$realmspersistencedata.hasUnreadNews = false;
                  RealmsMainScreen.this.hasUnreadNews = false;
                  RealmsPersistence.writeFile(realmspersistence$realmspersistencedata);
               }

            }
         }, DEFAULT_NARRATION);
      }

      public void renderWidget(GuiGraphics p_281287_, int p_282698_, int p_282096_, float p_283518_) {
         RealmsMainScreen.this.renderNews(p_281287_, p_282698_, p_282096_, RealmsMainScreen.this.hasUnreadNews, this.getX(), this.getY(), this.isHoveredOrFocused(), this.active);
      }
   }

   @OnlyIn(Dist.CLIENT)
   class NotificationMessageEntry extends RealmsMainScreen.Entry {
      private static final int SIDE_MARGINS = 40;
      private static final int ITEM_HEIGHT = 36;
      private static final int OUTLINE_COLOR = -12303292;
      private final Component text;
      private final List<AbstractWidget> children = new ArrayList<>();
      @Nullable
      private final RealmsMainScreen.CrossButton dismissButton;
      private final MultiLineTextWidget textWidget;
      private final GridLayout gridLayout;
      private final FrameLayout textFrame;
      private int lastEntryWidth = -1;

      public NotificationMessageEntry(Component pText, RealmsNotification pNotification) {
         this.text = pText;
         this.gridLayout = new GridLayout();
         int i = 7;
         this.gridLayout.addChild(new ImageWidget(20, 20, RealmsMainScreen.INFO_ICON_LOCATION), 0, 0, this.gridLayout.newCellSettings().padding(7, 7, 0, 0));
         this.gridLayout.addChild(SpacerElement.width(40), 0, 0);
         this.textFrame = this.gridLayout.addChild(new FrameLayout(0, 9 * 3), 0, 1, this.gridLayout.newCellSettings().paddingTop(7));
         this.textWidget = this.textFrame.addChild((new MultiLineTextWidget(pText, RealmsMainScreen.this.font)).setCentered(true).setMaxRows(3), this.textFrame.newChildLayoutSettings().alignHorizontallyCenter().alignVerticallyTop());
         this.gridLayout.addChild(SpacerElement.width(40), 0, 2);
         if (pNotification.dismissable()) {
            this.dismissButton = this.gridLayout.addChild(new RealmsMainScreen.CrossButton((p_275478_) -> {
               RealmsMainScreen.this.dismissNotification(pNotification.uuid());
            }, Component.translatable("mco.notification.dismiss")), 0, 2, this.gridLayout.newCellSettings().alignHorizontallyRight().padding(0, 7, 7, 0));
         } else {
            this.dismissButton = null;
         }

         this.gridLayout.visitWidgets(this.children::add);
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
         return this.dismissButton != null && this.dismissButton.keyPressed(pKeyCode, pScanCode, pModifiers) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }

      private void updateEntryWidth(int pEntryWidth) {
         if (this.lastEntryWidth != pEntryWidth) {
            this.refreshLayout(pEntryWidth);
            this.lastEntryWidth = pEntryWidth;
         }

      }

      private void refreshLayout(int pWidth) {
         int i = pWidth - 80;
         this.textFrame.setMinWidth(i);
         this.textWidget.setMaxWidth(i);
         this.gridLayout.arrangeElements();
      }

      public void renderBack(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
         super.renderBack(pGuiGraphics, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTick);
         pGuiGraphics.renderOutline(pLeft - 2, pTop - 2, pWidth, 70, -12303292);
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.gridLayout.setPosition(pLeft, pTop);
         this.updateEntryWidth(pWidth - 4);
         this.children.forEach((p_280688_) -> {
            p_280688_.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         });
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
         if (this.dismissButton != null) {
            this.dismissButton.mouseClicked(pMouseX, pMouseY, pButton);
         }

         return true;
      }

      public Component getNarration() {
         return this.text;
      }
   }

   @OnlyIn(Dist.CLIENT)
   class PendingInvitesButton extends ImageButton {
      private static final Component TITLE = Component.translatable("mco.invites.title");
      private static final Tooltip NO_PENDING_INVITES = Tooltip.create(Component.translatable("mco.invites.nopending"));
      private static final Tooltip PENDING_INVITES = Tooltip.create(Component.translatable("mco.invites.pending"));
      private static final int WIDTH = 18;
      private static final int HEIGHT = 15;
      private static final int X_OFFSET = 10;
      private static final int INVITES_WIDTH = 8;
      private static final int INVITES_HEIGHT = 8;
      private static final int INVITES_OFFSET = 11;

      public PendingInvitesButton() {
         super(RealmsMainScreen.this.width / 2 + 64 + 10, 15, 18, 15, 0, 0, 15, RealmsMainScreen.INVITE_ICON_LOCATION, 18, 30, (p_279110_) -> {
            RealmsMainScreen.this.minecraft.setScreen(new RealmsPendingInvitesScreen(RealmsMainScreen.this.lastScreen, TITLE));
         }, TITLE);
         this.setTooltip(NO_PENDING_INVITES);
      }

      public void tick() {
         this.setTooltip(RealmsMainScreen.this.numberOfPendingInvites == 0 ? NO_PENDING_INVITES : PENDING_INVITES);
      }

      public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
         super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         this.drawInvitations(pGuiGraphics);
      }

      private void drawInvitations(GuiGraphics pGuiGraphics) {
         boolean flag = this.active && RealmsMainScreen.this.numberOfPendingInvites != 0;
         if (flag) {
            int i = (Math.min(RealmsMainScreen.this.numberOfPendingInvites, 6) - 1) * 8;
            int j = (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + RealmsMainScreen.this.animTick) * 0.57F), Mth.cos((float)RealmsMainScreen.this.animTick * 0.35F))) * -6.0F);
            float f = this.isHoveredOrFocused() ? 8.0F : 0.0F;
            pGuiGraphics.blit(RealmsMainScreen.INVITATION_ICONS_LOCATION, this.getX() + 11, this.getY() + j, (float)i, f, 8, 8, 48, 16);
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
      public RealmSelectionList() {
         super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 44, RealmsMainScreen.this.height - 64, 36);
      }

      public void setSelected(@Nullable RealmsMainScreen.Entry p_86849_) {
         super.setSelected(p_86849_);
         if (p_86849_ != null) {
            RealmsMainScreen.this.updateButtonStates(p_86849_.getServer());
         } else {
            RealmsMainScreen.this.updateButtonStates((RealmsServer)null);
         }

      }

      public int getMaxPosition() {
         return this.getItemCount() * 36;
      }

      public int getRowWidth() {
         return 300;
      }
   }

   @OnlyIn(Dist.CLIENT)
   interface RealmsCall<T> {
      T request(RealmsClient pRealmsClient) throws RealmsServiceException;
   }

   @OnlyIn(Dist.CLIENT)
   class ServerEntry extends RealmsMainScreen.Entry {
      private static final int SKIN_HEAD_LARGE_WIDTH = 36;
      private final RealmsServer serverData;

      public ServerEntry(RealmsServer pServerData) {
         this.serverData = pServerData;
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.renderMcoServerItem(this.serverData, pGuiGraphics, pLeft, pTop, pMouseX, pMouseY);
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
         if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
            RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(this.serverData, RealmsMainScreen.this));
         } else if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
            if (Util.getMillis() - RealmsMainScreen.this.lastClickTime < 250L && this.isFocused()) {
               RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
               RealmsMainScreen.this.play(this.serverData, RealmsMainScreen.this);
            }

            RealmsMainScreen.this.lastClickTime = Util.getMillis();
         }

         return true;
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
         if (CommonInputs.selected(pKeyCode) && RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
            RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsMainScreen.this.play(this.serverData, RealmsMainScreen.this);
            return true;
         } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
         }
      }

      private void renderMcoServerItem(RealmsServer pRealmsServer, GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
         this.renderLegacy(pRealmsServer, pGuiGraphics, pX + 36, pY, pMouseX, pMouseY);
      }

      private void renderLegacy(RealmsServer pRealmsServer, GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
         if (pRealmsServer.state == RealmsServer.State.UNINITIALIZED) {
            pGuiGraphics.blit(RealmsMainScreen.WORLDICON_LOCATION, pX + 10, pY + 6, 0.0F, 0.0F, 40, 20, 40, 20);
            float f = 0.5F + (1.0F + Mth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
            int l = -16777216 | (int)(127.0F * f) << 16 | (int)(255.0F * f) << 8 | (int)(127.0F * f);
            pGuiGraphics.drawCenteredString(RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, pX + 10 + 40 + 75, pY + 12, l);
         } else {
            int i = 225;
            int j = 2;
            this.renderStatusLights(pRealmsServer, pGuiGraphics, pX, pY, pMouseX, pMouseY, 225, 2);
            if (!"0".equals(pRealmsServer.serverPing.nrOfPlayers)) {
               String s = ChatFormatting.GRAY + pRealmsServer.serverPing.nrOfPlayers;
               pGuiGraphics.drawString(RealmsMainScreen.this.font, s, pX + 207 - RealmsMainScreen.this.font.width(s), pY + 3, 8421504, false);
               if (pMouseX >= pX + 207 - RealmsMainScreen.this.font.width(s) && pMouseX <= pX + 207 && pMouseY >= pY + 1 && pMouseY <= pY + 10 && pMouseY < RealmsMainScreen.this.height - 40 && pMouseY > 32 && !RealmsMainScreen.this.shouldShowPopup()) {
                  RealmsMainScreen.this.setTooltipForNextRenderPass(Component.literal(pRealmsServer.serverPing.playerList));
               }
            }

            if (RealmsMainScreen.this.isSelfOwnedServer(pRealmsServer) && pRealmsServer.expired) {
               Component component = pRealmsServer.expiredTrial ? RealmsMainScreen.TRIAL_EXPIRED_TEXT : RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
               int j1 = pY + 11 + 5;
               pGuiGraphics.drawString(RealmsMainScreen.this.font, component, pX + 2, j1 + 1, 15553363, false);
            } else {
               if (pRealmsServer.worldType == RealmsServer.WorldType.MINIGAME) {
                  int i1 = 13413468;
                  int k = RealmsMainScreen.this.font.width(RealmsMainScreen.SELECT_MINIGAME_PREFIX);
                  pGuiGraphics.drawString(RealmsMainScreen.this.font, RealmsMainScreen.SELECT_MINIGAME_PREFIX, pX + 2, pY + 12, 13413468, false);
                  pGuiGraphics.drawString(RealmsMainScreen.this.font, pRealmsServer.getMinigameName(), pX + 2 + k, pY + 12, 7105644, false);
               } else {
                  pGuiGraphics.drawString(RealmsMainScreen.this.font, pRealmsServer.getDescription(), pX + 2, pY + 12, 7105644, false);
               }

               if (!RealmsMainScreen.this.isSelfOwnedServer(pRealmsServer)) {
                  pGuiGraphics.drawString(RealmsMainScreen.this.font, pRealmsServer.owner, pX + 2, pY + 12 + 11, 5000268, false);
               }
            }

            pGuiGraphics.drawString(RealmsMainScreen.this.font, pRealmsServer.getName(), pX + 2, pY + 1, 16777215, false);
            RealmsUtil.renderPlayerFace(pGuiGraphics, pX - 36, pY, 32, pRealmsServer.ownerUUID);
         }
      }

      private void renderStatusLights(RealmsServer pRealmsServer, GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, int pWidth, int pHeight) {
         int i = pX + pWidth + 22;
         if (pRealmsServer.expired) {
            RealmsMainScreen.this.drawExpired(pGuiGraphics, i, pY + pHeight, pMouseX, pMouseY);
         } else if (pRealmsServer.state == RealmsServer.State.CLOSED) {
            RealmsMainScreen.this.drawClose(pGuiGraphics, i, pY + pHeight, pMouseX, pMouseY);
         } else if (RealmsMainScreen.this.isSelfOwnedServer(pRealmsServer) && pRealmsServer.daysLeft < 7) {
            RealmsMainScreen.this.drawExpiring(pGuiGraphics, i, pY + pHeight, pMouseX, pMouseY, pRealmsServer.daysLeft);
         } else if (pRealmsServer.state == RealmsServer.State.OPEN) {
            RealmsMainScreen.this.drawOpen(pGuiGraphics, i, pY + pHeight, pMouseX, pMouseY);
         }

      }

      public Component getNarration() {
         return (Component)(this.serverData.state == RealmsServer.State.UNINITIALIZED ? RealmsMainScreen.UNITIALIZED_WORLD_NARRATION : Component.translatable("narrator.select", this.serverData.name));
      }

      @Nullable
      public RealmsServer getServer() {
         return this.serverData;
      }
   }

   @OnlyIn(Dist.CLIENT)
   class TrialEntry extends RealmsMainScreen.Entry {
      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         this.renderTrialItem(pGuiGraphics, pIndex, pLeft, pTop, pMouseX, pMouseY);
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
         RealmsMainScreen.this.popupOpenedByUser = true;
         return true;
      }

      private void renderTrialItem(GuiGraphics pGuiGraphics, int pIndex, int pLeft, int pTop, int pMouseX, int pMouseY) {
         int i = pTop + 8;
         int j = 0;
         boolean flag = false;
         if (pLeft <= pMouseX && pMouseX <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && pTop <= pMouseY && pMouseY <= pTop + 32) {
            flag = true;
         }

         int k = 8388479;
         if (flag && !RealmsMainScreen.this.shouldShowPopup()) {
            k = 6077788;
         }

         for(Component component : RealmsMainScreen.TRIAL_MESSAGE_LINES) {
            pGuiGraphics.drawCenteredString(RealmsMainScreen.this.font, component, RealmsMainScreen.this.width / 2, i + j, k);
            j += 10;
         }

      }

      public Component getNarration() {
         return RealmsMainScreen.TRIAL_TEXT;
      }
   }
}