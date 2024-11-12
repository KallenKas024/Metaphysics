package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundRecipeBookSeenRecipePacket implements Packet<ServerGamePacketListener> {
   private final ResourceLocation recipe;

   public ServerboundRecipeBookSeenRecipePacket(Recipe<?> pRecipe) {
      this.recipe = pRecipe.getId();
   }

   public ServerboundRecipeBookSeenRecipePacket(FriendlyByteBuf pBuffer) {
      this.recipe = pBuffer.readResourceLocation();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeResourceLocation(this.recipe);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleRecipeBookSeenRecipePacket(this);
   }

   public ResourceLocation getRecipe() {
      return this.recipe;
   }
}