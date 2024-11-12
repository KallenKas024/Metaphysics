package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class StuckInBodyLayer<T extends LivingEntity, M extends PlayerModel<T>> extends RenderLayer<T, M> {
   public StuckInBodyLayer(LivingEntityRenderer<T, M> pRenderer) {
      super(pRenderer);
   }

   protected abstract int numStuck(T pEntity);

   protected abstract void renderStuckItem(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Entity pEntity, float pX, float pY, float pZ, float pPartialTick);

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      int i = this.numStuck(pLivingEntity);
      RandomSource randomsource = RandomSource.create((long)pLivingEntity.getId());
      if (i > 0) {
         for(int j = 0; j < i; ++j) {
            pPoseStack.pushPose();
            ModelPart modelpart = this.getParentModel().getRandomModelPart(randomsource);
            ModelPart.Cube modelpart$cube = modelpart.getRandomCube(randomsource);
            modelpart.translateAndRotate(pPoseStack);
            float f = randomsource.nextFloat();
            float f1 = randomsource.nextFloat();
            float f2 = randomsource.nextFloat();
            float f3 = Mth.lerp(f, modelpart$cube.minX, modelpart$cube.maxX) / 16.0F;
            float f4 = Mth.lerp(f1, modelpart$cube.minY, modelpart$cube.maxY) / 16.0F;
            float f5 = Mth.lerp(f2, modelpart$cube.minZ, modelpart$cube.maxZ) / 16.0F;
            pPoseStack.translate(f3, f4, f5);
            f = -1.0F * (f * 2.0F - 1.0F);
            f1 = -1.0F * (f1 * 2.0F - 1.0F);
            f2 = -1.0F * (f2 * 2.0F - 1.0F);
            this.renderStuckItem(pPoseStack, pBuffer, pPackedLight, pLivingEntity, f, f1, f2, pPartialTicks);
            pPoseStack.popPose();
         }

      }
   }
}