package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AttackSweepParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   AttackSweepParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pQuadSizeMultiplier, SpriteSet pSprites) {
      super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
      this.sprites = pSprites;
      this.lifetime = 4;
      float f = this.random.nextFloat() * 0.6F + 0.4F;
      this.rCol = f;
      this.gCol = f;
      this.bCol = f;
      this.quadSize = 1.0F - (float)pQuadSizeMultiplier * 0.5F;
      this.setSpriteFromAge(pSprites);
   }

   public int getLightColor(float pPartialTick) {
      return 15728880;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.setSpriteFromAge(this.sprites);
      }
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_LIT;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet pSprites) {
         this.sprites = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         return new AttackSweepParticle(pLevel, pX, pY, pZ, pXSpeed, this.sprites);
      }
   }
}