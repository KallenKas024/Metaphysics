package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCommandSuggestionPacket implements Packet<ServerGamePacketListener> {
   private final int id;
   private final String command;

   public ServerboundCommandSuggestionPacket(int pId, String pCommand) {
      this.id = pId;
      this.command = pCommand;
   }

   public ServerboundCommandSuggestionPacket(FriendlyByteBuf pBuffer) {
      this.id = pBuffer.readVarInt();
      this.command = pBuffer.readUtf(32500);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeUtf(this.command, 32500);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleCustomCommandSuggestions(this);
   }

   public int getId() {
      return this.id;
   }

   public String getCommand() {
      return this.command;
   }
}