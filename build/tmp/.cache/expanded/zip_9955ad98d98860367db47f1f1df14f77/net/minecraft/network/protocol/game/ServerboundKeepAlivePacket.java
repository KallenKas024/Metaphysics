package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundKeepAlivePacket implements Packet<ServerGamePacketListener> {
   private final long id;

   public ServerboundKeepAlivePacket(long pId) {
      this.id = pId;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleKeepAlive(this);
   }

   public ServerboundKeepAlivePacket(FriendlyByteBuf pBuffer) {
      this.id = pBuffer.readLong();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeLong(this.id);
   }

   public long getId() {
      return this.id;
   }
}