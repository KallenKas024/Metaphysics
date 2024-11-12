package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundLockDifficultyPacket implements Packet<ServerGamePacketListener> {
   private final boolean locked;

   public ServerboundLockDifficultyPacket(boolean pLocked) {
      this.locked = pLocked;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleLockDifficulty(this);
   }

   public ServerboundLockDifficultyPacket(FriendlyByteBuf pBuffer) {
      this.locked = pBuffer.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBoolean(this.locked);
   }

   public boolean isLocked() {
      return this.locked;
   }
}