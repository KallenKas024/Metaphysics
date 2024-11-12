package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ColorableHierarchicalModel<E extends Entity> extends HierarchicalModel<E> {
   private float r = 1.0F;
   private float g = 1.0F;
   private float b = 1.0F;

   public void setColor(float pR, float pG, float pB) {
      this.r = pR;
      this.g = pG;
      this.b = pB;
   }

   public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      super.renderToBuffer(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, this.r * pRed, this.g * pGreen, this.b * pBlue, pAlpha);
   }
}