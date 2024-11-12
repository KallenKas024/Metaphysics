package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundDisconnectPacket implements Packet<ClientGamePacketListener> {
   private final Component reason;

   public ClientboundDisconnectPacket(Component pReason) {
      this.reason = pReason;
   }

   public ClientboundDisconnectPacket(FriendlyByteBuf pBuffer) {
      this.reason = pBuffer.readComponent();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeComponent(this.reason);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleDisconnect(this);
   }

   public Component getReason() {
      return this.reason;
   }
}