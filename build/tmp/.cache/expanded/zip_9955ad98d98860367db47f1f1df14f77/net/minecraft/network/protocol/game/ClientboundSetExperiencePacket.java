package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetExperiencePacket implements Packet<ClientGamePacketListener> {
   private final float experienceProgress;
   private final int totalExperience;
   private final int experienceLevel;

   public ClientboundSetExperiencePacket(float pExperienceProgress, int pTotalExperience, int pExperienceLevel) {
      this.experienceProgress = pExperienceProgress;
      this.totalExperience = pTotalExperience;
      this.experienceLevel = pExperienceLevel;
   }

   public ClientboundSetExperiencePacket(FriendlyByteBuf pBuffer) {
      this.experienceProgress = pBuffer.readFloat();
      this.experienceLevel = pBuffer.readVarInt();
      this.totalExperience = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeFloat(this.experienceProgress);
      pBuffer.writeVarInt(this.experienceLevel);
      pBuffer.writeVarInt(this.totalExperience);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetExperience(this);
   }

   public float getExperienceProgress() {
      return this.experienceProgress;
   }

   public int getTotalExperience() {
      return this.totalExperience;
   }

   public int getExperienceLevel() {
      return this.experienceLevel;
   }
}