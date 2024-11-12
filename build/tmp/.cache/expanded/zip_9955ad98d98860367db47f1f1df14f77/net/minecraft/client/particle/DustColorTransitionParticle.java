package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@OnlyIn(Dist.CLIENT)
public class DustColorTransitionParticle extends DustParticleBase<DustColorTransitionOptions> {
   private final Vector3f fromColor;
   private final Vector3f toColor;

   protected DustColorTransitionParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, DustColorTransitionOptions pOptions, SpriteSet pSprites) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, pOptions, pSprites);
      float f = this.random.nextFloat() * 0.4F + 0.6F;
      this.fromColor = this.randomizeColor(pOptions.getFromColor(), f);
      this.toColor = this.randomizeColor(pOptions.getToColor(), f);
   }

   private Vector3f randomizeColor(Vector3f pVector, float pMultiplier) {
      return new Vector3f(this.randomizeColor(pVector.x(), pMultiplier), this.randomizeColor(pVector.y(), pMultiplier), this.randomizeColor(pVector.z(), pMultiplier));
   }

   private void lerpColors(float pPartialTick) {
      float f = ((float)this.age + pPartialTick) / ((float)this.lifetime + 1.0F);
      Vector3f vector3f = (new Vector3f((Vector3fc)this.fromColor)).lerp(this.toColor, f);
      this.rCol = vector3f.x();
      this.gCol = vector3f.y();
      this.bCol = vector3f.z();
   }

   public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
      this.lerpColors(pPartialTicks);
      super.render(pBuffer, pRenderInfo, pPartialTicks);
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<DustColorTransitionOptions> {
      private final SpriteSet sprites;

      public Provider(SpriteSet pSprites) {
         this.sprites = pSprites;
      }

      public Particle createParticle(DustColorTransitionOptions pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         return new DustColorTransitionParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, pType, this.sprites);
      }
   }
}