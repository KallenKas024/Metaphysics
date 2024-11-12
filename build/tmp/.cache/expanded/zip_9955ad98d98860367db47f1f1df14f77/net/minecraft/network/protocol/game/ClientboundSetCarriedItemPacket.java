package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetCarriedItemPacket implements Packet<ClientGamePacketListener> {
   private final int slot;

   public ClientboundSetCarriedItemPacket(int pSlot) {
      this.slot = pSlot;
   }

   public ClientboundSetCarriedItemPacket(FriendlyByteBuf pBuffer) {
      this.slot = pBuffer.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.slot);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetCarriedItem(this);
   }

   public int getSlot() {
      return this.slot;
   }
}