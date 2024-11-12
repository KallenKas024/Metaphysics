package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureTemplateManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String STRUCTURE_DIRECTORY_NAME = "structures";
   private static final String TEST_STRUCTURES_DIR = "gameteststructures";
   private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
   private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
   private final Map<ResourceLocation, Optional<StructureTemplate>> structureRepository = Maps.newConcurrentMap();
   private final DataFixer fixerUpper;
   private ResourceManager resourceManager;
   private final Path generatedDir;
   private final List<StructureTemplateManager.Source> sources;
   private final HolderGetter<Block> blockLookup;
   private static final FileToIdConverter LISTER = new FileToIdConverter("structures", ".nbt");

   public StructureTemplateManager(ResourceManager pResourceManager, LevelStorageSource.LevelStorageAccess pLevelStorageAccess, DataFixer pFixerUpper, HolderGetter<Block> pBlockLookup) {
      this.resourceManager = pResourceManager;
      this.fixerUpper = pFixerUpper;
      this.generatedDir = pLevelStorageAccess.getLevelPath(LevelResource.GENERATED_DIR).normalize();
      this.blockLookup = pBlockLookup;
      ImmutableList.Builder<StructureTemplateManager.Source> builder = ImmutableList.builder();
      builder.add(new StructureTemplateManager.Source(this::loadFromGenerated, this::listGenerated));
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         builder.add(new StructureTemplateManager.Source(this::loadFromTestStructures, this::listTestStructures));
      }

      builder.add(new StructureTemplateManager.Source(this::loadFromResource, this::listResources));
      this.sources = builder.build();
   }

   public StructureTemplate getOrCreate(ResourceLocation pId) {
      Optional<StructureTemplate> optional = this.get(pId);
      if (optional.isPresent()) {
         return optional.get();
      } else {
         StructureTemplate structuretemplate = new StructureTemplate();
         this.structureRepository.put(pId, Optional.of(structuretemplate));
         return structuretemplate;
      }
   }

   public Optional<StructureTemplate> get(ResourceLocation pId) {
      return this.structureRepository.computeIfAbsent(pId, this::tryLoad);
   }

   public Stream<ResourceLocation> listTemplates() {
      return this.sources.stream().flatMap((p_230376_) -> {
         return p_230376_.lister().get();
      }).distinct();
   }

   private Optional<StructureTemplate> tryLoad(ResourceLocation p_230426_) {
      for(StructureTemplateManager.Source structuretemplatemanager$source : this.sources) {
         try {
            Optional<StructureTemplate> optional = structuretemplatemanager$source.loader().apply(p_230426_);
            if (optional.isPresent()) {
               return optional;
            }
         } catch (Exception exception) {
         }
      }

      return Optional.empty();
   }

   public void onResourceManagerReload(ResourceManager pResourceManager) {
      this.resourceManager = pResourceManager;
      this.structureRepository.clear();
   }

   private Optional<StructureTemplate> loadFromResource(ResourceLocation p_230428_) {
      ResourceLocation resourcelocation = LISTER.idToFile(p_230428_);
      return this.load(() -> {
         return this.resourceManager.open(resourcelocation);
      }, (p_230366_) -> {
         LOGGER.error("Couldn't load structure {}", p_230428_, p_230366_);
      });
   }

   private Stream<ResourceLocation> listResources() {
      return LISTER.listMatchingResources(this.resourceManager).keySet().stream().map(LISTER::fileToId);
   }

   private Optional<StructureTemplate> loadFromTestStructures(ResourceLocation p_230430_) {
      return this.loadFromSnbt(p_230430_, Paths.get("gameteststructures"));
   }

   private Stream<ResourceLocation> listTestStructures() {
      return this.listFolderContents(Paths.get("gameteststructures"), "minecraft", ".snbt");
   }

   private Optional<StructureTemplate> loadFromGenerated(ResourceLocation p_230432_) {
      if (!Files.isDirectory(this.generatedDir)) {
         return Optional.empty();
      } else {
         Path path = createAndValidatePathToStructure(this.generatedDir, p_230432_, ".nbt");
         return this.load(() -> {
            return new FileInputStream(path.toFile());
         }, (p_230400_) -> {
            LOGGER.error("Couldn't load structure from {}", path, p_230400_);
         });
      }
   }

   private Stream<ResourceLocation> listGenerated() {
      if (!Files.isDirectory(this.generatedDir)) {
         return Stream.empty();
      } else {
         try {
            return Files.list(this.generatedDir).filter((p_230419_) -> {
               return Files.isDirectory(p_230419_);
            }).flatMap((p_230410_) -> {
               return this.listGeneratedInNamespace(p_230410_);
            });
         } catch (IOException ioexception) {
            return Stream.empty();
         }
      }
   }

   private Stream<ResourceLocation> listGeneratedInNamespace(Path pPath) {
      Path path = pPath.resolve("structures");
      return this.listFolderContents(path, pPath.getFileName().toString(), ".nbt");
   }

   private Stream<ResourceLocation> listFolderContents(Path pFolder, String pNamespace, String pPath) {
      if (!Files.isDirectory(pFolder)) {
         return Stream.empty();
      } else {
         int i = pPath.length();
         Function<String, String> function = (p_230358_) -> {
            return p_230358_.substring(0, p_230358_.length() - i);
         };

         try {
            return Files.walk(pFolder).filter((p_230381_) -> {
               return p_230381_.toString().endsWith(pPath);
            }).mapMulti((p_230386_, p_230387_) -> {
               try {
                  p_230387_.accept(new ResourceLocation(pNamespace, function.apply(this.relativize(pFolder, p_230386_))));
               } catch (ResourceLocationException resourcelocationexception) {
                  LOGGER.error("Invalid location while listing pack contents", (Throwable)resourcelocationexception);
               }

            });
         } catch (IOException ioexception) {
            LOGGER.error("Failed to list folder contents", (Throwable)ioexception);
            return Stream.empty();
         }
      }
   }

   private String relativize(Path pRoot, Path pPath) {
      return pRoot.relativize(pPath).toString().replace(File.separator, "/");
   }

   private Optional<StructureTemplate> loadFromSnbt(ResourceLocation pId, Path pPath) {
      if (!Files.isDirectory(pPath)) {
         return Optional.empty();
      } else {
         Path path = FileUtil.createPathToResource(pPath, pId.getPath(), ".snbt");

         try (BufferedReader bufferedreader = Files.newBufferedReader(path)) {
            String s = IOUtils.toString((Reader)bufferedreader);
            return Optional.of(this.readStructure(NbtUtils.snbtToStructure(s)));
         } catch (NoSuchFileException nosuchfileexception) {
            return Optional.empty();
         } catch (CommandSyntaxException | IOException ioexception) {
            LOGGER.error("Couldn't load structure from {}", path, ioexception);
            return Optional.empty();
         }
      }
   }

   private Optional<StructureTemplate> load(StructureTemplateManager.InputStreamOpener pInputStream, Consumer<Throwable> pOnError) {
      try (InputStream inputstream = pInputStream.open()) {
         return Optional.of(this.readStructure(inputstream));
      } catch (FileNotFoundException filenotfoundexception) {
         return Optional.empty();
      } catch (Throwable throwable) {
         pOnError.accept(throwable);
         return Optional.empty();
      }
   }

   private StructureTemplate readStructure(InputStream pStream) throws IOException {
      CompoundTag compoundtag = NbtIo.readCompressed(pStream);
      return this.readStructure(compoundtag);
   }

   public StructureTemplate readStructure(CompoundTag pNbt) {
      StructureTemplate structuretemplate = new StructureTemplate();
      int i = NbtUtils.getDataVersion(pNbt, 500);
      structuretemplate.load(this.blockLookup, DataFixTypes.STRUCTURE.updateToCurrentVersion(this.fixerUpper, pNbt, i));
      return structuretemplate;
   }

   public boolean save(ResourceLocation pId) {
      Optional<StructureTemplate> optional = this.structureRepository.get(pId);
      if (!optional.isPresent()) {
         return false;
      } else {
         StructureTemplate structuretemplate = optional.get();
         Path path = createAndValidatePathToStructure(this.generatedDir, pId, ".nbt");
         Path path1 = path.getParent();
         if (path1 == null) {
            return false;
         } else {
            try {
               Files.createDirectories(Files.exists(path1) ? path1.toRealPath() : path1);
            } catch (IOException ioexception) {
               LOGGER.error("Failed to create parent directory: {}", (Object)path1);
               return false;
            }

            CompoundTag compoundtag = structuretemplate.save(new CompoundTag());

            try {
               try (OutputStream outputstream = new FileOutputStream(path.toFile())) {
                  NbtIo.writeCompressed(compoundtag, outputstream);
               }

               return true;
            } catch (Throwable throwable1) {
               return false;
            }
         }
      }
   }

   public Path getPathToGeneratedStructure(ResourceLocation pId, String pExtension) {
      return createPathToStructure(this.generatedDir, pId, pExtension);
   }

   public static Path createPathToStructure(Path pPath, ResourceLocation pId, String pExtension) {
      try {
         Path path = pPath.resolve(pId.getNamespace());
         Path path1 = path.resolve("structures");
         return FileUtil.createPathToResource(path1, pId.getPath(), pExtension);
      } catch (InvalidPathException invalidpathexception) {
         throw new ResourceLocationException("Invalid resource path: " + pId, invalidpathexception);
      }
   }

   private static Path createAndValidatePathToStructure(Path pPath, ResourceLocation pId, String pExtension) {
      if (pId.getPath().contains("//")) {
         throw new ResourceLocationException("Invalid resource path: " + pId);
      } else {
         Path path = createPathToStructure(pPath, pId, pExtension);
         if (path.startsWith(pPath) && FileUtil.isPathNormalized(path) && FileUtil.isPathPortable(path)) {
            return path;
         } else {
            throw new ResourceLocationException("Invalid resource path: " + path);
         }
      }
   }

   public void remove(ResourceLocation pId) {
      this.structureRepository.remove(pId);
   }

   @FunctionalInterface
   interface InputStreamOpener {
      InputStream open() throws IOException;
   }

   static record Source(Function<ResourceLocation, Optional<StructureTemplate>> loader, Supplier<Stream<ResourceLocation>> lister) {
   }
}