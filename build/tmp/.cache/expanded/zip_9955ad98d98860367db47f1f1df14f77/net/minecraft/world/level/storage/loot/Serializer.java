package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

/**
 * A serializer and deserializer for values of type {@code T} to and from JSON.
 */
public interface Serializer<T> {
   /**
    * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
    */
   void serialize(JsonObject pJson, T pValue, JsonSerializationContext pSerializationContext);

   /**
    * Deserialize a value by reading it from the JsonObject.
    */
   T deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext);
}