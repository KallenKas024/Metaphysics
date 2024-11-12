package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PortalParticle extends TextureSheetParticle {
   private final double xStart;
   private final double yStart;
   private final double zStart;

   protected PortalParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      super(pLevel, pX, pY, pZ);
      this.xd = pXSpeed;
      this.yd = pYSpeed;
      this.zd = pZSpeed;
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.xStart = this.x;
      this.yStart = this.y;
      this.zStart = this.z;
      this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);
      float f = this.random.nextFloat() * 0.6F + 0.4F;
      this.rCol = f * 0.9F;
      this.gCol = f * 0.3F;
      this.bCol = f;
      this.lifetime = (int)(Math.random() * 10.0D) + 40;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double pX, double pY, double pZ) {
      this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
      this.setLocationFromBoundingbox();
   }

   public float getQuadSize(float pScaleFactor) {
      float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
      f = 1.0F - f;
      f *= f;
      f = 1.0F - f;
      return this.quadSize * f;
   }

   public int getLightColor(float pPartialTick) {
      int i = super.getLightColor(pPartialTick);
      float f = (float)this.age / (float)this.lifetime;
      f *= f;
      f *= f;
      int j = i & 255;
      int k = i >> 16 & 255;
      k += (int)(f * 15.0F * 16.0F);
      if (k > 240) {
         k = 240;
      }

      return j | k << 16;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         float f = (float)this.age / (float)this.lifetime;
         float f1 = -f + f * f * 2.0F;
         float f2 = 1.0F - f1;
         this.x = this.xStart + this.xd * (double)f2;
         this.y = this.yStart + this.yd * (double)f2 + (double)(1.0F - f);
         this.z = this.zStart + this.zd * (double)f2;
         this.setPos(this.x, this.y, this.z); // FORGE: update the particle's bounding box
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet pSprite) {
         this.sprite = pSprite;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         PortalParticle portalparticle = new PortalParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         portalparticle.pickSprite(this.sprite);
         return portalparticle;
      }
   }
}
