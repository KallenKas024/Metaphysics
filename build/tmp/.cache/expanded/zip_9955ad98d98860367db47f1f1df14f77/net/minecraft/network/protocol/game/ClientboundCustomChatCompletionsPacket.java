package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundCustomChatCompletionsPacket(ClientboundCustomChatCompletionsPacket.Action action, List<String> entries) implements Packet<ClientGamePacketListener> {
   public ClientboundCustomChatCompletionsPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readEnum(ClientboundCustomChatCompletionsPacket.Action.class), pBuffer.readList(FriendlyByteBuf::readUtf));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeEnum(this.action);
      pBuffer.writeCollection(this.entries, FriendlyByteBuf::writeUtf);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleCustomChatCompletions(this);
   }

   public static enum Action {
      ADD,
      REMOVE,
      SET;
   }
}