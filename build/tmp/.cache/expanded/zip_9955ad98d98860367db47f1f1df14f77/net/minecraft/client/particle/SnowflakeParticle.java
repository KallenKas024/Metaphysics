package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnowflakeParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   protected SnowflakeParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
      super(pLevel, pX, pY, pZ);
      this.gravity = 0.225F;
      this.friction = 1.0F;
      this.sprites = pSprites;
      this.xd = pXSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.05F;
      this.yd = pYSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.05F;
      this.zd = pZSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.05F;
      this.quadSize = 0.1F * (this.random.nextFloat() * this.random.nextFloat() * 1.0F + 1.0F);
      this.lifetime = (int)(16.0D / ((double)this.random.nextFloat() * 0.8D + 0.2D)) + 2;
      this.setSpriteFromAge(pSprites);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
      this.xd *= (double)0.95F;
      this.yd *= (double)0.9F;
      this.zd *= (double)0.95F;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet pSprites) {
         this.sprites = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SnowflakeParticle snowflakeparticle = new SnowflakeParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprites);
         snowflakeparticle.setColor(0.923F, 0.964F, 0.999F);
         return snowflakeparticle;
      }
   }
}