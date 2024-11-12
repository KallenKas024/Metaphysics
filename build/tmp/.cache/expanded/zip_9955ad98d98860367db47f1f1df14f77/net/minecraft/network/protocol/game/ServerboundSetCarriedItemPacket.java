package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSetCarriedItemPacket implements Packet<ServerGamePacketListener> {
   private final int slot;

   public ServerboundSetCarriedItemPacket(int pSlot) {
      this.slot = pSlot;
   }

   public ServerboundSetCarriedItemPacket(FriendlyByteBuf pBuffer) {
      this.slot = pBuffer.readShort();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeShort(this.slot);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleSetCarriedItem(this);
   }

   public int getSlot() {
      return this.slot;
   }
}