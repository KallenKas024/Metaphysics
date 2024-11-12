package net.minecraft.network.protocol.game;

import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatPacket(String message, Instant timeStamp, long salt, @Nullable MessageSignature signature, LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener> {
   public ServerboundChatPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readUtf(256), pBuffer.readInstant(), pBuffer.readLong(), pBuffer.readNullable(MessageSignature::read), new LastSeenMessages.Update(pBuffer));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUtf(this.message, 256);
      pBuffer.writeInstant(this.timeStamp);
      pBuffer.writeLong(this.salt);
      pBuffer.writeNullable(this.signature, MessageSignature::write);
      this.lastSeenMessages.write(pBuffer);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleChat(this);
   }
}