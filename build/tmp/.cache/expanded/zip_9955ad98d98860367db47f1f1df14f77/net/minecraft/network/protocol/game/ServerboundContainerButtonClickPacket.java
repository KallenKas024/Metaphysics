package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerButtonClickPacket implements Packet<ServerGamePacketListener> {
   private final int containerId;
   private final int buttonId;

   public ServerboundContainerButtonClickPacket(int pContainerId, int pButtonId) {
      this.containerId = pContainerId;
      this.buttonId = pButtonId;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleContainerButtonClick(this);
   }

   public ServerboundContainerButtonClickPacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readByte();
      this.buttonId = pBuffer.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeByte(this.buttonId);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getButtonId() {
      return this.buttonId;
   }
}