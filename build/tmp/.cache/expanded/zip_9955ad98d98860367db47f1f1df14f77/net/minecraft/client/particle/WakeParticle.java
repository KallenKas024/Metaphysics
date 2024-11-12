package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WakeParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   WakeParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
      super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
      this.sprites = pSprites;
      this.xd *= (double)0.3F;
      this.yd = Math.random() * (double)0.2F + (double)0.1F;
      this.zd *= (double)0.3F;
      this.setSize(0.01F, 0.01F);
      this.lifetime = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
      this.setSpriteFromAge(pSprites);
      this.gravity = 0.0F;
      this.xd = pXSpeed;
      this.yd = pYSpeed;
      this.zd = pZSpeed;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      int i = 60 - this.lifetime;
      if (this.lifetime-- <= 0) {
         this.remove();
      } else {
         this.yd -= (double)this.gravity;
         this.move(this.xd, this.yd, this.zd);
         this.xd *= (double)0.98F;
         this.yd *= (double)0.98F;
         this.zd *= (double)0.98F;
         float f = (float)i * 0.001F;
         this.setSize(f, f);
         this.setSprite(this.sprites.get(i % 4, 4));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet pSprites) {
         this.sprites = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         return new WakeParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprites);
      }
   }
}