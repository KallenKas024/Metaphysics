package net.minecraft.realms;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.RealmsServer;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.quickplay.QuickPlayLog;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsConnect {
   static final Logger LOGGER = LogUtils.getLogger();
   final Screen onlineScreen;
   volatile boolean aborted;
   @Nullable
   Connection connection;

   public RealmsConnect(Screen pOnlineScreen) {
      this.onlineScreen = pOnlineScreen;
   }

   public void connect(final RealmsServer pServer, ServerAddress pAddress) {
      final Minecraft minecraft = Minecraft.getInstance();
      minecraft.setConnectedToRealms(true);
      minecraft.prepareForMultiplayer();
      minecraft.getNarrator().sayNow(Component.translatable("mco.connect.success"));
      final String s = pAddress.getHost();
      final int i = pAddress.getPort();
      (new Thread("Realms-connect-task") {
         public void run() {
            InetSocketAddress inetsocketaddress = null;

            try {
               inetsocketaddress = new InetSocketAddress(s, i);
               if (RealmsConnect.this.aborted) {
                  return;
               }

               RealmsConnect.this.connection = Connection.connectToServer(inetsocketaddress, minecraft.options.useNativeTransport());
               if (RealmsConnect.this.aborted) {
                  return;
               }

               ClientHandshakePacketListenerImpl clienthandshakepacketlistenerimpl = new ClientHandshakePacketListenerImpl(RealmsConnect.this.connection, minecraft, pServer.toServerData(s), RealmsConnect.this.onlineScreen, false, (Duration)null, (p_120726_) -> {
               });
               if (pServer.worldType == RealmsServer.WorldType.MINIGAME) {
                  clienthandshakepacketlistenerimpl.setMinigameName(pServer.minigameName);
               }

               RealmsConnect.this.connection.setListener(clienthandshakepacketlistenerimpl);
               if (RealmsConnect.this.aborted) {
                  return;
               }

               RealmsConnect.this.connection.send(new ClientIntentionPacket(s, i, ConnectionProtocol.LOGIN));
               if (RealmsConnect.this.aborted) {
                  return;
               }

               String s3 = minecraft.getUser().getName();
               UUID uuid = minecraft.getUser().getProfileId();
               RealmsConnect.this.connection.send(new ServerboundHelloPacket(s3, Optional.ofNullable(uuid)));
               minecraft.updateReportEnvironment(ReportEnvironment.realm(pServer));
               minecraft.quickPlayLog().setWorldData(QuickPlayLog.Type.REALMS, String.valueOf(pServer.id), pServer.name);
            } catch (Exception exception) {
               minecraft.getDownloadedPackSource().clearServerPack();
               if (RealmsConnect.this.aborted) {
                  return;
               }

               RealmsConnect.LOGGER.error("Couldn't connect to world", (Throwable)exception);
               String s1 = exception.toString();
               if (inetsocketaddress != null) {
                  String s2 = inetsocketaddress + ":" + i;
                  s1 = s1.replaceAll(s2, "");
               }

               DisconnectedRealmsScreen disconnectedrealmsscreen = new DisconnectedRealmsScreen(RealmsConnect.this.onlineScreen, CommonComponents.CONNECT_FAILED, Component.translatable("disconnect.genericReason", s1));
               minecraft.execute(() -> {
                  minecraft.setScreen(disconnectedrealmsscreen);
               });
            }

         }
      }).start();
   }

   public void abort() {
      this.aborted = true;
      if (this.connection != null && this.connection.isConnected()) {
         this.connection.disconnect(Component.translatable("disconnect.genericReason"));
         this.connection.handleDisconnection();
      }

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
}