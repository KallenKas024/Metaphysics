package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SculkChargeParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   SculkChargeParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprite) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      this.friction = 0.96F;
      this.sprites = pSprite;
      this.scale(1.5F);
      this.hasPhysics = false;
      this.setSpriteFromAge(pSprite);
   }

   public int getLightColor(float pPartialTick) {
      return 240;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
   }

   @OnlyIn(Dist.CLIENT)
   public static record Provider(SpriteSet sprite) implements ParticleProvider<SculkChargeParticleOptions> {
      public Particle createParticle(SculkChargeParticleOptions p_233918_, ClientLevel p_233919_, double p_233920_, double p_233921_, double p_233922_, double p_233923_, double p_233924_, double p_233925_) {
         SculkChargeParticle sculkchargeparticle = new SculkChargeParticle(p_233919_, p_233920_, p_233921_, p_233922_, p_233923_, p_233924_, p_233925_, this.sprite);
         sculkchargeparticle.setAlpha(1.0F);
         sculkchargeparticle.setParticleSpeed(p_233923_, p_233924_, p_233925_);
         sculkchargeparticle.oRoll = p_233918_.roll();
         sculkchargeparticle.roll = p_233918_.roll();
         sculkchargeparticle.setLifetime(p_233919_.random.nextInt(12) + 8);
         return sculkchargeparticle;
      }
   }
}