package com.mojang.math;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

public record GivensParameters(float sinHalf, float cosHalf) {
   public static GivensParameters fromUnnormalized(float pSinHalf, float pCosHalf) {
      float f = Math.invsqrt(pSinHalf * pSinHalf + pCosHalf * pCosHalf);
      return new GivensParameters(f * pSinHalf, f * pCosHalf);
   }

   public static GivensParameters fromPositiveAngle(float pAngle) {
      float f = Math.sin(pAngle / 2.0F);
      float f1 = Math.cosFromSin(f, pAngle / 2.0F);
      return new GivensParameters(f, f1);
   }

   public GivensParameters inverse() {
      return new GivensParameters(-this.sinHalf, this.cosHalf);
   }

   public Quaternionf aroundX(Quaternionf pQuaternion) {
      return pQuaternion.set(this.sinHalf, 0.0F, 0.0F, this.cosHalf);
   }

   public Quaternionf aroundY(Quaternionf pQuaternion) {
      return pQuaternion.set(0.0F, this.sinHalf, 0.0F, this.cosHalf);
   }

   public Quaternionf aroundZ(Quaternionf pQuaternion) {
      return pQuaternion.set(0.0F, 0.0F, this.sinHalf, this.cosHalf);
   }

   public float cos() {
      return this.cosHalf * this.cosHalf - this.sinHalf * this.sinHalf;
   }

   public float sin() {
      return 2.0F * this.sinHalf * this.cosHalf;
   }

   public Matrix3f aroundX(Matrix3f pMatrix) {
      pMatrix.m01 = 0.0F;
      pMatrix.m02 = 0.0F;
      pMatrix.m10 = 0.0F;
      pMatrix.m20 = 0.0F;
      float f = this.cos();
      float f1 = this.sin();
      pMatrix.m11 = f;
      pMatrix.m22 = f;
      pMatrix.m12 = f1;
      pMatrix.m21 = -f1;
      pMatrix.m00 = 1.0F;
      return pMatrix;
   }

   public Matrix3f aroundY(Matrix3f pMatrix) {
      pMatrix.m01 = 0.0F;
      pMatrix.m10 = 0.0F;
      pMatrix.m12 = 0.0F;
      pMatrix.m21 = 0.0F;
      float f = this.cos();
      float f1 = this.sin();
      pMatrix.m00 = f;
      pMatrix.m22 = f;
      pMatrix.m02 = -f1;
      pMatrix.m20 = f1;
      pMatrix.m11 = 1.0F;
      return pMatrix;
   }

   public Matrix3f aroundZ(Matrix3f pMatrix) {
      pMatrix.m02 = 0.0F;
      pMatrix.m12 = 0.0F;
      pMatrix.m20 = 0.0F;
      pMatrix.m21 = 0.0F;
      float f = this.cos();
      float f1 = this.sin();
      pMatrix.m00 = f;
      pMatrix.m11 = f;
      pMatrix.m01 = f1;
      pMatrix.m10 = -f1;
      pMatrix.m22 = 1.0F;
      return pMatrix;
   }
}