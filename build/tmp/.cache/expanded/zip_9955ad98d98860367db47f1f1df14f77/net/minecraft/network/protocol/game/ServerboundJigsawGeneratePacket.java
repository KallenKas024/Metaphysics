package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundJigsawGeneratePacket implements Packet<ServerGamePacketListener> {
   private final BlockPos pos;
   private final int levels;
   private final boolean keepJigsaws;

   public ServerboundJigsawGeneratePacket(BlockPos pPos, int pLevels, boolean pKeepJigsaws) {
      this.pos = pPos;
      this.levels = pLevels;
      this.keepJigsaws = pKeepJigsaws;
   }

   public ServerboundJigsawGeneratePacket(FriendlyByteBuf pBuffer) {
      this.pos = pBuffer.readBlockPos();
      this.levels = pBuffer.readVarInt();
      this.keepJigsaws = pBuffer.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeVarInt(this.levels);
      pBuffer.writeBoolean(this.keepJigsaws);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleJigsawGenerate(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int levels() {
      return this.levels;
   }

   public boolean keepJigsaws() {
      return this.keepJigsaws;
   }
}