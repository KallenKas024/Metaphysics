package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ChestRaftModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.WaterPatchModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class BoatRenderer extends EntityRenderer<Boat> {
   private final Map<Boat.Type, Pair<ResourceLocation, ListModel<Boat>>> boatResources;

   public BoatRenderer(EntityRendererProvider.Context pContext, boolean pChestBoat) {
      super(pContext);
      this.shadowRadius = 0.8F;
      this.boatResources = Stream.of(Boat.Type.values()).collect(ImmutableMap.toImmutableMap((p_173938_) -> {
         return p_173938_;
      }, (p_247941_) -> {
         return Pair.of(new ResourceLocation(getTextureLocation(p_247941_, pChestBoat)), this.createBoatModel(pContext, p_247941_, pChestBoat));
      }));
   }

   private ListModel<Boat> createBoatModel(EntityRendererProvider.Context pContext, Boat.Type pType, boolean pChestBoat) {
      ModelLayerLocation modellayerlocation = pChestBoat ? ModelLayers.createChestBoatModelName(pType) : ModelLayers.createBoatModelName(pType);
      ModelPart modelpart = pContext.bakeLayer(modellayerlocation);
      if (pType == Boat.Type.BAMBOO) {
         return (ListModel<Boat>)(pChestBoat ? new ChestRaftModel(modelpart) : new RaftModel(modelpart));
      } else {
         return (ListModel<Boat>)(pChestBoat ? new ChestBoatModel(modelpart) : new BoatModel(modelpart));
      }
   }

   private static String getTextureLocation(Boat.Type pType, boolean pChestBoat) {
      return pChestBoat ? "textures/entity/chest_boat/" + pType.getName() + ".png" : "textures/entity/boat/" + pType.getName() + ".png";
   }

   public void render(Boat pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      pPoseStack.pushPose();
      pPoseStack.translate(0.0F, 0.375F, 0.0F);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F - pEntityYaw));
      float f = (float)pEntity.getHurtTime() - pPartialTicks;
      float f1 = pEntity.getDamage() - pPartialTicks;
      if (f1 < 0.0F) {
         f1 = 0.0F;
      }

      if (f > 0.0F) {
         pPoseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(f) * f * f1 / 10.0F * (float)pEntity.getHurtDir()));
      }

      float f2 = pEntity.getBubbleAngle(pPartialTicks);
      if (!Mth.equal(f2, 0.0F)) {
         pPoseStack.mulPose((new Quaternionf()).setAngleAxis(pEntity.getBubbleAngle(pPartialTicks) * ((float)Math.PI / 180F), 1.0F, 0.0F, 1.0F));
      }

      Pair<ResourceLocation, ListModel<Boat>> pair = getModelWithLocation(pEntity);
      ResourceLocation resourcelocation = pair.getFirst();
      ListModel<Boat> listmodel = pair.getSecond();
      pPoseStack.scale(-1.0F, -1.0F, 1.0F);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
      listmodel.setupAnim(pEntity, pPartialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = pBuffer.getBuffer(listmodel.renderType(resourcelocation));
      listmodel.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      if (!pEntity.isUnderWater()) {
         VertexConsumer vertexconsumer1 = pBuffer.getBuffer(RenderType.waterMask());
         if (listmodel instanceof WaterPatchModel) {
            WaterPatchModel waterpatchmodel = (WaterPatchModel)listmodel;
            waterpatchmodel.waterPatch().render(pPoseStack, vertexconsumer1, pPackedLight, OverlayTexture.NO_OVERLAY);
         }
      }

      pPoseStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   @Deprecated // forge: override getModelWithLocation to change the texture / model
   public ResourceLocation getTextureLocation(Boat pEntity) {
      return getModelWithLocation(pEntity).getFirst();
   }

   public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) { return this.boatResources.get(boat.getVariant()); }
}
