package net.minecraft.client.resources.model;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BuiltInModel implements BakedModel {
   private final ItemTransforms itemTransforms;
   private final ItemOverrides overrides;
   private final TextureAtlasSprite particleTexture;
   private final boolean usesBlockLight;

   public BuiltInModel(ItemTransforms pItemTransforms, ItemOverrides pOverrides, TextureAtlasSprite pParticleTexture, boolean pUsesBlockLight) {
      this.itemTransforms = pItemTransforms;
      this.overrides = pOverrides;
      this.particleTexture = pParticleTexture;
      this.usesBlockLight = pUsesBlockLight;
   }

   public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom) {
      return Collections.emptyList();
   }

   public boolean useAmbientOcclusion() {
      return false;
   }

   public boolean isGui3d() {
      return true;
   }

   public boolean usesBlockLight() {
      return this.usesBlockLight;
   }

   public boolean isCustomRenderer() {
      return true;
   }

   public TextureAtlasSprite getParticleIcon() {
      return this.particleTexture;
   }

   public ItemTransforms getTransforms() {
      return this.itemTransforms;
   }

   public ItemOverrides getOverrides() {
      return this.overrides;
   }
}