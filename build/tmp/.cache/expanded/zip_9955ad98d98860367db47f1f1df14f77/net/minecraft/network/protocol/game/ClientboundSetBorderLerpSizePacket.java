package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderLerpSizePacket implements Packet<ClientGamePacketListener> {
   private final double oldSize;
   private final double newSize;
   private final long lerpTime;

   public ClientboundSetBorderLerpSizePacket(WorldBorder pWorldBorder) {
      this.oldSize = pWorldBorder.getSize();
      this.newSize = pWorldBorder.getLerpTarget();
      this.lerpTime = pWorldBorder.getLerpRemainingTime();
   }

   public ClientboundSetBorderLerpSizePacket(FriendlyByteBuf pBuffer) {
      this.oldSize = pBuffer.readDouble();
      this.newSize = pBuffer.readDouble();
      this.lerpTime = pBuffer.readVarLong();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeDouble(this.oldSize);
      pBuffer.writeDouble(this.newSize);
      pBuffer.writeVarLong(this.lerpTime);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetBorderLerpSize(this);
   }

   public double getOldSize() {
      return this.oldSize;
   }

   public double getNewSize() {
      return this.newSize;
   }

   public long getLerpTime() {
      return this.lerpTime;
   }
}