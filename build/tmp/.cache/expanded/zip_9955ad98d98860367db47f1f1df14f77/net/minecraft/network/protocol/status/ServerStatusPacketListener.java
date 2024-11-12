package net.minecraft.network.protocol.status;

import net.minecraft.network.protocol.game.ServerPacketListener;

/**
 * PacketListener for the server side of the STATUS protocol.
 */
public interface ServerStatusPacketListener extends ServerPacketListener {
   void handlePingRequest(ServerboundPingRequestPacket pPacket);

   void handleStatusRequest(ServerboundStatusRequestPacket pPacket);
}