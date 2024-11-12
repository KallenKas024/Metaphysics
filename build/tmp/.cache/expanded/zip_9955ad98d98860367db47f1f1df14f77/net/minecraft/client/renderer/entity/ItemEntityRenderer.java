package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemEntityRenderer extends EntityRenderer<ItemEntity> {
   private static final float ITEM_BUNDLE_OFFSET_SCALE = 0.15F;
   private static final int ITEM_COUNT_FOR_5_BUNDLE = 48;
   private static final int ITEM_COUNT_FOR_4_BUNDLE = 32;
   private static final int ITEM_COUNT_FOR_3_BUNDLE = 16;
   private static final int ITEM_COUNT_FOR_2_BUNDLE = 1;
   private static final float FLAT_ITEM_BUNDLE_OFFSET_X = 0.0F;
   private static final float FLAT_ITEM_BUNDLE_OFFSET_Y = 0.0F;
   private static final float FLAT_ITEM_BUNDLE_OFFSET_Z = 0.09375F;
   private final ItemRenderer itemRenderer;
   private final RandomSource random = RandomSource.create();

   public ItemEntityRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.itemRenderer = pContext.getItemRenderer();
      this.shadowRadius = 0.15F;
      this.shadowStrength = 0.75F;
   }

   protected int getRenderAmount(ItemStack pStack) {
      int i = 1;
      if (pStack.getCount() > 48) {
         i = 5;
      } else if (pStack.getCount() > 32) {
         i = 4;
      } else if (pStack.getCount() > 16) {
         i = 3;
      } else if (pStack.getCount() > 1) {
         i = 2;
      }

      return i;
   }

   public void render(ItemEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      pPoseStack.pushPose();
      ItemStack itemstack = pEntity.getItem();
      int i = itemstack.isEmpty() ? 187 : Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
      this.random.setSeed((long)i);
      BakedModel bakedmodel = this.itemRenderer.getModel(itemstack, pEntity.level(), (LivingEntity)null, pEntity.getId());
      boolean flag = bakedmodel.isGui3d();
      int j = this.getRenderAmount(itemstack);
      float f = 0.25F;
      float f1 = shouldBob() ? Mth.sin(((float)pEntity.getAge() + pPartialTicks) / 10.0F + pEntity.bobOffs) * 0.1F + 0.1F : 0;
      float f2 = bakedmodel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
      pPoseStack.translate(0.0F, f1 + 0.25F * f2, 0.0F);
      float f3 = pEntity.getSpin(pPartialTicks);
      pPoseStack.mulPose(Axis.YP.rotation(f3));
      if (!flag) {
         float f7 = -0.0F * (float)(j - 1) * 0.5F;
         float f8 = -0.0F * (float)(j - 1) * 0.5F;
         float f9 = -0.09375F * (float)(j - 1) * 0.5F;
         pPoseStack.translate(f7, f8, f9);
      }

      for(int k = 0; k < j; ++k) {
         pPoseStack.pushPose();
         if (k > 0) {
            if (flag) {
               float f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               pPoseStack.translate(shouldSpreadItems() ? f11 : 0, shouldSpreadItems() ? f13 : 0, shouldSpreadItems() ? f10 : 0);
            } else {
               float f12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               float f14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               pPoseStack.translate(shouldSpreadItems() ? f12 : 0, shouldSpreadItems() ? f14 : 0, 0.0D);
            }
         }

         this.itemRenderer.render(itemstack, ItemDisplayContext.GROUND, false, pPoseStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY, bakedmodel);
         pPoseStack.popPose();
         if (!flag) {
            pPoseStack.translate(0.0, 0.0, 0.09375F);
         }
      }

      pPoseStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(ItemEntity pEntity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }

   /*==================================== FORGE START ===========================================*/

   /**
    * @return If items should spread out when rendered in 3D
    */
   public boolean shouldSpreadItems() {
      return true;
   }

   /**
    * @return If items should have a bob effect
    */
   public boolean shouldBob() {
      return true;
   }
   /*==================================== FORGE END =============================================*/
}
