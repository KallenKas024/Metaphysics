package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import org.slf4j.Logger;

public class FolderRepositorySource implements RepositorySource {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Path folder;
   private final PackType packType;
   private final PackSource packSource;

   public FolderRepositorySource(Path pFolder, PackType pPackType, PackSource pPackSource) {
      this.folder = pFolder;
      this.packType = pPackType;
      this.packSource = pPackSource;
   }

   private static String nameFromPath(Path pPath) {
      return pPath.getFileName().toString();
   }

   public void loadPacks(Consumer<Pack> pOnLoad) {
      try {
         FileUtil.createDirectoriesSafe(this.folder);
         discoverPacks(this.folder, false, (p_248243_, p_248244_) -> {
            String s = nameFromPath(p_248243_);
            Pack pack = Pack.readMetaAndCreate("file/" + s, Component.literal(s), false, p_248244_, this.packType, Pack.Position.TOP, this.packSource);
            if (pack != null) {
               pOnLoad.accept(pack);
            }

         });
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to list packs in {}", this.folder, ioexception);
      }

   }

   public static void discoverPacks(Path pDirectoryPath, boolean pIsBuiltin, BiConsumer<Path, Pack.ResourcesSupplier> pResources) throws IOException {
      try (DirectoryStream<Path> directorystream = Files.newDirectoryStream(pDirectoryPath)) {
         for(Path path : directorystream) {
            Pack.ResourcesSupplier pack$resourcessupplier = detectPackResources(path, pIsBuiltin);
            if (pack$resourcessupplier != null) {
               pResources.accept(path, pack$resourcessupplier);
            }
         }
      }

   }

   @Nullable
   public static Pack.ResourcesSupplier detectPackResources(Path pDirectoryPath, boolean pIsBuiltin) {
      BasicFileAttributes basicfileattributes;
      try {
         basicfileattributes = Files.readAttributes(pDirectoryPath, BasicFileAttributes.class);
      } catch (NoSuchFileException nosuchfileexception) {
         return null;
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to read properties of '{}', ignoring", pDirectoryPath, ioexception);
         return null;
      }

      if (basicfileattributes.isDirectory() && Files.isRegularFile(pDirectoryPath.resolve("pack.mcmeta"))) {
         return (p_255538_) -> {
            return new PathPackResources(p_255538_, pDirectoryPath, pIsBuiltin);
         };
      } else {
         if (basicfileattributes.isRegularFile() && pDirectoryPath.getFileName().toString().endsWith(".zip")) {
            FileSystem filesystem = pDirectoryPath.getFileSystem();
            if (filesystem == FileSystems.getDefault() || filesystem instanceof LinkFileSystem) {
               File file1 = pDirectoryPath.toFile();
               return (p_255541_) -> {
                  return new FilePackResources(p_255541_, file1, pIsBuiltin);
               };
            }
         }

         LOGGER.info("Found non-pack entry '{}', ignoring", (Object)pDirectoryPath);
         return null;
      }
   }
}