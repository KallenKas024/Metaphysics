package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundContainerClosePacket implements Packet<ClientGamePacketListener> {
   private final int containerId;

   public ClientboundContainerClosePacket(int pContainerId) {
      this.containerId = pContainerId;
   }

   public ClientboundContainerClosePacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readUnsignedByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.containerId);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleContainerClose(this);
   }

   public int getContainerId() {
      return this.containerId;
   }
}