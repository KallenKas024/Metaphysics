package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetChunkCacheRadiusPacket implements Packet<ClientGamePacketListener> {
   private final int radius;

   public ClientboundSetChunkCacheRadiusPacket(int pRadius) {
      this.radius = pRadius;
   }

   public ClientboundSetChunkCacheRadiusPacket(FriendlyByteBuf pBuffer) {
      this.radius = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.radius);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetChunkCacheRadius(this);
   }

   public int getRadius() {
      return this.radius;
   }
}