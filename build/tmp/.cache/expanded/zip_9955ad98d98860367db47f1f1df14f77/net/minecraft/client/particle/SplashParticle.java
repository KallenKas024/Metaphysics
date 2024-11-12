package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SplashParticle extends WaterDropParticle {
   SplashParticle(ClientLevel p_107929_, double p_107930_, double p_107931_, double p_107932_, double p_107933_, double p_107934_, double p_107935_) {
      super(p_107929_, p_107930_, p_107931_, p_107932_);
      this.gravity = 0.04F;
      if (p_107934_ == 0.0D && (p_107933_ != 0.0D || p_107935_ != 0.0D)) {
         this.xd = p_107933_;
         this.yd = 0.1D;
         this.zd = p_107935_;
      }

   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SplashParticle splashparticle = new SplashParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         splashparticle.pickSprite(this.sprite);
         return splashparticle;
      }
   }
}