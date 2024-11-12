package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRotateHeadPacket implements Packet<ClientGamePacketListener> {
   private final int entityId;
   private final byte yHeadRot;

   public ClientboundRotateHeadPacket(Entity pEntity, byte pYHeadRot) {
      this.entityId = pEntity.getId();
      this.yHeadRot = pYHeadRot;
   }

   public ClientboundRotateHeadPacket(FriendlyByteBuf pBuffer) {
      this.entityId = pBuffer.readVarInt();
      this.yHeadRot = pBuffer.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeByte(this.yHeadRot);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleRotateMob(this);
   }

   public Entity getEntity(Level pLevel) {
      return pLevel.getEntity(this.entityId);
   }

   public byte getYHeadRot() {
      return this.yHeadRot;
   }
}