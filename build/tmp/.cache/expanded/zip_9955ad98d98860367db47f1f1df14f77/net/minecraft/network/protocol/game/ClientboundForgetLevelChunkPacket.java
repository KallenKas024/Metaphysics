package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundForgetLevelChunkPacket implements Packet<ClientGamePacketListener> {
   private final int x;
   private final int z;

   public ClientboundForgetLevelChunkPacket(int pX, int pZ) {
      this.x = pX;
      this.z = pZ;
   }

   public ClientboundForgetLevelChunkPacket(FriendlyByteBuf pBuffer) {
      this.x = pBuffer.readInt();
      this.z = pBuffer.readInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.x);
      pBuffer.writeInt(this.z);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleForgetLevelChunk(this);
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }
}