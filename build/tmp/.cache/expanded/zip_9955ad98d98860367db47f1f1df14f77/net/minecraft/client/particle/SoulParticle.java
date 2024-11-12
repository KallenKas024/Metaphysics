package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoulParticle extends RisingParticle {
   private final SpriteSet sprites;
   protected boolean isGlowing;

   SoulParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      this.sprites = pSprites;
      this.scale(1.5F);
      this.setSpriteFromAge(pSprites);
   }

   public int getLightColor(float pPartialTick) {
      return this.isGlowing ? 240 : super.getLightColor(pPartialTick);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
   }

   @OnlyIn(Dist.CLIENT)
   public static class EmissiveProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public EmissiveProvider(SpriteSet pSprite) {
         this.sprite = pSprite;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SoulParticle soulparticle = new SoulParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
         soulparticle.setAlpha(1.0F);
         soulparticle.isGlowing = true;
         return soulparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SoulParticle soulparticle = new SoulParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
         soulparticle.setAlpha(1.0F);
         return soulparticle;
      }
   }
}