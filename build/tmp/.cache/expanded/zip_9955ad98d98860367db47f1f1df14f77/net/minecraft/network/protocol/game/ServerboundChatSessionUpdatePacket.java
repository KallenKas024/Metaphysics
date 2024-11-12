package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatSessionUpdatePacket(RemoteChatSession.Data chatSession) implements Packet<ServerGamePacketListener> {
   public ServerboundChatSessionUpdatePacket(FriendlyByteBuf pBuffer) {
      this(RemoteChatSession.Data.read(pBuffer));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      RemoteChatSession.Data.write(pBuffer, this.chatSession);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleChatSessionUpdate(this);
   }
}