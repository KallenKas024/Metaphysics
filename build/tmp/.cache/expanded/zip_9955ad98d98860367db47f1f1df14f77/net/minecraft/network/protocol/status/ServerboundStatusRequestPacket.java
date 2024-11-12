package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundStatusRequestPacket implements Packet<ServerStatusPacketListener> {
   public ServerboundStatusRequestPacket() {
   }

   public ServerboundStatusRequestPacket(FriendlyByteBuf pBuffer) {
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerStatusPacketListener pHandler) {
      pHandler.handleStatusRequest(this);
   }
}