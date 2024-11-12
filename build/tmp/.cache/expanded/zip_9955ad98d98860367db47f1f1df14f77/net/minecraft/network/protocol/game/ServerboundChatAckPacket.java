package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatAckPacket(int offset) implements Packet<ServerGamePacketListener> {
   public ServerboundChatAckPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readVarInt());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.offset);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleChatAck(this);
   }
}