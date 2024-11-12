package net.minecraft.client.gui.screens;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.client.quickplay.QuickPlay;
import net.minecraft.client.quickplay.QuickPlayLog;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ConnectScreen extends Screen {
   private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
   static final Logger LOGGER = LogUtils.getLogger();
   private static final long NARRATION_DELAY_MS = 2000L;
   static final Component ABORT_CONNECTION = Component.translatable("connect.aborted");
   public static final Component UNKNOWN_HOST_MESSAGE = Component.translatable("disconnect.genericReason", Component.translatable("disconnect.unknownHost"));
   @Nullable
   volatile Connection connection;
   @Nullable
   ChannelFuture channelFuture;
   volatile boolean aborted;
   final Screen parent;
   private Component status = Component.translatable("connect.connecting");
   private long lastNarration = -1L;
   final Component connectFailedTitle;

   private ConnectScreen(Screen pParent, Component pConnectFailedTitle) {
      super(GameNarrator.NO_TITLE);
      this.parent = pParent;
      this.connectFailedTitle = pConnectFailedTitle;
   }

   public static void startConnecting(Screen pParent, Minecraft pMinecraft, ServerAddress pServerAddress, ServerData pServerData, boolean pIsQuickPlay) {
      if (pMinecraft.screen instanceof ConnectScreen) {
         LOGGER.error("Attempt to connect while already connecting");
      } else {
         ConnectScreen connectscreen = new ConnectScreen(pParent, pIsQuickPlay ? QuickPlay.ERROR_TITLE : CommonComponents.CONNECT_FAILED);
         pMinecraft.clearLevel();
         pMinecraft.prepareForMultiplayer();
         pMinecraft.updateReportEnvironment(ReportEnvironment.thirdParty(pServerData != null ? pServerData.ip : pServerAddress.getHost()));
         pMinecraft.quickPlayLog().setWorldData(QuickPlayLog.Type.MULTIPLAYER, pServerData.ip, pServerData.name);
         pMinecraft.setScreen(connectscreen);
         connectscreen.connect(pMinecraft, pServerAddress, pServerData);
      }
   }

   private void connect(final Minecraft pMinecraft, final ServerAddress pServerAddress, @Nullable final ServerData pServerData) {
      LOGGER.info("Connecting to {}, {}", pServerAddress.getHost(), pServerAddress.getPort());
      Thread thread = new Thread("Server Connector #" + UNIQUE_THREAD_ID.incrementAndGet()) {
         public void run() {
            InetSocketAddress inetsocketaddress = null;

            try {
               if (ConnectScreen.this.aborted) {
                  return;
               }

               Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(pServerAddress).map(ResolvedServerAddress::asInetSocketAddress);
               if (ConnectScreen.this.aborted) {
                  return;
               }

               if (!optional.isPresent()) {
                  ConnectScreen.LOGGER.error("Couldn't connect to server: Unknown host \"{}\"", pServerAddress.getHost());
                  net.minecraftforge.network.DualStackUtils.logInitialPreferences();
                  pMinecraft.execute(() -> {
                     pMinecraft.setScreen(new DisconnectedScreen(ConnectScreen.this.parent, ConnectScreen.this.connectFailedTitle, ConnectScreen.UNKNOWN_HOST_MESSAGE));
                  });
                  return;
               }

               inetsocketaddress = optional.get();
               Connection connection;
               synchronized(ConnectScreen.this) {
                  if (ConnectScreen.this.aborted) {
                     return;
                  }

                  connection = new Connection(PacketFlow.CLIENTBOUND);
                  ConnectScreen.this.channelFuture = Connection.connect(inetsocketaddress, pMinecraft.options.useNativeTransport(), connection);
               }

               ConnectScreen.this.channelFuture.syncUninterruptibly();
               synchronized(ConnectScreen.this) {
                  if (ConnectScreen.this.aborted) {
                     connection.disconnect(ConnectScreen.ABORT_CONNECTION);
                     return;
                  }

                  ConnectScreen.this.connection = connection;
               }

               ConnectScreen.this.connection.setListener(new ClientHandshakePacketListenerImpl(ConnectScreen.this.connection, pMinecraft, pServerData, ConnectScreen.this.parent, false, (Duration)null, ConnectScreen.this::updateStatus));
               ConnectScreen.this.connection.send(new ClientIntentionPacket(inetsocketaddress.getHostName(), inetsocketaddress.getPort(), ConnectionProtocol.LOGIN));
               ConnectScreen.this.connection.send(new ServerboundHelloPacket(pMinecraft.getUser().getName(), Optional.ofNullable(pMinecraft.getUser().getProfileId())));
            } catch (Exception exception2) {
               if (ConnectScreen.this.aborted) {
                  return;
               }

               Throwable throwable = exception2.getCause();
               Exception exception;
               if (throwable instanceof Exception exception1) {
                  exception = exception1;
               } else {
                  exception = exception2;
               }

               ConnectScreen.LOGGER.error("Couldn't connect to server", (Throwable)exception2);
               String s = inetsocketaddress == null ? exception.getMessage() : exception.getMessage().replaceAll(inetsocketaddress.getHostName() + ":" + inetsocketaddress.getPort(), "").replaceAll(inetsocketaddress.toString(), "");
               pMinecraft.execute(() -> {
                  pMinecraft.setScreen(new DisconnectedScreen(ConnectScreen.this.parent, ConnectScreen.this.connectFailedTitle, Component.translatable("disconnect.genericReason", s)));
               });
            }

         }
      };
      thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
      thread.start();
   }

   private void updateStatus(Component pStatus) {
      this.status = pStatus;
   }

   public void tick() {
      if (this.connection != null) {
         if (this.connection.isConnected()) {
            this.connection.tick();
         } else {
            this.connection.handleDisconnection();
         }
      }

   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected void init() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_289624_) -> {
         synchronized(this) {
            this.aborted = true;
            if (this.channelFuture != null) {
               this.channelFuture.cancel(true);
               this.channelFuture = null;
            }

            if (this.connection != null) {
               this.connection.disconnect(ABORT_CONNECTION);
            }
         }

         this.minecraft.setScreen(this.parent);
      }).bounds(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20).build());
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
      long i = Util.getMillis();
      if (i - this.lastNarration > 2000L) {
         this.lastNarration = i;
         this.minecraft.getNarrator().sayNow(Component.translatable("narrator.joining"));
      }

      pGuiGraphics.drawCenteredString(this.font, this.status, this.width / 2, this.height / 2 - 50, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }
}
