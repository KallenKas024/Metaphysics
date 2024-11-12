package net.minecraft.client.resources.model;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AtlasSet implements AutoCloseable {
   private final Map<ResourceLocation, AtlasSet.AtlasEntry> atlases;

   public AtlasSet(Map<ResourceLocation, ResourceLocation> pAtlasMap, TextureManager pTextureManager) {
      this.atlases = pAtlasMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (p_261403_) -> {
         TextureAtlas textureatlas = new TextureAtlas(p_261403_.getKey());
         pTextureManager.register(p_261403_.getKey(), textureatlas);
         return new AtlasSet.AtlasEntry(textureatlas, p_261403_.getValue());
      }));
   }

   public TextureAtlas getAtlas(ResourceLocation pLocation) {
      return this.atlases.get(pLocation).atlas();
   }

   public void close() {
      this.atlases.values().forEach(AtlasSet.AtlasEntry::close);
      this.atlases.clear();
   }

   public Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> scheduleLoad(ResourceManager pResourceManager, int pMipLevel, Executor pExecutor) {
      return this.atlases.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (p_261401_) -> {
         AtlasSet.AtlasEntry atlasset$atlasentry = p_261401_.getValue();
         return SpriteLoader.create(atlasset$atlasentry.atlas).loadAndStitch(pResourceManager, atlasset$atlasentry.atlasInfoLocation, pMipLevel, pExecutor).thenApply((p_250418_) -> {
            return new AtlasSet.StitchResult(atlasset$atlasentry.atlas, p_250418_);
         });
      }));
   }

   @OnlyIn(Dist.CLIENT)
   static record AtlasEntry(TextureAtlas atlas, ResourceLocation atlasInfoLocation) implements AutoCloseable {
      public void close() {
         this.atlas.clearTextureData();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class StitchResult {
      private final TextureAtlas atlas;
      private final SpriteLoader.Preparations preparations;

      public StitchResult(TextureAtlas pAtlas, SpriteLoader.Preparations pPreperations) {
         this.atlas = pAtlas;
         this.preparations = pPreperations;
      }

      @Nullable
      public TextureAtlasSprite getSprite(ResourceLocation pLocation) {
         return this.preparations.regions().get(pLocation);
      }

      public TextureAtlasSprite missing() {
         return this.preparations.missing();
      }

      public CompletableFuture<Void> readyForUpload() {
         return this.preparations.readyForUpload();
      }

      public void upload() {
         this.atlas.upload(this.preparations);
      }
   }
}