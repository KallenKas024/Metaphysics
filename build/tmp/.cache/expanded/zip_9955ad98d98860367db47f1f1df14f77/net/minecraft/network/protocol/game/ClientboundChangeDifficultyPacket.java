package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ClientboundChangeDifficultyPacket implements Packet<ClientGamePacketListener> {
   private final Difficulty difficulty;
   private final boolean locked;

   public ClientboundChangeDifficultyPacket(Difficulty pDifficulty, boolean pLocked) {
      this.difficulty = pDifficulty;
      this.locked = pLocked;
   }

   public ClientboundChangeDifficultyPacket(FriendlyByteBuf pBuffer) {
      this.difficulty = Difficulty.byId(pBuffer.readUnsignedByte());
      this.locked = pBuffer.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.difficulty.getId());
      pBuffer.writeBoolean(this.locked);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleChangeDifficulty(this);
   }

   public boolean isLocked() {
      return this.locked;
   }

   public Difficulty getDifficulty() {
      return this.difficulty;
   }
}