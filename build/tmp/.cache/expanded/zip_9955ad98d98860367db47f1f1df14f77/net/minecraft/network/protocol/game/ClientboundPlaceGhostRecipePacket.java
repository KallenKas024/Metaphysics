package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ClientboundPlaceGhostRecipePacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final ResourceLocation recipe;

   public ClientboundPlaceGhostRecipePacket(int pContainerId, Recipe<?> pRecipe) {
      this.containerId = pContainerId;
      this.recipe = pRecipe.getId();
   }

   public ClientboundPlaceGhostRecipePacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readByte();
      this.recipe = pBuffer.readResourceLocation();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pByteBuf) {
      pByteBuf.writeByte(this.containerId);
      pByteBuf.writeResourceLocation(this.recipe);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handlePlaceRecipe(this);
   }

   public ResourceLocation getRecipe() {
      return this.recipe;
   }

   public int getContainerId() {
      return this.containerId;
   }
}