package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundStatusResponsePacket(ServerStatus status, @org.jetbrains.annotations.Nullable String cachedStatus) implements Packet<ClientStatusPacketListener> {
   public ClientboundStatusResponsePacket(ServerStatus status) {
      this(status, null);
   }

   public ClientboundStatusResponsePacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readJsonWithCodec(ServerStatus.CODEC));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      if (cachedStatus != null) pBuffer.writeUtf(cachedStatus);
      else
      pBuffer.writeJsonWithCodec(ServerStatus.CODEC, this.status);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientStatusPacketListener pHandler) {
      pHandler.handleStatusResponse(this);
   }
}
