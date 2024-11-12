package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public final class NativeImage implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Set<StandardOpenOption> OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
   private final NativeImage.Format format;
   private final int width;
   private final int height;
   private final boolean useStbFree;
   private long pixels;
   private final long size;

   public NativeImage(int pWidth, int pHeight, boolean pUseCalloc) {
      this(NativeImage.Format.RGBA, pWidth, pHeight, pUseCalloc);
   }

   public NativeImage(NativeImage.Format pFormat, int pWidth, int pHeight, boolean pUseCalloc) {
      if (pWidth > 0 && pHeight > 0) {
         this.format = pFormat;
         this.width = pWidth;
         this.height = pHeight;
         this.size = (long)pWidth * (long)pHeight * (long)pFormat.components();
         this.useStbFree = false;
         if (pUseCalloc) {
            this.pixels = MemoryUtil.nmemCalloc(1L, this.size);
         } else {
            this.pixels = MemoryUtil.nmemAlloc(this.size);
         }

      } else {
         throw new IllegalArgumentException("Invalid texture size: " + pWidth + "x" + pHeight);
      }
   }

   private NativeImage(NativeImage.Format pFormat, int pWidth, int pHeight, boolean pUseStbFree, long pPixels) {
      if (pWidth > 0 && pHeight > 0) {
         this.format = pFormat;
         this.width = pWidth;
         this.height = pHeight;
         this.useStbFree = pUseStbFree;
         this.pixels = pPixels;
         this.size = (long)pWidth * (long)pHeight * (long)pFormat.components();
      } else {
         throw new IllegalArgumentException("Invalid texture size: " + pWidth + "x" + pHeight);
      }
   }

   public String toString() {
      return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pixels + (this.useStbFree ? "S" : "N") + "]";
   }

   private boolean isOutsideBounds(int pX, int pY) {
      return pX < 0 || pX >= this.width || pY < 0 || pY >= this.height;
   }

   public static NativeImage read(InputStream pTextureStream) throws IOException {
      return read(NativeImage.Format.RGBA, pTextureStream);
   }

   public static NativeImage read(@Nullable NativeImage.Format pFormat, InputStream pTextureStream) throws IOException {
      ByteBuffer bytebuffer = null;

      NativeImage nativeimage;
      try {
         bytebuffer = TextureUtil.readResource(pTextureStream);
         bytebuffer.rewind();
         nativeimage = read(pFormat, bytebuffer);
      } finally {
         MemoryUtil.memFree(bytebuffer);
         IOUtils.closeQuietly(pTextureStream);
      }

      return nativeimage;
   }

   public static NativeImage read(ByteBuffer pTextureData) throws IOException {
      return read(NativeImage.Format.RGBA, pTextureData);
   }

   public static NativeImage read(byte[] pBytes) throws IOException {
      try (MemoryStack memorystack = MemoryStack.stackPush()) {
         ByteBuffer bytebuffer = memorystack.malloc(pBytes.length);
         bytebuffer.put(pBytes);
         bytebuffer.rewind();
         return read(bytebuffer);
      }
   }

   public static NativeImage read(@Nullable NativeImage.Format pFormat, ByteBuffer pTextureData) throws IOException {
      if (pFormat != null && !pFormat.supportedByStb()) {
         throw new UnsupportedOperationException("Don't know how to read format " + pFormat);
      } else if (MemoryUtil.memAddress(pTextureData) == 0L) {
         throw new IllegalArgumentException("Invalid buffer");
      } else {
         try (MemoryStack memorystack = MemoryStack.stackPush()) {
            IntBuffer intbuffer = memorystack.mallocInt(1);
            IntBuffer intbuffer1 = memorystack.mallocInt(1);
            IntBuffer intbuffer2 = memorystack.mallocInt(1);
            ByteBuffer bytebuffer = STBImage.stbi_load_from_memory(pTextureData, intbuffer, intbuffer1, intbuffer2, pFormat == null ? 0 : pFormat.components);
            if (bytebuffer == null) {
               throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            } else {
               return new NativeImage(pFormat == null ? NativeImage.Format.getStbFormat(intbuffer2.get(0)) : pFormat, intbuffer.get(0), intbuffer1.get(0), true, MemoryUtil.memAddress(bytebuffer));
            }
         }
      }
   }

   private static void setFilter(boolean pLinear, boolean pMipmap) {
      RenderSystem.assertOnRenderThreadOrInit();
      if (pLinear) {
         GlStateManager._texParameter(3553, 10241, pMipmap ? 9987 : 9729);
         GlStateManager._texParameter(3553, 10240, 9729);
      } else {
         GlStateManager._texParameter(3553, 10241, pMipmap ? 9986 : 9728);
         GlStateManager._texParameter(3553, 10240, 9728);
      }

   }

   private void checkAllocated() {
      if (this.pixels == 0L) {
         throw new IllegalStateException("Image is not allocated.");
      }
   }

   public void close() {
      if (this.pixels != 0L) {
         if (this.useStbFree) {
            STBImage.nstbi_image_free(this.pixels);
         } else {
            MemoryUtil.nmemFree(this.pixels);
         }
      }

      this.pixels = 0L;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public NativeImage.Format format() {
      return this.format;
   }

   public int getPixelRGBA(int pX, int pY) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelRGBA only works on RGBA images; have %s", this.format));
      } else if (this.isOutsideBounds(pX, pY)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", pX, pY, this.width, this.height));
      } else {
         this.checkAllocated();
         long i = ((long)pX + (long)pY * (long)this.width) * 4L;
         return MemoryUtil.memGetInt(this.pixels + i);
      }
   }

   public void setPixelRGBA(int pX, int pY, int pAbgrColor) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelRGBA only works on RGBA images; have %s", this.format));
      } else if (this.isOutsideBounds(pX, pY)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", pX, pY, this.width, this.height));
      } else {
         this.checkAllocated();
         long i = ((long)pX + (long)pY * (long)this.width) * 4L;
         MemoryUtil.memPutInt(this.pixels + i, pAbgrColor);
      }
   }

   public NativeImage mappedCopy(IntUnaryOperator pFunction) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", this.format));
      } else {
         this.checkAllocated();
         NativeImage nativeimage = new NativeImage(this.width, this.height, false);
         int i = this.width * this.height;
         IntBuffer intbuffer = MemoryUtil.memIntBuffer(this.pixels, i);
         IntBuffer intbuffer1 = MemoryUtil.memIntBuffer(nativeimage.pixels, i);

         for(int j = 0; j < i; ++j) {
            intbuffer1.put(j, pFunction.applyAsInt(intbuffer.get(j)));
         }

         return nativeimage;
      }
   }

   public void applyToAllPixels(IntUnaryOperator pFunction) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", this.format));
      } else {
         this.checkAllocated();
         int i = this.width * this.height;
         IntBuffer intbuffer = MemoryUtil.memIntBuffer(this.pixels, i);

         for(int j = 0; j < i; ++j) {
            intbuffer.put(j, pFunction.applyAsInt(intbuffer.get(j)));
         }

      }
   }

   public int[] getPixelsRGBA() {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelsRGBA only works on RGBA images; have %s", this.format));
      } else {
         this.checkAllocated();
         int[] aint = new int[this.width * this.height];
         MemoryUtil.memIntBuffer(this.pixels, this.width * this.height).get(aint);
         return aint;
      }
   }

   public void setPixelLuminance(int pX, int pY, byte pLuminance) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasLuminance()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelLuminance only works on image with luminance; have %s", this.format));
      } else if (this.isOutsideBounds(pX, pY)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", pX, pY, this.width, this.height));
      } else {
         this.checkAllocated();
         long i = ((long)pX + (long)pY * (long)this.width) * (long)this.format.components() + (long)(this.format.luminanceOffset() / 8);
         MemoryUtil.memPutByte(this.pixels + i, pLuminance);
      }
   }

   public byte getRedOrLuminance(int pX, int pY) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasLuminanceOrRed()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no red or luminance in %s", this.format));
      } else if (this.isOutsideBounds(pX, pY)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", pX, pY, this.width, this.height));
      } else {
         int i = (pX + pY * this.width) * this.format.components() + this.format.luminanceOrRedOffset() / 8;
         return MemoryUtil.memGetByte(this.pixels + (long)i);
      }
   }

   public byte getGreenOrLuminance(int pX, int pY) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasLuminanceOrGreen()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no green or luminance in %s", this.format));
      } else if (this.isOutsideBounds(pX, pY)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", pX, pY, this.width, this.height));
      } else {
         int i = (pX + pY * this.width) * this.format.components() + this.format.luminanceOrGreenOffset() / 8;
         return MemoryUtil.memGetByte(this.pixels + (long)i);
      }
   }

   public byte getBlueOrLuminance(int pX, int pY) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasLuminanceOrBlue()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no blue or luminance in %s", this.format));
      } else if (this.isOutsideBounds(pX, pY)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", pX, pY, this.width, this.height));
      } else {
         int i = (pX + pY * this.width) * this.format.components() + this.format.luminanceOrBlueOffset() / 8;
         return MemoryUtil.memGetByte(this.pixels + (long)i);
      }
   }

   public byte getLuminanceOrAlpha(int pX, int pY) {
      if (!this.format.hasLuminanceOrAlpha()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no luminance or alpha in %s", this.format));
      } else if (this.isOutsideBounds(pX, pY)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", pX, pY, this.width, this.height));
      } else {
         int i = (pX + pY * this.width) * this.format.components() + this.format.luminanceOrAlphaOffset() / 8;
         return MemoryUtil.memGetByte(this.pixels + (long)i);
      }
   }

   public void blendPixel(int pX, int pY, int pAbgrColor) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new UnsupportedOperationException("Can only call blendPixel with RGBA format");
      } else {
         int i = this.getPixelRGBA(pX, pY);
         float f = (float)FastColor.ABGR32.alpha(pAbgrColor) / 255.0F;
         float f1 = (float)FastColor.ABGR32.blue(pAbgrColor) / 255.0F;
         float f2 = (float)FastColor.ABGR32.green(pAbgrColor) / 255.0F;
         float f3 = (float)FastColor.ABGR32.red(pAbgrColor) / 255.0F;
         float f4 = (float)FastColor.ABGR32.alpha(i) / 255.0F;
         float f5 = (float)FastColor.ABGR32.blue(i) / 255.0F;
         float f6 = (float)FastColor.ABGR32.green(i) / 255.0F;
         float f7 = (float)FastColor.ABGR32.red(i) / 255.0F;
         float f8 = 1.0F - f;
         float f9 = f * f + f4 * f8;
         float f10 = f1 * f + f5 * f8;
         float f11 = f2 * f + f6 * f8;
         float f12 = f3 * f + f7 * f8;
         if (f9 > 1.0F) {
            f9 = 1.0F;
         }

         if (f10 > 1.0F) {
            f10 = 1.0F;
         }

         if (f11 > 1.0F) {
            f11 = 1.0F;
         }

         if (f12 > 1.0F) {
            f12 = 1.0F;
         }

         int j = (int)(f9 * 255.0F);
         int k = (int)(f10 * 255.0F);
         int l = (int)(f11 * 255.0F);
         int i1 = (int)(f12 * 255.0F);
         this.setPixelRGBA(pX, pY, FastColor.ABGR32.color(j, k, l, i1));
      }
   }

   /** @deprecated */
   @Deprecated
   public int[] makePixelArray() {
      if (this.format != NativeImage.Format.RGBA) {
         throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
      } else {
         this.checkAllocated();
         int[] aint = new int[this.getWidth() * this.getHeight()];

         for(int i = 0; i < this.getHeight(); ++i) {
            for(int j = 0; j < this.getWidth(); ++j) {
               int k = this.getPixelRGBA(j, i);
               aint[j + i * this.getWidth()] = FastColor.ARGB32.color(FastColor.ABGR32.alpha(k), FastColor.ABGR32.red(k), FastColor.ABGR32.green(k), FastColor.ABGR32.blue(k));
            }
         }

         return aint;
      }
   }

   public void upload(int pLevel, int pXOffset, int pYOffset, boolean pMipmap) {
      this.upload(pLevel, pXOffset, pYOffset, 0, 0, this.width, this.height, false, pMipmap);
   }

   public void upload(int pLevel, int pXOffset, int pYOffset, int pUnpackSkipPixels, int pUnpackSkipRows, int pWidth, int pHeight, boolean pMipmap, boolean pAutoClose) {
      this.upload(pLevel, pXOffset, pYOffset, pUnpackSkipPixels, pUnpackSkipRows, pWidth, pHeight, false, false, pMipmap, pAutoClose);
   }

   public void upload(int pLevel, int pXOffset, int pYOffset, int pUnpackSkipPixels, int pUnpackSkipRows, int pWidth, int pHeight, boolean pBlur, boolean pClamp, boolean pMipmap, boolean pAutoClose) {
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> {
            this._upload(pLevel, pXOffset, pYOffset, pUnpackSkipPixels, pUnpackSkipRows, pWidth, pHeight, pBlur, pClamp, pMipmap, pAutoClose);
         });
      } else {
         this._upload(pLevel, pXOffset, pYOffset, pUnpackSkipPixels, pUnpackSkipRows, pWidth, pHeight, pBlur, pClamp, pMipmap, pAutoClose);
      }

   }

   private void _upload(int pLevel, int pXOffset, int pYOffset, int pUnpackSkipPixels, int pUnpackSkipRows, int pWidth, int pHeight, boolean pBlur, boolean pClamp, boolean pMipmap, boolean pAutoClose) {
      try {
         RenderSystem.assertOnRenderThreadOrInit();
         this.checkAllocated();
         setFilter(pBlur, pMipmap);
         if (pWidth == this.getWidth()) {
            GlStateManager._pixelStore(3314, 0);
         } else {
            GlStateManager._pixelStore(3314, this.getWidth());
         }

         GlStateManager._pixelStore(3316, pUnpackSkipPixels);
         GlStateManager._pixelStore(3315, pUnpackSkipRows);
         this.format.setUnpackPixelStoreState();
         GlStateManager._texSubImage2D(3553, pLevel, pXOffset, pYOffset, pWidth, pHeight, this.format.glFormat(), 5121, this.pixels);
         if (pClamp) {
            GlStateManager._texParameter(3553, 10242, 33071);
            GlStateManager._texParameter(3553, 10243, 33071);
         }
      } finally {
         if (pAutoClose) {
            this.close();
         }

      }

   }

   public void downloadTexture(int pLevel, boolean pOpaque) {
      RenderSystem.assertOnRenderThread();
      this.checkAllocated();
      this.format.setPackPixelStoreState();
      GlStateManager._getTexImage(3553, pLevel, this.format.glFormat(), 5121, this.pixels);
      if (pOpaque && this.format.hasAlpha()) {
         for(int i = 0; i < this.getHeight(); ++i) {
            for(int j = 0; j < this.getWidth(); ++j) {
               this.setPixelRGBA(j, i, this.getPixelRGBA(j, i) | 255 << this.format.alphaOffset());
            }
         }
      }

   }

   public void downloadDepthBuffer(float pUnused) {
      RenderSystem.assertOnRenderThread();
      if (this.format.components() != 1) {
         throw new IllegalStateException("Depth buffer must be stored in NativeImage with 1 component.");
      } else {
         this.checkAllocated();
         this.format.setPackPixelStoreState();
         GlStateManager._readPixels(0, 0, this.width, this.height, 6402, 5121, this.pixels);
      }
   }

   public void drawPixels() {
      RenderSystem.assertOnRenderThread();
      this.format.setUnpackPixelStoreState();
      GlStateManager._glDrawPixels(this.width, this.height, this.format.glFormat(), 5121, this.pixels);
   }

   public void writeToFile(File pFile) throws IOException {
      this.writeToFile(pFile.toPath());
   }

   /**
    * Renders given glyph into this image
    */
   public void copyFromFont(STBTTFontinfo pInfo, int pGlyphIndex, int pWidth, int pHeight, float pScaleX, float pScaleY, float pShiftX, float pShiftY, int pX, int pY) {
      if (pX >= 0 && pX + pWidth <= this.getWidth() && pY >= 0 && pY + pHeight <= this.getHeight()) {
         if (this.format.components() != 1) {
            throw new IllegalArgumentException("Can only write fonts into 1-component images.");
         } else {
            STBTruetype.nstbtt_MakeGlyphBitmapSubpixel(pInfo.address(), this.pixels + (long)pX + (long)(pY * this.getWidth()), pWidth, pHeight, this.getWidth(), pScaleX, pScaleY, pShiftX, pShiftY, pGlyphIndex);
         }
      } else {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", pX, pY, pWidth, pHeight, this.getWidth(), this.getHeight()));
      }
   }

   public void writeToFile(Path pPath) throws IOException {
      if (!this.format.supportedByStb()) {
         throw new UnsupportedOperationException("Don't know how to write format " + this.format);
      } else {
         this.checkAllocated();

         try (WritableByteChannel writablebytechannel = Files.newByteChannel(pPath, OPEN_OPTIONS)) {
            if (!this.writeToChannel(writablebytechannel)) {
               throw new IOException("Could not write image to the PNG file \"" + pPath.toAbsolutePath() + "\": " + STBImage.stbi_failure_reason());
            }
         }

      }
   }

   public byte[] asByteArray() throws IOException {
      try (
         ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
         WritableByteChannel writablebytechannel = Channels.newChannel(bytearrayoutputstream);
      ) {
         if (!this.writeToChannel(writablebytechannel)) {
            throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
         } else {
            return bytearrayoutputstream.toByteArray();
         }
      }
   }

   private boolean writeToChannel(WritableByteChannel pChannel) throws IOException {
      NativeImage.WriteCallback nativeimage$writecallback = new NativeImage.WriteCallback(pChannel);

      boolean flag;
      try {
         int i = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.components());
         if (i < this.getHeight()) {
            LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", this.getHeight(), i);
         }

         if (STBImageWrite.nstbi_write_png_to_func(nativeimage$writecallback.address(), 0L, this.getWidth(), i, this.format.components(), this.pixels, 0) != 0) {
            nativeimage$writecallback.throwIfException();
            return true;
         }

         flag = false;
      } finally {
         nativeimage$writecallback.free();
      }

      return flag;
   }

   public void copyFrom(NativeImage pOther) {
      if (pOther.format() != this.format) {
         throw new UnsupportedOperationException("Image formats don't match.");
      } else {
         int i = this.format.components();
         this.checkAllocated();
         pOther.checkAllocated();
         if (this.width == pOther.width) {
            MemoryUtil.memCopy(pOther.pixels, this.pixels, Math.min(this.size, pOther.size));
         } else {
            int j = Math.min(this.getWidth(), pOther.getWidth());
            int k = Math.min(this.getHeight(), pOther.getHeight());

            for(int l = 0; l < k; ++l) {
               int i1 = l * pOther.getWidth() * i;
               int j1 = l * this.getWidth() * i;
               MemoryUtil.memCopy(pOther.pixels + (long)i1, this.pixels + (long)j1, (long)j);
            }
         }

      }
   }

   public void fillRect(int pX, int pY, int pWidth, int pHeight, int pValue) {
      for(int i = pY; i < pY + pHeight; ++i) {
         for(int j = pX; j < pX + pWidth; ++j) {
            this.setPixelRGBA(j, i, pValue);
         }
      }

   }

   public void copyRect(int pXFrom, int pYFrom, int pXToDelta, int pYToDelta, int pWidth, int pHeight, boolean pMirrorX, boolean pMirrorY) {
      this.copyRect(this, pXFrom, pYFrom, pXFrom + pXToDelta, pYFrom + pYToDelta, pWidth, pHeight, pMirrorX, pMirrorY);
   }

   public void copyRect(NativeImage pSource, int pXFrom, int pYFrom, int pXTo, int pYTo, int pWidth, int pHeight, boolean pMirrorX, boolean pMirrorY) {
      for(int i = 0; i < pHeight; ++i) {
         for(int j = 0; j < pWidth; ++j) {
            int k = pMirrorX ? pWidth - 1 - j : j;
            int l = pMirrorY ? pHeight - 1 - i : i;
            int i1 = this.getPixelRGBA(pXFrom + j, pYFrom + i);
            pSource.setPixelRGBA(pXTo + k, pYTo + l, i1);
         }
      }

   }

   public void flipY() {
      this.checkAllocated();

      try (MemoryStack memorystack = MemoryStack.stackPush()) {
         int i = this.format.components();
         int j = this.getWidth() * i;
         long k = memorystack.nmalloc(j);

         for(int l = 0; l < this.getHeight() / 2; ++l) {
            int i1 = l * this.getWidth() * i;
            int j1 = (this.getHeight() - 1 - l) * this.getWidth() * i;
            MemoryUtil.memCopy(this.pixels + (long)i1, k, (long)j);
            MemoryUtil.memCopy(this.pixels + (long)j1, this.pixels + (long)i1, (long)j);
            MemoryUtil.memCopy(k, this.pixels + (long)j1, (long)j);
         }
      }

   }

   public void resizeSubRectTo(int pX, int pY, int pWidth, int pHeight, NativeImage pImage) {
      this.checkAllocated();
      if (pImage.format() != this.format) {
         throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
      } else {
         int i = this.format.components();
         STBImageResize.nstbir_resize_uint8(this.pixels + (long)((pX + pY * this.getWidth()) * i), pWidth, pHeight, this.getWidth() * i, pImage.pixels, pImage.getWidth(), pImage.getHeight(), 0, i);
      }
   }

   public void untrack() {
      DebugMemoryUntracker.untrack(this.pixels);
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Format {
      RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
      RGB(3, 6407, true, true, true, false, false, 0, 8, 16, 255, 255, true),
      LUMINANCE_ALPHA(2, 33319, false, false, false, true, true, 255, 255, 255, 0, 8, true),
      LUMINANCE(1, 6403, false, false, false, true, false, 0, 0, 0, 0, 255, true);

      final int components;
      private final int glFormat;
      private final boolean hasRed;
      private final boolean hasGreen;
      private final boolean hasBlue;
      private final boolean hasLuminance;
      private final boolean hasAlpha;
      private final int redOffset;
      private final int greenOffset;
      private final int blueOffset;
      private final int luminanceOffset;
      private final int alphaOffset;
      private final boolean supportedByStb;

      private Format(int pComponents, int pGlFormat, boolean pHasRed, boolean pHasGreen, boolean pHasBlue, boolean pHasLuminance, boolean pHasAlpha, int pRedOffset, int pGreenOffset, int pBlueOffset, int pLuminanceOffset, int pAlphaOffset, boolean pSupportedByStb) {
         this.components = pComponents;
         this.glFormat = pGlFormat;
         this.hasRed = pHasRed;
         this.hasGreen = pHasGreen;
         this.hasBlue = pHasBlue;
         this.hasLuminance = pHasLuminance;
         this.hasAlpha = pHasAlpha;
         this.redOffset = pRedOffset;
         this.greenOffset = pGreenOffset;
         this.blueOffset = pBlueOffset;
         this.luminanceOffset = pLuminanceOffset;
         this.alphaOffset = pAlphaOffset;
         this.supportedByStb = pSupportedByStb;
      }

      public int components() {
         return this.components;
      }

      public void setPackPixelStoreState() {
         RenderSystem.assertOnRenderThread();
         GlStateManager._pixelStore(3333, this.components());
      }

      public void setUnpackPixelStoreState() {
         RenderSystem.assertOnRenderThreadOrInit();
         GlStateManager._pixelStore(3317, this.components());
      }

      public int glFormat() {
         return this.glFormat;
      }

      public boolean hasRed() {
         return this.hasRed;
      }

      public boolean hasGreen() {
         return this.hasGreen;
      }

      public boolean hasBlue() {
         return this.hasBlue;
      }

      public boolean hasLuminance() {
         return this.hasLuminance;
      }

      public boolean hasAlpha() {
         return this.hasAlpha;
      }

      public int redOffset() {
         return this.redOffset;
      }

      public int greenOffset() {
         return this.greenOffset;
      }

      public int blueOffset() {
         return this.blueOffset;
      }

      public int luminanceOffset() {
         return this.luminanceOffset;
      }

      public int alphaOffset() {
         return this.alphaOffset;
      }

      public boolean hasLuminanceOrRed() {
         return this.hasLuminance || this.hasRed;
      }

      public boolean hasLuminanceOrGreen() {
         return this.hasLuminance || this.hasGreen;
      }

      public boolean hasLuminanceOrBlue() {
         return this.hasLuminance || this.hasBlue;
      }

      public boolean hasLuminanceOrAlpha() {
         return this.hasLuminance || this.hasAlpha;
      }

      public int luminanceOrRedOffset() {
         return this.hasLuminance ? this.luminanceOffset : this.redOffset;
      }

      public int luminanceOrGreenOffset() {
         return this.hasLuminance ? this.luminanceOffset : this.greenOffset;
      }

      public int luminanceOrBlueOffset() {
         return this.hasLuminance ? this.luminanceOffset : this.blueOffset;
      }

      public int luminanceOrAlphaOffset() {
         return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
      }

      public boolean supportedByStb() {
         return this.supportedByStb;
      }

      static NativeImage.Format getStbFormat(int pChannels) {
         switch (pChannels) {
            case 1:
               return LUMINANCE;
            case 2:
               return LUMINANCE_ALPHA;
            case 3:
               return RGB;
            case 4:
            default:
               return RGBA;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum InternalGlFormat {
      RGBA(6408),
      RGB(6407),
      RG(33319),
      RED(6403);

      private final int glFormat;

      private InternalGlFormat(int pGlFormat) {
         this.glFormat = pGlFormat;
      }

      public int glFormat() {
         return this.glFormat;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class WriteCallback extends STBIWriteCallback {
      private final WritableByteChannel output;
      @Nullable
      private IOException exception;

      WriteCallback(WritableByteChannel pOutput) {
         this.output = pOutput;
      }

      public void invoke(long pContext, long pData, int pSize) {
         ByteBuffer bytebuffer = getData(pData, pSize);

         try {
            this.output.write(bytebuffer);
         } catch (IOException ioexception) {
            this.exception = ioexception;
         }

      }

      public void throwIfException() throws IOException {
         if (this.exception != null) {
            throw this.exception;
         }
      }
   }
}