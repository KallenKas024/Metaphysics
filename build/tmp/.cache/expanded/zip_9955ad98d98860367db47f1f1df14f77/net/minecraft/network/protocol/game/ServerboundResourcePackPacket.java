package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundResourcePackPacket implements Packet<ServerGamePacketListener> {
   private final ServerboundResourcePackPacket.Action action;

   public ServerboundResourcePackPacket(ServerboundResourcePackPacket.Action pAction) {
      this.action = pAction;
   }

   public ServerboundResourcePackPacket(FriendlyByteBuf pBuffer) {
      this.action = pBuffer.readEnum(ServerboundResourcePackPacket.Action.class);
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
      pHandler.handleResourcePackResponse(this);
   }

   public ServerboundResourcePackPacket.Action getAction() {
      return this.action;
   }

   public static enum Action {
      SUCCESSFULLY_LOADED,
      DECLINED,
      FAILED_DOWNLOAD,
      ACCEPTED;
   }
}