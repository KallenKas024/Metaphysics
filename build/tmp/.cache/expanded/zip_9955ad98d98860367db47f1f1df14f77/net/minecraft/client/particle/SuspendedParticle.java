package net.minecraft.client.particle;

import java.util.Optional;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SuspendedParticle extends TextureSheetParticle {
   SuspendedParticle(ClientLevel pLevel, SpriteSet pSprites, double pX, double pY, double pZ) {
      super(pLevel, pX, pY - 0.125D, pZ);
      this.setSize(0.01F, 0.01F);
      this.pickSprite(pSprites);
      this.quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
      this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
      this.hasPhysics = false;
      this.friction = 1.0F;
      this.gravity = 0.0F;
   }

   SuspendedParticle(ClientLevel pLevel, SpriteSet pSprites, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      super(pLevel, pX, pY - 0.125D, pZ, pXSpeed, pYSpeed, pZSpeed);
      this.setSize(0.01F, 0.01F);
      this.pickSprite(pSprites);
      this.quadSize *= this.random.nextFloat() * 0.6F + 0.6F;
      this.lifetime = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
      this.hasPhysics = false;
      this.friction = 1.0F;
      this.gravity = 0.0F;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   @OnlyIn(Dist.CLIENT)
   public static class CrimsonSporeProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public CrimsonSporeProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         RandomSource randomsource = pLevel.random;
         double d0 = randomsource.nextGaussian() * (double)1.0E-6F;
         double d1 = randomsource.nextGaussian() * (double)1.0E-4F;
         double d2 = randomsource.nextGaussian() * (double)1.0E-6F;
         SuspendedParticle suspendedparticle = new SuspendedParticle(pLevel, this.sprite, pX, pY, pZ, d0, d1, d2);
         suspendedparticle.setColor(0.9F, 0.4F, 0.5F);
         return suspendedparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class SporeBlossomAirProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public SporeBlossomAirProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SuspendedParticle suspendedparticle = new SuspendedParticle(pLevel, this.sprite, pX, pY, pZ, 0.0D, (double)-0.8F, 0.0D) {
            public Optional<ParticleGroup> getParticleGroup() {
               return Optional.of(ParticleGroup.SPORE_BLOSSOM);
            }
         };
         suspendedparticle.lifetime = Mth.randomBetweenInclusive(pLevel.random, 500, 1000);
         suspendedparticle.gravity = 0.01F;
         suspendedparticle.setColor(0.32F, 0.5F, 0.22F);
         return suspendedparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class UnderwaterProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public UnderwaterProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SuspendedParticle suspendedparticle = new SuspendedParticle(pLevel, this.sprite, pX, pY, pZ);
         suspendedparticle.setColor(0.4F, 0.4F, 0.7F);
         return suspendedparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class WarpedSporeProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public WarpedSporeProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         double d0 = (double)pLevel.random.nextFloat() * -1.9D * (double)pLevel.random.nextFloat() * 0.1D;
         SuspendedParticle suspendedparticle = new SuspendedParticle(pLevel, this.sprite, pX, pY, pZ, 0.0D, d0, 0.0D);
         suspendedparticle.setColor(0.1F, 0.1F, 0.3F);
         suspendedparticle.setSize(0.001F, 0.001F);
         return suspendedparticle;
      }
   }
}