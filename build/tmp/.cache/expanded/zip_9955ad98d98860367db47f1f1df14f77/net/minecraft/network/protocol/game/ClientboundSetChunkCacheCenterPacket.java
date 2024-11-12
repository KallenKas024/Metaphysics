package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetChunkCacheCenterPacket implements Packet<ClientGamePacketListener> {
   private final int x;
   private final int z;

   public ClientboundSetChunkCacheCenterPacket(int pX, int pZ) {
      this.x = pX;
      this.z = pZ;
   }

   public ClientboundSetChunkCacheCenterPacket(FriendlyByteBuf pBuffer) {
      this.x = pBuffer.readVarInt();
      this.z = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.x);
      pBuffer.writeVarInt(this.z);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetChunkCacheCenter(this);
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }
}