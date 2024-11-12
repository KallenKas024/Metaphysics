package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;

public class ServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
   private static final Component IGNORE_STATUS_REASON = Component.translatable("disconnect.ignoring_status_request");
   private final MinecraftServer server;
   private final Connection connection;

   public ServerHandshakePacketListenerImpl(MinecraftServer pServer, Connection pConnection) {
      this.server = pServer;
      this.connection = pConnection;
   }

   /**
    * There are two recognized intentions for initiating a handshake: logging in and acquiring server status. The
    * NetworkManager's protocol will be reconfigured according to the specified intention, although a login-intention
    * must pass a versioncheck or receive a disconnect otherwise
    */
   public void handleIntention(ClientIntentionPacket pPacket) {
      if (!net.minecraftforge.server.ServerLifecycleHooks.handleServerLogin(pPacket, this.connection)) return;
      switch (pPacket.getIntention()) {
         case LOGIN:
            this.connection.setProtocol(ConnectionProtocol.LOGIN);
            if (pPacket.getProtocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
               Component component;
               if (pPacket.getProtocolVersion() < 754) {
                  component = Component.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getCurrentVersion().getName());
               } else {
                  component = Component.translatable("multiplayer.disconnect.incompatible", SharedConstants.getCurrentVersion().getName());
               }

               this.connection.send(new ClientboundLoginDisconnectPacket(component));
               this.connection.disconnect(component);
            } else {
               this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
            }
            break;
         case STATUS:
            ServerStatus serverstatus = this.server.getStatus();
            if (this.server.repliesToStatus() && serverstatus != null) {
               this.connection.setProtocol(ConnectionProtocol.STATUS);
               this.connection.setListener(new ServerStatusPacketListenerImpl(serverstatus, this.connection, this.server.getStatusJson()));
            } else {
               this.connection.disconnect(IGNORE_STATUS_REASON);
            }
            break;
         default:
            throw new UnsupportedOperationException("Invalid intention " + pPacket.getIntention());
      }

   }

   /**
    * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
    */
   public void onDisconnect(Component pReason) {
   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected();
   }
}
