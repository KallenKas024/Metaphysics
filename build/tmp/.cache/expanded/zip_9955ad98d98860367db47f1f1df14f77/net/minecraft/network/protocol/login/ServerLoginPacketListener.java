package net.minecraft.network.protocol.login;

import net.minecraft.network.protocol.game.ServerPacketListener;

/**
 * PacketListener for the server side of the LOGIN protocol.
 */
public interface ServerLoginPacketListener extends ServerPacketListener {
   void handleHello(ServerboundHelloPacket pPacket);

   void handleKey(ServerboundKeyPacket pPacket);

   void handleCustomQueryPacket(ServerboundCustomQueryPacket pPacket);
}