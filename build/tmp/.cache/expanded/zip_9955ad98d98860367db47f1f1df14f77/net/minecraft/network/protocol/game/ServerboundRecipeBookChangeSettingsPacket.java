package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.RecipeBookType;

public class ServerboundRecipeBookChangeSettingsPacket implements Packet<ServerGamePacketListener> {
   private final RecipeBookType bookType;
   private final boolean isOpen;
   private final boolean isFiltering;

   public ServerboundRecipeBookChangeSettingsPacket(RecipeBookType pBookType, boolean pIsOpen, boolean pIsFiltering) {
      this.bookType = pBookType;
      this.isOpen = pIsOpen;
      this.isFiltering = pIsFiltering;
   }

   public ServerboundRecipeBookChangeSettingsPacket(FriendlyByteBuf pBuffer) {
      this.bookType = pBuffer.readEnum(RecipeBookType.class);
      this.isOpen = pBuffer.readBoolean();
      this.isFiltering = pBuffer.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeEnum(this.bookType);
      pBuffer.writeBoolean(this.isOpen);
      pBuffer.writeBoolean(this.isFiltering);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleRecipeBookChangeSettingsPacket(this);
   }

   public RecipeBookType getBookType() {
      return this.bookType;
   }

   public boolean isOpen() {
      return this.isOpen;
   }

   public boolean isFiltering() {
      return this.isFiltering;
   }
}