package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SpriteLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ResourceLocation location;
   private final int maxSupportedTextureSize;
   private final int minWidth;
   private final int minHeight;

   public SpriteLoader(ResourceLocation pLocation, int pMaxSupportedTextureSize, int pMinWidth, int pMinHeight) {
      this.location = pLocation;
      this.maxSupportedTextureSize = pMaxSupportedTextureSize;
      this.minWidth = pMinWidth;
      this.minHeight = pMinHeight;
   }

   public static SpriteLoader create(TextureAtlas pAtlas) {
      return new SpriteLoader(pAtlas.location(), pAtlas.maxSupportedTextureSize(), pAtlas.getWidth(), pAtlas.getHeight());
   }

   public SpriteLoader.Preparations stitch(List<SpriteContents> pContents, int pMipLevel, Executor pExecutor) {
      int i = this.maxSupportedTextureSize;
      Stitcher<SpriteContents> stitcher = new Stitcher<>(i, i, pMipLevel);
      int j = Integer.MAX_VALUE;
      int k = 1 << pMipLevel;

      for(SpriteContents spritecontents : pContents) {
         j = Math.min(j, Math.min(spritecontents.width(), spritecontents.height()));
         int l = Math.min(Integer.lowestOneBit(spritecontents.width()), Integer.lowestOneBit(spritecontents.height()));
         if (l < k) {
            LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", spritecontents.name(), spritecontents.width(), spritecontents.height(), Mth.log2(k), Mth.log2(l));
            k = l;
         }

         stitcher.registerSprite(spritecontents);
      }

      int j1 = Math.min(j, k);
      int k1 = Mth.log2(j1);
      int l1;
      if (false) { // Forge: Do not lower the mipmap level
         LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, pMipLevel, k1, j1);
         l1 = k1;
      } else {
         l1 = pMipLevel;
      }

      try {
         stitcher.stitch();
      } catch (StitcherException stitcherexception) {
         CrashReport crashreport = CrashReport.forThrowable(stitcherexception, "Stitching");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Stitcher");
         crashreportcategory.setDetail("Sprites", stitcherexception.getAllSprites().stream().map((p_249576_) -> {
            return String.format(Locale.ROOT, "%s[%dx%d]", p_249576_.name(), p_249576_.width(), p_249576_.height());
         }).collect(Collectors.joining(",")));
         crashreportcategory.setDetail("Max Texture Size", i);
         throw new ReportedException(crashreport);
      }

      int i1 = Math.max(stitcher.getWidth(), this.minWidth);
      int i2 = Math.max(stitcher.getHeight(), this.minHeight);
      Map<ResourceLocation, TextureAtlasSprite> map = this.getStitchedSprites(stitcher, i1, i2);
      TextureAtlasSprite textureatlassprite = map.get(MissingTextureAtlasSprite.getLocation());
      CompletableFuture<Void> completablefuture;
      if (l1 > 0) {
         completablefuture = CompletableFuture.runAsync(() -> {
            map.values().forEach((p_251202_) -> {
               p_251202_.contents().increaseMipLevel(l1);
            });
         }, pExecutor);
      } else {
         completablefuture = CompletableFuture.completedFuture((Void)null);
      }

      return new SpriteLoader.Preparations(i1, i2, l1, textureatlassprite, map, completablefuture);
   }

   public static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(List<Supplier<SpriteContents>> pSpriteSuppliers, Executor pExecutor) {
      List<CompletableFuture<SpriteContents>> list = pSpriteSuppliers.stream().map((p_261395_) -> {
         return CompletableFuture.supplyAsync(p_261395_, pExecutor);
      }).toList();
      return Util.sequence(list).thenApply((p_252234_) -> {
         return p_252234_.stream().filter(Objects::nonNull).toList();
      });
   }

   public CompletableFuture<SpriteLoader.Preparations> loadAndStitch(ResourceManager pResouceManager, ResourceLocation pLocation, int pMipLevel, Executor pExecutor) {
      return CompletableFuture.supplyAsync(() -> {
         return SpriteResourceLoader.load(pResouceManager, pLocation).list(pResouceManager);
      }, pExecutor).thenCompose((p_261390_) -> {
         return runSpriteSuppliers(p_261390_, pExecutor);
      }).thenApply((p_261393_) -> {
         return this.stitch(p_261393_, pMipLevel, pExecutor);
      });
   }

   @Nullable
   public static SpriteContents loadSprite(ResourceLocation pLocation, Resource pResource) {
      AnimationMetadataSection animationmetadatasection;
      try {
         animationmetadatasection = pResource.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
      } catch (Exception exception) {
         LOGGER.error("Unable to parse metadata from {}", pLocation, exception);
         return null;
      }

      NativeImage nativeimage;
      try (InputStream inputstream = pResource.open()) {
         nativeimage = NativeImage.read(inputstream);
      } catch (IOException ioexception) {
         LOGGER.error("Using missing texture, unable to load {}", pLocation, ioexception);
         return null;
      }

      FrameSize framesize = animationmetadatasection.calculateFrameSize(nativeimage.getWidth(), nativeimage.getHeight());
      if (Mth.isMultipleOf(nativeimage.getWidth(), framesize.width()) && Mth.isMultipleOf(nativeimage.getHeight(), framesize.height())) {
         SpriteContents contents = net.minecraftforge.client.ForgeHooksClient.loadSpriteContents(pLocation, pResource, framesize, nativeimage, animationmetadatasection);
         if (contents != null)
            return contents;
         return new SpriteContents(pLocation, framesize, nativeimage, animationmetadatasection);
      } else {
         LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", pLocation, nativeimage.getWidth(), nativeimage.getHeight(), framesize.width(), framesize.height());
         nativeimage.close();
         return null;
      }
   }

   private Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> pStitcher, int pX, int pY) {
      Map<ResourceLocation, TextureAtlasSprite> map = new HashMap<>();
      pStitcher.gatherSprites((p_251421_, p_250533_, p_251913_) -> {
         TextureAtlasSprite sprite = net.minecraftforge.client.ForgeHooksClient.loadTextureAtlasSprite(this.location, p_251421_, pX, pY, p_250533_, p_251913_, p_251421_.byMipLevel.length - 1);
         if (sprite != null) {
            map.put(p_251421_.name(), sprite);
            return;
         }
         map.put(p_251421_.name(), new TextureAtlasSprite(this.location, p_251421_, pX, pY, p_250533_, p_251913_));
      });
      return map;
   }

   @OnlyIn(Dist.CLIENT)
   public static record Preparations(int width, int height, int mipLevel, TextureAtlasSprite missing, Map<ResourceLocation, TextureAtlasSprite> regions, CompletableFuture<Void> readyForUpload) {
      public CompletableFuture<SpriteLoader.Preparations> waitForUpload() {
         return this.readyForUpload.thenApply((p_249056_) -> {
            return this;
         });
      }
   }
}
