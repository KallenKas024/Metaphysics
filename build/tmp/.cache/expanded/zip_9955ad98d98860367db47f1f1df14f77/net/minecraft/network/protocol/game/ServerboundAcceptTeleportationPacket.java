package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundAcceptTeleportationPacket implements Packet<ServerGamePacketListener> {
   private final int id;

   public ServerboundAcceptTeleportationPacket(int pId) {
      this.id = pId;
   }

   public ServerboundAcceptTeleportationPacket(FriendlyByteBuf pBuffer) {
      this.id = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.id);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleAcceptTeleportPacket(this);
   }

   public int getId() {
      return this.id;
   }
}