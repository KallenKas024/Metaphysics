package net.minecraft.client.resources;

import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class DownloadedPackSource implements RepositorySource {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
   private static final int MAX_PACK_SIZE_BYTES = 262144000;
   private static final int MAX_KEPT_PACKS = 10;
   private static final String SERVER_ID = "server";
   private static final Component SERVER_NAME = Component.translatable("resourcePack.server.name");
   private static final Component APPLYING_PACK_TEXT = Component.translatable("multiplayer.applyingPack");
   private final File serverPackDir;
   private final ReentrantLock downloadLock = new ReentrantLock();
   @Nullable
   private CompletableFuture<?> currentDownload;
   @Nullable
   private Pack serverPack;

   public DownloadedPackSource(File pSeverPackDir) {
      this.serverPackDir = pSeverPackDir;
   }

   public void loadPacks(Consumer<Pack> pOnLoad) {
      if (this.serverPack != null) {
         pOnLoad.accept(this.serverPack);
      }

   }

   private static Map<String, String> getDownloadHeaders() {
      return Map.of("X-Minecraft-Username", Minecraft.getInstance().getUser().getName(), "X-Minecraft-UUID", Minecraft.getInstance().getUser().getUuid(), "X-Minecraft-Version", SharedConstants.getCurrentVersion().getName(), "X-Minecraft-Version-ID", SharedConstants.getCurrentVersion().getId(), "X-Minecraft-Pack-Format", String.valueOf(SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES)), "User-Agent", "Minecraft Java/" + SharedConstants.getCurrentVersion().getName());
   }

   public CompletableFuture<?> downloadAndSelectResourcePack(URL pUrl, String pHash, boolean pClearScreenAfterStop) {
      String s = Hashing.sha1().hashString(pUrl.toString(), StandardCharsets.UTF_8).toString();
      String s1 = SHA1.matcher(pHash).matches() ? pHash : "";
      this.downloadLock.lock();

      CompletableFuture completablefuture1;
      try {
         Minecraft minecraft = Minecraft.getInstance();
         File file1 = new File(this.serverPackDir, s);
         CompletableFuture<?> completablefuture;
         if (file1.exists()) {
            completablefuture = CompletableFuture.completedFuture("");
         } else {
            ProgressScreen progressscreen = new ProgressScreen(pClearScreenAfterStop);
            Map<String, String> map = getDownloadHeaders();
            minecraft.executeBlocking(() -> {
               minecraft.setScreen(progressscreen);
            });
            completablefuture = HttpUtil.downloadTo(file1, pUrl, map, 262144000, progressscreen, minecraft.getProxy());
         }

         this.currentDownload = completablefuture.thenCompose((p_251155_) -> {
            if (!this.checkHash(s1, file1)) {
               return CompletableFuture.failedFuture(new RuntimeException("Hash check failure for file " + file1 + ", see log"));
            } else {
               minecraft.execute(() -> {
                  if (!pClearScreenAfterStop) {
                     minecraft.setScreen(new GenericDirtMessageScreen(APPLYING_PACK_TEXT));
                  }

               });
               return this.setServerPack(file1, PackSource.SERVER);
            }
         }).exceptionallyCompose((p_249744_) -> {
            return this.clearServerPack().thenAcceptAsync((p_251750_) -> {
               LOGGER.warn("Pack application failed: {}, deleting file {}", p_249744_.getMessage(), file1);
               deleteQuietly(file1);
            }, Util.ioPool()).thenAcceptAsync((p_248937_) -> {
               minecraft.setScreen(new ConfirmScreen((p_249339_) -> {
                  if (p_249339_) {
                     minecraft.setScreen((Screen)null);
                  } else {
                     ClientPacketListener clientpacketlistener = minecraft.getConnection();
                     if (clientpacketlistener != null) {
                        clientpacketlistener.getConnection().disconnect(Component.translatable("connect.aborted"));
                     }
                  }

               }, Component.translatable("multiplayer.texturePrompt.failure.line1"), Component.translatable("multiplayer.texturePrompt.failure.line2"), CommonComponents.GUI_PROCEED, Component.translatable("menu.disconnect")));
            }, minecraft);
         }).thenAcceptAsync((p_250279_) -> {
            this.clearOldDownloads();
         }, Util.ioPool());
         completablefuture1 = this.currentDownload;
      } finally {
         this.downloadLock.unlock();
      }

      return completablefuture1;
   }

   private static void deleteQuietly(File pFile) {
      try {
         Files.delete(pFile.toPath());
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to delete file {}: {}", pFile, ioexception.getMessage());
      }

   }

   public CompletableFuture<Void> clearServerPack() {
      this.downloadLock.lock();

      CompletableFuture completablefuture;
      try {
         if (this.currentDownload != null) {
            this.currentDownload.cancel(true);
         }

         this.currentDownload = null;
         if (this.serverPack == null) {
            return CompletableFuture.completedFuture((Void)null);
         }

         this.serverPack = null;
         completablefuture = Minecraft.getInstance().delayTextureReload();
      } finally {
         this.downloadLock.unlock();
      }

      return completablefuture;
   }

   private boolean checkHash(String pHash, File pFile) {
      try {
         String s = com.google.common.io.Files.asByteSource(pFile).hash(Hashing.sha1()).toString();
         if (pHash.isEmpty()) {
            LOGGER.info("Found file {} without verification hash", (Object)pFile);
            return true;
         }

         if (s.toLowerCase(Locale.ROOT).equals(pHash.toLowerCase(Locale.ROOT))) {
            LOGGER.info("Found file {} matching requested hash {}", pFile, pHash);
            return true;
         }

         LOGGER.warn("File {} had wrong hash (expected {}, found {}).", pFile, pHash, s);
      } catch (IOException ioexception) {
         LOGGER.warn("File {} couldn't be hashed.", pFile, ioexception);
      }

      return false;
   }

   private void clearOldDownloads() {
      if (this.serverPackDir.isDirectory()) {
         try {
            List<File> list = new ArrayList<>(FileUtils.listFiles(this.serverPackDir, TrueFileFilter.TRUE, (IOFileFilter)null));
            list.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            int i = 0;

            for(File file1 : list) {
               if (i++ >= 10) {
                  LOGGER.info("Deleting old server resource pack {}", (Object)file1.getName());
                  FileUtils.deleteQuietly(file1);
               }
            }
         } catch (Exception exception) {
            LOGGER.error("Error while deleting old server resource pack : {}", (Object)exception.getMessage());
         }

      }
   }

   public CompletableFuture<Void> setServerPack(File pFile, PackSource pPackSource) {
      Pack.ResourcesSupplier pack$resourcessupplier = (p_255464_) -> {
         return new FilePackResources(p_255464_, pFile, false);
      };
      Pack.Info pack$info = Pack.readPackInfo("server", pack$resourcessupplier);
      if (pack$info == null) {
         return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid pack metadata at " + pFile));
      } else {
         LOGGER.info("Applying server pack {}", (Object)pFile);
         this.serverPack = Pack.create("server", SERVER_NAME, true, pack$resourcessupplier, pack$info, PackType.CLIENT_RESOURCES, Pack.Position.TOP, true, pPackSource);
         return Minecraft.getInstance().delayTextureReload();
      }
   }

   public CompletableFuture<Void> loadBundledResourcePack(LevelStorageSource.LevelStorageAccess pLevel) {
      Path path = pLevel.getLevelPath(LevelResource.MAP_RESOURCE_FILE);
      return Files.exists(path) && !Files.isDirectory(path) ? this.setServerPack(path.toFile(), PackSource.WORLD) : CompletableFuture.completedFuture((Void)null);
   }
}