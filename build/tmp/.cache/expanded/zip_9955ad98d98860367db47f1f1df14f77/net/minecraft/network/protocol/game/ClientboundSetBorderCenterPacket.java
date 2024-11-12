package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderCenterPacket implements Packet<ClientGamePacketListener> {
   private final double newCenterX;
   private final double newCenterZ;

   public ClientboundSetBorderCenterPacket(WorldBorder pWorldBorder) {
      this.newCenterX = pWorldBorder.getCenterX();
      this.newCenterZ = pWorldBorder.getCenterZ();
   }

   public ClientboundSetBorderCenterPacket(FriendlyByteBuf pBuffer) {
      this.newCenterX = pBuffer.readDouble();
      this.newCenterZ = pBuffer.readDouble();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeDouble(this.newCenterX);
      pBuffer.writeDouble(this.newCenterZ);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetBorderCenter(this);
   }

   public double getNewCenterZ() {
      return this.newCenterZ;
   }

   public double getNewCenterX() {
      return this.newCenterX;
   }
}