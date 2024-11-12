package net.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.server.MinecraftServer;

public class MemoryServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
   private final MinecraftServer server;
   private final Connection connection;

   public MemoryServerHandshakePacketListenerImpl(MinecraftServer pServer, Connection pConnection) {
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
      this.connection.setProtocol(pPacket.getIntention());
      this.connection.setListener(new ServerLoginPacketListenerImpl(this.server, this.connection));
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
