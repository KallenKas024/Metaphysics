package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.Util;
import org.slf4j.Logger;

public class VanillaPackResourcesBuilder {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static Consumer<VanillaPackResourcesBuilder> developmentConfig = (p_251787_) -> {
   };
   private static final Map<PackType, Path> ROOT_DIR_BY_TYPE = Util.make(() -> {
      synchronized(VanillaPackResources.class) {
         ImmutableMap.Builder<PackType, Path> builder = ImmutableMap.builder();

         for(PackType packtype : PackType.values()) {
            String s = "/" + packtype.getDirectory() + "/.mcassetsroot";
            URL url = VanillaPackResources.class.getResource(s);
            if (url == null) {
               LOGGER.error("File {} does not exist in classpath", (Object)s);
            } else {
               try {
                  URI uri = url.toURI();
                  String s1 = uri.getScheme();
                  if (!"jar".equals(s1) && !"file".equals(s1)) {
                     LOGGER.warn("Assets URL '{}' uses unexpected schema", (Object)uri);
                  }

                  Path path = safeGetPath(uri);
                  builder.put(packtype, path.getParent());
               } catch (Exception exception) {
                  LOGGER.error("Couldn't resolve path to vanilla assets", (Throwable)exception);
               }
            }
         }

         return builder.build();
      }
   });
   private final Set<Path> rootPaths = new LinkedHashSet<>();
   private final Map<PackType, Set<Path>> pathsForType = new EnumMap<>(PackType.class);
   private BuiltInMetadata metadata = BuiltInMetadata.of();
   private final Set<String> namespaces = new HashSet<>();

   private static Path safeGetPath(URI pUri) throws IOException {
      try {
         return Paths.get(pUri);
      } catch (FileSystemNotFoundException filesystemnotfoundexception) {
      } catch (Throwable throwable) {
         LOGGER.warn("Unable to get path for: {}", pUri, throwable);
      }

      try {
         FileSystems.newFileSystem(pUri, Collections.emptyMap());
      } catch (FileSystemAlreadyExistsException filesystemalreadyexistsexception) {
      }

      return Paths.get(pUri);
   }

   private boolean validateDirPath(Path pPath) {
      if (!Files.exists(pPath)) {
         return false;
      } else if (!Files.isDirectory(pPath)) {
         throw new IllegalArgumentException("Path " + pPath.toAbsolutePath() + " is not directory");
      } else {
         return true;
      }
   }

   private void pushRootPath(Path pRootPath) {
      if (this.validateDirPath(pRootPath)) {
         this.rootPaths.add(pRootPath);
      }

   }

   private void pushPathForType(PackType pPackType, Path pPath) {
      if (this.validateDirPath(pPath)) {
         this.pathsForType.computeIfAbsent(pPackType, (p_250639_) -> {
            return new LinkedHashSet();
         }).add(pPath);
      }

   }

   public VanillaPackResourcesBuilder pushJarResources() {
      ROOT_DIR_BY_TYPE.forEach((p_251514_, p_251979_) -> {
         this.pushRootPath(p_251979_.getParent());
         this.pushPathForType(p_251514_, p_251979_);
      });
      return this;
   }

   public VanillaPackResourcesBuilder pushClasspathResources(PackType pPackType, Class<?> pClazz) {
      Enumeration<URL> enumeration = null;

      try {
         enumeration = pClazz.getClassLoader().getResources(pPackType.getDirectory() + "/");
      } catch (IOException ioexception) {
      }

      while(enumeration != null && enumeration.hasMoreElements()) {
         URL url = enumeration.nextElement();

         try {
            URI uri = url.toURI();
            if ("file".equals(uri.getScheme())) {
               Path path = Paths.get(uri);
               this.pushRootPath(path.getParent());
               this.pushPathForType(pPackType, path);
            }
         } catch (Exception exception) {
            LOGGER.error("Failed to extract path from {}", url, exception);
         }
      }

      return this;
   }

   public VanillaPackResourcesBuilder applyDevelopmentConfig() {
      developmentConfig.accept(this);
      return this;
   }

   public VanillaPackResourcesBuilder pushUniversalPath(Path pPath) {
      this.pushRootPath(pPath);

      for(PackType packtype : PackType.values()) {
         this.pushPathForType(packtype, pPath.resolve(packtype.getDirectory()));
      }

      return this;
   }

   public VanillaPackResourcesBuilder pushAssetPath(PackType pPackType, Path pPath) {
      this.pushRootPath(pPath);
      this.pushPathForType(pPackType, pPath);
      return this;
   }

   public VanillaPackResourcesBuilder setMetadata(BuiltInMetadata pMetadata) {
      this.metadata = pMetadata;
      return this;
   }

   public VanillaPackResourcesBuilder exposeNamespace(String... pNamespaces) {
      this.namespaces.addAll(Arrays.asList(pNamespaces));
      return this;
   }

   public VanillaPackResources build() {
      Map<PackType, List<Path>> map = new EnumMap<>(PackType.class);

      for(PackType packtype : PackType.values()) {
         List<Path> list = copyAndReverse(this.pathsForType.getOrDefault(packtype, Set.of()));
         map.put(packtype, list);
      }

      return new VanillaPackResources(this.metadata, Set.copyOf(this.namespaces), copyAndReverse(this.rootPaths), map);
   }

   private static List<Path> copyAndReverse(Collection<Path> pPaths) {
      List<Path> list = new ArrayList<>(pPaths);
      Collections.reverse(list);
      return List.copyOf(list);
   }
}