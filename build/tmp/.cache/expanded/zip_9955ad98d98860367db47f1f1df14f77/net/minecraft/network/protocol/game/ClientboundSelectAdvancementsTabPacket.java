package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundSelectAdvancementsTabPacket implements Packet<ClientGamePacketListener> {
   @Nullable
   private final ResourceLocation tab;

   public ClientboundSelectAdvancementsTabPacket(@Nullable ResourceLocation pTab) {
      this.tab = pTab;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSelectAdvancementsTab(this);
   }

   public ClientboundSelectAdvancementsTabPacket(FriendlyByteBuf pBuffer) {
      this.tab = pBuffer.readNullable(FriendlyByteBuf::readResourceLocation);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeNullable(this.tab, FriendlyByteBuf::writeResourceLocation);
   }

   @Nullable
   public ResourceLocation getTab() {
      return this.tab;
   }
}