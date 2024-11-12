package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ClientboundUpdateRecipesPacket implements Packet<ClientGamePacketListener> {
   private final List<Recipe<?>> recipes;

   public ClientboundUpdateRecipesPacket(Collection<Recipe<?>> pRecipes) {
      this.recipes = Lists.newArrayList(pRecipes);
   }

   public ClientboundUpdateRecipesPacket(FriendlyByteBuf pBuffer) {
      this.recipes = pBuffer.readList(ClientboundUpdateRecipesPacket::fromNetwork);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeCollection(this.recipes, ClientboundUpdateRecipesPacket::toNetwork);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleUpdateRecipes(this);
   }

   public List<Recipe<?>> getRecipes() {
      return this.recipes;
   }

   public static Recipe<?> fromNetwork(FriendlyByteBuf p_133648_) {
      ResourceLocation resourcelocation = p_133648_.readResourceLocation();
      ResourceLocation resourcelocation1 = p_133648_.readResourceLocation();
      return BuiltInRegistries.RECIPE_SERIALIZER.getOptional(resourcelocation).orElseThrow(() -> {
         return new IllegalArgumentException("Unknown recipe serializer " + resourcelocation);
      }).fromNetwork(resourcelocation1, p_133648_);
   }

   public static <T extends Recipe<?>> void toNetwork(FriendlyByteBuf p_179470_, T p_179471_) {
      p_179470_.writeResourceLocation(BuiltInRegistries.RECIPE_SERIALIZER.getKey(p_179471_.getSerializer()));
      p_179470_.writeResourceLocation(p_179471_.getId());
      ((net.minecraft.world.item.crafting.RecipeSerializer<T>)p_179471_.getSerializer()).toNetwork(p_179470_, p_179471_);
   }
}