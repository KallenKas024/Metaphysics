package com.mojang.math;

import org.apache.commons.lang3.tuple.Triple;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MatrixUtil {
   private static final float G = 3.0F + 2.0F * Math.sqrt(2.0F);
   private static final GivensParameters PI_4 = GivensParameters.fromPositiveAngle(((float)java.lang.Math.PI / 4F));

   private MatrixUtil() {
   }

   public static Matrix4f mulComponentWise(Matrix4f pMatrix, float pScalar) {
      return pMatrix.set(pMatrix.m00() * pScalar, pMatrix.m01() * pScalar, pMatrix.m02() * pScalar, pMatrix.m03() * pScalar, pMatrix.m10() * pScalar, pMatrix.m11() * pScalar, pMatrix.m12() * pScalar, pMatrix.m13() * pScalar, pMatrix.m20() * pScalar, pMatrix.m21() * pScalar, pMatrix.m22() * pScalar, pMatrix.m23() * pScalar, pMatrix.m30() * pScalar, pMatrix.m31() * pScalar, pMatrix.m32() * pScalar, pMatrix.m33() * pScalar);
   }

   private static GivensParameters approxGivensQuat(float p_276275_, float p_276276_, float p_276282_) {
      float f = 2.0F * (p_276275_ - p_276282_);
      return G * p_276276_ * p_276276_ < f * f ? GivensParameters.fromUnnormalized(p_276276_, f) : PI_4;
   }

   private static GivensParameters qrGivensQuat(float p_253897_, float p_254413_) {
      float f = (float)java.lang.Math.hypot((double)p_253897_, (double)p_254413_);
      float f1 = f > 1.0E-6F ? p_254413_ : 0.0F;
      float f2 = Math.abs(p_253897_) + Math.max(f, 1.0E-6F);
      if (p_253897_ < 0.0F) {
         float f3 = f1;
         f1 = f2;
         f2 = f3;
      }

      return GivensParameters.fromUnnormalized(f1, f2);
   }

   private static void similarityTransform(Matrix3f pInput, Matrix3f pTempStorage) {
      pInput.mul(pTempStorage);
      pTempStorage.transpose();
      pTempStorage.mul(pInput);
      pInput.set((Matrix3fc)pTempStorage);
   }

   private static void stepJacobi(Matrix3f pInput, Matrix3f pTempStorage, Quaternionf pResultEigenvector, Quaternionf pResultEigenvalue) {
      if (pInput.m01 * pInput.m01 + pInput.m10 * pInput.m10 > 1.0E-6F) {
         GivensParameters givensparameters = approxGivensQuat(pInput.m00, 0.5F * (pInput.m01 + pInput.m10), pInput.m11);
         Quaternionf quaternionf = givensparameters.aroundZ(pResultEigenvector);
         pResultEigenvalue.mul(quaternionf);
         givensparameters.aroundZ(pTempStorage);
         similarityTransform(pInput, pTempStorage);
      }

      if (pInput.m02 * pInput.m02 + pInput.m20 * pInput.m20 > 1.0E-6F) {
         GivensParameters givensparameters1 = approxGivensQuat(pInput.m00, 0.5F * (pInput.m02 + pInput.m20), pInput.m22).inverse();
         Quaternionf quaternionf1 = givensparameters1.aroundY(pResultEigenvector);
         pResultEigenvalue.mul(quaternionf1);
         givensparameters1.aroundY(pTempStorage);
         similarityTransform(pInput, pTempStorage);
      }

      if (pInput.m12 * pInput.m12 + pInput.m21 * pInput.m21 > 1.0E-6F) {
         GivensParameters givensparameters2 = approxGivensQuat(pInput.m11, 0.5F * (pInput.m12 + pInput.m21), pInput.m22);
         Quaternionf quaternionf2 = givensparameters2.aroundX(pResultEigenvector);
         pResultEigenvalue.mul(quaternionf2);
         givensparameters2.aroundX(pTempStorage);
         similarityTransform(pInput, pTempStorage);
      }

   }

   public static Quaternionf eigenvalueJacobi(Matrix3f pInput, int pIterations) {
      Quaternionf quaternionf = new Quaternionf();
      Matrix3f matrix3f = new Matrix3f();
      Quaternionf quaternionf1 = new Quaternionf();

      for(int i = 0; i < pIterations; ++i) {
         stepJacobi(pInput, matrix3f, quaternionf1, quaternionf);
      }

      quaternionf.normalize();
      return quaternionf;
   }

   public static Triple<Quaternionf, Vector3f, Quaternionf> svdDecompose(Matrix3f pMatrix) {
      Matrix3f matrix3f = new Matrix3f(pMatrix);
      matrix3f.transpose();
      matrix3f.mul(pMatrix);
      Quaternionf quaternionf = eigenvalueJacobi(matrix3f, 5);
      float f = matrix3f.m00;
      float f1 = matrix3f.m11;
      boolean flag = (double)f < 1.0E-6D;
      boolean flag1 = (double)f1 < 1.0E-6D;
      Matrix3f matrix3f1 = pMatrix.rotate(quaternionf);
      Quaternionf quaternionf1 = new Quaternionf();
      Quaternionf quaternionf2 = new Quaternionf();
      GivensParameters givensparameters;
      if (flag) {
         givensparameters = qrGivensQuat(matrix3f1.m11, -matrix3f1.m10);
      } else {
         givensparameters = qrGivensQuat(matrix3f1.m00, matrix3f1.m01);
      }

      Quaternionf quaternionf3 = givensparameters.aroundZ(quaternionf2);
      Matrix3f matrix3f2 = givensparameters.aroundZ(matrix3f);
      quaternionf1.mul(quaternionf3);
      matrix3f2.transpose().mul(matrix3f1);
      if (flag) {
         givensparameters = qrGivensQuat(matrix3f2.m22, -matrix3f2.m20);
      } else {
         givensparameters = qrGivensQuat(matrix3f2.m00, matrix3f2.m02);
      }

      givensparameters = givensparameters.inverse();
      Quaternionf quaternionf4 = givensparameters.aroundY(quaternionf2);
      Matrix3f matrix3f3 = givensparameters.aroundY(matrix3f1);
      quaternionf1.mul(quaternionf4);
      matrix3f3.transpose().mul(matrix3f2);
      if (flag1) {
         givensparameters = qrGivensQuat(matrix3f3.m22, -matrix3f3.m21);
      } else {
         givensparameters = qrGivensQuat(matrix3f3.m11, matrix3f3.m12);
      }

      Quaternionf quaternionf5 = givensparameters.aroundX(quaternionf2);
      Matrix3f matrix3f4 = givensparameters.aroundX(matrix3f2);
      quaternionf1.mul(quaternionf5);
      matrix3f4.transpose().mul(matrix3f3);
      Vector3f vector3f = new Vector3f(matrix3f4.m00, matrix3f4.m11, matrix3f4.m22);
      return Triple.of(quaternionf1, vector3f, quaternionf.conjugate());
   }
}