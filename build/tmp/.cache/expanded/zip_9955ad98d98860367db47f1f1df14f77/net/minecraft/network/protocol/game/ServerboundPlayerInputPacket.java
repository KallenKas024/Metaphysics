package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPlayerInputPacket implements Packet<ServerGamePacketListener> {
   private static final int FLAG_JUMPING = 1;
   private static final int FLAG_SHIFT_KEY_DOWN = 2;
   /** Positive for left strafe, negative for right */
   private final float xxa;
   private final float zza;
   private final boolean isJumping;
   private final boolean isShiftKeyDown;

   public ServerboundPlayerInputPacket(float pXxa, float pZza, boolean pIsJumping, boolean pIsShiftKeyDown) {
      this.xxa = pXxa;
      this.zza = pZza;
      this.isJumping = pIsJumping;
      this.isShiftKeyDown = pIsShiftKeyDown;
   }

   public ServerboundPlayerInputPacket(FriendlyByteBuf pBuffer) {
      this.xxa = pBuffer.readFloat();
      this.zza = pBuffer.readFloat();
      byte b0 = pBuffer.readByte();
      this.isJumping = (b0 & 1) > 0;
      this.isShiftKeyDown = (b0 & 2) > 0;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeFloat(this.xxa);
      pBuffer.writeFloat(this.zza);
      byte b0 = 0;
      if (this.isJumping) {
         b0 = (byte)(b0 | 1);
      }

      if (this.isShiftKeyDown) {
         b0 = (byte)(b0 | 2);
      }

      pBuffer.writeByte(b0);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handlePlayerInput(this);
   }

   public float getXxa() {
      return this.xxa;
   }

   public float getZza() {
      return this.zza;
   }

   public boolean isJumping() {
      return this.isJumping;
   }

   public boolean isShiftKeyDown() {
      return this.isShiftKeyDown;
   }
}