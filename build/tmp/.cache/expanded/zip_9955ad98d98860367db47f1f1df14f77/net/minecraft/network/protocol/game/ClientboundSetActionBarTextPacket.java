package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetActionBarTextPacket implements Packet<ClientGamePacketListener> {
   private final Component text;

   public ClientboundSetActionBarTextPacket(Component pText) {
      this.text = pText;
   }

   public ClientboundSetActionBarTextPacket(FriendlyByteBuf pBuffer) {
      this.text = pBuffer.readComponent();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeComponent(this.text);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.setActionBarText(this);
   }

   public Component getText() {
      return this.text;
   }
}