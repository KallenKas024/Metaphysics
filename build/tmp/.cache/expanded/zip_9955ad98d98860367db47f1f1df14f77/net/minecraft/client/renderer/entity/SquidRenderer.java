package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SquidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidRenderer<T extends Squid> extends MobRenderer<T, SquidModel<T>> {
   private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid/squid.png");

   public SquidRenderer(EntityRendererProvider.Context pContext, SquidModel<T> pModel) {
      super(pContext, pModel, 0.7F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(T pEntity) {
      return SQUID_LOCATION;
   }

   protected void setupRotations(T pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      float f = Mth.lerp(pPartialTicks, pEntityLiving.xBodyRotO, pEntityLiving.xBodyRot);
      float f1 = Mth.lerp(pPartialTicks, pEntityLiving.zBodyRotO, pEntityLiving.zBodyRot);
      pPoseStack.translate(0.0F, 0.5F, 0.0F);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F - pRotationYaw));
      pPoseStack.mulPose(Axis.XP.rotationDegrees(f));
      pPoseStack.mulPose(Axis.YP.rotationDegrees(f1));
      pPoseStack.translate(0.0F, -1.2F, 0.0F);
   }

   /**
    * Defines what float the third param in setRotationAngles of ModelBase is
    */
   protected float getBob(T pLivingBase, float pPartialTicks) {
      return Mth.lerp(pPartialTicks, pLivingBase.oldTentacleAngle, pLivingBase.tentacleAngle);
   }
}