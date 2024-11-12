package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBookSettings;

public class ClientboundRecipePacket implements Packet<ClientGamePacketListener> {
   private final ClientboundRecipePacket.State state;
   private final List<ResourceLocation> recipes;
   private final List<ResourceLocation> toHighlight;
   private final RecipeBookSettings bookSettings;

   public ClientboundRecipePacket(ClientboundRecipePacket.State pState, Collection<ResourceLocation> pRecipes, Collection<ResourceLocation> pToHighlight, RecipeBookSettings pBookSettings) {
      this.state = pState;
      this.recipes = ImmutableList.copyOf(pRecipes);
      this.toHighlight = ImmutableList.copyOf(pToHighlight);
      this.bookSettings = pBookSettings;
   }

   public ClientboundRecipePacket(FriendlyByteBuf pBuffer) {
      this.state = pBuffer.readEnum(ClientboundRecipePacket.State.class);
      this.bookSettings = RecipeBookSettings.read(pBuffer);
      this.recipes = pBuffer.readList(FriendlyByteBuf::readResourceLocation);
      if (this.state == ClientboundRecipePacket.State.INIT) {
         this.toHighlight = pBuffer.readList(FriendlyByteBuf::readResourceLocation);
      } else {
         this.toHighlight = ImmutableList.of();
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeEnum(this.state);
      this.bookSettings.write(pBuffer);
      pBuffer.writeCollection(this.recipes, FriendlyByteBuf::writeResourceLocation);
      if (this.state == ClientboundRecipePacket.State.INIT) {
         pBuffer.writeCollection(this.toHighlight, FriendlyByteBuf::writeResourceLocation);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleAddOrRemoveRecipes(this);
   }

   public List<ResourceLocation> getRecipes() {
      return this.recipes;
   }

   public List<ResourceLocation> getHighlights() {
      return this.toHighlight;
   }

   public RecipeBookSettings getBookSettings() {
      return this.bookSettings;
   }

   public ClientboundRecipePacket.State getState() {
      return this.state;
   }

   public static enum State {
      INIT,
      ADD,
      REMOVE;
   }
}