package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AshParticle extends BaseAshSmokeParticle {
   protected AshParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, float pQuadSizeMultiplier, SpriteSet pSprites) {
      super(pLevel, pX, pY, pZ, 0.1F, -0.1F, 0.1F, pXSpeed, pYSpeed, pZSpeed, pQuadSizeMultiplier, pSprites, 0.5F, 20, 0.1F, false);
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet pSprites) {
         this.sprites = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         return new AshParticle(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D, 1.0F, this.sprites);
      }
   }
}