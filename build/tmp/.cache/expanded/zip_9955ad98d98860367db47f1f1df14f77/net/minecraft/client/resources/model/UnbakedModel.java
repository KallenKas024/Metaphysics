package net.minecraft.client.resources.model;

import java.util.Collection;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface UnbakedModel {
   Collection<ResourceLocation> getDependencies();

   void resolveParents(Function<ResourceLocation, UnbakedModel> pResolver);

   @Nullable
   BakedModel bake(ModelBaker pBaker, Function<Material, TextureAtlasSprite> pSpriteGetter, ModelState pState, ResourceLocation pLocation);
}