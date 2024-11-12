package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public record ServerboundClientInformationPacket(String language, int viewDistance, ChatVisiblity chatVisibility, boolean chatColors, int modelCustomisation, HumanoidArm mainHand, boolean textFilteringEnabled, boolean allowsListing) implements Packet<ServerGamePacketListener> {
   public static final int MAX_LANGUAGE_LENGTH = 16;

   public ServerboundClientInformationPacket(FriendlyByteBuf pBuffer) {
      this(pBuffer.readUtf(16), pBuffer.readByte(), pBuffer.readEnum(ChatVisiblity.class), pBuffer.readBoolean(), pBuffer.readUnsignedByte(), pBuffer.readEnum(HumanoidArm.class), pBuffer.readBoolean(), pBuffer.readBoolean());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUtf(this.language);
      pBuffer.writeByte(this.viewDistance);
      pBuffer.writeEnum(this.chatVisibility);
      pBuffer.writeBoolean(this.chatColors);
      pBuffer.writeByte(this.modelCustomisation);
      pBuffer.writeEnum(this.mainHand);
      pBuffer.writeBoolean(this.textFilteringEnabled);
      pBuffer.writeBoolean(this.allowsListing);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleClientInformation(this);
   }
}