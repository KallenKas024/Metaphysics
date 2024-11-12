package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundClientCommandPacket implements Packet<ServerGamePacketListener> {
   private final ServerboundClientCommandPacket.Action action;

   public ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action pAction) {
      this.action = pAction;
   }

   public ServerboundClientCommandPacket(FriendlyByteBuf pBuffer) {
      this.action = pBuffer.readEnum(ServerboundClientCommandPacket.Action.class);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeEnum(this.action);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleClientCommand(this);
   }

   public ServerboundClientCommandPacket.Action getAction() {
      return this.action;
   }

   public static enum Action {
      PERFORM_RESPAWN,
      REQUEST_STATS;
   }
}