package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NoRenderParticle extends Particle {
   protected NoRenderParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
      super(pLevel, pX, pY, pZ);
   }

   protected NoRenderParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
   }

   public final void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.NO_RENDER;
   }
}