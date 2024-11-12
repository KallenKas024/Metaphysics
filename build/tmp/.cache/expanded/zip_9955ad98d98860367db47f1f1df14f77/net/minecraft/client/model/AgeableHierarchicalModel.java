package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AgeableHierarchicalModel<E extends Entity> extends HierarchicalModel<E> {
   private final float youngScaleFactor;
   private final float bodyYOffset;

   public AgeableHierarchicalModel(float pYoungScaleFactor, float pBodyYOffset) {
      this(pYoungScaleFactor, pBodyYOffset, RenderType::entityCutoutNoCull);
   }

   public AgeableHierarchicalModel(float pYoungScaleFactor, float pBodyYOffset, Function<ResourceLocation, RenderType> pRenderType) {
      super(pRenderType);
      this.bodyYOffset = pBodyYOffset;
      this.youngScaleFactor = pYoungScaleFactor;
   }

   public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      if (this.young) {
         pPoseStack.pushPose();
         pPoseStack.scale(this.youngScaleFactor, this.youngScaleFactor, this.youngScaleFactor);
         pPoseStack.translate(0.0F, this.bodyYOffset / 16.0F, 0.0F);
         this.root().render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
         pPoseStack.popPose();
      } else {
         this.root().render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
      }

   }
}