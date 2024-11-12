package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDistancePacket implements Packet<ClientGamePacketListener> {
   private final int warningBlocks;

   public ClientboundSetBorderWarningDistancePacket(WorldBorder pWorldBorder) {
      this.warningBlocks = pWorldBorder.getWarningBlocks();
   }

   public ClientboundSetBorderWarningDistancePacket(FriendlyByteBuf pBuffer) {
      this.warningBlocks = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.warningBlocks);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetBorderWarningDistance(this);
   }

   public int getWarningBlocks() {
      return this.warningBlocks;
   }
}