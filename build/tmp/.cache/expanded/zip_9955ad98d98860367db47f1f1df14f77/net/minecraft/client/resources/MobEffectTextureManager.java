package net.minecraft.client.resources;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobEffectTextureManager extends TextureAtlasHolder {
   public MobEffectTextureManager(TextureManager pTextureManager) {
      super(pTextureManager, new ResourceLocation("textures/atlas/mob_effects.png"), new ResourceLocation("mob_effects"));
   }

   public TextureAtlasSprite get(MobEffect pEffect) {
      return this.getSprite(BuiltInRegistries.MOB_EFFECT.getKey(pEffect));
   }
}