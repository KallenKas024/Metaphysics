package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundLoginCompressionPacket implements Packet<ClientLoginPacketListener> {
   private final int compressionThreshold;

   public ClientboundLoginCompressionPacket(int pCompressionThreshold) {
      this.compressionThreshold = pCompressionThreshold;
   }

   public ClientboundLoginCompressionPacket(FriendlyByteBuf pBuffer) {
      this.compressionThreshold = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.compressionThreshold);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientLoginPacketListener pHandler) {
      pHandler.handleCompression(this);
   }

   public int getCompressionThreshold() {
      return this.compressionThreshold;
   }
}