package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public class TextureUtil {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int MIN_MIPMAP_LEVEL = 0;
   private static final int DEFAULT_IMAGE_BUFFER_SIZE = 8192;

   public static int generateTextureId() {
      RenderSystem.assertOnRenderThreadOrInit();
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         int[] aint = new int[ThreadLocalRandom.current().nextInt(15) + 1];
         GlStateManager._genTextures(aint);
         int i = GlStateManager._genTexture();
         GlStateManager._deleteTextures(aint);
         return i;
      } else {
         return GlStateManager._genTexture();
      }
   }

   public static void releaseTextureId(int pTextureId) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._deleteTexture(pTextureId);
   }

   public static void prepareImage(int pTextureId, int pWidth, int pHeight) {
      prepareImage(NativeImage.InternalGlFormat.RGBA, pTextureId, 0, pWidth, pHeight);
   }

   public static void prepareImage(NativeImage.InternalGlFormat pPixelFormat, int pTextureId, int pWidth, int pHeight) {
      prepareImage(pPixelFormat, pTextureId, 0, pWidth, pHeight);
   }

   public static void prepareImage(int pTextureId, int pMipmapLevel, int pWidth, int pHeight) {
      prepareImage(NativeImage.InternalGlFormat.RGBA, pTextureId, pMipmapLevel, pWidth, pHeight);
   }

   public static void prepareImage(NativeImage.InternalGlFormat pPixelFormat, int pTextureId, int pMipmapLevel, int pWidth, int pHeight) {
      RenderSystem.assertOnRenderThreadOrInit();
      bind(pTextureId);
      if (pMipmapLevel >= 0) {
         GlStateManager._texParameter(3553, 33085, pMipmapLevel);
         GlStateManager._texParameter(3553, 33082, 0);
         GlStateManager._texParameter(3553, 33083, pMipmapLevel);
         GlStateManager._texParameter(3553, 34049, 0.0F);
      }

      for(int i = 0; i <= pMipmapLevel; ++i) {
         GlStateManager._texImage2D(3553, i, pPixelFormat.glFormat(), pWidth >> i, pHeight >> i, 0, 6408, 5121, (IntBuffer)null);
      }

   }

   private static void bind(int pTextureId) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._bindTexture(pTextureId);
   }

   public static ByteBuffer readResource(InputStream pInputStream) throws IOException {
      ReadableByteChannel readablebytechannel = Channels.newChannel(pInputStream);
      if (readablebytechannel instanceof SeekableByteChannel seekablebytechannel) {
         return readResource(readablebytechannel, (int)seekablebytechannel.size() + 1);
      } else {
         return readResource(readablebytechannel, 8192);
      }
   }

   private static ByteBuffer readResource(ReadableByteChannel pChannel, int pSize) throws IOException {
      ByteBuffer bytebuffer = MemoryUtil.memAlloc(pSize);

      try {
         while(pChannel.read(bytebuffer) != -1) {
            if (!bytebuffer.hasRemaining()) {
               bytebuffer = MemoryUtil.memRealloc(bytebuffer, bytebuffer.capacity() * 2);
            }
         }

         return bytebuffer;
      } catch (IOException ioexception) {
         MemoryUtil.memFree(bytebuffer);
         throw ioexception;
      }
   }

   public static void writeAsPNG(Path pOutputDir, String pTextureName, int pTextureId, int pAmount, int pWidth, int pHeight) {
      writeAsPNG(pOutputDir, pTextureName, pTextureId, pAmount, pWidth, pHeight, (IntUnaryOperator)null);
   }

   public static void writeAsPNG(Path pOutputDir, String pTextureName, int pTextureId, int pAmount, int pWidth, int pHeight, @Nullable IntUnaryOperator pFunction) {
      RenderSystem.assertOnRenderThread();
      bind(pTextureId);

      for(int i = 0; i <= pAmount; ++i) {
         int j = pWidth >> i;
         int k = pHeight >> i;

         try (NativeImage nativeimage = new NativeImage(j, k, false)) {
            nativeimage.downloadTexture(i, false);
            if (pFunction != null) {
               nativeimage.applyToAllPixels(pFunction);
            }

            Path path = pOutputDir.resolve(pTextureName + "_" + i + ".png");
            nativeimage.writeToFile(path);
            LOGGER.debug("Exported png to: {}", (Object)path.toAbsolutePath());
         } catch (IOException ioexception) {
            LOGGER.debug("Unable to write: ", (Throwable)ioexception);
         }
      }

   }

   public static Path getDebugTexturePath(Path pBasePath) {
      return pBasePath.resolve("screenshots").resolve("debug");
   }

   public static Path getDebugTexturePath() {
      return getDebugTexturePath(Path.of("."));
   }
}