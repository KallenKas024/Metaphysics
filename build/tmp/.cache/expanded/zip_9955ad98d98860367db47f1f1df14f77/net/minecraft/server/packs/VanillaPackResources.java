package net.minecraft.server.packs;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;

public class VanillaPackResources implements PackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final BuiltInMetadata metadata;
   private final Set<String> namespaces;
   private final List<Path> rootPaths;
   private final Map<PackType, List<Path>> pathsForType;

   VanillaPackResources(BuiltInMetadata pMetadata, Set<String> pNamespaces, List<Path> pRootPaths, Map<PackType, List<Path>> pPathsForType) {
      this.metadata = pMetadata;
      this.namespaces = pNamespaces;
      this.rootPaths = pRootPaths;
      this.pathsForType = pPathsForType;
   }

   @Nullable
   public IoSupplier<InputStream> getRootResource(String... pElements) {
      FileUtil.validatePath(pElements);
      List<String> list = List.of(pElements);

      for(Path path : this.rootPaths) {
         Path path1 = FileUtil.resolvePath(path, list);
         if (Files.exists(path1) && PathPackResources.validatePath(path1)) {
            return IoSupplier.create(path1);
         }
      }

      return null;
   }

   public void listRawPaths(PackType pPackType, ResourceLocation pPackLocation, Consumer<Path> pOutput) {
      FileUtil.decomposePath(pPackLocation.getPath()).get().ifLeft((p_248238_) -> {
         String s = pPackLocation.getNamespace();

         for(Path path : this.pathsForType.get(pPackType)) {
            Path path1 = path.resolve(s);
            pOutput.accept(FileUtil.resolvePath(path1, p_248238_));
         }

      }).ifRight((p_248232_) -> {
         LOGGER.error("Invalid path {}: {}", pPackLocation, p_248232_.message());
      });
   }

   public void listResources(PackType pPackType, String pNamespace, String pPath, PackResources.ResourceOutput pResourceOutput) {
      FileUtil.decomposePath(pPath).get().ifLeft((p_248228_) -> {
         List<Path> list = this.pathsForType.get(pPackType);
         int i = list.size();
         if (i == 1) {
            getResources(pResourceOutput, pNamespace, list.get(0), p_248228_);
         } else if (i > 1) {
            Map<ResourceLocation, IoSupplier<InputStream>> map = new HashMap<>();

            for(int j = 0; j < i - 1; ++j) {
               getResources(map::putIfAbsent, pNamespace, list.get(j), p_248228_);
            }

            Path path = list.get(i - 1);
            if (map.isEmpty()) {
               getResources(pResourceOutput, pNamespace, path, p_248228_);
            } else {
               getResources(map::putIfAbsent, pNamespace, path, p_248228_);
               map.forEach(pResourceOutput);
            }
         }

      }).ifRight((p_248234_) -> {
         LOGGER.error("Invalid path {}: {}", pPath, p_248234_.message());
      });
   }

   private static void getResources(PackResources.ResourceOutput pResourceOutput, String pNamespace, Path pRoot, List<String> pPaths) {
      Path path = pRoot.resolve(pNamespace);
      PathPackResources.listPath(pNamespace, path, pPaths, pResourceOutput);
   }

   @Nullable
   public IoSupplier<InputStream> getResource(PackType pPackType, ResourceLocation pLocation) {
      return FileUtil.decomposePath(pLocation.getPath()).get().map((p_248224_) -> {
         String s = pLocation.getNamespace();

         for(Path path : this.pathsForType.get(pPackType)) {
            Path path1 = FileUtil.resolvePath(path.resolve(s), p_248224_);
            if (Files.exists(path1) && PathPackResources.validatePath(path1)) {
               return IoSupplier.create(path1);
            }
         }

         return null;
      }, (p_248230_) -> {
         LOGGER.error("Invalid path {}: {}", pLocation, p_248230_.message());
         return null;
      });
   }

   public Set<String> getNamespaces(PackType pType) {
      return this.namespaces;
   }

   @Nullable
   public <T> T getMetadataSection(MetadataSectionSerializer<T> pDeserializer) {
      IoSupplier<InputStream> iosupplier = this.getRootResource("pack.mcmeta");
      if (iosupplier != null) {
         try (InputStream inputstream = iosupplier.get()) {
            T t = AbstractPackResources.getMetadataFromStream(pDeserializer, inputstream);
            if (t != null) {
               return t;
            }
         } catch (IOException ioexception) {
         }
      }

      return this.metadata.get(pDeserializer);
   }

   public String packId() {
      return "vanilla";
   }

   public boolean isBuiltin() {
      return true;
   }

   public void close() {
   }

   public ResourceProvider asProvider() {
      return (p_248239_) -> {
         return Optional.ofNullable(this.getResource(PackType.CLIENT_RESOURCES, p_248239_)).map((p_248221_) -> {
            return new Resource(this, p_248221_);
         });
      };
   }
}