package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class HttpTexture extends SimpleTexture {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SKIN_WIDTH = 64;
   private static final int SKIN_HEIGHT = 64;
   private static final int LEGACY_SKIN_HEIGHT = 32;
   @Nullable
   private final File file;
   private final String urlString;
   private final boolean processLegacySkin;
   @Nullable
   private final Runnable onDownloaded;
   @Nullable
   private CompletableFuture<?> future;
   private boolean uploaded;

   public HttpTexture(@Nullable File pFile, String pUrlString, ResourceLocation pLocation, boolean pProcessLegacySkin, @Nullable Runnable pOnDownloaded) {
      super(pLocation);
      this.file = pFile;
      this.urlString = pUrlString;
      this.processLegacySkin = pProcessLegacySkin;
      this.onDownloaded = pOnDownloaded;
   }

   private void loadCallback(NativeImage pImage) {
      if (this.onDownloaded != null) {
         this.onDownloaded.run();
      }

      Minecraft.getInstance().execute(() -> {
         this.uploaded = true;
         if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
               this.upload(pImage);
            });
         } else {
            this.upload(pImage);
         }

      });
   }

   private void upload(NativeImage pImage) {
      TextureUtil.prepareImage(this.getId(), pImage.getWidth(), pImage.getHeight());
      pImage.upload(0, 0, 0, true);
   }

   public void load(ResourceManager pResourceManager) throws IOException {
      Minecraft.getInstance().execute(() -> {
         if (!this.uploaded) {
            try {
               super.load(pResourceManager);
            } catch (IOException ioexception) {
               LOGGER.warn("Failed to load texture: {}", this.location, ioexception);
            }

            this.uploaded = true;
         }

      });
      if (this.future == null) {
         NativeImage nativeimage;
         if (this.file != null && this.file.isFile()) {
            LOGGER.debug("Loading http texture from local cache ({})", (Object)this.file);
            FileInputStream fileinputstream = new FileInputStream(this.file);
            nativeimage = this.load(fileinputstream);
         } else {
            nativeimage = null;
         }

         if (nativeimage != null) {
            this.loadCallback(nativeimage);
         } else {
            this.future = CompletableFuture.runAsync(() -> {
               HttpURLConnection httpurlconnection = null;
               LOGGER.debug("Downloading http texture from {} to {}", this.urlString, this.file);

               try {
                  httpurlconnection = (HttpURLConnection)(new URL(this.urlString)).openConnection(Minecraft.getInstance().getProxy());
                  httpurlconnection.setDoInput(true);
                  httpurlconnection.setDoOutput(false);
                  httpurlconnection.connect();
                  if (httpurlconnection.getResponseCode() / 100 == 2) {
                     InputStream inputstream;
                     if (this.file != null) {
                        FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), this.file);
                        inputstream = new FileInputStream(this.file);
                     } else {
                        inputstream = httpurlconnection.getInputStream();
                     }

                     Minecraft.getInstance().execute(() -> {
                        NativeImage nativeimage1 = this.load(inputstream);
                        if (nativeimage1 != null) {
                           this.loadCallback(nativeimage1);
                        }

                     });
                     return;
                  }
               } catch (Exception exception) {
                  LOGGER.error("Couldn't download http texture", (Throwable)exception);
                  return;
               } finally {
                  if (httpurlconnection != null) {
                     httpurlconnection.disconnect();
                  }

               }

            }, Util.backgroundExecutor());
         }
      }
   }

   @Nullable
   private NativeImage load(InputStream pStream) {
      NativeImage nativeimage = null;

      try {
         nativeimage = NativeImage.read(pStream);
         if (this.processLegacySkin) {
            nativeimage = this.processLegacySkin(nativeimage);
         }
      } catch (Exception exception) {
         LOGGER.warn("Error while loading the skin texture", (Throwable)exception);
      }

      return nativeimage;
   }

   @Nullable
   private NativeImage processLegacySkin(NativeImage pImage) {
      int i = pImage.getHeight();
      int j = pImage.getWidth();
      if (j == 64 && (i == 32 || i == 64)) {
         boolean flag = i == 32;
         if (flag) {
            NativeImage nativeimage = new NativeImage(64, 64, true);
            nativeimage.copyFrom(pImage);
            pImage.close();
            pImage = nativeimage;
            nativeimage.fillRect(0, 32, 64, 32, 0);
            nativeimage.copyRect(4, 16, 16, 32, 4, 4, true, false);
            nativeimage.copyRect(8, 16, 16, 32, 4, 4, true, false);
            nativeimage.copyRect(0, 20, 24, 32, 4, 12, true, false);
            nativeimage.copyRect(4, 20, 16, 32, 4, 12, true, false);
            nativeimage.copyRect(8, 20, 8, 32, 4, 12, true, false);
            nativeimage.copyRect(12, 20, 16, 32, 4, 12, true, false);
            nativeimage.copyRect(44, 16, -8, 32, 4, 4, true, false);
            nativeimage.copyRect(48, 16, -8, 32, 4, 4, true, false);
            nativeimage.copyRect(40, 20, 0, 32, 4, 12, true, false);
            nativeimage.copyRect(44, 20, -8, 32, 4, 12, true, false);
            nativeimage.copyRect(48, 20, -16, 32, 4, 12, true, false);
            nativeimage.copyRect(52, 20, -8, 32, 4, 12, true, false);
         }

         setNoAlpha(pImage, 0, 0, 32, 16);
         if (flag) {
            doNotchTransparencyHack(pImage, 32, 0, 64, 32);
         }

         setNoAlpha(pImage, 0, 16, 64, 32);
         setNoAlpha(pImage, 16, 48, 48, 64);
         return pImage;
      } else {
         pImage.close();
         LOGGER.warn("Discarding incorrectly sized ({}x{}) skin texture from {}", j, i, this.urlString);
         return null;
      }
   }

   private static void doNotchTransparencyHack(NativeImage pImage, int pX, int pY, int pWidth, int pHeight) {
      for(int i = pX; i < pWidth; ++i) {
         for(int j = pY; j < pHeight; ++j) {
            int k = pImage.getPixelRGBA(i, j);
            if ((k >> 24 & 255) < 128) {
               return;
            }
         }
      }

      for(int l = pX; l < pWidth; ++l) {
         for(int i1 = pY; i1 < pHeight; ++i1) {
            pImage.setPixelRGBA(l, i1, pImage.getPixelRGBA(l, i1) & 16777215);
         }
      }

   }

   private static void setNoAlpha(NativeImage pImage, int pX, int pY, int pWidth, int pHeight) {
      for(int i = pX; i < pWidth; ++i) {
         for(int j = pY; j < pHeight; ++j) {
            pImage.setPixelRGBA(i, j, pImage.getPixelRGBA(i, j) | -16777216);
         }
      }

   }
}