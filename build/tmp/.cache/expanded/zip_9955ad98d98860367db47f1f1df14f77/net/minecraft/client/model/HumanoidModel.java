package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidModel<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
   public static final float OVERLAY_SCALE = 0.25F;
   public static final float HAT_OVERLAY_SCALE = 0.5F;
   public static final float LEGGINGS_OVERLAY_SCALE = -0.1F;
   private static final float DUCK_WALK_ROTATION = 0.005F;
   private static final float SPYGLASS_ARM_ROT_Y = 0.2617994F;
   private static final float SPYGLASS_ARM_ROT_X = 1.9198622F;
   private static final float SPYGLASS_ARM_CROUCH_ROT_X = 0.2617994F;
   public static final float TOOT_HORN_XROT_BASE = 1.4835298F;
   public static final float TOOT_HORN_YROT_BASE = ((float)Math.PI / 6F);
   public final ModelPart head;
   /** The Biped's Headwear. Used for the outer layer of player skins. */
   public final ModelPart hat;
   public final ModelPart body;
   /** The Biped's Right Arm */
   public final ModelPart rightArm;
   /** The Biped's Left Arm */
   public final ModelPart leftArm;
   /** The Biped's Right Leg */
   public final ModelPart rightLeg;
   /** The Biped's Left Leg */
   public final ModelPart leftLeg;
   public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
   public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
   public boolean crouching;
   public float swimAmount;

   public HumanoidModel(ModelPart pRoot) {
      this(pRoot, RenderType::entityCutoutNoCull);
   }

   public HumanoidModel(ModelPart pRoot, Function<ResourceLocation, RenderType> pRenderType) {
      super(pRenderType, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
      this.head = pRoot.getChild("head");
      this.hat = pRoot.getChild("hat");
      this.body = pRoot.getChild("body");
      this.rightArm = pRoot.getChild("right_arm");
      this.leftArm = pRoot.getChild("left_arm");
      this.rightLeg = pRoot.getChild("right_leg");
      this.leftLeg = pRoot.getChild("left_leg");
   }

   public static MeshDefinition createMesh(CubeDeformation pCubeDeformation, float pYOffset) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, pCubeDeformation), PartPose.offset(0.0F, 0.0F + pYOffset, 0.0F));
      partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, pCubeDeformation.extend(0.5F)), PartPose.offset(0.0F, 0.0F + pYOffset, 0.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(0.0F, 0.0F + pYOffset, 0.0F));
      partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(-5.0F, 2.0F + pYOffset, 0.0F));
      partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(5.0F, 2.0F + pYOffset, 0.0F));
      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(-1.9F, 12.0F + pYOffset, 0.0F));
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(1.9F, 12.0F + pYOffset, 0.0F));
      return meshdefinition;
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      this.swimAmount = pEntity.getSwimAmount(pPartialTick);
      super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      boolean flag = pEntity.getFallFlyingTicks() > 4;
      boolean flag1 = pEntity.isVisuallySwimming();
      this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      if (flag) {
         this.head.xRot = (-(float)Math.PI / 4F);
      } else if (this.swimAmount > 0.0F) {
         if (flag1) {
            this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (-(float)Math.PI / 4F));
         } else {
            this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, pHeadPitch * ((float)Math.PI / 180F));
         }
      } else {
         this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
      }

      this.body.yRot = 0.0F;
      this.rightArm.z = 0.0F;
      this.rightArm.x = -5.0F;
      this.leftArm.z = 0.0F;
      this.leftArm.x = 5.0F;
      float f = 1.0F;
      if (flag) {
         f = (float)pEntity.getDeltaMovement().lengthSqr();
         f /= 0.2F;
         f *= f * f;
      }

      if (f < 1.0F) {
         f = 1.0F;
      }

      this.rightArm.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 2.0F * pLimbSwingAmount * 0.5F / f;
      this.leftArm.xRot = Mth.cos(pLimbSwing * 0.6662F) * 2.0F * pLimbSwingAmount * 0.5F / f;
      this.rightArm.zRot = 0.0F;
      this.leftArm.zRot = 0.0F;
      this.rightLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount / f;
      this.leftLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 1.4F * pLimbSwingAmount / f;
      this.rightLeg.yRot = 0.005F;
      this.leftLeg.yRot = -0.005F;
      this.rightLeg.zRot = 0.005F;
      this.leftLeg.zRot = -0.005F;
      if (this.riding) {
         this.rightArm.xRot += (-(float)Math.PI / 5F);
         this.leftArm.xRot += (-(float)Math.PI / 5F);
         this.rightLeg.xRot = -1.4137167F;
         this.rightLeg.yRot = ((float)Math.PI / 10F);
         this.rightLeg.zRot = 0.07853982F;
         this.leftLeg.xRot = -1.4137167F;
         this.leftLeg.yRot = (-(float)Math.PI / 10F);
         this.leftLeg.zRot = -0.07853982F;
      }

      this.rightArm.yRot = 0.0F;
      this.leftArm.yRot = 0.0F;
      boolean flag2 = pEntity.getMainArm() == HumanoidArm.RIGHT;
      if (pEntity.isUsingItem()) {
         boolean flag3 = pEntity.getUsedItemHand() == InteractionHand.MAIN_HAND;
         if (flag3 == flag2) {
            this.poseRightArm(pEntity);
         } else {
            this.poseLeftArm(pEntity);
         }
      } else {
         boolean flag4 = flag2 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
         if (flag2 != flag4) {
            this.poseLeftArm(pEntity);
            this.poseRightArm(pEntity);
         } else {
            this.poseRightArm(pEntity);
            this.poseLeftArm(pEntity);
         }
      }

      this.setupAttackAnimation(pEntity, pAgeInTicks);
      if (this.crouching) {
         this.body.xRot = 0.5F;
         this.rightArm.xRot += 0.4F;
         this.leftArm.xRot += 0.4F;
         this.rightLeg.z = 4.0F;
         this.leftLeg.z = 4.0F;
         this.rightLeg.y = 12.2F;
         this.leftLeg.y = 12.2F;
         this.head.y = 4.2F;
         this.body.y = 3.2F;
         this.leftArm.y = 5.2F;
         this.rightArm.y = 5.2F;
      } else {
         this.body.xRot = 0.0F;
         this.rightLeg.z = 0.0F;
         this.leftLeg.z = 0.0F;
         this.rightLeg.y = 12.0F;
         this.leftLeg.y = 12.0F;
         this.head.y = 0.0F;
         this.body.y = 0.0F;
         this.leftArm.y = 2.0F;
         this.rightArm.y = 2.0F;
      }

      if (this.rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {
         AnimationUtils.bobModelPart(this.rightArm, pAgeInTicks, 1.0F);
      }

      if (this.leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {
         AnimationUtils.bobModelPart(this.leftArm, pAgeInTicks, -1.0F);
      }

      if (this.swimAmount > 0.0F) {
         float f5 = pLimbSwing % 26.0F;
         HumanoidArm humanoidarm = this.getAttackArm(pEntity);
         float f1 = humanoidarm == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
         float f2 = humanoidarm == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
         if (!pEntity.isUsingItem()) {
            if (f5 < 14.0F) {
               this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, 0.0F);
               this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, 0.0F);
               this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float)Math.PI);
               this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float)Math.PI);
               this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, (float)Math.PI + 1.8707964F * this.quadraticArmUpdate(f5) / this.quadraticArmUpdate(14.0F));
               this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, (float)Math.PI - 1.8707964F * this.quadraticArmUpdate(f5) / this.quadraticArmUpdate(14.0F));
            } else if (f5 >= 14.0F && f5 < 22.0F) {
               float f6 = (f5 - 14.0F) / 8.0F;
               this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, ((float)Math.PI / 2F) * f6);
               this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, ((float)Math.PI / 2F) * f6);
               this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float)Math.PI);
               this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float)Math.PI);
               this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, 5.012389F - 1.8707964F * f6);
               this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, 1.2707963F + 1.8707964F * f6);
            } else if (f5 >= 22.0F && f5 < 26.0F) {
               float f3 = (f5 - 22.0F) / 4.0F;
               this.leftArm.xRot = this.rotlerpRad(f2, this.leftArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f3);
               this.rightArm.xRot = Mth.lerp(f1, this.rightArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f3);
               this.leftArm.yRot = this.rotlerpRad(f2, this.leftArm.yRot, (float)Math.PI);
               this.rightArm.yRot = Mth.lerp(f1, this.rightArm.yRot, (float)Math.PI);
               this.leftArm.zRot = this.rotlerpRad(f2, this.leftArm.zRot, (float)Math.PI);
               this.rightArm.zRot = Mth.lerp(f1, this.rightArm.zRot, (float)Math.PI);
            }
         }

         float f7 = 0.3F;
         float f4 = 0.33333334F;
         this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(pLimbSwing * 0.33333334F + (float)Math.PI));
         this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(pLimbSwing * 0.33333334F));
      }

      this.hat.copyFrom(this.head);
   }

   private void poseRightArm(T pLivingEntity) {
      switch (this.rightArmPose) {
         case EMPTY:
            this.rightArm.yRot = 0.0F;
            break;
         case BLOCK:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F;
            this.rightArm.yRot = (-(float)Math.PI / 6F);
            break;
         case ITEM:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float)Math.PI / 10F);
            this.rightArm.yRot = 0.0F;
            break;
         case THROW_SPEAR:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float)Math.PI;
            this.rightArm.yRot = 0.0F;
            break;
         case BOW_AND_ARROW:
            this.rightArm.yRot = -0.1F + this.head.yRot;
            this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
            this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            break;
         case CROSSBOW_CHARGE:
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, pLivingEntity, true);
            break;
         case CROSSBOW_HOLD:
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
            break;
         case BRUSH:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float)Math.PI / 5F);
            this.rightArm.yRot = 0.0F;
            break;
         case SPYGLASS:
            this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (pLivingEntity.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            this.rightArm.yRot = this.head.yRot - 0.2617994F;
            break;
         case TOOT_HORN:
            this.rightArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
            this.rightArm.yRot = this.head.yRot - ((float)Math.PI / 6F);
         default:
            this.rightArmPose.applyTransform(this, pLivingEntity, net.minecraft.world.entity.HumanoidArm.RIGHT);
      }

   }

   private void poseLeftArm(T pLivingEntity) {
      switch (this.leftArmPose) {
         case EMPTY:
            this.leftArm.yRot = 0.0F;
            break;
         case BLOCK:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.9424779F;
            this.leftArm.yRot = ((float)Math.PI / 6F);
            break;
         case ITEM:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float)Math.PI / 10F);
            this.leftArm.yRot = 0.0F;
            break;
         case THROW_SPEAR:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float)Math.PI;
            this.leftArm.yRot = 0.0F;
            break;
         case BOW_AND_ARROW:
            this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
            this.leftArm.yRot = 0.1F + this.head.yRot;
            this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            break;
         case CROSSBOW_CHARGE:
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, pLivingEntity, false);
            break;
         case CROSSBOW_HOLD:
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
            break;
         case BRUSH:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float)Math.PI / 5F);
            this.leftArm.yRot = 0.0F;
            break;
         case SPYGLASS:
            this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (pLivingEntity.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            this.leftArm.yRot = this.head.yRot + 0.2617994F;
            break;
         case TOOT_HORN:
            this.leftArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
            this.leftArm.yRot = this.head.yRot + ((float)Math.PI / 6F);
         default:
            this.leftArmPose.applyTransform(this, pLivingEntity, net.minecraft.world.entity.HumanoidArm.LEFT);
      }

   }

   protected void setupAttackAnimation(T pLivingEntity, float pAgeInTicks) {
      if (!(this.attackTime <= 0.0F)) {
         HumanoidArm humanoidarm = this.getAttackArm(pLivingEntity);
         ModelPart modelpart = this.getArm(humanoidarm);
         float f = this.attackTime;
         this.body.yRot = Mth.sin(Mth.sqrt(f) * ((float)Math.PI * 2F)) * 0.2F;
         if (humanoidarm == HumanoidArm.LEFT) {
            this.body.yRot *= -1.0F;
         }

         this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
         this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
         this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
         this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
         this.rightArm.yRot += this.body.yRot;
         this.leftArm.yRot += this.body.yRot;
         this.leftArm.xRot += this.body.yRot;
         f = 1.0F - this.attackTime;
         f *= f;
         f *= f;
         f = 1.0F - f;
         float f1 = Mth.sin(f * (float)Math.PI);
         float f2 = Mth.sin(this.attackTime * (float)Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
         modelpart.xRot -= f1 * 1.2F + f2;
         modelpart.yRot += this.body.yRot * 2.0F;
         modelpart.zRot += Mth.sin(this.attackTime * (float)Math.PI) * -0.4F;
      }
   }

   protected float rotlerpRad(float pAngle, float pMaxAngle, float pMul) {
      float f = (pMul - pMaxAngle) % ((float)Math.PI * 2F);
      if (f < -(float)Math.PI) {
         f += ((float)Math.PI * 2F);
      }

      if (f >= (float)Math.PI) {
         f -= ((float)Math.PI * 2F);
      }

      return pMaxAngle + pAngle * f;
   }

   private float quadraticArmUpdate(float pLimbSwing) {
      return -65.0F * pLimbSwing + pLimbSwing * pLimbSwing;
   }

   public void copyPropertiesTo(HumanoidModel<T> pModel) {
      super.copyPropertiesTo(pModel);
      pModel.leftArmPose = this.leftArmPose;
      pModel.rightArmPose = this.rightArmPose;
      pModel.crouching = this.crouching;
      pModel.head.copyFrom(this.head);
      pModel.hat.copyFrom(this.hat);
      pModel.body.copyFrom(this.body);
      pModel.rightArm.copyFrom(this.rightArm);
      pModel.leftArm.copyFrom(this.leftArm);
      pModel.rightLeg.copyFrom(this.rightLeg);
      pModel.leftLeg.copyFrom(this.leftLeg);
   }

   public void setAllVisible(boolean pVisible) {
      this.head.visible = pVisible;
      this.hat.visible = pVisible;
      this.body.visible = pVisible;
      this.rightArm.visible = pVisible;
      this.leftArm.visible = pVisible;
      this.rightLeg.visible = pVisible;
      this.leftLeg.visible = pVisible;
   }

   public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
      this.getArm(pSide).translateAndRotate(pPoseStack);
   }

   protected ModelPart getArm(HumanoidArm pSide) {
      return pSide == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
   }

   public ModelPart getHead() {
      return this.head;
   }

   private HumanoidArm getAttackArm(T pEntity) {
      HumanoidArm humanoidarm = pEntity.getMainArm();
      return pEntity.swingingArm == InteractionHand.MAIN_HAND ? humanoidarm : humanoidarm.getOpposite();
   }

   @OnlyIn(Dist.CLIENT)
   public static enum ArmPose implements net.minecraftforge.common.IExtensibleEnum {
      EMPTY(false),
      ITEM(false),
      BLOCK(false),
      BOW_AND_ARROW(true),
      THROW_SPEAR(false),
      CROSSBOW_CHARGE(true),
      CROSSBOW_HOLD(true),
      SPYGLASS(false),
      TOOT_HORN(false),
      BRUSH(false);

      private final boolean twoHanded;

      private ArmPose(boolean pTwoHanded) {
         this.twoHanded = pTwoHanded;
         this.forgeArmPose = null;
      }

      public boolean isTwoHanded() {
         return this.twoHanded;
      }
      // FORGE START
      @javax.annotation.Nullable
      private final net.minecraftforge.client.IArmPoseTransformer forgeArmPose;

      private ArmPose(boolean twoHanded, @javax.annotation.Nonnull net.minecraftforge.client.IArmPoseTransformer forgeArmPose) {
         this.twoHanded = twoHanded;
         com.google.common.base.Preconditions.checkNotNull(forgeArmPose, "Cannot create new ArmPose with null transformer!");
         this.forgeArmPose = forgeArmPose;
      }

      public static ArmPose create(String name, boolean twoHanded, @javax.annotation.Nonnull net.minecraftforge.client.IArmPoseTransformer forgeArmPose) {
         throw new IllegalStateException("Enum not extended");
      }

      public <T extends LivingEntity> void applyTransform(HumanoidModel<T> model, T entity, net.minecraft.world.entity.HumanoidArm arm) {
         if (this.forgeArmPose != null) this.forgeArmPose.applyTransform(model, entity, arm);
      }
      // FORGE END
   }
}
