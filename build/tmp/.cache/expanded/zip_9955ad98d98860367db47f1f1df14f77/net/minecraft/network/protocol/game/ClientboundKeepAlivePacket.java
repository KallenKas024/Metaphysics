package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundKeepAlivePacket implements Packet<ClientGamePacketListener> {
   private final long id;

   public ClientboundKeepAlivePacket(long pId) {
      this.id = pId;
   }

   public ClientboundKeepAlivePacket(FriendlyByteBuf pBuffer) {
      this.id = pBuffer.readLong();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeLong(this.id);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleKeepAlive(this);
   }

   public long getId() {
      return this.id;
   }
}