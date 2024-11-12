package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetDefaultSpawnPositionPacket implements Packet<ClientGamePacketListener> {
   private final BlockPos pos;
   private final float angle;

   public ClientboundSetDefaultSpawnPositionPacket(BlockPos pPos, float pAngle) {
      this.pos = pPos;
      this.angle = pAngle;
   }

   public ClientboundSetDefaultSpawnPositionPacket(FriendlyByteBuf pBuffer) {
      this.pos = pBuffer.readBlockPos();
      this.angle = pBuffer.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeFloat(this.angle);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetSpawn(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public float getAngle() {
      return this.angle;
   }
}