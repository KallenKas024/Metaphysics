package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface PositionSourceType<T extends PositionSource> {
   /** This PositionSource type represents blocks within the world and a fixed position. */
   PositionSourceType<BlockPositionSource> BLOCK = register("block", new BlockPositionSource.Type());
   /**
    * This PositionSource type represents an entity within the world. This source type will keep a reference to the
    * entity itself.
    */
   PositionSourceType<EntityPositionSource> ENTITY = register("entity", new EntityPositionSource.Type());

   /**
    * Reads a PositionSource from the byte buffer.
    * @return The PositionSource that was read.
    * @param pByteBuf The byte buffer to read from.
    */
   T read(FriendlyByteBuf pByteBuf);

   /**
    * Writes a PositionSource to a byte buffer.
    * @param pByteBuf The byte buffer to write to.
    * @param pSource The PositionSource to write.
    */
   void write(FriendlyByteBuf pByteBuf, T pSource);

   /**
    * Gets a codec that can handle the serialization of PositionSources of this type.
    * @return A codec that can serialize PositionSources of this type.
    */
   Codec<T> codec();

   /**
    * Registers a new PositionSource type with the game registry.
    * @see net.minecraft.core.Registry#POSITION_SOURCE_TYPE
    * @return The newly registered source type.
    * @param pId The Id to register the type to.
    * @param pType The type to register.
    */
   static <S extends PositionSourceType<T>, T extends PositionSource> S register(String pId, S pType) {
      return Registry.register(BuiltInRegistries.POSITION_SOURCE_TYPE, pId, pType);
   }

   /**
    * Reads a PositionSource from a byte buffer. This will first read the Id of the source type which will then be used
    * to deserialize the source itself.
    * @param pByteBuf The buffer to read the PositionSource from.
    */
   static PositionSource fromNetwork(FriendlyByteBuf pByteBuf) {
      ResourceLocation resourcelocation = pByteBuf.readResourceLocation();
      return BuiltInRegistries.POSITION_SOURCE_TYPE.getOptional(resourcelocation).orElseThrow(() -> {
         return new IllegalArgumentException("Unknown position source type " + resourcelocation);
      }).read(pByteBuf);
   }

   /**
    * Writes a PositionSource to a network byte buffer. This will first write the Id of the source type and then write
    * the source itself.
    * @param pSource The PositionSource to write.
    * @param pByteBuf The byte buffer to write to.
    */
   static <T extends PositionSource> void toNetwork(T pSource, FriendlyByteBuf pByteBuf) {
      pByteBuf.writeResourceLocation(BuiltInRegistries.POSITION_SOURCE_TYPE.getKey(pSource.getType()));
      ((PositionSourceType<T>)pSource.getType()).write(pByteBuf, pSource);
   }
}