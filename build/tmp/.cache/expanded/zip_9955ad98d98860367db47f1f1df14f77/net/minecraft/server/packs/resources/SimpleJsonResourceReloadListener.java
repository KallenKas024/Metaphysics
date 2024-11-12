package net.minecraft.server.packs.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Gson gson;
   private final String directory;

   public SimpleJsonResourceReloadListener(Gson pGson, String pDirectory) {
      this.gson = pGson;
      this.directory = pDirectory;
   }

   /**
    * Performs any reloading that can be done off-thread, such as file IO
    */
   protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
      Map<ResourceLocation, JsonElement> map = new HashMap<>();
      scanDirectory(pResourceManager, this.directory, this.gson, map);
      return map;
   }

   public static void scanDirectory(ResourceManager pResourceManager, String pName, Gson pGson, Map<ResourceLocation, JsonElement> pOutput) {
      FileToIdConverter filetoidconverter = FileToIdConverter.json(pName);

      for(Map.Entry<ResourceLocation, Resource> entry : filetoidconverter.listMatchingResources(pResourceManager).entrySet()) {
         ResourceLocation resourcelocation = entry.getKey();
         ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

         try (Reader reader = entry.getValue().openAsReader()) {
            JsonElement jsonelement = GsonHelper.fromJson(pGson, reader, JsonElement.class);
            JsonElement jsonelement1 = pOutput.put(resourcelocation1, jsonelement);
            if (jsonelement1 != null) {
               throw new IllegalStateException("Duplicate data file ignored with ID " + resourcelocation1);
            }
         } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
            LOGGER.error("Couldn't parse data file {} from {}", resourcelocation1, resourcelocation, jsonparseexception);
         }
      }

   }

   protected ResourceLocation getPreparedPath(ResourceLocation rl) {
      return rl.withPath(this.directory + "/" + rl.getPath() + ".json");
   }
}
