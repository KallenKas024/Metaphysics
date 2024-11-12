package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemCrackinessLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
   private static final Map<IronGolem.Crackiness, ResourceLocation> resourceLocations = ImmutableMap.of(IronGolem.Crackiness.LOW, new ResourceLocation("textures/entity/iron_golem/iron_golem_crackiness_low.png"), IronGolem.Crackiness.MEDIUM, new ResourceLocation("textures/entity/iron_golem/iron_golem_crackiness_medium.png"), IronGolem.Crackiness.HIGH, new ResourceLocation("textures/entity/iron_golem/iron_golem_crackiness_high.png"));

   public IronGolemCrackinessLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> pRenderer) {
      super(pRenderer);
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, IronGolem pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (!pLivingEntity.isInvisible()) {
         IronGolem.Crackiness irongolem$crackiness = pLivingEntity.getCrackiness();
         if (irongolem$crackiness != IronGolem.Crackiness.NONE) {
            ResourceLocation resourcelocation = resourceLocations.get(irongolem$crackiness);
            renderColoredCutoutModel(this.getParentModel(), resourcelocation, pPoseStack, pBuffer, pPackedLight, pLivingEntity, 1.0F, 1.0F, 1.0F);
         }
      }
   }
}