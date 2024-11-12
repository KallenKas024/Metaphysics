package net.minecraft.network.protocol.login;

import net.minecraft.network.PacketListener;

/**
 * PacketListener for the client side of the LOGIN protocol.
 */
public interface ClientLoginPacketListener extends PacketListener {
   void handleHello(ClientboundHelloPacket pPacket);

   void handleGameProfile(ClientboundGameProfilePacket pPacket);

   void handleDisconnect(ClientboundLoginDisconnectPacket pPacket);

   void handleCompression(ClientboundLoginCompressionPacket pPacket);

   void handleCustomQuery(ClientboundCustomQueryPacket pPacket);
}