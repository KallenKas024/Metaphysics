package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderSizePacket implements Packet<ClientGamePacketListener> {
   private final double size;

   public ClientboundSetBorderSizePacket(WorldBorder pWorldBorder) {
      this.size = pWorldBorder.getLerpTarget();
   }

   public ClientboundSetBorderSizePacket(FriendlyByteBuf pBuffer) {
      this.size = pBuffer.readDouble();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeDouble(this.size);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetBorderSize(this);
   }

   public double getSize() {
      return this.size;
   }
}