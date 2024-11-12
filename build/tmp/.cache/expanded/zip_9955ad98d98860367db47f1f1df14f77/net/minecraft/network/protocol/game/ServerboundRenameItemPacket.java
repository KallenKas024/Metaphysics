package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundRenameItemPacket implements Packet<ServerGamePacketListener> {
   private final String name;

   public ServerboundRenameItemPacket(String pName) {
      this.name = pName;
   }

   public ServerboundRenameItemPacket(FriendlyByteBuf pBuffer) {
      this.name = pBuffer.readUtf();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUtf(this.name);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleRenameItem(this);
   }

   public String getName() {
      return this.name;
   }
}