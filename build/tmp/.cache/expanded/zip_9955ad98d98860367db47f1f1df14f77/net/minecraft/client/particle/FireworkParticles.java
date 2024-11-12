package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireworkParticles {
   @OnlyIn(Dist.CLIENT)
   public static class FlashProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public FlashProvider(SpriteSet pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         FireworkParticles.OverlayParticle fireworkparticles$overlayparticle = new FireworkParticles.OverlayParticle(pLevel, pX, pY, pZ);
         fireworkparticles$overlayparticle.pickSprite(this.sprite);
         return fireworkparticles$overlayparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class OverlayParticle extends TextureSheetParticle {
      OverlayParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
         super(pLevel, pX, pY, pZ);
         this.lifetime = 4;
      }

      public ParticleRenderType getRenderType() {
         return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
      }

      public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
         this.setAlpha(0.6F - ((float)this.age + pPartialTicks - 1.0F) * 0.25F * 0.5F);
         super.render(pBuffer, pRenderInfo, pPartialTicks);
      }

      public float getQuadSize(float pScaleFactor) {
         return 7.1F * Mth.sin(((float)this.age + pScaleFactor - 1.0F) * 0.25F * (float)Math.PI);
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class SparkParticle extends SimpleAnimatedParticle {
      private boolean trail;
      private boolean flicker;
      private final ParticleEngine engine;
      private float fadeR;
      private float fadeG;
      private float fadeB;
      private boolean hasFade;

      SparkParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, ParticleEngine pEngine, SpriteSet pSprites) {
         super(pLevel, pX, pY, pZ, pSprites, 0.1F);
         this.xd = pXSpeed;
         this.yd = pYSpeed;
         this.zd = pZSpeed;
         this.engine = pEngine;
         this.quadSize *= 0.75F;
         this.lifetime = 48 + this.random.nextInt(12);
         this.setSpriteFromAge(pSprites);
      }

      public void setTrail(boolean pTrail) {
         this.trail = pTrail;
      }

      public void setFlicker(boolean pTwinkle) {
         this.flicker = pTwinkle;
      }

      public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
         if (!this.flicker || this.age < this.lifetime / 3 || (this.age + this.lifetime) / 3 % 2 == 0) {
            super.render(pBuffer, pRenderInfo, pPartialTicks);
         }

      }

      public void tick() {
         super.tick();
         if (this.trail && this.age < this.lifetime / 2 && (this.age + this.lifetime) % 2 == 0) {
            FireworkParticles.SparkParticle fireworkparticles$sparkparticle = new FireworkParticles.SparkParticle(this.level, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D, this.engine, this.sprites);
            fireworkparticles$sparkparticle.setAlpha(0.99F);
            fireworkparticles$sparkparticle.setColor(this.rCol, this.gCol, this.bCol);
            fireworkparticles$sparkparticle.age = fireworkparticles$sparkparticle.lifetime / 2;
            if (this.hasFade) {
               fireworkparticles$sparkparticle.hasFade = true;
               fireworkparticles$sparkparticle.fadeR = this.fadeR;
               fireworkparticles$sparkparticle.fadeG = this.fadeG;
               fireworkparticles$sparkparticle.fadeB = this.fadeB;
            }

            fireworkparticles$sparkparticle.flicker = this.flicker;
            this.engine.add(fireworkparticles$sparkparticle);
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class SparkProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public SparkProvider(SpriteSet pSprites) {
         this.sprites = pSprites;
      }

      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         FireworkParticles.SparkParticle fireworkparticles$sparkparticle = new FireworkParticles.SparkParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, Minecraft.getInstance().particleEngine, this.sprites);
         fireworkparticles$sparkparticle.setAlpha(0.99F);
         return fireworkparticles$sparkparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Starter extends NoRenderParticle {
      private int life;
      private final ParticleEngine engine;
      private ListTag explosions;
      private boolean twinkleDelay;

      public Starter(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, ParticleEngine pEngine, @Nullable CompoundTag pTag) {
         super(pLevel, pX, pY, pZ);
         this.xd = pXSpeed;
         this.yd = pYSpeed;
         this.zd = pZSpeed;
         this.engine = pEngine;
         this.lifetime = 8;
         if (pTag != null) {
            this.explosions = pTag.getList("Explosions", 10);
            if (this.explosions.isEmpty()) {
               this.explosions = null;
            } else {
               this.lifetime = this.explosions.size() * 2 - 1;

               for(int i = 0; i < this.explosions.size(); ++i) {
                  CompoundTag compoundtag = this.explosions.getCompound(i);
                  if (compoundtag.getBoolean("Flicker")) {
                     this.twinkleDelay = true;
                     this.lifetime += 15;
                     break;
                  }
               }
            }
         }

      }

      public void tick() {
         if (this.life == 0 && this.explosions != null) {
            boolean flag = this.isFarAwayFromCamera();
            boolean flag1 = false;
            if (this.explosions.size() >= 3) {
               flag1 = true;
            } else {
               for(int i = 0; i < this.explosions.size(); ++i) {
                  CompoundTag compoundtag = this.explosions.getCompound(i);
                  if (FireworkRocketItem.Shape.getShape(compoundtag) == FireworkRocketItem.Shape.LARGE_BALL) {
                     flag1 = true;
                     break;
                  }
               }
            }

            SoundEvent soundevent1;
            if (flag1) {
               soundevent1 = flag ? SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_LARGE_BLAST;
            } else {
               soundevent1 = flag ? SoundEvents.FIREWORK_ROCKET_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_BLAST;
            }

            this.level.playLocalSound(this.x, this.y, this.z, soundevent1, SoundSource.AMBIENT, 20.0F, 0.95F + this.random.nextFloat() * 0.1F, true);
         }

         if (this.life % 2 == 0 && this.explosions != null && this.life / 2 < this.explosions.size()) {
            int k = this.life / 2;
            CompoundTag compoundtag1 = this.explosions.getCompound(k);
            FireworkRocketItem.Shape fireworkrocketitem$shape = FireworkRocketItem.Shape.getShape(compoundtag1);
            boolean flag4 = compoundtag1.getBoolean("Trail");
            boolean flag2 = compoundtag1.getBoolean("Flicker");
            int[] aint = compoundtag1.getIntArray("Colors");
            int[] aint1 = compoundtag1.getIntArray("FadeColors");
            if (aint.length == 0) {
               aint = new int[]{DyeColor.BLACK.getFireworkColor()};
            }

            var factory = net.minecraftforge.client.FireworkShapeFactoryRegistry.get(fireworkrocketitem$shape);
            if (factory != null)
               factory.build(this, flag4, flag2, aint, aint1);
            else
            switch (fireworkrocketitem$shape) {
               case SMALL_BALL:
               default:
                  this.createParticleBall(0.25D, 2, aint, aint1, flag4, flag2);
                  break;
               case LARGE_BALL:
                  this.createParticleBall(0.5D, 4, aint, aint1, flag4, flag2);
                  break;
               case STAR:
                  this.createParticleShape(0.5D, new double[][]{{0.0D, 1.0D}, {0.3455D, 0.309D}, {0.9511D, 0.309D}, {0.3795918367346939D, -0.12653061224489795D}, {0.6122448979591837D, -0.8040816326530612D}, {0.0D, -0.35918367346938773D}}, aint, aint1, flag4, flag2, false);
                  break;
               case CREEPER:
                  this.createParticleShape(0.5D, new double[][]{{0.0D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.6D}, {0.6D, 0.6D}, {0.6D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.0D}, {0.4D, 0.0D}, {0.4D, -0.6D}, {0.2D, -0.6D}, {0.2D, -0.4D}, {0.0D, -0.4D}}, aint, aint1, flag4, flag2, true);
                  break;
               case BURST:
                  this.createParticleBurst(aint, aint1, flag4, flag2);
            }

            int j = aint[0];
            float f = (float)((j & 16711680) >> 16) / 255.0F;
            float f1 = (float)((j & '\uff00') >> 8) / 255.0F;
            float f2 = (float)((j & 255) >> 0) / 255.0F;
            Particle particle = this.engine.createParticle(ParticleTypes.FLASH, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
            particle.setColor(f, f1, f2);
         }

         ++this.life;
         if (this.life > this.lifetime) {
            if (this.twinkleDelay) {
               boolean flag3 = this.isFarAwayFromCamera();
               SoundEvent soundevent = flag3 ? SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR : SoundEvents.FIREWORK_ROCKET_TWINKLE;
               this.level.playLocalSound(this.x, this.y, this.z, soundevent, SoundSource.AMBIENT, 20.0F, 0.9F + this.random.nextFloat() * 0.15F, true);
            }

            this.remove();
         }

      }

      private boolean isFarAwayFromCamera() {
         Minecraft minecraft = Minecraft.getInstance();
         return minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(this.x, this.y, this.z) >= 256.0D;
      }

      /**
       * Creates a single particle.
       */
      public void createParticle(double pX, double pY, double pZ, double pMotionX, double pMotionY, double pMotionZ, int[] pSparkColors, int[] pSparkColorFades, boolean pHasTrail, boolean pHasTwinkle) {
         FireworkParticles.SparkParticle fireworkparticles$sparkparticle = (FireworkParticles.SparkParticle)this.engine.createParticle(ParticleTypes.FIREWORK, pX, pY, pZ, pMotionX, pMotionY, pMotionZ);
         fireworkparticles$sparkparticle.setTrail(pHasTrail);
         fireworkparticles$sparkparticle.setFlicker(pHasTwinkle);
         fireworkparticles$sparkparticle.setAlpha(0.99F);
         int i = this.random.nextInt(pSparkColors.length);
         fireworkparticles$sparkparticle.setColor(pSparkColors[i]);
         if (pSparkColorFades.length > 0) {
            fireworkparticles$sparkparticle.setFadeColor(Util.getRandom(pSparkColorFades, this.random));
         }

      }

      /**
       * Creates a small ball or large ball type explosion effect.
       */
      public void createParticleBall(double pSpeed, int pSize, int[] pColours, int[] pFadeColours, boolean pTrail, boolean pTwinkle) {
         double d0 = this.x;
         double d1 = this.y;
         double d2 = this.z;

         for(int i = -pSize; i <= pSize; ++i) {
            for(int j = -pSize; j <= pSize; ++j) {
               for(int k = -pSize; k <= pSize; ++k) {
                  double d3 = (double)j + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double d4 = (double)i + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double d5 = (double)k + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5) / pSpeed + this.random.nextGaussian() * 0.05D;
                  this.createParticle(d0, d1, d2, d3 / d6, d4 / d6, d5 / d6, pColours, pFadeColours, pTrail, pTwinkle);
                  if (i != -pSize && i != pSize && j != -pSize && j != pSize) {
                     k += pSize * 2 - 1;
                  }
               }
            }
         }

      }

      /**
       * Creates a creeper-shaped or star-shaped explosion.
       */
      public void createParticleShape(double pSpeed, double[][] pShape, int[] pColours, int[] pFadeColours, boolean pTrail, boolean pTwinkle, boolean pCreeper) {
         double d0 = pShape[0][0];
         double d1 = pShape[0][1];
         this.createParticle(this.x, this.y, this.z, d0 * pSpeed, d1 * pSpeed, 0.0D, pColours, pFadeColours, pTrail, pTwinkle);
         float f = this.random.nextFloat() * (float)Math.PI;
         double d2 = pCreeper ? 0.034D : 0.34D;

         for(int i = 0; i < 3; ++i) {
            double d3 = (double)f + (double)((float)i * (float)Math.PI) * d2;
            double d4 = d0;
            double d5 = d1;

            for(int j = 1; j < pShape.length; ++j) {
               double d6 = pShape[j][0];
               double d7 = pShape[j][1];

               for(double d8 = 0.25D; d8 <= 1.0D; d8 += 0.25D) {
                  double d9 = Mth.lerp(d8, d4, d6) * pSpeed;
                  double d10 = Mth.lerp(d8, d5, d7) * pSpeed;
                  double d11 = d9 * Math.sin(d3);
                  d9 *= Math.cos(d3);

                  for(double d12 = -1.0D; d12 <= 1.0D; d12 += 2.0D) {
                     this.createParticle(this.x, this.y, this.z, d9 * d12, d10, d11 * d12, pColours, pFadeColours, pTrail, pTwinkle);
                  }
               }

               d4 = d6;
               d5 = d7;
            }
         }

      }

      /**
       * Creates a burst type explosion effect.
       */
      public void createParticleBurst(int[] pColours, int[] pFadeColours, boolean pTrail, boolean pTwinkle) {
         double d0 = this.random.nextGaussian() * 0.05D;
         double d1 = this.random.nextGaussian() * 0.05D;

         for(int i = 0; i < 70; ++i) {
            double d2 = this.xd * 0.5D + this.random.nextGaussian() * 0.15D + d0;
            double d3 = this.zd * 0.5D + this.random.nextGaussian() * 0.15D + d1;
            double d4 = this.yd * 0.5D + this.random.nextDouble() * 0.5D;
            this.createParticle(this.x, this.y, this.z, d2, d4, d3, pColours, pFadeColours, pTrail, pTwinkle);
         }

      }
   }
}
