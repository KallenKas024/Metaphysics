package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundGameProfilePacket implements Packet<ClientLoginPacketListener> {
   private final GameProfile gameProfile;

   public ClientboundGameProfilePacket(GameProfile pGameProfile) {
      this.gameProfile = pGameProfile;
   }

   public ClientboundGameProfilePacket(FriendlyByteBuf pBuffer) {
      this.gameProfile = pBuffer.readGameProfile();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeGameProfile(this.gameProfile);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientLoginPacketListener pHandler) {
      pHandler.handleGameProfile(this);
   }

   public GameProfile getGameProfile() {
      return this.gameProfile;
   }
}