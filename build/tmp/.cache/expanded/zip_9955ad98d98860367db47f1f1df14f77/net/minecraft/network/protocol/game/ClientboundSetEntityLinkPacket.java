package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundSetEntityLinkPacket implements Packet<ClientGamePacketListener> {
   private final int sourceId;
   /** The entity that is holding the leash, or -1 to clear the holder. */
   private final int destId;

   /**
    * 
    * @param pDestination The entity to link to or {@code null} to break any existing link.
    */
   public ClientboundSetEntityLinkPacket(Entity pSource, @Nullable Entity pDestination) {
      this.sourceId = pSource.getId();
      this.destId = pDestination != null ? pDestination.getId() : 0;
   }

   /**
    * 
    * @param pDestination The entity to link to or {@code null} to break any existing link.
    */
   public ClientboundSetEntityLinkPacket(FriendlyByteBuf pBuffer) {
      this.sourceId = pBuffer.readInt();
      this.destId = pBuffer.readInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.sourceId);
      pBuffer.writeInt(this.destId);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleEntityLinkPacket(this);
   }

   public int getSourceId() {
      return this.sourceId;
   }

   public int getDestId() {
      return this.destId;
   }
}