package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ArmedModel {
   void translateToHand(HumanoidArm pSide, PoseStack pPoseStack);
}