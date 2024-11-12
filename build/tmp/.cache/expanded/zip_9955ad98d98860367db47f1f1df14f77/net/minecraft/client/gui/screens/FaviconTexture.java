package net.minecraft.client.gui.screens;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FaviconTexture implements AutoCloseable {
   private static final ResourceLocation MISSING_LOCATION = new ResourceLocation("textures/misc/unknown_server.png");
   private static final int WIDTH = 64;
   private static final int HEIGHT = 64;
   private final TextureManager textureManager;
   private final ResourceLocation textureLocation;
   @Nullable
   private DynamicTexture texture;
   private boolean closed;

   private FaviconTexture(TextureManager pTextureManager, ResourceLocation pTextureLocation) {
      this.textureManager = pTextureManager;
      this.textureLocation = pTextureLocation;
   }

   public static FaviconTexture forWorld(TextureManager pTextureManager, String pWorldName) {
      return new FaviconTexture(pTextureManager, new ResourceLocation("minecraft", "worlds/" + Util.sanitizeName(pWorldName, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(pWorldName) + "/icon"));
   }

   public static FaviconTexture forServer(TextureManager pTextureManager, String pWorldName) {
      return new FaviconTexture(pTextureManager, new ResourceLocation("minecraft", "servers/" + Hashing.sha1().hashUnencodedChars(pWorldName) + "/icon"));
   }

   public void upload(NativeImage pImage) {
      if (pImage.getWidth() == 64 && pImage.getHeight() == 64) {
         try {
            this.checkOpen();
            if (this.texture == null) {
               this.texture = new DynamicTexture(pImage);
            } else {
               this.texture.setPixels(pImage);
               this.texture.upload();
            }

            this.textureManager.register(this.textureLocation, this.texture);
         } catch (Throwable throwable) {
            pImage.close();
            this.clear();
            throw throwable;
         }
      } else {
         pImage.close();
         throw new IllegalArgumentException("Icon must be 64x64, but was " + pImage.getWidth() + "x" + pImage.getHeight());
      }
   }

   public void clear() {
      this.checkOpen();
      if (this.texture != null) {
         this.textureManager.release(this.textureLocation);
         this.texture.close();
         this.texture = null;
      }

   }

   public ResourceLocation textureLocation() {
      return this.texture != null ? this.textureLocation : MISSING_LOCATION;
   }

   public void close() {
      this.clear();
      this.closed = true;
   }

   private void checkOpen() {
      if (this.closed) {
         throw new IllegalStateException("Icon already closed");
      }
   }
}