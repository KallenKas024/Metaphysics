package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundBlockDestructionPacket implements Packet<ClientGamePacketListener> {
   private final int id;
   private final BlockPos pos;
   private final int progress;

   public ClientboundBlockDestructionPacket(int pId, BlockPos pPos, int pProgress) {
      this.id = pId;
      this.pos = pPos;
      this.progress = pProgress;
   }

   public ClientboundBlockDestructionPacket(FriendlyByteBuf pBuffer) {
      this.id = pBuffer.readVarInt();
      this.pos = pBuffer.readBlockPos();
      this.progress = pBuffer.readUnsignedByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeByte(this.progress);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleBlockDestruction(this);
   }

   public int getId() {
      return this.id;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getProgress() {
      return this.progress;
   }
}