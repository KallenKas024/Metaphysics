package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepFurLayer extends RenderLayer<Sheep, SheepModel<Sheep>> {
   private static final ResourceLocation SHEEP_FUR_LOCATION = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
   private final SheepFurModel<Sheep> model;

   public SheepFurLayer(RenderLayerParent<Sheep, SheepModel<Sheep>> pRenderer, EntityModelSet pModelSet) {
      super(pRenderer);
      this.model = new SheepFurModel<>(pModelSet.bakeLayer(ModelLayers.SHEEP_FUR));
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Sheep pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (!pLivingEntity.isSheared()) {
         if (pLivingEntity.isInvisible()) {
            Minecraft minecraft = Minecraft.getInstance();
            boolean flag = minecraft.shouldEntityAppearGlowing(pLivingEntity);
            if (flag) {
               this.getParentModel().copyPropertiesTo(this.model);
               this.model.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
               this.model.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
               VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.outline(SHEEP_FUR_LOCATION));
               this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F), 0.0F, 0.0F, 0.0F, 1.0F);
            }

         } else {
            float f;
            float f1;
            float f2;
            if (pLivingEntity.hasCustomName() && "jeb_".equals(pLivingEntity.getName().getString())) {
               int i1 = 25;
               int i = pLivingEntity.tickCount / 25 + pLivingEntity.getId();
               int j = DyeColor.values().length;
               int k = i % j;
               int l = (i + 1) % j;
               float f3 = ((float)(pLivingEntity.tickCount % 25) + pPartialTicks) / 25.0F;
               float[] afloat1 = Sheep.getColorArray(DyeColor.byId(k));
               float[] afloat2 = Sheep.getColorArray(DyeColor.byId(l));
               f = afloat1[0] * (1.0F - f3) + afloat2[0] * f3;
               f1 = afloat1[1] * (1.0F - f3) + afloat2[1] * f3;
               f2 = afloat1[2] * (1.0F - f3) + afloat2[2] * f3;
            } else {
               float[] afloat = Sheep.getColorArray(pLivingEntity.getColor());
               f = afloat[0];
               f1 = afloat[1];
               f2 = afloat[2];
            }

            coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, SHEEP_FUR_LOCATION, pPoseStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, f, f1, f2);
         }
      }
   }
}