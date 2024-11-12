package net.minecraft.client.model;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EntityModel<T extends Entity> extends Model {
   public float attackTime;
   public boolean riding;
   public boolean young = true;

   protected EntityModel() {
      this(RenderType::entityCutoutNoCull);
   }

   protected EntityModel(Function<ResourceLocation, RenderType> pRenderType) {
      super(pRenderType);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public abstract void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch);

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
   }

   public void copyPropertiesTo(EntityModel<T> pOtherModel) {
      pOtherModel.attackTime = this.attackTime;
      pOtherModel.riding = this.riding;
      pOtherModel.young = this.young;
   }
}