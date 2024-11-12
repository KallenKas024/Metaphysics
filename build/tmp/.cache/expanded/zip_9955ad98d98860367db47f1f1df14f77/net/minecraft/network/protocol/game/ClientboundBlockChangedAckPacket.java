package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundBlockChangedAckPacket(int sequence) implements Packet<ClientGamePacketListener> {
   public ClientboundBlockChangedAckPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readVarInt());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.sequence);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleBlockChangedAck(this);
   }
}