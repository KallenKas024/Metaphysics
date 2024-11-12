package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundBlockEntityTagQuery implements Packet<ServerGamePacketListener> {
   private final int transactionId;
   private final BlockPos pos;

   public ServerboundBlockEntityTagQuery(int pTransactionId, BlockPos pPos) {
      this.transactionId = pTransactionId;
      this.pos = pPos;
   }

   public ServerboundBlockEntityTagQuery(FriendlyByteBuf pBuffer) {
      this.transactionId = pBuffer.readVarInt();
      this.pos = pBuffer.readBlockPos();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.transactionId);
      pBuffer.writeBlockPos(this.pos);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleBlockEntityTagQuery(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}