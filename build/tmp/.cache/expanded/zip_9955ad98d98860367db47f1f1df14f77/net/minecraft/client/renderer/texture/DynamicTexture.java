package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class DynamicTexture extends AbstractTexture implements Dumpable {
   private static final Logger LOGGER = LogUtils.getLogger();
   @Nullable
   private NativeImage pixels;

   public DynamicTexture(NativeImage pPixels) {
      this.pixels = pPixels;
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
            this.upload();
         });
      } else {
         TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
         this.upload();
      }

   }

   public DynamicTexture(int pWidth, int pHeight, boolean pUseCalloc) {
      RenderSystem.assertOnGameThreadOrInit();
      this.pixels = new NativeImage(pWidth, pHeight, pUseCalloc);
      TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
   }

   public void load(ResourceManager pManager) {
   }

   public void upload() {
      if (this.pixels != null) {
         this.bind();
         this.pixels.upload(0, 0, 0, false);
      } else {
         LOGGER.warn("Trying to upload disposed texture {}", (int)this.getId());
      }

   }

   @Nullable
   public NativeImage getPixels() {
      return this.pixels;
   }

   public void setPixels(NativeImage pPixels) {
      if (this.pixels != null) {
         this.pixels.close();
      }

      this.pixels = pPixels;
   }

   public void close() {
      if (this.pixels != null) {
         this.pixels.close();
         this.releaseId();
         this.pixels = null;
      }

   }

   public void dumpContents(ResourceLocation pResourceLocation, Path pPath) throws IOException {
      if (this.pixels != null) {
         String s = pResourceLocation.toDebugFileName() + ".png";
         Path path = pPath.resolve(s);
         this.pixels.writeToFile(path);
      }

   }
}