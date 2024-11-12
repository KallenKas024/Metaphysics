package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WardenEmissiveLayer<T extends Warden, M extends WardenModel<T>> extends RenderLayer<T, M> {
   private final ResourceLocation texture;
   private final WardenEmissiveLayer.AlphaFunction<T> alphaFunction;
   private final WardenEmissiveLayer.DrawSelector<T, M> drawSelector;

   public WardenEmissiveLayer(RenderLayerParent<T, M> pRenderer, ResourceLocation pTexture, WardenEmissiveLayer.AlphaFunction<T> pAlphaFunction, WardenEmissiveLayer.DrawSelector<T, M> pDrawSelector) {
      super(pRenderer);
      this.texture = pTexture;
      this.alphaFunction = pAlphaFunction;
      this.drawSelector = pDrawSelector;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (!pLivingEntity.isInvisible()) {
         this.onlyDrawSelectedParts();
         VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
         this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F), 1.0F, 1.0F, 1.0F, this.alphaFunction.apply(pLivingEntity, pPartialTick, pAgeInTicks));
         this.resetDrawForAllParts();
      }
   }

   private void onlyDrawSelectedParts() {
      List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel());
      this.getParentModel().root().getAllParts().forEach((p_234918_) -> {
         p_234918_.skipDraw = true;
      });
      list.forEach((p_234916_) -> {
         p_234916_.skipDraw = false;
      });
   }

   private void resetDrawForAllParts() {
      this.getParentModel().root().getAllParts().forEach((p_234913_) -> {
         p_234913_.skipDraw = false;
      });
   }

   @OnlyIn(Dist.CLIENT)
   public interface AlphaFunction<T extends Warden> {
      float apply(T pLivingEntity, float pPartialTick, float pAgeInTicks);
   }

   @OnlyIn(Dist.CLIENT)
   public interface DrawSelector<T extends Warden, M extends EntityModel<T>> {
      List<ModelPart> getPartsToDraw(M pParentModel);
   }
}