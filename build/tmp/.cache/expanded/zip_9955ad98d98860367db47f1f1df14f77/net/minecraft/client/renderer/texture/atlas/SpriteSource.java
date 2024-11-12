package net.minecraft.client.renderer.texture.atlas;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SpriteSource {
   FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");

   void run(ResourceManager pResourceManager, SpriteSource.Output pOutput);

   SpriteSourceType type();

   @OnlyIn(Dist.CLIENT)
   public interface Output {
      default void add(ResourceLocation pLocation, Resource pResource) {
         this.add(pLocation, () -> {
            return SpriteLoader.loadSprite(pLocation, pResource);
         });
      }

      void add(ResourceLocation pLocation, SpriteSource.SpriteSupplier pSprite);

      void removeAll(Predicate<ResourceLocation> pPredicate);
   }

   @OnlyIn(Dist.CLIENT)
   public interface SpriteSupplier extends Supplier<SpriteContents> {
      default void discard() {
      }
   }
}