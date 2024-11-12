package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.MenuType;

public class ClientboundOpenScreenPacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final MenuType<?> type;
   private final Component title;

   public ClientboundOpenScreenPacket(int pContainerId, MenuType<?> pMenuType, Component pTitle) {
      this.containerId = pContainerId;
      this.type = pMenuType;
      this.title = pTitle;
   }

   public ClientboundOpenScreenPacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readVarInt();
      this.type = pBuffer.readById(BuiltInRegistries.MENU);
      this.title = pBuffer.readComponent();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.containerId);
      pBuffer.writeId(BuiltInRegistries.MENU, this.type);
      pBuffer.writeComponent(this.title);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleOpenScreen(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   @Nullable
   public MenuType<?> getType() {
      return this.type;
   }

   public Component getTitle() {
      return this.title;
   }
}