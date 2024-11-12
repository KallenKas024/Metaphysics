package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public abstract class BuiltInPackSource implements RepositorySource {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String VANILLA_ID = "vanilla";
   private final PackType packType;
   private final VanillaPackResources vanillaPack;
   private final ResourceLocation packDir;

   public BuiltInPackSource(PackType pPackType, VanillaPackResources pVanillaType, ResourceLocation pPackDir) {
      this.packType = pPackType;
      this.vanillaPack = pVanillaType;
      this.packDir = pPackDir;
   }

   public void loadPacks(Consumer<Pack> pOnLoad) {
      Pack pack = this.createVanillaPack(this.vanillaPack);
      if (pack != null) {
         pOnLoad.accept(pack);
      }

      this.listBundledPacks(pOnLoad);
   }

   @Nullable
   protected abstract Pack createVanillaPack(PackResources pResources);

   protected abstract Component getPackTitle(String pId);

   public VanillaPackResources getVanillaPack() {
      return this.vanillaPack;
   }

   private void listBundledPacks(Consumer<Pack> pPackConsumer) {
      Map<String, Function<String, Pack>> map = new HashMap<>();
      this.populatePackList(map::put);
      map.forEach((p_250371_, p_250946_) -> {
         Pack pack = p_250946_.apply(p_250371_);
         if (pack != null) {
            pPackConsumer.accept(pack);
         }

      });
   }

   protected void populatePackList(BiConsumer<String, Function<String, Pack>> p_250341_) {
      this.vanillaPack.listRawPaths(this.packType, this.packDir, (p_250248_) -> {
         this.discoverPacksInPath(p_250248_, p_250341_);
      });
   }

   protected void discoverPacksInPath(@Nullable Path pDirectoryPath, BiConsumer<String, Function<String, Pack>> p_249898_) {
      if (pDirectoryPath != null && Files.isDirectory(pDirectoryPath)) {
         try {
            FolderRepositorySource.discoverPacks(pDirectoryPath, true, (p_252012_, p_249772_) -> {
               p_249898_.accept(pathToId(p_252012_), (p_250601_) -> {
                  return this.createBuiltinPack(p_250601_, p_249772_, this.getPackTitle(p_250601_));
               });
            });
         } catch (IOException ioexception) {
            LOGGER.warn("Failed to discover packs in {}", pDirectoryPath, ioexception);
         }
      }

   }

   private static String pathToId(Path pPath) {
      return StringUtils.removeEnd(pPath.getFileName().toString(), ".zip");
   }

   @Nullable
   protected abstract Pack createBuiltinPack(String pId, Pack.ResourcesSupplier pResources, Component pTitle);
}