package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface ResourceProvider {
   Optional<Resource> getResource(ResourceLocation pLocation);

   default Resource getResourceOrThrow(ResourceLocation pLocation) throws FileNotFoundException {
      return this.getResource(pLocation).orElseThrow(() -> {
         return new FileNotFoundException(pLocation.toString());
      });
   }

   default InputStream open(ResourceLocation pLocation) throws IOException {
      return this.getResourceOrThrow(pLocation).open();
   }

   default BufferedReader openAsReader(ResourceLocation pLocation) throws IOException {
      return this.getResourceOrThrow(pLocation).openAsReader();
   }

   static ResourceProvider fromMap(Map<ResourceLocation, Resource> pResources) {
      return (p_248274_) -> {
         return Optional.ofNullable(pResources.get(p_248274_));
      };
   }
}