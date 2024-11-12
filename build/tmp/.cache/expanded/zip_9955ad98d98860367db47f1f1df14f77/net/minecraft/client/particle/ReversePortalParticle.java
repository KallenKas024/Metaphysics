package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReversePortalParticle extends PortalParticle {
   ReversePortalParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      this.quadSize *= 1.5F;
      this.lifetime = (int)(Math.random() * 2.0D) + 60;
   }

   public float getQuadSize(float pScaleFactor) {
      float f = 1.0F - ((float)this.age + pScaleFactor) / ((float)this.lifetime * 1.5F);
      return this.quadSize * f;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         float f = (float)this.age / (float)this.lifetime;
         this.x += this.xd * (double)f;
         this.y += this.yd * (double)f;
         this.z += this.zd * (double)f;
         this.setPos(this.x, this.y, this.z); // FORGE: update the particle's bounding box
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class ReversePortalProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public ReversePortalProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         ReversePortalParticle reverseportalparticle = new ReversePortalParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         reverseportalparticle.pickSprite(this.sprite);
         return reverseportalparticle;
      }
   }
}
