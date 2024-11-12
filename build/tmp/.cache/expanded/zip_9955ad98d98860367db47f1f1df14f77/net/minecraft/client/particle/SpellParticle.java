package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpellParticle extends TextureSheetParticle {
   private static final RandomSource RANDOM = RandomSource.create();
   private final SpriteSet sprites;

   SpellParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
      super(pLevel, pX, pY, pZ, 0.5D - RANDOM.nextDouble(), pYSpeed, 0.5D - RANDOM.nextDouble());
      this.friction = 0.96F;
      this.gravity = -0.1F;
      this.speedUpWhenYMotionIsBlocked = true;
      this.sprites = pSprites;
      this.yd *= (double)0.2F;
      if (pXSpeed == 0.0D && pZSpeed == 0.0D) {
         this.xd *= (double)0.1F;
         this.zd *= (double)0.1F;
      }

      this.quadSize *= 0.75F;
      this.lifetime = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
      this.hasPhysics = false;
      this.setSpriteFromAge(pSprites);
      if (this.isCloseToScopingPlayer()) {
         this.setAlpha(0.0F);
      }

   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
      if (this.isCloseToScopingPlayer()) {
         this.setAlpha(0.0F);
      } else {
         this.setAlpha(Mth.lerp(0.05F, this.alpha, 1.0F));
      }

   }

   private boolean isCloseToScopingPlayer() {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer localplayer = minecraft.player;
      return localplayer != null && localplayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0D && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping();
   }

   @OnlyIn(Dist.CLIENT)
   public static class AmbientMobProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public AmbientMobProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         Particle particle = new SpellParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
         particle.setAlpha(0.15F);
         particle.setColor((float)pXSpeed, (float)pYSpeed, (float)pZSpeed);
         return particle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class InstantProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public InstantProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         return new SpellParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class MobProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public MobProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         Particle particle = new SpellParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
         particle.setColor((float)pXSpeed, (float)pYSpeed, (float)pZSpeed);
         return particle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Provider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         return new SpellParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class WitchProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public WitchProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SpellParticle spellparticle = new SpellParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
         float f = pLevel.random.nextFloat() * 0.5F + 0.35F;
         spellparticle.setColor(1.0F * f, 0.0F * f, 1.0F * f);
         return spellparticle;
      }
   }
}