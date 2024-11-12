package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
   private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
   private final A innerModel;
   private final A outerModel;
   private final TextureAtlas armorTrimAtlas;

   public HumanoidArmorLayer(RenderLayerParent<T, M> pRenderer, A pInnerModel, A pOuterModel, ModelManager pModelManager) {
      super(pRenderer);
      this.innerModel = pInnerModel;
      this.outerModel = pOuterModel;
      this.armorTrimAtlas = pModelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.CHEST, pPackedLight, this.getArmorModel(EquipmentSlot.CHEST));
      this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.LEGS, pPackedLight, this.getArmorModel(EquipmentSlot.LEGS));
      this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.FEET, pPackedLight, this.getArmorModel(EquipmentSlot.FEET));
      this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.HEAD, pPackedLight, this.getArmorModel(EquipmentSlot.HEAD));
   }

   private void renderArmorPiece(PoseStack pPoseStack, MultiBufferSource pBuffer, T pLivingEntity, EquipmentSlot pSlot, int pPackedLight, A pModel) {
      ItemStack itemstack = pLivingEntity.getItemBySlot(pSlot);
      Item $$9 = itemstack.getItem();
      if ($$9 instanceof ArmorItem armoritem) {
         if (armoritem.getEquipmentSlot() == pSlot) {
            this.getParentModel().copyPropertiesTo(pModel);
            this.setPartVisibility(pModel, pSlot);
            net.minecraft.client.model.Model model = getArmorModelHook(pLivingEntity, itemstack, pSlot, pModel);
            boolean flag = this.usesInnerModel(pSlot);
            if (armoritem instanceof net.minecraft.world.item.DyeableLeatherItem) {
               int i = ((net.minecraft.world.item.DyeableLeatherItem)armoritem).getColor(itemstack);
               float f = (float)(i >> 16 & 255) / 255.0F;
               float f1 = (float)(i >> 8 & 255) / 255.0F;
               float f2 = (float)(i & 255) / 255.0F;
               this.renderModel(pPoseStack, pBuffer, pPackedLight, armoritem, model, flag, f, f1, f2, this.getArmorResource(pLivingEntity, itemstack, pSlot, null));
               this.renderModel(pPoseStack, pBuffer, pPackedLight, armoritem, model, flag, 1.0F, 1.0F, 1.0F, this.getArmorResource(pLivingEntity, itemstack, pSlot, "overlay"));
            } else {
               this.renderModel(pPoseStack, pBuffer, pPackedLight, armoritem, model, flag, 1.0F, 1.0F, 1.0F, this.getArmorResource(pLivingEntity, itemstack, pSlot, null));
            }

            ArmorTrim.getTrim(pLivingEntity.level().registryAccess(), itemstack).ifPresent((p_289638_) -> {
               this.renderTrim(armoritem.getMaterial(), pPoseStack, pBuffer, pPackedLight, p_289638_, model, flag);
            });
            if (itemstack.hasFoil()) {
               this.renderGlint(pPoseStack, pBuffer, pPackedLight, model);
            }

         }
      }
   }

   protected void setPartVisibility(A pModel, EquipmentSlot pSlot) {
      pModel.setAllVisible(false);
      switch (pSlot) {
         case HEAD:
            pModel.head.visible = true;
            pModel.hat.visible = true;
            break;
         case CHEST:
            pModel.body.visible = true;
            pModel.rightArm.visible = true;
            pModel.leftArm.visible = true;
            break;
         case LEGS:
            pModel.body.visible = true;
            pModel.rightLeg.visible = true;
            pModel.leftLeg.visible = true;
            break;
         case FEET:
            pModel.rightLeg.visible = true;
            pModel.leftLeg.visible = true;
      }

   }

   private void renderModel(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, ArmorItem pArmorItem, A pModel, boolean pWithGlint, float pRed, float pGreen, float pBlue, @Nullable String pArmorSuffix) {
       renderModel(pPoseStack, pBuffer, pPackedLight, pArmorItem, pModel, pWithGlint, pRed, pGreen, pBlue, this.getArmorLocation(pArmorItem, pWithGlint, pArmorSuffix));
   }
   private void renderModel(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, ArmorItem pArmorItem, net.minecraft.client.model.Model pModel, boolean pWithGlint, float pRed, float pGreen, float pBlue, ResourceLocation armorResource) {
      VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.armorCutoutNoCull(armorResource));
      pModel.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, pRed, pGreen, pBlue, 1.0F);
   }

   private void renderTrim(ArmorMaterial pArmorMaterial, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, ArmorTrim pTrim, A pModel, boolean pInnerTexture) {
      renderTrim(pArmorMaterial, pPoseStack, pBuffer, pPackedLight, pTrim, pModel, pInnerTexture);
   }
   private void renderTrim(ArmorMaterial pArmorMaterial, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, ArmorTrim pTrim, net.minecraft.client.model.Model pModel, boolean pInnerTexture) {
      TextureAtlasSprite textureatlassprite = this.armorTrimAtlas.getSprite(pInnerTexture ? pTrim.innerTexture(pArmorMaterial) : pTrim.outerTexture(pArmorMaterial));
      VertexConsumer vertexconsumer = textureatlassprite.wrap(pBuffer.getBuffer(Sheets.armorTrimsSheet()));
      pModel.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderGlint(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, A pModel) {
      renderGlint(pPoseStack, pBuffer, pPackedLight, pModel);
   }
   private void renderGlint(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, net.minecraft.client.model.Model pModel) {
      pModel.renderToBuffer(pPoseStack, pBuffer.getBuffer(RenderType.armorEntityGlint()), pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   private A getArmorModel(EquipmentSlot pSlot) {
      return (A)(this.usesInnerModel(pSlot) ? this.innerModel : this.outerModel);
   }

   private boolean usesInnerModel(EquipmentSlot pSlot) {
      return pSlot == EquipmentSlot.LEGS;
   }

   @Deprecated //Use the more sensitive version getArmorResource below
   private ResourceLocation getArmorLocation(ArmorItem pArmorItem, boolean pLayer2, @Nullable String pSuffix) {
      String s = "textures/models/armor/" + pArmorItem.getMaterial().getName() + "_layer_" + (pLayer2 ? 2 : 1) + (pSuffix == null ? "" : "_" + pSuffix) + ".png";
      return ARMOR_LOCATION_CACHE.computeIfAbsent(s, ResourceLocation::new);
   }

   /*=================================== FORGE START =========================================*/

   /**
    * Hook to allow item-sensitive armor model. for HumanoidArmorLayer.
    */
   protected net.minecraft.client.model.Model getArmorModelHook(T entity, ItemStack itemStack, EquipmentSlot slot, A model) {
      return net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
   }

   /**
    * More generic ForgeHook version of the above function, it allows for Items to have more control over what texture they provide.
    *
    * @param entity Entity wearing the armor
    * @param stack ItemStack for the armor
    * @param slot Slot ID that the item is in
    * @param type Subtype, can be null or "overlay"
    * @return ResourceLocation pointing at the armor's texture
    */
   public ResourceLocation getArmorResource(net.minecraft.world.entity.Entity entity, ItemStack stack, EquipmentSlot slot, @Nullable String type) {
      ArmorItem item = (ArmorItem)stack.getItem();
      String texture = item.getMaterial().getName();
      String domain = "minecraft";
      int idx = texture.indexOf(':');
      if (idx != -1) {
         domain = texture.substring(0, idx);
         texture = texture.substring(idx + 1);
      }
      String s1 = String.format(java.util.Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, (usesInnerModel(slot) ? 2 : 1), type == null ? "" : String.format(java.util.Locale.ROOT, "_%s", type));

      s1 = net.minecraftforge.client.ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
      ResourceLocation resourcelocation = ARMOR_LOCATION_CACHE.get(s1);

      if (resourcelocation == null) {
         resourcelocation = new ResourceLocation(s1);
         ARMOR_LOCATION_CACHE.put(s1, resourcelocation);
      }

      return resourcelocation;
   }
   /*=================================== FORGE END ===========================================*/
}
