package net.minecraft.world.level.storage.loot;

/**
 * Represents the registry entry for a serializer for some type T. For example every type of {@link NumberProvider} has
 * a {@link LootNumberProviderType} (which extends SerializerType) that stores its serializer and is registered to a
 * registry to provide the type name in form of the registry ResourceLocation.
 */
public class SerializerType<T> {
   private final Serializer<? extends T> serializer;

   public SerializerType(Serializer<? extends T> pSerializer) {
      this.serializer = pSerializer;
   }

   public Serializer<? extends T> getSerializer() {
      return this.serializer;
   }
}