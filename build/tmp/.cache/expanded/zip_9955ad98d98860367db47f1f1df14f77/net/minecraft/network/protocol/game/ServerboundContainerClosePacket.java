package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerClosePacket implements Packet<ServerGamePacketListener> {
   private final int containerId;

   public ServerboundContainerClosePacket(int pContainerId) {
      this.containerId = pContainerId;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleContainerClose(this);
   }

   public ServerboundContainerClosePacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.containerId);
   }

   public int getContainerId() {
      return this.containerId;
   }
}