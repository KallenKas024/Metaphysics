package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SonicBoomParticle extends HugeExplosionParticle {
   protected SonicBoomParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pQuadSizeMultiplier, SpriteSet pSprites) {
      super(pLevel, pX, pY, pZ, pQuadSizeMultiplier, pSprites);
      this.lifetime = 16;
      this.quadSize = 1.5F;
      this.setSpriteFromAge(pSprites);
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet pSprites) {
         this.sprites = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         return new SonicBoomParticle(pLevel, pX, pY, pZ, pXSpeed, this.sprites);
      }
   }
}