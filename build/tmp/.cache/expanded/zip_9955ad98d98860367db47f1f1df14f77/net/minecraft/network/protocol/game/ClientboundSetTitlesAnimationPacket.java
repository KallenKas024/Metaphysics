package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetTitlesAnimationPacket implements Packet<ClientGamePacketListener> {
   private final int fadeIn;
   private final int stay;
   private final int fadeOut;

   public ClientboundSetTitlesAnimationPacket(int pFadeIn, int pStay, int pFadeOut) {
      this.fadeIn = pFadeIn;
      this.stay = pStay;
      this.fadeOut = pFadeOut;
   }

   public ClientboundSetTitlesAnimationPacket(FriendlyByteBuf pBuffer) {
      this.fadeIn = pBuffer.readInt();
      this.stay = pBuffer.readInt();
      this.fadeOut = pBuffer.readInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.fadeIn);
      pBuffer.writeInt(this.stay);
      pBuffer.writeInt(this.fadeOut);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.setTitlesAnimation(this);
   }

   public int getFadeIn() {
      return this.fadeIn;
   }

   public int getStay() {
      return this.stay;
   }

   public int getFadeOut() {
      return this.fadeOut;
   }
}