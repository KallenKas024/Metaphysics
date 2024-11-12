package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoadingOverlay extends Overlay {
   static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");
   private static final int LOGO_BACKGROUND_COLOR = FastColor.ARGB32.color(255, 239, 50, 61);
   private static final int LOGO_BACKGROUND_COLOR_DARK = FastColor.ARGB32.color(255, 0, 0, 0);
   private static final IntSupplier BRAND_BACKGROUND = () -> {
      return Minecraft.getInstance().options.darkMojangStudiosBackground().get() ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
   };
   private static final int LOGO_SCALE = 240;
   private static final float LOGO_QUARTER_FLOAT = 60.0F;
   private static final int LOGO_QUARTER = 60;
   private static final int LOGO_HALF = 120;
   private static final float LOGO_OVERLAP = 0.0625F;
   private static final float SMOOTHING = 0.95F;
   public static final long FADE_OUT_TIME = 1000L;
   public static final long FADE_IN_TIME = 500L;
   private final Minecraft minecraft;
   private final ReloadInstance reload;
   private final Consumer<Optional<Throwable>> onFinish;
   private final boolean fadeIn;
   private float currentProgress;
   private long fadeOutStart = -1L;
   private long fadeInStart = -1L;

   public LoadingOverlay(Minecraft pMinecraft, ReloadInstance pReload, Consumer<Optional<Throwable>> pOnFinish, boolean pFadeIn) {
      this.minecraft = pMinecraft;
      this.reload = pReload;
      this.onFinish = pOnFinish;
      this.fadeIn = pFadeIn;
   }

   public static void registerTextures(Minecraft pMinecraft) {
      pMinecraft.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new LoadingOverlay.LogoTexture());
   }

   private static int replaceAlpha(int pColor, int pAlpha) {
      return pColor & 16777215 | pAlpha << 24;
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      int i = pGuiGraphics.guiWidth();
      int j = pGuiGraphics.guiHeight();
      long k = Util.getMillis();
      if (this.fadeIn && this.fadeInStart == -1L) {
         this.fadeInStart = k;
      }

      float f = this.fadeOutStart > -1L ? (float)(k - this.fadeOutStart) / 1000.0F : -1.0F;
      float f1 = this.fadeInStart > -1L ? (float)(k - this.fadeInStart) / 500.0F : -1.0F;
      float f2;
      if (f >= 1.0F) {
         if (this.minecraft.screen != null) {
            this.minecraft.screen.render(pGuiGraphics, 0, 0, pPartialTick);
         }

         int l = Mth.ceil((1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
         pGuiGraphics.fill(RenderType.guiOverlay(), 0, 0, i, j, replaceAlpha(BRAND_BACKGROUND.getAsInt(), l));
         f2 = 1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F);
      } else if (this.fadeIn) {
         if (this.minecraft.screen != null && f1 < 1.0F) {
            this.minecraft.screen.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         }

         int l1 = Mth.ceil(Mth.clamp((double)f1, 0.15D, 1.0D) * 255.0D);
         pGuiGraphics.fill(RenderType.guiOverlay(), 0, 0, i, j, replaceAlpha(BRAND_BACKGROUND.getAsInt(), l1));
         f2 = Mth.clamp(f1, 0.0F, 1.0F);
      } else {
         int i2 = BRAND_BACKGROUND.getAsInt();
         float f3 = (float)(i2 >> 16 & 255) / 255.0F;
         float f4 = (float)(i2 >> 8 & 255) / 255.0F;
         float f5 = (float)(i2 & 255) / 255.0F;
         GlStateManager._clearColor(f3, f4, f5, 1.0F);
         GlStateManager._clear(16384, Minecraft.ON_OSX);
         f2 = 1.0F;
      }

      int j2 = (int)((double)pGuiGraphics.guiWidth() * 0.5D);
      int k2 = (int)((double)pGuiGraphics.guiHeight() * 0.5D);
      double d1 = Math.min((double)pGuiGraphics.guiWidth() * 0.75D, (double)pGuiGraphics.guiHeight()) * 0.25D;
      int i1 = (int)(d1 * 0.5D);
      double d0 = d1 * 4.0D;
      int j1 = (int)(d0 * 0.5D);
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, f2);
      pGuiGraphics.blit(MOJANG_STUDIOS_LOGO_LOCATION, j2 - j1, k2 - i1, j1, (int)d1, -0.0625F, 0.0F, 120, 60, 120, 120);
      pGuiGraphics.blit(MOJANG_STUDIOS_LOGO_LOCATION, j2, k2 - i1, j1, (int)d1, 0.0625F, 60.0F, 120, 60, 120, 120);
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      int k1 = (int)((double)pGuiGraphics.guiHeight() * 0.8325D);
      float f6 = this.reload.getActualProgress();
      this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + f6 * 0.050000012F, 0.0F, 1.0F);
      if (f < 1.0F) {
         this.drawProgressBar(pGuiGraphics, i / 2 - j1, k1 - 5, i / 2 + j1, k1 + 5, 1.0F - Mth.clamp(f, 0.0F, 1.0F));
      }

      if (f >= 2.0F) {
         this.minecraft.setOverlay((Overlay)null);
      }

      if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || f1 >= 2.0F)) {
         this.fadeOutStart = Util.getMillis(); // Moved up to guard against inf loops caused by callback
         try {
            this.reload.checkExceptions();
            this.onFinish.accept(Optional.empty());
         } catch (Throwable throwable) {
            this.onFinish.accept(Optional.of(throwable));
         }

         if (this.minecraft.screen != null) {
            this.minecraft.screen.init(this.minecraft, pGuiGraphics.guiWidth(), pGuiGraphics.guiHeight());
         }
      }

   }

   private void drawProgressBar(GuiGraphics pGuiGraphics, int pMinX, int pMinY, int pMaxX, int pMaxY, float pPartialTick) {
      int i = Mth.ceil((float)(pMaxX - pMinX - 2) * this.currentProgress);
      int j = Math.round(pPartialTick * 255.0F);
      int k = FastColor.ARGB32.color(j, 255, 255, 255);
      pGuiGraphics.fill(pMinX + 2, pMinY + 2, pMinX + i, pMaxY - 2, k);
      pGuiGraphics.fill(pMinX + 1, pMinY, pMaxX - 1, pMinY + 1, k);
      pGuiGraphics.fill(pMinX + 1, pMaxY, pMaxX - 1, pMaxY - 1, k);
      pGuiGraphics.fill(pMinX, pMinY, pMinX + 1, pMaxY, k);
      pGuiGraphics.fill(pMaxX, pMinY, pMaxX - 1, pMaxY, k);
   }

   public boolean isPauseScreen() {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   static class LogoTexture extends SimpleTexture {
      public LogoTexture() {
         super(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
      }

      protected SimpleTexture.TextureImage getTextureImage(ResourceManager pResourceManager) {
         VanillaPackResources vanillapackresources = Minecraft.getInstance().getVanillaPackResources();
         IoSupplier<InputStream> iosupplier = vanillapackresources.getResource(PackType.CLIENT_RESOURCES, LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
         if (iosupplier == null) {
            return new SimpleTexture.TextureImage(new FileNotFoundException(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION.toString()));
         } else {
            try (InputStream inputstream = iosupplier.get()) {
               return new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputstream));
            } catch (IOException ioexception) {
               return new SimpleTexture.TextureImage(ioexception);
            }
         }
      }
   }
}
