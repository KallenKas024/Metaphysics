package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NoteParticle extends TextureSheetParticle {
   NoteParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pColor) {
      super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
      this.friction = 0.66F;
      this.speedUpWhenYMotionIsBlocked = true;
      this.xd *= (double)0.01F;
      this.yd *= (double)0.01F;
      this.zd *= (double)0.01F;
      this.yd += 0.2D;
      this.rCol = Math.max(0.0F, Mth.sin(((float)pColor + 0.0F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
      this.gCol = Math.max(0.0F, Mth.sin(((float)pColor + 0.33333334F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
      this.bCol = Math.max(0.0F, Mth.sin(((float)pColor + 0.6666667F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
      this.quadSize *= 1.5F;
      this.lifetime = 6;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public float getQuadSize(float pScaleFactor) {
      return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         NoteParticle noteparticle = new NoteParticle(pLevel, pX, pY, pZ, pXSpeed);
         noteparticle.pickSprite(this.sprite);
         return noteparticle;
      }
   }
}