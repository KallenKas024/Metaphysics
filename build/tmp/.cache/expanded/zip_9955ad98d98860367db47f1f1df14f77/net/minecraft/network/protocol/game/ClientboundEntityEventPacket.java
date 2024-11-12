package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundEntityEventPacket implements Packet<ClientGamePacketListener> {
   private final int entityId;
   private final byte eventId;

   public ClientboundEntityEventPacket(Entity pEntity, byte pEventId) {
      this.entityId = pEntity.getId();
      this.eventId = pEventId;
   }

   public ClientboundEntityEventPacket(FriendlyByteBuf pBuffer) {
      this.entityId = pBuffer.readInt();
      this.eventId = pBuffer.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.entityId);
      pBuffer.writeByte(this.eventId);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleEntityEvent(this);
   }

   @Nullable
   public Entity getEntity(Level pLevel) {
      return pLevel.getEntity(this.entityId);
   }

   public byte getEventId() {
      return this.eventId;
   }
}