package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CritParticle extends TextureSheetParticle {
   CritParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
      this.friction = 0.7F;
      this.gravity = 0.5F;
      this.xd *= (double)0.1F;
      this.yd *= (double)0.1F;
      this.zd *= (double)0.1F;
      this.xd += pXSpeed * 0.4D;
      this.yd += pYSpeed * 0.4D;
      this.zd += pZSpeed * 0.4D;
      float f = (float)(Math.random() * (double)0.3F + (double)0.6F);
      this.rCol = f;
      this.gCol = f;
      this.bCol = f;
      this.quadSize *= 0.75F;
      this.lifetime = Math.max((int)(6.0D / (Math.random() * 0.8D + 0.6D)), 1);
      this.hasPhysics = false;
      this.tick();
   }

   public float getQuadSize(float pScaleFactor) {
      return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      super.tick();
      this.gCol *= 0.96F;
      this.bCol *= 0.9F;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   @OnlyIn(Dist.CLIENT)
   public static class DamageIndicatorProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public DamageIndicatorProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         CritParticle critparticle = new CritParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed + 1.0D, pZSpeed);
         critparticle.setLifetime(20);
         critparticle.pickSprite(this.sprite);
         return critparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class MagicProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public MagicProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         CritParticle critparticle = new CritParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         critparticle.rCol *= 0.3F;
         critparticle.gCol *= 0.8F;
         critparticle.pickSprite(this.sprite);
         return critparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         CritParticle critparticle = new CritParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         critparticle.pickSprite(this.sprite);
         return critparticle;
      }
   }
}