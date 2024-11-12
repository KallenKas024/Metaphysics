package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSignUpdatePacket implements Packet<ServerGamePacketListener> {
   private static final int MAX_STRING_LENGTH = 384;
   private final BlockPos pos;
   private final String[] lines;
   private final boolean isFrontText;

   public ServerboundSignUpdatePacket(BlockPos pPos, boolean pIsFrontText, String pLine1, String pLine2, String pLine3, String pLine4) {
      this.pos = pPos;
      this.isFrontText = pIsFrontText;
      this.lines = new String[]{pLine1, pLine2, pLine3, pLine4};
   }

   public ServerboundSignUpdatePacket(FriendlyByteBuf pBuffer) {
      this.pos = pBuffer.readBlockPos();
      this.isFrontText = pBuffer.readBoolean();
      this.lines = new String[4];

      for(int i = 0; i < 4; ++i) {
         this.lines[i] = pBuffer.readUtf(384);
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeBoolean(this.isFrontText);

      for(int i = 0; i < 4; ++i) {
         pBuffer.writeUtf(this.lines[i]);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleSignUpdate(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public boolean isFrontText() {
      return this.isFrontText;
   }

   public String[] getLines() {
      return this.lines;
   }
}