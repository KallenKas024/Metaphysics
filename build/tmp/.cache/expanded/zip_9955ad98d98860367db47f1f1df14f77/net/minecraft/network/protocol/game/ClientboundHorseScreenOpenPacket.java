package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundHorseScreenOpenPacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final int size;
   private final int entityId;

   public ClientboundHorseScreenOpenPacket(int pContainerId, int pSize, int pEntityId) {
      this.containerId = pContainerId;
      this.size = pSize;
      this.entityId = pEntityId;
   }

   public ClientboundHorseScreenOpenPacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readUnsignedByte();
      this.size = pBuffer.readVarInt();
      this.entityId = pBuffer.readInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeVarInt(this.size);
      pBuffer.writeInt(this.entityId);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleHorseScreenOpen(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getSize() {
      return this.size;
   }

   public int getEntityId() {
      return this.entityId;
   }
}