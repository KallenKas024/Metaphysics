package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SpriteContents implements Stitcher.Entry, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ResourceLocation name;
   final int width;
   final int height;
   private final NativeImage originalImage;
   public NativeImage[] byMipLevel;
   @Nullable
   final SpriteContents.AnimatedTexture animatedTexture;
   @Nullable
   public final net.minecraftforge.client.textures.ForgeTextureMetadata forgeMeta;

   /**
    * @deprecated Forge: Use the {@linkplain SpriteContents#SpriteContents(ResourceLocation, FrameSize, NativeImage, AnimationMetadataSection, net.minecraftforge.client.textures.ForgeTextureMetadata) overload with Forge metadata parameter} to properly forward custom loaders.
    */
   @Deprecated
   public SpriteContents(ResourceLocation pName, FrameSize pFrameSize, NativeImage pOriginalImage, AnimationMetadataSection pMetadata) {
      this(pName, pFrameSize, pOriginalImage, pMetadata, null);
   }

   public SpriteContents(ResourceLocation pName, FrameSize pFrameSize, NativeImage pOriginalImage, AnimationMetadataSection pMetadata, @org.jetbrains.annotations.Nullable net.minecraftforge.client.textures.ForgeTextureMetadata forgeMeta) {
      this.name = pName;
      this.width = pFrameSize.width();
      this.height = pFrameSize.height();
      this.animatedTexture = this.createAnimatedTexture(pFrameSize, pOriginalImage.getWidth(), pOriginalImage.getHeight(), pMetadata);
      this.originalImage = pOriginalImage;
      this.byMipLevel = new NativeImage[]{this.originalImage};
      this.forgeMeta = forgeMeta;
   }

   public NativeImage getOriginalImage() {
      return this.originalImage;
   }

   public void increaseMipLevel(int pMipLevel) {
      try {
         this.byMipLevel = MipmapGenerator.generateMipLevels(this.byMipLevel, pMipLevel);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Generating mipmaps for frame");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Sprite being mipmapped");
         crashreportcategory.setDetail("First frame", () -> {
            StringBuilder stringbuilder = new StringBuilder();
            if (stringbuilder.length() > 0) {
               stringbuilder.append(", ");
            }

            stringbuilder.append(this.originalImage.getWidth()).append("x").append(this.originalImage.getHeight());
            return stringbuilder.toString();
         });
         CrashReportCategory crashreportcategory1 = crashreport.addCategory("Frame being iterated");
         crashreportcategory1.setDetail("Sprite name", this.name);
         crashreportcategory1.setDetail("Sprite size", () -> {
            return this.width + " x " + this.height;
         });
         crashreportcategory1.setDetail("Sprite frames", () -> {
            return this.getFrameCount() + " frames";
         });
         crashreportcategory1.setDetail("Mipmap levels", pMipLevel);
         throw new ReportedException(crashreport);
      }
   }

   int getFrameCount() {
      return this.animatedTexture != null ? this.animatedTexture.frames.size() : 1;
   }

   @Nullable
   private SpriteContents.AnimatedTexture createAnimatedTexture(FrameSize pFrameSize, int pWidth, int pHeight, AnimationMetadataSection pMetadata) {
      int i = pWidth / pFrameSize.width();
      int j = pHeight / pFrameSize.height();
      int k = i * j;
      List<SpriteContents.FrameInfo> list = new ArrayList<>();
      pMetadata.forEachFrame((p_251291_, p_251837_) -> {
         list.add(new SpriteContents.FrameInfo(p_251291_, p_251837_));
      });
      if (list.isEmpty()) {
         for(int l = 0; l < k; ++l) {
            list.add(new SpriteContents.FrameInfo(l, pMetadata.getDefaultFrameTime()));
         }
      } else {
         int i1 = 0;
         IntSet intset = new IntOpenHashSet();

         for(Iterator<SpriteContents.FrameInfo> iterator = list.iterator(); iterator.hasNext(); ++i1) {
            SpriteContents.FrameInfo spritecontents$frameinfo = iterator.next();
            boolean flag = true;
            if (spritecontents$frameinfo.time <= 0) {
               LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", this.name, i1, spritecontents$frameinfo.time);
               flag = false;
            }

            if (spritecontents$frameinfo.index < 0 || spritecontents$frameinfo.index >= k) {
               LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", this.name, i1, spritecontents$frameinfo.index);
               flag = false;
            }

            if (flag) {
               intset.add(spritecontents$frameinfo.index);
            } else {
               iterator.remove();
            }
         }

         int[] aint = IntStream.range(0, k).filter((p_251185_) -> {
            return !intset.contains(p_251185_);
         }).toArray();
         if (aint.length > 0) {
            LOGGER.warn("Unused frames in sprite {}: {}", this.name, Arrays.toString(aint));
         }
      }

      return list.size() <= 1 ? null : new SpriteContents.AnimatedTexture(ImmutableList.copyOf(list), i, pMetadata.isInterpolatedFrames());
   }

   void upload(int pX, int pY, int pFrameX, int pFrameY, NativeImage[] pAtlasData) {
      for(int i = 0; i < this.byMipLevel.length; ++i) {
         // Forge: Skip uploading if the texture would be made invalid by mip level
         if ((this.width >> i) <= 0 || (this.height >> i) <= 0)
            break;
         pAtlasData[i].upload(i, pX >> i, pY >> i, pFrameX >> i, pFrameY >> i, this.width >> i, this.height >> i, this.byMipLevel.length > 1, false);
      }

   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }

   public ResourceLocation name() {
      return this.name;
   }

   public IntStream getUniqueFrames() {
      return this.animatedTexture != null ? this.animatedTexture.getUniqueFrames() : IntStream.of(1);
   }

   @Nullable
   public SpriteTicker createTicker() {
      return this.animatedTexture != null ? this.animatedTexture.createTicker() : null;
   }

   public void close() {
      for(NativeImage nativeimage : this.byMipLevel) {
         nativeimage.close();
      }

   }

   public String toString() {
      return "SpriteContents{name=" + this.name + ", frameCount=" + this.getFrameCount() + ", height=" + this.height + ", width=" + this.width + "}";
   }

   public boolean isTransparent(int pFrame, int pX, int pY) {
      int i = pX;
      int j = pY;
      if (this.animatedTexture != null) {
         i = pX + this.animatedTexture.getFrameX(pFrame) * this.width;
         j = pY + this.animatedTexture.getFrameY(pFrame) * this.height;
      }

      return (this.originalImage.getPixelRGBA(i, j) >> 24 & 255) == 0;
   }

   public void uploadFirstFrame(int pX, int pY) {
      if (this.animatedTexture != null) {
         this.animatedTexture.uploadFirstFrame(pX, pY);
      } else {
         this.upload(pX, pY, 0, 0, this.byMipLevel);
      }

   }

   @OnlyIn(Dist.CLIENT)
   class AnimatedTexture {
      final List<SpriteContents.FrameInfo> frames;
      private final int frameRowSize;
      private final boolean interpolateFrames;

      AnimatedTexture(List<SpriteContents.FrameInfo> pFrames, int pFrameRowSize, boolean pInterpolateFrames) {
         this.frames = pFrames;
         this.frameRowSize = pFrameRowSize;
         this.interpolateFrames = pInterpolateFrames;
      }

      int getFrameX(int pFrameIndex) {
         return pFrameIndex % this.frameRowSize;
      }

      int getFrameY(int pFrameIndex) {
         return pFrameIndex / this.frameRowSize;
      }

      void uploadFrame(int pX, int pY, int pFrameIndex) {
         int i = this.getFrameX(pFrameIndex) * SpriteContents.this.width;
         int j = this.getFrameY(pFrameIndex) * SpriteContents.this.height;
         SpriteContents.this.upload(pX, pY, i, j, SpriteContents.this.byMipLevel);
      }

      public SpriteTicker createTicker() {
         return SpriteContents.this.new Ticker(this, this.interpolateFrames ? SpriteContents.this.new InterpolationData() : null);
      }

      public void uploadFirstFrame(int pX, int pY) {
         this.uploadFrame(pX, pY, (this.frames.get(0)).index);
      }

      public IntStream getUniqueFrames() {
         return this.frames.stream().mapToInt((p_249981_) -> {
            return p_249981_.index;
         }).distinct();
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class FrameInfo {
      final int index;
      final int time;

      FrameInfo(int pIndex, int pTime) {
         this.index = pIndex;
         this.time = pTime;
      }
   }

   @OnlyIn(Dist.CLIENT)
   final class InterpolationData implements AutoCloseable {
      private final NativeImage[] activeFrame = new NativeImage[SpriteContents.this.byMipLevel.length];

      InterpolationData() {
         for(int i = 0; i < this.activeFrame.length; ++i) {
            int j = SpriteContents.this.width >> i;
            int k = SpriteContents.this.height >> i;
            // Forge: Guard against invalid texture size, because we allow generating mipmaps regardless of texture sizes
            this.activeFrame[i] = new NativeImage(Math.max(1, j), Math.max(1, k), false);
         }

      }

      void uploadInterpolatedFrame(int pX, int pY, SpriteContents.Ticker pTicker) {
         SpriteContents.AnimatedTexture spritecontents$animatedtexture = pTicker.animationInfo;
         List<SpriteContents.FrameInfo> list = spritecontents$animatedtexture.frames;
         SpriteContents.FrameInfo spritecontents$frameinfo = list.get(pTicker.frame);
         double d0 = 1.0D - (double)pTicker.subFrame / (double)spritecontents$frameinfo.time;
         int i = spritecontents$frameinfo.index;
         int j = (list.get((pTicker.frame + 1) % list.size())).index;
         if (i != j) {
            for(int k = 0; k < this.activeFrame.length; ++k) {
               int l = SpriteContents.this.width >> k;
               int i1 = SpriteContents.this.height >> k;
               // Forge: Guard against invalid texture size, because we allow generating mipmaps regardless of texture sizes
               if (l < 1 || i1 < 1)
                  continue;

               for(int j1 = 0; j1 < i1; ++j1) {
                  for(int k1 = 0; k1 < l; ++k1) {
                     int l1 = this.getPixel(spritecontents$animatedtexture, i, k, k1, j1);
                     int i2 = this.getPixel(spritecontents$animatedtexture, j, k, k1, j1);
                     int j2 = this.mix(d0, l1 >> 16 & 255, i2 >> 16 & 255);
                     int k2 = this.mix(d0, l1 >> 8 & 255, i2 >> 8 & 255);
                     int l2 = this.mix(d0, l1 & 255, i2 & 255);
                     this.activeFrame[k].setPixelRGBA(k1, j1, l1 & -16777216 | j2 << 16 | k2 << 8 | l2);
                  }
               }
            }

            SpriteContents.this.upload(pX, pY, 0, 0, this.activeFrame);
         }

      }

      private int getPixel(SpriteContents.AnimatedTexture pAnimatedTexture, int pFrameIndex, int pMipLevel, int pX, int pY) {
         return SpriteContents.this.byMipLevel[pMipLevel].getPixelRGBA(pX + (pAnimatedTexture.getFrameX(pFrameIndex) * SpriteContents.this.width >> pMipLevel), pY + (pAnimatedTexture.getFrameY(pFrameIndex) * SpriteContents.this.height >> pMipLevel));
      }

      private int mix(double pDelta, int pColor1, int pColor2) {
         return (int)(pDelta * (double)pColor1 + (1.0D - pDelta) * (double)pColor2);
      }

      public void close() {
         for(NativeImage nativeimage : this.activeFrame) {
            nativeimage.close();
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   class Ticker implements SpriteTicker {
      int frame;
      int subFrame;
      final SpriteContents.AnimatedTexture animationInfo;
      @Nullable
      private final SpriteContents.InterpolationData interpolationData;

      Ticker(SpriteContents.AnimatedTexture pAnimationInfo, @Nullable SpriteContents.InterpolationData pInterpolationData) {
         this.animationInfo = pAnimationInfo;
         this.interpolationData = pInterpolationData;
      }

      public void tickAndUpload(int pX, int pY) {
         ++this.subFrame;
         SpriteContents.FrameInfo spritecontents$frameinfo = this.animationInfo.frames.get(this.frame);
         if (this.subFrame >= spritecontents$frameinfo.time) {
            int i = spritecontents$frameinfo.index;
            this.frame = (this.frame + 1) % this.animationInfo.frames.size();
            this.subFrame = 0;
            int j = (this.animationInfo.frames.get(this.frame)).index;
            if (i != j) {
               this.animationInfo.uploadFrame(pX, pY, j);
            }
         } else if (this.interpolationData != null) {
            if (!RenderSystem.isOnRenderThread()) {
               RenderSystem.recordRenderCall(() -> {
                  this.interpolationData.uploadInterpolatedFrame(pX, pY, this);
               });
            } else {
               this.interpolationData.uploadInterpolatedFrame(pX, pY, this);
            }
         }

      }

      public void close() {
         if (this.interpolationData != null) {
            this.interpolationData.close();
         }

      }
   }
}
