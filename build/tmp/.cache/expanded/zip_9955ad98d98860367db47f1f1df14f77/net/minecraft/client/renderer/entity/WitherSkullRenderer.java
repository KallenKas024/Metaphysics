package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherSkullRenderer extends EntityRenderer<WitherSkull> {
   private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
   private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");
   private final SkullModel model;

   public WitherSkullRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.model = new SkullModel(pContext.bakeLayer(ModelLayers.WITHER_SKULL));
   }

   public static LayerDefinition createSkullLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   protected int getBlockLightLevel(WitherSkull pEntity, BlockPos pPos) {
      return 15;
   }

   public void render(WitherSkull pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      pPoseStack.pushPose();
      pPoseStack.scale(-1.0F, -1.0F, 1.0F);
      float f = Mth.rotLerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot());
      float f1 = Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());
      VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(this.getTextureLocation(pEntity)));
      this.model.setupAnim(0.0F, f, f1);
      this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      pPoseStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(WitherSkull pEntity) {
      return pEntity.isDangerous() ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
   }
}