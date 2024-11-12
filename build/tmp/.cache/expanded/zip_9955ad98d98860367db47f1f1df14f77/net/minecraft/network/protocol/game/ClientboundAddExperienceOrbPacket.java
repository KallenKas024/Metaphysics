package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.ExperienceOrb;

public class ClientboundAddExperienceOrbPacket implements Packet<ClientGamePacketListener> {
   private final int id;
   private final double x;
   private final double y;
   private final double z;
   private final int value;

   public ClientboundAddExperienceOrbPacket(ExperienceOrb pOrbEntity) {
      this.id = pOrbEntity.getId();
      this.x = pOrbEntity.getX();
      this.y = pOrbEntity.getY();
      this.z = pOrbEntity.getZ();
      this.value = pOrbEntity.getValue();
   }

   public ClientboundAddExperienceOrbPacket(FriendlyByteBuf pBuffer) {
      this.id = pBuffer.readVarInt();
      this.x = pBuffer.readDouble();
      this.y = pBuffer.readDouble();
      this.z = pBuffer.readDouble();
      this.value = pBuffer.readShort();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeShort(this.value);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleAddExperienceOrb(this);
   }

   public int getId() {
      return this.id;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public int getValue() {
      return this.value;
   }
}