package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ShriekParticle extends TextureSheetParticle {
   private static final Vector3f ROTATION_VECTOR = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
   private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
   private static final float MAGICAL_X_ROT = 1.0472F;
   private int delay;

   ShriekParticle(ClientLevel pLevel, double pX, double pY, double pZ, int pDelay) {
      super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
      this.quadSize = 0.85F;
      this.delay = pDelay;
      this.lifetime = 30;
      this.gravity = 0.0F;
      this.xd = 0.0D;
      this.yd = 0.1D;
      this.zd = 0.0D;
   }

   public float getQuadSize(float pScaleFactor) {
      return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 0.75F, 0.0F, 1.0F);
   }

   public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
      if (this.delay <= 0) {
         this.alpha = 1.0F - Mth.clamp(((float)this.age + pPartialTicks) / (float)this.lifetime, 0.0F, 1.0F);
         this.renderRotatedParticle(pBuffer, pRenderInfo, pPartialTicks, (p_253347_) -> {
            p_253347_.mul((new Quaternionf()).rotationX(-1.0472F));
         });
         this.renderRotatedParticle(pBuffer, pRenderInfo, pPartialTicks, (p_253346_) -> {
            p_253346_.mul((new Quaternionf()).rotationYXZ(-(float)Math.PI, 1.0472F, 0.0F));
         });
      }
   }

   private void renderRotatedParticle(VertexConsumer pConsumer, Camera pCamera, float pPartialTick, Consumer<Quaternionf> pQuaternion) {
      Vec3 vec3 = pCamera.getPosition();
      float f = (float)(Mth.lerp((double)pPartialTick, this.xo, this.x) - vec3.x());
      float f1 = (float)(Mth.lerp((double)pPartialTick, this.yo, this.y) - vec3.y());
      float f2 = (float)(Mth.lerp((double)pPartialTick, this.zo, this.z) - vec3.z());
      Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
      pQuaternion.accept(quaternionf);
      quaternionf.transform(TRANSFORM_VECTOR);
      Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
      float f3 = this.getQuadSize(pPartialTick);

      for(int i = 0; i < 4; ++i) {
         Vector3f vector3f = avector3f[i];
         vector3f.rotate(quaternionf);
         vector3f.mul(f3);
         vector3f.add(f, f1, f2);
      }

      int j = this.getLightColor(pPartialTick);
      this.makeCornerVertex(pConsumer, avector3f[0], this.getU1(), this.getV1(), j);
      this.makeCornerVertex(pConsumer, avector3f[1], this.getU1(), this.getV0(), j);
      this.makeCornerVertex(pConsumer, avector3f[2], this.getU0(), this.getV0(), j);
      this.makeCornerVertex(pConsumer, avector3f[3], this.getU0(), this.getV1(), j);
   }

   private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVertex, float pU, float pV, int pPackedLight) {
      pConsumer.vertex((double)pVertex.x(), (double)pVertex.y(), (double)pVertex.z()).uv(pU, pV).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(pPackedLight).endVertex();
   }

   public int getLightColor(float pPartialTick) {
      return 240;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      if (this.delay > 0) {
         --this.delay;
      } else {
         super.tick();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<ShriekParticleOption> {
      private final SpriteSet sprite;

      public Provider(SpriteSet pSprite) {
         this.sprite = pSprite;
      }

      public Particle createParticle(ShriekParticleOption pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         ShriekParticle shriekparticle = new ShriekParticle(pLevel, pX, pY, pZ, pType.getDelay());
         shriekparticle.pickSprite(this.sprite);
         shriekparticle.setAlpha(1.0F);
         return shriekparticle;
      }
   }
}