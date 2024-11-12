package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ServerSelectionList extends ObjectSelectionList<ServerSelectionList.Entry> {
   static final Logger LOGGER = LogUtils.getLogger();
   static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER)).build());
   private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
   static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");
   static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
   static final Component SCANNING_LABEL = Component.translatable("lanServer.scanning");
   static final Component CANT_RESOLVE_TEXT = Component.translatable("multiplayer.status.cannot_resolve").withStyle((p_264689_) -> {
      return p_264689_.withColor(-65536);
   });
   static final Component CANT_CONNECT_TEXT = Component.translatable("multiplayer.status.cannot_connect").withStyle((p_264688_) -> {
      return p_264688_.withColor(-65536);
   });
   static final Component INCOMPATIBLE_STATUS = Component.translatable("multiplayer.status.incompatible");
   static final Component NO_CONNECTION_STATUS = Component.translatable("multiplayer.status.no_connection");
   static final Component PINGING_STATUS = Component.translatable("multiplayer.status.pinging");
   static final Component ONLINE_STATUS = Component.translatable("multiplayer.status.online");
   private final JoinMultiplayerScreen screen;
   private final List<ServerSelectionList.OnlineServerEntry> onlineServers = Lists.newArrayList();
   private final ServerSelectionList.Entry lanHeader = new ServerSelectionList.LANHeader();
   private final List<ServerSelectionList.NetworkServerEntry> networkServers = Lists.newArrayList();

   public ServerSelectionList(JoinMultiplayerScreen pScreen, Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
      super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
      this.screen = pScreen;
   }

   private void refreshEntries() {
      this.clearEntries();
      this.onlineServers.forEach((p_169979_) -> {
         this.addEntry(p_169979_);
      });
      this.addEntry(this.lanHeader);
      this.networkServers.forEach((p_169976_) -> {
         this.addEntry(p_169976_);
      });
   }

   public void setSelected(@Nullable ServerSelectionList.Entry pEntry) {
      super.setSelected(pEntry);
      this.screen.onSelectedChange();
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
      ServerSelectionList.Entry serverselectionlist$entry = this.getSelected();
      return serverselectionlist$entry != null && serverselectionlist$entry.keyPressed(pKeyCode, pScanCode, pModifiers) || super.keyPressed(pKeyCode, pScanCode, pModifiers);
   }

   public void updateOnlineServers(ServerList pServers) {
      this.onlineServers.clear();

      for(int i = 0; i < pServers.size(); ++i) {
         this.onlineServers.add(new ServerSelectionList.OnlineServerEntry(this.screen, pServers.get(i)));
      }

      this.refreshEntries();
   }

   public void updateNetworkServers(List<LanServer> pLanServers) {
      int i = pLanServers.size() - this.networkServers.size();
      this.networkServers.clear();

      for(LanServer lanserver : pLanServers) {
         this.networkServers.add(new ServerSelectionList.NetworkServerEntry(this.screen, lanserver));
      }

      this.refreshEntries();

      for(int i1 = this.networkServers.size() - i; i1 < this.networkServers.size(); ++i1) {
         ServerSelectionList.NetworkServerEntry serverselectionlist$networkserverentry = this.networkServers.get(i1);
         int j = i1 - this.networkServers.size() + this.children().size();
         int k = this.getRowTop(j);
         int l = this.getRowBottom(j);
         if (l >= this.y0 && k <= this.y1) {
            this.minecraft.getNarrator().say(Component.translatable("multiplayer.lan.server_found", serverselectionlist$networkserverentry.getServerNarration()));
         }
      }

   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 30;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 85;
   }

   public void removed() {
   }

   @OnlyIn(Dist.CLIENT)
   public abstract static class Entry extends ObjectSelectionList.Entry<ServerSelectionList.Entry> implements AutoCloseable {
      public void close() {
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class LANHeader extends ServerSelectionList.Entry {
      private final Minecraft minecraft = Minecraft.getInstance();

      public void render(GuiGraphics p_281475_, int p_282477_, int p_282819_, int p_282001_, int p_281911_, int p_283126_, int p_282303_, int p_281998_, boolean p_282625_, float p_281811_) {
         int i = p_282819_ + p_283126_ / 2 - 9 / 2;
         p_281475_.drawString(this.minecraft.font, ServerSelectionList.SCANNING_LABEL, this.minecraft.screen.width / 2 - this.minecraft.font.width(ServerSelectionList.SCANNING_LABEL) / 2, i, 16777215, false);
         String s = LoadingDotsText.get(Util.getMillis());
         p_281475_.drawString(this.minecraft.font, s, this.minecraft.screen.width / 2 - this.minecraft.font.width(s) / 2, i + 9, 8421504, false);
      }

      public Component getNarration() {
         return ServerSelectionList.SCANNING_LABEL;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class NetworkServerEntry extends ServerSelectionList.Entry {
      private static final int ICON_WIDTH = 32;
      private static final Component LAN_SERVER_HEADER = Component.translatable("lanServer.title");
      private static final Component HIDDEN_ADDRESS_TEXT = Component.translatable("selectServer.hiddenAddress");
      private final JoinMultiplayerScreen screen;
      protected final Minecraft minecraft;
      protected final LanServer serverData;
      private long lastClickTime;

      protected NetworkServerEntry(JoinMultiplayerScreen pScreen, LanServer pServerData) {
         this.screen = pScreen;
         this.serverData = pServerData;
         this.minecraft = Minecraft.getInstance();
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         pGuiGraphics.drawString(this.minecraft.font, LAN_SERVER_HEADER, pLeft + 32 + 3, pTop + 1, 16777215, false);
         pGuiGraphics.drawString(this.minecraft.font, this.serverData.getMotd(), pLeft + 32 + 3, pTop + 12, 8421504, false);
         if (this.minecraft.options.hideServerAddress) {
            pGuiGraphics.drawString(this.minecraft.font, HIDDEN_ADDRESS_TEXT, pLeft + 32 + 3, pTop + 12 + 11, 3158064, false);
         } else {
            pGuiGraphics.drawString(this.minecraft.font, this.serverData.getAddress(), pLeft + 32 + 3, pTop + 12 + 11, 3158064, false);
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
         this.screen.setSelected(this);
         if (Util.getMillis() - this.lastClickTime < 250L) {
            this.screen.joinSelectedServer();
         }

         this.lastClickTime = Util.getMillis();
         return false;
      }

      public LanServer getServerData() {
         return this.serverData;
      }

      public Component getNarration() {
         return Component.translatable("narrator.select", this.getServerNarration());
      }

      public Component getServerNarration() {
         return Component.empty().append(LAN_SERVER_HEADER).append(CommonComponents.SPACE).append(this.serverData.getMotd());
      }
   }

   @OnlyIn(Dist.CLIENT)
   public class OnlineServerEntry extends ServerSelectionList.Entry {
      private static final int ICON_WIDTH = 32;
      private static final int ICON_HEIGHT = 32;
      private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
      private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
      private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
      private static final int ICON_OVERLAY_X_MOVE_UP = 96;
      private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
      private static final int ICON_OVERLAY_Y_SELECTED = 32;
      private final JoinMultiplayerScreen screen;
      private final Minecraft minecraft;
      private final ServerData serverData;
      private final FaviconTexture icon;
      @Nullable
      private byte[] lastIconBytes;
      private long lastClickTime;

      protected OnlineServerEntry(JoinMultiplayerScreen pScreen, ServerData pServerData) {
         this.screen = pScreen;
         this.serverData = pServerData;
         this.minecraft = Minecraft.getInstance();
         this.icon = FaviconTexture.forServer(this.minecraft.getTextureManager(), pServerData.ip);
      }

      public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
         if (!this.serverData.pinged) {
            this.serverData.pinged = true;
            this.serverData.ping = -2L;
            this.serverData.motd = CommonComponents.EMPTY;
            this.serverData.status = CommonComponents.EMPTY;
            ServerSelectionList.THREAD_POOL.submit(() -> {
               try {
                  this.screen.getPinger().pingServer(this.serverData, () -> {
                     this.minecraft.execute(this::updateServerList);
                  });
               } catch (UnknownHostException unknownhostexception) {
                  this.serverData.ping = -1L;
                  this.serverData.motd = ServerSelectionList.CANT_RESOLVE_TEXT;
               } catch (Exception exception) {
                  this.serverData.ping = -1L;
                  this.serverData.motd = ServerSelectionList.CANT_CONNECT_TEXT;
               }

            });
         }

         boolean flag = !this.isCompatible();
         pGuiGraphics.drawString(this.minecraft.font, this.serverData.name, pLeft + 32 + 3, pTop + 1, 16777215, false);
         List<FormattedCharSequence> list = this.minecraft.font.split(this.serverData.motd, pWidth - 32 - 2);

         for(int i = 0; i < Math.min(list.size(), 2); ++i) {
            pGuiGraphics.drawString(this.minecraft.font, list.get(i), pLeft + 32 + 3, pTop + 12 + 9 * i, 8421504, false);
         }

         Component component1 = (Component)(flag ? this.serverData.version.copy().withStyle(ChatFormatting.RED) : this.serverData.status);
         int j = this.minecraft.font.width(component1);
         pGuiGraphics.drawString(this.minecraft.font, component1, pLeft + pWidth - j - 15 - 2, pTop + 1, 8421504, false);
         int k = 0;
         int l;
         List<Component> list1;
         Component component;
         if (flag) {
            l = 5;
            component = ServerSelectionList.INCOMPATIBLE_STATUS;
            list1 = this.serverData.playerList;
         } else if (this.pingCompleted()) {
            if (this.serverData.ping < 0L) {
               l = 5;
            } else if (this.serverData.ping < 150L) {
               l = 0;
            } else if (this.serverData.ping < 300L) {
               l = 1;
            } else if (this.serverData.ping < 600L) {
               l = 2;
            } else if (this.serverData.ping < 1000L) {
               l = 3;
            } else {
               l = 4;
            }

            if (this.serverData.ping < 0L) {
               component = ServerSelectionList.NO_CONNECTION_STATUS;
               list1 = Collections.emptyList();
            } else {
               component = Component.translatable("multiplayer.status.ping", this.serverData.ping);
               list1 = this.serverData.playerList;
            }
         } else {
            k = 1;
            l = (int)(Util.getMillis() / 100L + (long)(pIndex * 2) & 7L);
            if (l > 4) {
               l = 8 - l;
            }

            component = ServerSelectionList.PINGING_STATUS;
            list1 = Collections.emptyList();
         }

         pGuiGraphics.blit(ServerSelectionList.GUI_ICONS_LOCATION, pLeft + pWidth - 15, pTop, (float)(k * 10), (float)(176 + l * 8), 10, 8, 256, 256);
         byte[] abyte = this.serverData.getIconBytes();
         if (!Arrays.equals(abyte, this.lastIconBytes)) {
            if (this.uploadServerIcon(abyte)) {
               this.lastIconBytes = abyte;
            } else {
               this.serverData.setIconBytes((byte[])null);
               this.updateServerList();
            }
         }

         this.drawIcon(pGuiGraphics, pLeft, pTop, this.icon.textureLocation());
         int i1 = pMouseX - pLeft;
         int j1 = pMouseY - pTop;
         if (i1 >= pWidth - 15 && i1 <= pWidth - 5 && j1 >= 0 && j1 <= 8) {
            this.screen.setToolTip(Collections.singletonList(component));
         } else if (i1 >= pWidth - j - 15 - 2 && i1 <= pWidth - 15 - 2 && j1 >= 0 && j1 <= 8) {
            this.screen.setToolTip(list1);
         }

         net.minecraftforge.client.ForgeHooksClient.drawForgePingInfo(this.screen, serverData, pGuiGraphics, pLeft, pTop, pWidth, i1, j1);

         if (this.minecraft.options.touchscreen().get() || pHovering) {
            pGuiGraphics.fill(pLeft, pTop, pLeft + 32, pTop + 32, -1601138544);
            int k1 = pMouseX - pLeft;
            int l1 = pMouseY - pTop;
            if (this.canJoin()) {
               if (k1 < 32 && k1 > 16) {
                  pGuiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 0.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  pGuiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 0.0F, 0.0F, 32, 32, 256, 256);
               }
            }

            if (pIndex > 0) {
               if (k1 < 16 && l1 < 16) {
                  pGuiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 96.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  pGuiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 96.0F, 0.0F, 32, 32, 256, 256);
               }
            }

            if (pIndex < this.screen.getServers().size() - 1) {
               if (k1 < 16 && l1 > 16) {
                  pGuiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 64.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  pGuiGraphics.blit(ServerSelectionList.ICON_OVERLAY_LOCATION, pLeft, pTop, 64.0F, 0.0F, 32, 32, 256, 256);
               }
            }
         }

      }

      private boolean pingCompleted() {
         return this.serverData.pinged && this.serverData.ping != -2L;
      }

      private boolean isCompatible() {
         return this.serverData.protocol == SharedConstants.getCurrentVersion().getProtocolVersion();
      }

      public void updateServerList() {
         this.screen.getServers().save();
      }

      protected void drawIcon(GuiGraphics pGuiGraphics, int pX, int pY, ResourceLocation pIcon) {
         RenderSystem.enableBlend();
         pGuiGraphics.blit(pIcon, pX, pY, 0.0F, 0.0F, 32, 32, 32, 32);
         RenderSystem.disableBlend();
      }

      private boolean canJoin() {
         return true;
      }

      private boolean uploadServerIcon(@Nullable byte[] pIconBytes) {
         if (pIconBytes == null) {
            this.icon.clear();
         } else {
            try {
               this.icon.upload(NativeImage.read(pIconBytes));
            } catch (Throwable throwable) {
               ServerSelectionList.LOGGER.error("Invalid icon for server {} ({})", this.serverData.name, this.serverData.ip, throwable);
               return false;
            }
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
         if (Screen.hasShiftDown()) {
            ServerSelectionList serverselectionlist = this.screen.serverSelectionList;
            int i = serverselectionlist.children().indexOf(this);
            if (i == -1) {
               return true;
            }

            if (pKeyCode == 264 && i < this.screen.getServers().size() - 1 || pKeyCode == 265 && i > 0) {
               this.swap(i, pKeyCode == 264 ? i + 1 : i - 1);
               return true;
            }
         }

         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }

      private void swap(int pPos1, int pPos2) {
         this.screen.getServers().swap(pPos1, pPos2);
         this.screen.serverSelectionList.updateOnlineServers(this.screen.getServers());
         ServerSelectionList.Entry serverselectionlist$entry = this.screen.serverSelectionList.children().get(pPos2);
         this.screen.serverSelectionList.setSelected(serverselectionlist$entry);
         ServerSelectionList.this.ensureVisible(serverselectionlist$entry);
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
         double d0 = pMouseX - (double)ServerSelectionList.this.getRowLeft();
         double d1 = pMouseY - (double)ServerSelectionList.this.getRowTop(ServerSelectionList.this.children().indexOf(this));
         if (d0 <= 32.0D) {
            if (d0 < 32.0D && d0 > 16.0D && this.canJoin()) {
               this.screen.setSelected(this);
               this.screen.joinSelectedServer();
               return true;
            }

            int i = this.screen.serverSelectionList.children().indexOf(this);
            if (d0 < 16.0D && d1 < 16.0D && i > 0) {
               this.swap(i, i - 1);
               return true;
            }

            if (d0 < 16.0D && d1 > 16.0D && i < this.screen.getServers().size() - 1) {
               this.swap(i, i + 1);
               return true;
            }
         }

         this.screen.setSelected(this);
         if (Util.getMillis() - this.lastClickTime < 250L) {
            this.screen.joinSelectedServer();
         }

         this.lastClickTime = Util.getMillis();
         return true;
      }

      public ServerData getServerData() {
         return this.serverData;
      }

      public Component getNarration() {
         MutableComponent mutablecomponent = Component.empty();
         mutablecomponent.append(Component.translatable("narrator.select", this.serverData.name));
         mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
         if (!this.isCompatible()) {
            mutablecomponent.append(ServerSelectionList.INCOMPATIBLE_STATUS);
            mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
            mutablecomponent.append(Component.translatable("multiplayer.status.version.narration", this.serverData.version));
            mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
            mutablecomponent.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
         } else if (this.serverData.ping < 0L) {
            mutablecomponent.append(ServerSelectionList.NO_CONNECTION_STATUS);
         } else if (!this.pingCompleted()) {
            mutablecomponent.append(ServerSelectionList.PINGING_STATUS);
         } else {
            mutablecomponent.append(ServerSelectionList.ONLINE_STATUS);
            mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
            mutablecomponent.append(Component.translatable("multiplayer.status.ping.narration", this.serverData.ping));
            mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
            mutablecomponent.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
            if (this.serverData.players != null) {
               mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
               mutablecomponent.append(Component.translatable("multiplayer.status.player_count.narration", this.serverData.players.online(), this.serverData.players.max()));
               mutablecomponent.append(CommonComponents.NARRATION_SEPARATOR);
               mutablecomponent.append(ComponentUtils.formatList(this.serverData.playerList, Component.literal(", ")));
            }
         }

         return mutablecomponent;
      }

      public void close() {
         this.icon.close();
      }
   }
}
