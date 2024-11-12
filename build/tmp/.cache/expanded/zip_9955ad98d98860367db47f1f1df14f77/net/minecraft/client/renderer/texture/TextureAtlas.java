package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureAtlas extends AbstractTexture implements Dumpable, Tickable {
   private static final Logger LOGGER = LogUtils.getLogger();
   /** @deprecated */
   @Deprecated
   public static final ResourceLocation LOCATION_BLOCKS = InventoryMenu.BLOCK_ATLAS;
   /** @deprecated */
   @Deprecated
   public static final ResourceLocation LOCATION_PARTICLES = new ResourceLocation("textures/atlas/particles.png");
   private List<SpriteContents> sprites = List.of();
   private List<TextureAtlasSprite.Ticker> animatedTextures = List.of();
   private Map<ResourceLocation, TextureAtlasSprite> texturesByName = Map.of();
   private final ResourceLocation location;
   private final int maxSupportedTextureSize;
   private int width;
   private int height;
   private int mipLevel;

   public TextureAtlas(ResourceLocation pLocation) {
      this.location = pLocation;
      this.maxSupportedTextureSize = RenderSystem.maxSupportedTextureSize();
   }

   public void load(ResourceManager pResourceManager) {
   }

   public void upload(SpriteLoader.Preparations pPreparations) {
      LOGGER.info("Created: {}x{}x{} {}-atlas", pPreparations.width(), pPreparations.height(), pPreparations.mipLevel(), this.location);
      TextureUtil.prepareImage(this.getId(), pPreparations.mipLevel(), pPreparations.width(), pPreparations.height());
      this.width = pPreparations.width();
      this.height = pPreparations.height();
      this.mipLevel = pPreparations.mipLevel();
      this.clearTextureData();
      this.texturesByName = Map.copyOf(pPreparations.regions());
      List<SpriteContents> list = new ArrayList<>();
      List<TextureAtlasSprite.Ticker> list1 = new ArrayList<>();

      for(TextureAtlasSprite textureatlassprite : pPreparations.regions().values()) {
         list.add(textureatlassprite.contents());

         try {
            textureatlassprite.uploadFirstFrame();
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Texture being stitched together");
            crashreportcategory.setDetail("Atlas path", this.location);
            crashreportcategory.setDetail("Sprite", textureatlassprite);
            throw new ReportedException(crashreport);
         }

         TextureAtlasSprite.Ticker textureatlassprite$ticker = textureatlassprite.createTicker();
         if (textureatlassprite$ticker != null) {
            list1.add(textureatlassprite$ticker);
         }
      }

      this.sprites = List.copyOf(list);
      this.animatedTextures = List.copyOf(list1);

      net.minecraftforge.client.ForgeHooksClient.onTextureStitchedPost(this);
   }

   public void dumpContents(ResourceLocation pResourceLocation, Path pPath) throws IOException {
      String s = pResourceLocation.toDebugFileName();
      TextureUtil.writeAsPNG(pPath, s, this.getId(), this.mipLevel, this.width, this.height);
      dumpSpriteNames(pPath, s, this.texturesByName);
   }

   private static void dumpSpriteNames(Path pOutputDir, String pOutputFilename, Map<ResourceLocation, TextureAtlasSprite> p_261722_) {
      Path path = pOutputDir.resolve(pOutputFilename + ".txt");

      try (Writer writer = Files.newBufferedWriter(path)) {
         for(Map.Entry<ResourceLocation, TextureAtlasSprite> entry : p_261722_.entrySet().stream().sorted(Entry.comparingByKey()).toList()) {
            TextureAtlasSprite textureatlassprite = entry.getValue();
            writer.write(String.format(Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", entry.getKey(), textureatlassprite.getX(), textureatlassprite.getY(), textureatlassprite.contents().width(), textureatlassprite.contents().height()));
         }
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to write file {}", path, ioexception);
      }

   }

   public void cycleAnimationFrames() {
      this.bind();

      for(TextureAtlasSprite.Ticker textureatlassprite$ticker : this.animatedTextures) {
         textureatlassprite$ticker.tickAndUpload();
      }

   }

   public void tick() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::cycleAnimationFrames);
      } else {
         this.cycleAnimationFrames();
      }

   }

   public TextureAtlasSprite getSprite(ResourceLocation pName) {
      TextureAtlasSprite textureatlassprite = this.texturesByName.get(pName);
      return textureatlassprite == null ? this.texturesByName.get(MissingTextureAtlasSprite.getLocation()) : textureatlassprite;
   }

   public void clearTextureData() {
      this.sprites.forEach(SpriteContents::close);
      this.animatedTextures.forEach(TextureAtlasSprite.Ticker::close);
      this.sprites = List.of();
      this.animatedTextures = List.of();
      this.texturesByName = Map.of();
   }

   public ResourceLocation location() {
      return this.location;
   }

   public int maxSupportedTextureSize() {
      return this.maxSupportedTextureSize;
   }

   int getWidth() {
      return this.width;
   }

   int getHeight() {
      return this.height;
   }

   public void updateFilter(SpriteLoader.Preparations pPreparations) {
      this.setFilter(false, pPreparations.mipLevel() > 0);
   }

   /**
    * {@return the set of sprites in this atlas.}
    */
   public java.util.Set<ResourceLocation> getTextureLocations() {
      return java.util.Collections.unmodifiableSet(texturesByName.keySet());
   }
}
