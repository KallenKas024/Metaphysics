package com.mojang.blaze3d.shaders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class AbstractUniform {
   public void set(float pX) {
   }

   public void set(float pX, float pY) {
   }

   public void set(float pX, float pY, float pZ) {
   }

   public void set(float pX, float pY, float pZ, float pW) {
   }

   public void setSafe(float pX, float pY, float pZ, float pW) {
   }

   public void setSafe(int pX, int pY, int pZ, int pW) {
   }

   public void set(int pX) {
   }

   public void set(int pX, int pY) {
   }

   public void set(int pX, int pY, int pZ) {
   }

   public void set(int pX, int pY, int pZ, int pW) {
   }

   public void set(float[] pValueArray) {
   }

   public void set(Vector3f pVector) {
   }

   public void set(Vector4f pVector) {
   }

   public void setMat2x2(float pM00, float pM01, float pM10, float pM11) {
   }

   public void setMat2x3(float pM00, float pM01, float pM02, float pM10, float pM11, float pM12) {
   }

   public void setMat2x4(float pM00, float pM01, float pM02, float pM03, float pM10, float pM11, float pM12, float pM13) {
   }

   public void setMat3x2(float pM00, float pM01, float pM10, float pM11, float pM20, float pM21) {
   }

   public void setMat3x3(float pM00, float pM01, float pM02, float pM10, float pM11, float pM12, float pM20, float pM21, float pM22) {
   }

   public void setMat3x4(float pM00, float pM01, float pM02, float pM03, float pM10, float pM11, float pM12, float pM13, float pM20, float pM21, float pM22, float pM23) {
   }

   public void setMat4x2(float pM00, float pM01, float pM02, float pM03, float pM10, float pM11, float pM12, float pM13) {
   }

   public void setMat4x3(float pM00, float pM01, float pM02, float pM03, float pM10, float pM11, float pM12, float pM13, float pM20, float pM21, float pM22, float pM23) {
   }

   public void setMat4x4(float pM00, float pM01, float pM02, float pM03, float pM10, float pM11, float pM12, float pM13, float pM20, float pM21, float pM22, float pM23, float pM30, float pM31, float pM32, float pM33) {
   }

   public void set(Matrix4f pMatrix) {
   }

   public void set(Matrix3f pMatrix) {
   }
}