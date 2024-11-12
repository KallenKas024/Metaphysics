package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.Optional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OutlineBufferSource implements MultiBufferSource {
   private final MultiBufferSource.BufferSource bufferSource;
   private final MultiBufferSource.BufferSource outlineBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
   private int teamR = 255;
   private int teamG = 255;
   private int teamB = 255;
   private int teamA = 255;

   public OutlineBufferSource(MultiBufferSource.BufferSource pBufferSource) {
      this.bufferSource = pBufferSource;
   }

   public VertexConsumer getBuffer(RenderType pRenderType) {
      if (pRenderType.isOutline()) {
         VertexConsumer vertexconsumer2 = this.outlineBufferSource.getBuffer(pRenderType);
         return new OutlineBufferSource.EntityOutlineGenerator(vertexconsumer2, this.teamR, this.teamG, this.teamB, this.teamA);
      } else {
         VertexConsumer vertexconsumer = this.bufferSource.getBuffer(pRenderType);
         Optional<RenderType> optional = pRenderType.outline();
         if (optional.isPresent()) {
            VertexConsumer vertexconsumer1 = this.outlineBufferSource.getBuffer(optional.get());
            OutlineBufferSource.EntityOutlineGenerator outlinebuffersource$entityoutlinegenerator = new OutlineBufferSource.EntityOutlineGenerator(vertexconsumer1, this.teamR, this.teamG, this.teamB, this.teamA);
            return VertexMultiConsumer.create(outlinebuffersource$entityoutlinegenerator, vertexconsumer);
         } else {
            return vertexconsumer;
         }
      }
   }

   public void setColor(int pRed, int pGreen, int pBlue, int pAlpha) {
      this.teamR = pRed;
      this.teamG = pGreen;
      this.teamB = pBlue;
      this.teamA = pAlpha;
   }

   public void endOutlineBatch() {
      this.outlineBufferSource.endBatch();
   }

   @OnlyIn(Dist.CLIENT)
   static class EntityOutlineGenerator extends DefaultedVertexConsumer {
      private final VertexConsumer delegate;
      private double x;
      private double y;
      private double z;
      private float u;
      private float v;

      EntityOutlineGenerator(VertexConsumer pDelegate, int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
         this.delegate = pDelegate;
         super.defaultColor(pDefaultR, pDefaultG, pDefaultB, pDefaultA);
      }

      public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
      }

      public void unsetDefaultColor() {
      }

      public VertexConsumer vertex(double pX, double pY, double pZ) {
         this.x = pX;
         this.y = pY;
         this.z = pZ;
         return this;
      }

      public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
         return this;
      }

      public VertexConsumer uv(float pU, float pV) {
         this.u = pU;
         this.v = pV;
         return this;
      }

      public VertexConsumer overlayCoords(int pU, int pV) {
         return this;
      }

      public VertexConsumer uv2(int pU, int pV) {
         return this;
      }

      public VertexConsumer normal(float pX, float pY, float pZ) {
         return this;
      }

      public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
         this.delegate.vertex((double)pX, (double)pY, (double)pZ).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(pTexU, pTexV).endVertex();
      }

      public void endVertex() {
         this.delegate.vertex(this.x, this.y, this.z).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(this.u, this.v).endVertex();
      }
   }
}