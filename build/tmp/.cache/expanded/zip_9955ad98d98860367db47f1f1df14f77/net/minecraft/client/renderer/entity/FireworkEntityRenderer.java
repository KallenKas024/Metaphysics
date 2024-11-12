package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireworkEntityRenderer extends EntityRenderer<FireworkRocketEntity> {
   private final ItemRenderer itemRenderer;

   public FireworkEntityRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.itemRenderer = pContext.getItemRenderer();
   }

   public void render(FireworkRocketEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      pPoseStack.pushPose();
      pPoseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      if (pEntity.isShotAtAngle()) {
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
         pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
         pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
      }

      this.itemRenderer.renderStatic(pEntity.getItem(), ItemDisplayContext.GROUND, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pEntity.level(), pEntity.getId());
      pPoseStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(FireworkRocketEntity pEntity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}