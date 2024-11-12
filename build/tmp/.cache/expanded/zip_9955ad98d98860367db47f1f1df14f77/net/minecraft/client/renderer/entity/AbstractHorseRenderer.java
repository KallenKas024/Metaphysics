package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HorseModel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractHorseRenderer<T extends AbstractHorse, M extends HorseModel<T>> extends MobRenderer<T, M> {
   private final float scale;

   public AbstractHorseRenderer(EntityRendererProvider.Context pContext, M pModel, float pScale) {
      super(pContext, pModel, 0.75F);
      this.scale = pScale;
   }

   protected void scale(T pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
      pPoseStack.scale(this.scale, this.scale, this.scale);
      super.scale(pLivingEntity, pPoseStack, pPartialTickTime);
   }
}