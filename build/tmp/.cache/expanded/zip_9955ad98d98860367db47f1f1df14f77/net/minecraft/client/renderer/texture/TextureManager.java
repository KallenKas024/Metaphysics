package net.minecraft.client.renderer.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureManager implements PreparableReloadListener, Tickable, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
   private final Map<ResourceLocation, AbstractTexture> byPath = Maps.newHashMap();
   private final Set<Tickable> tickableTextures = Sets.newHashSet();
   private final Map<String, Integer> prefixRegister = Maps.newHashMap();
   private final ResourceManager resourceManager;

   public TextureManager(ResourceManager pResourceManager) {
      this.resourceManager = pResourceManager;
   }

   public void bindForSetup(ResourceLocation pPath) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this._bind(pPath);
         });
      } else {
         this._bind(pPath);
      }

   }

   private void _bind(ResourceLocation pPath) {
      AbstractTexture abstracttexture = this.byPath.get(pPath);
      if (abstracttexture == null) {
         abstracttexture = new SimpleTexture(pPath);
         this.register(pPath, abstracttexture);
      }

      abstracttexture.bind();
   }

   public void register(ResourceLocation pPath, AbstractTexture pTexture) {
      pTexture = this.loadTexture(pPath, pTexture);
      AbstractTexture abstracttexture = this.byPath.put(pPath, pTexture);
      if (abstracttexture != pTexture) {
         if (abstracttexture != null && abstracttexture != MissingTextureAtlasSprite.getTexture()) {
            this.safeClose(pPath, abstracttexture);
         }

         if (pTexture instanceof Tickable) {
            this.tickableTextures.add((Tickable)pTexture);
         }
      }

   }

   private void safeClose(ResourceLocation p_118509_, AbstractTexture p_118510_) {
      if (p_118510_ != MissingTextureAtlasSprite.getTexture()) {
         this.tickableTextures.remove(p_118510_);

         try {
            p_118510_.close();
         } catch (Exception exception) {
            LOGGER.warn("Failed to close texture {}", p_118509_, exception);
         }
      }

      p_118510_.releaseId();
   }

   private AbstractTexture loadTexture(ResourceLocation pPath, AbstractTexture pTexture) {
      try {
         pTexture.load(this.resourceManager);
         return pTexture;
      } catch (IOException ioexception) {
         if (pPath != INTENTIONAL_MISSING_TEXTURE) {
            LOGGER.warn("Failed to load texture: {}", pPath, ioexception);
         }

         return MissingTextureAtlasSprite.getTexture();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Registering texture");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Resource location being registered");
         crashreportcategory.setDetail("Resource location", pPath);
         crashreportcategory.setDetail("Texture object class", () -> {
            return pTexture.getClass().getName();
         });
         throw new ReportedException(crashreport);
      }
   }

   public AbstractTexture getTexture(ResourceLocation pPath) {
      AbstractTexture abstracttexture = this.byPath.get(pPath);
      if (abstracttexture == null) {
         abstracttexture = new SimpleTexture(pPath);
         this.register(pPath, abstracttexture);
      }

      return abstracttexture;
   }

   public AbstractTexture getTexture(ResourceLocation pPath, AbstractTexture pDefaultTexture) {
      return this.byPath.getOrDefault(pPath, pDefaultTexture);
   }

   public ResourceLocation register(String pName, DynamicTexture pTexture) {
      Integer integer = this.prefixRegister.get(pName);
      if (integer == null) {
         integer = 1;
      } else {
         integer = integer + 1;
      }

      this.prefixRegister.put(pName, integer);
      ResourceLocation resourcelocation = new ResourceLocation(String.format(Locale.ROOT, "dynamic/%s_%d", pName, integer));
      this.register(resourcelocation, pTexture);
      return resourcelocation;
   }

   public CompletableFuture<Void> preload(ResourceLocation pPath, Executor pBackgroundExecutor) {
      if (!this.byPath.containsKey(pPath)) {
         PreloadedTexture preloadedtexture = new PreloadedTexture(this.resourceManager, pPath, pBackgroundExecutor);
         this.byPath.put(pPath, preloadedtexture);
         return preloadedtexture.getFuture().thenRunAsync(() -> {
            this.register(pPath, preloadedtexture);
         }, TextureManager::execute);
      } else {
         return CompletableFuture.completedFuture((Void)null);
      }
   }

   private static void execute(Runnable p_118489_) {
      Minecraft.getInstance().execute(() -> {
         RenderSystem.recordRenderCall(p_118489_::run);
      });
   }

   public void tick() {
      for(Tickable tickable : this.tickableTextures) {
         tickable.tick();
      }

   }

   public void release(ResourceLocation pPath) {
      AbstractTexture abstracttexture = this.byPath.remove(pPath);
      if (abstracttexture != null) {
         this.byPath.remove(pPath); // Forge: fix MC-98707
         this.safeClose(pPath, abstracttexture);
      }

   }

   public void close() {
      this.byPath.forEach(this::safeClose);
      this.byPath.clear();
      this.tickableTextures.clear();
      this.prefixRegister.clear();
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pStage, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
      CompletableFuture<Void> completablefuture = new CompletableFuture<>();
      CompletableFuture.allOf(TitleScreen.preloadResources(this, pBackgroundExecutor), this.preload(AbstractWidget.WIDGETS_LOCATION, pBackgroundExecutor)).thenCompose(pStage::wait).thenAcceptAsync((p_247950_) -> {
         MissingTextureAtlasSprite.getTexture();
         RealmsMainScreen.updateTeaserImages(this.resourceManager);
         Iterator<Map.Entry<ResourceLocation, AbstractTexture>> iterator = this.byPath.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry<ResourceLocation, AbstractTexture> entry = iterator.next();
            ResourceLocation resourcelocation = entry.getKey();
            AbstractTexture abstracttexture = entry.getValue();
            if (abstracttexture == MissingTextureAtlasSprite.getTexture() && !resourcelocation.equals(MissingTextureAtlasSprite.getLocation())) {
               iterator.remove();
            } else {
               abstracttexture.reset(this, pResourceManager, resourcelocation, pGameExecutor);
            }
         }

         Minecraft.getInstance().tell(() -> {
            completablefuture.complete((Void)null);
         });
      }, (p_118505_) -> {
         RenderSystem.recordRenderCall(p_118505_::run);
      });
      return completablefuture;
   }

   public void dumpAllSheets(Path pPath) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this._dumpAllSheets(pPath);
         });
      } else {
         this._dumpAllSheets(pPath);
      }

   }

   private void _dumpAllSheets(Path pPath) {
      try {
         Files.createDirectories(pPath);
      } catch (IOException ioexception) {
         LOGGER.error("Failed to create directory {}", pPath, ioexception);
         return;
      }

      this.byPath.forEach((p_276101_, p_276102_) -> {
         if (p_276102_ instanceof Dumpable dumpable) {
            try {
               dumpable.dumpContents(p_276101_, pPath);
            } catch (IOException ioexception1) {
               LOGGER.error("Failed to dump texture {}", p_276101_, ioexception1);
            }
         }

      });
   }
}
