package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class ServerboundSetCommandBlockPacket implements Packet<ServerGamePacketListener> {
   private static final int FLAG_TRACK_OUTPUT = 1;
   private static final int FLAG_CONDITIONAL = 2;
   private static final int FLAG_AUTOMATIC = 4;
   private final BlockPos pos;
   private final String command;
   private final boolean trackOutput;
   private final boolean conditional;
   private final boolean automatic;
   private final CommandBlockEntity.Mode mode;

   public ServerboundSetCommandBlockPacket(BlockPos pPos, String pCommand, CommandBlockEntity.Mode pMode, boolean pTrackOutput, boolean pConditional, boolean pAutomatic) {
      this.pos = pPos;
      this.command = pCommand;
      this.trackOutput = pTrackOutput;
      this.conditional = pConditional;
      this.automatic = pAutomatic;
      this.mode = pMode;
   }

   public ServerboundSetCommandBlockPacket(FriendlyByteBuf pBuffer) {
      this.pos = pBuffer.readBlockPos();
      this.command = pBuffer.readUtf();
      this.mode = pBuffer.readEnum(CommandBlockEntity.Mode.class);
      int i = pBuffer.readByte();
      this.trackOutput = (i & 1) != 0;
      this.conditional = (i & 2) != 0;
      this.automatic = (i & 4) != 0;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeUtf(this.command);
      pBuffer.writeEnum(this.mode);
      int i = 0;
      if (this.trackOutput) {
         i |= 1;
      }

      if (this.conditional) {
         i |= 2;
      }

      if (this.automatic) {
         i |= 4;
      }

      pBuffer.writeByte(i);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleSetCommandBlock(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public String getCommand() {
      return this.command;
   }

   public boolean isTrackOutput() {
      return this.trackOutput;
   }

   public boolean isConditional() {
      return this.conditional;
   }

   public boolean isAutomatic() {
      return this.automatic;
   }

   public CommandBlockEntity.Mode getMode() {
      return this.mode;
   }
}