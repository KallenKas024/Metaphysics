package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PufferfishRenderer extends MobRenderer<Pufferfish, EntityModel<Pufferfish>> {
   private static final ResourceLocation PUFFER_LOCATION = new ResourceLocation("textures/entity/fish/pufferfish.png");
   private int puffStateO = 3;
   private final EntityModel<Pufferfish> small;
   private final EntityModel<Pufferfish> mid;
   private final EntityModel<Pufferfish> big = this.getModel();

   public PufferfishRenderer(EntityRendererProvider.Context p_174358_) {
      super(p_174358_, new PufferfishBigModel<>(p_174358_.bakeLayer(ModelLayers.PUFFERFISH_BIG)), 0.2F);
      this.mid = new PufferfishMidModel<>(p_174358_.bakeLayer(ModelLayers.PUFFERFISH_MEDIUM));
      this.small = new PufferfishSmallModel<>(p_174358_.bakeLayer(ModelLayers.PUFFERFISH_SMALL));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Pufferfish pEntity) {
      return PUFFER_LOCATION;
   }

   public void render(Pufferfish pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      int i = pEntity.getPuffState();
      if (i != this.puffStateO) {
         if (i == 0) {
            this.model = this.small;
         } else if (i == 1) {
            this.model = this.mid;
         } else {
            this.model = this.big;
         }
      }

      this.puffStateO = i;
      this.shadowRadius = 0.1F + 0.1F * (float)i;
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   protected void setupRotations(Pufferfish pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      pPoseStack.translate(0.0F, Mth.cos(pAgeInTicks * 0.05F) * 0.08F, 0.0F);
      super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
   }
}