package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import java.util.Deque;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class PoseStack implements net.minecraftforge.client.extensions.IForgePoseStack {
   private final Deque<PoseStack.Pose> poseStack = Util.make(Queues.newArrayDeque(), (p_85848_) -> {
      Matrix4f matrix4f = new Matrix4f();
      Matrix3f matrix3f = new Matrix3f();
      p_85848_.add(new PoseStack.Pose(matrix4f, matrix3f));
   });

   public void translate(double pX, double pY, double pZ) {
      this.translate((float)pX, (float)pY, (float)pZ);
   }

   public void translate(float pX, float pY, float pZ) {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      posestack$pose.pose.translate(pX, pY, pZ);
   }

   public void scale(float pX, float pY, float pZ) {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      posestack$pose.pose.scale(pX, pY, pZ);
      if (pX == pY && pY == pZ) {
         if (pX > 0.0F) {
            return;
         }

         posestack$pose.normal.scale(-1.0F);
      }

      float f = 1.0F / pX;
      float f1 = 1.0F / pY;
      float f2 = 1.0F / pZ;
      float f3 = Mth.fastInvCubeRoot(f * f1 * f2);
      posestack$pose.normal.scale(f3 * f, f3 * f1, f3 * f2);
   }

   public void mulPose(Quaternionf pQuaternion) {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      posestack$pose.pose.rotate(pQuaternion);
      posestack$pose.normal.rotate(pQuaternion);
   }

   public void rotateAround(Quaternionf pQuaternion, float pX, float pY, float pZ) {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      posestack$pose.pose.rotateAround(pQuaternion, pX, pY, pZ);
      posestack$pose.normal.rotate(pQuaternion);
   }

   public void pushPose() {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      this.poseStack.addLast(new PoseStack.Pose(new Matrix4f(posestack$pose.pose), new Matrix3f(posestack$pose.normal)));
   }

   public void popPose() {
      this.poseStack.removeLast();
   }

   public PoseStack.Pose last() {
      return this.poseStack.getLast();
   }

   public boolean clear() {
      return this.poseStack.size() == 1;
   }

   public void setIdentity() {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      posestack$pose.pose.identity();
      posestack$pose.normal.identity();
   }

   public void mulPoseMatrix(Matrix4f pMatrix) {
      (this.poseStack.getLast()).pose.mul(pMatrix);
   }

   @OnlyIn(Dist.CLIENT)
   public static final class Pose {
      final Matrix4f pose;
      final Matrix3f normal;

      Pose(Matrix4f pPose, Matrix3f pNormal) {
         this.pose = pPose;
         this.normal = pNormal;
      }

      public Matrix4f pose() {
         return this.pose;
      }

      public Matrix3f normal() {
         return this.normal;
      }
   }
}
