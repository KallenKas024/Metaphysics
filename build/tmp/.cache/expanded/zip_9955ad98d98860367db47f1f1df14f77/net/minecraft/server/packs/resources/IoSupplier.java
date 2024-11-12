package net.minecraft.server.packs.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@FunctionalInterface
public interface IoSupplier<T> {
   static IoSupplier<InputStream> create(Path pPath) {
      return () -> {
         return Files.newInputStream(pPath);
      };
   }

   static IoSupplier<InputStream> create(ZipFile pZipFile, ZipEntry pZipEntry) {
      return () -> {
         return pZipFile.getInputStream(pZipEntry);
      };
   }

   T get() throws IOException;
}