package net.minecraft.server.packs.metadata;

import com.google.gson.JsonObject;

public interface MetadataSectionSerializer<T> {
   /**
    * The name of this section type as it appears in JSON.
    */
   String getMetadataSectionName();

   T fromJson(JsonObject pJson);
}