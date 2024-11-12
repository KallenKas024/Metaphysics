package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDelayPacket implements Packet<ClientGamePacketListener> {
   private final int warningDelay;

   public ClientboundSetBorderWarningDelayPacket(WorldBorder pWorldBorder) {
      this.warningDelay = pWorldBorder.getWarningTime();
   }

   public ClientboundSetBorderWarningDelayPacket(FriendlyByteBuf pBuffer) {
      this.warningDelay = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.warningDelay);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetBorderWarningDelay(this);
   }

   public int getWarningDelay() {
      return this.warningDelay;
   }
}