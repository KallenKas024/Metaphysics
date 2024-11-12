package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Model {
   protected final Function<ResourceLocation, RenderType> renderType;

   public Model(Function<ResourceLocation, RenderType> pRenderType) {
      this.renderType = pRenderType;
   }

   public final RenderType renderType(ResourceLocation pLocation) {
      return this.renderType.apply(pLocation);
   }

   public abstract void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha);
}