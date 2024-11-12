package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPaddleBoatPacket implements Packet<ServerGamePacketListener> {
   private final boolean left;
   private final boolean right;

   public ServerboundPaddleBoatPacket(boolean pLeft, boolean pRight) {
      this.left = pLeft;
      this.right = pRight;
   }

   public ServerboundPaddleBoatPacket(FriendlyByteBuf pBuffer) {
      this.left = pBuffer.readBoolean();
      this.right = pBuffer.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBoolean(this.left);
      pBuffer.writeBoolean(this.right);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handlePaddleBoat(this);
   }

   public boolean getLeft() {
      return this.left;
   }

   public boolean getRight() {
      return this.right;
   }
}