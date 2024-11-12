package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Uniform extends AbstractUniform implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int UT_INT1 = 0;
   public static final int UT_INT2 = 1;
   public static final int UT_INT3 = 2;
   public static final int UT_INT4 = 3;
   public static final int UT_FLOAT1 = 4;
   public static final int UT_FLOAT2 = 5;
   public static final int UT_FLOAT3 = 6;
   public static final int UT_FLOAT4 = 7;
   public static final int UT_MAT2 = 8;
   public static final int UT_MAT3 = 9;
   public static final int UT_MAT4 = 10;
   private static final boolean TRANSPOSE_MATRICIES = false;
   private int location;
   private final int count;
   private final int type;
   private final IntBuffer intValues;
   private final FloatBuffer floatValues;
   private final String name;
   private boolean dirty;
   private final Shader parent;

   public Uniform(String pName, int pType, int pCount, Shader pParent) {
      this.name = pName;
      this.count = pCount;
      this.type = pType;
      this.parent = pParent;
      if (pType <= 3) {
         this.intValues = MemoryUtil.memAllocInt(pCount);
         this.floatValues = null;
      } else {
         this.intValues = null;
         this.floatValues = MemoryUtil.memAllocFloat(pCount);
      }

      this.location = -1;
      this.markDirty();
   }

   public static int glGetUniformLocation(int pProgram, CharSequence pName) {
      return GlStateManager._glGetUniformLocation(pProgram, pName);
   }

   public static void uploadInteger(int pLocation, int pValue) {
      RenderSystem.glUniform1i(pLocation, pValue);
   }

   public static int glGetAttribLocation(int pProgram, CharSequence pName) {
      return GlStateManager._glGetAttribLocation(pProgram, pName);
   }

   public static void glBindAttribLocation(int pProgram, int pIndex, CharSequence pName) {
      GlStateManager._glBindAttribLocation(pProgram, pIndex, pName);
   }

   public void close() {
      if (this.intValues != null) {
         MemoryUtil.memFree(this.intValues);
      }

      if (this.floatValues != null) {
         MemoryUtil.memFree(this.floatValues);
      }

   }

   private void markDirty() {
      this.dirty = true;
      if (this.parent != null) {
         this.parent.markDirty();
      }

   }

   public static int getTypeFromString(String pTypeName) {
      int i = -1;
      if ("int".equals(pTypeName)) {
         i = 0;
      } else if ("float".equals(pTypeName)) {
         i = 4;
      } else if (pTypeName.startsWith("matrix")) {
         if (pTypeName.endsWith("2x2")) {
            i = 8;
         } else if (pTypeName.endsWith("3x3")) {
            i = 9;
         } else if (pTypeName.endsWith("4x4")) {
            i = 10;
         }
      }

      return i;
   }

   public void setLocation(int pLocation) {
      this.location = pLocation;
   }

   public String getName() {
      return this.name;
   }

   public final void set(float pX) {
      this.floatValues.position(0);
      this.floatValues.put(0, pX);
      this.markDirty();
   }

   public final void set(float pX, float pY) {
      this.floatValues.position(0);
      this.floatValues.put(0, pX);
      this.floatValues.put(1, pY);
      this.markDirty();
   }

   public final void set(int pIndex, float pValue) {
      this.floatValues.position(0);
      this.floatValues.put(pIndex, pValue);
      this.markDirty();
   }

   public final void set(float pX, float pY, float pZ) {
      this.floatValues.position(0);
      this.floatValues.put(0, pX);
      this.floatValues.put(1, pY);
      this.floatValues.put(2, pZ);
      this.markDirty();
   }

   public final void set(Vector3f pVector) {
      this.floatValues.position(0);
      pVector.get(this.floatValues);
      this.markDirty();
   }

   public final void set(float pX, float pY, float pZ, float pW) {
      this.floatValues.position(0);
      this.floatValues.put(pX);
      this.floatValues.put(pY);
      this.floatValues.put(pZ);
      this.floatValues.put(pW);
      this.floatValues.flip();
      this.markDirty();
   }

   public final void set(Vector4f pVector) {
      this.floatValues.position(0);
      pVector.get(this.floatValues);
      this.markDirty();
   }

   public final void setSafe(float pX, float pY, float pZ, float pW) {
      this.floatValues.position(0);
      if (this.type >= 4) {
         this.floatValues.put(0, pX);
      }

      if (this.type >= 5) {
         this.floatValues.put(1, pY);
      }

      if (this.type >= 6) {
         this.floatValues.put(2, pZ);
      }

      if (this.type >= 7) {
         this.floatValues.put(3, pW);
      }

      this.markDirty();
   }

   public final void setSafe(int pX, int pY, int pZ, int pW) {
      this.intValues.position(0);
      if (this.type >= 0) {
         this.intValues.put(0, pX);
      }

      if (this.type >= 1) {
         this.intValues.put(1, pY);
      }

      if (this.type >= 2) {
         this.intValues.put(2, pZ);
      }

      if (this.type >= 3) {
         this.intValues.put(3, pW);
      }

      this.markDirty();
   }

   public final void set(int pX) {
      this.intValues.position(0);
      this.intValues.put(0, pX);
      this.markDirty();
   }

   public final void set(int pX, int pY) {
      this.intValues.position(0);
      this.intValues.put(0, pX);
      this.intValues.put(1, pY);
      this.markDirty();
   }

   public final void set(int pX, int pY, int pZ) {
      this.intValues.position(0);
      this.intValues.put(0, pX);
      this.intValues.put(1, pY);
      this.intValues.put(2, pZ);
      this.markDirty();
   }

   public final void set(int pX, int pY, int pZ, int pW) {
      this.intValues.position(0);
      this.intValues.put(0, pX);
      this.intValues.put(1, pY);
      this.intValues.put(2, pZ);
      this.intValues.put(3, pW);
      this.markDirty();
   }

   public final void set(float[] pValueArray) {
      if (pValueArray.length < this.count) {
         LOGGER.warn("Uniform.set called with a too-small value array (expected {}, got {}). Ignoring.", this.count, pValueArray.length);
      } else {
         this.floatValues.position(0);
         this.floatValues.put(pValueArray);
         this.floatValues.position(0);
         this.markDirty();
      }
   }

   public final void setMat2x2(float pM00, float pM01, float pM10, float pM11) {
      this.floatValues.position(0);
      this.floatValues.put(0, pM00);
      this.floatValues.put(1, pM01);
      this.floatValues.put(2, pM10);
      this.floatValues.put(3, pM11);
      this.markDirty();
   }

   public final void setMat2x3(float pM00, float pM01, float pM02, float pM10, float pM11, float pM12) {
      this.floatValues.position(0);
      this.floatValues.put(0, pM00);
      this.floatValues.put(1, pM01);
      this.floatValues.put(2, pM02);
      this.floatValues.put(3, pM10);
      this.floatValues.put(4, pM11);
      this.floatValues.put(5, pM12);
      this.markDirty();
   }

   public final void setMat2x4(float pM00, float pM01, float pM02, float pM03, float pM10, float pM11, float pM12, float pM13) {
      this.floatValues.position(0);
      this.floatValues.put(0, pM00);
      this.floatValues.put(1, pM01);
      this.floatValues.put(2, pM02);
      this.floatValues.put(3, pM03);
      this.floatValues.put(4, pM10);
      this.floatValues.put(5, pM11);
      this.floatValues.put(6, pM12);
      this.floatValues.put(7, pM13);
      this.markDirty();
   }

   public final void setMat3x2(float pM00, float pM01, float pM10, float pM11, float pM20, float pM21) {
      this.floatValues.position(0);
      this.floatValues.put(0, pM00);
      this.floatValues.put(1, pM01);
      this.floatValues.put(2, pM10);
      this.floatValues.put(3, pM11);
      this.floatValues.put(4, pM20);
      this.floatValues.put(5, pM21);
      this.markDirty();
   }

   public final void setMat3x3(float pM00, float pM01, float pM02, float pM10, float pM11, float pM12, float pM20, float pM21, float pM22) {
      this.floatValues.position(0);
      this.floatValues.put(0, pM00);
      this.floatValues.put(1, pM01);
      this.floatValues.put(2, pM02);
      this.floatValues.put(3, pM10);
      this.floatValues.put(4, pM11);
      this.floatValues.put(5, pM12);
      this.floatValues.put(6, pM20);
      this.floatValues.put(7, pM21);
      this.floatValues.put(8, pM22);
      this.markDirty();
   }

   public final void setMat3x4(float pM00, float pM01, float pM02, float pM03, float pM10, float pM11, float pM12, float pM13, float pM20, float pM21, float pM22, float pM23) {
      this.floatValues.position(0);
      this.floatValues.put(0, pM00);
      this.floatValues.put(1, pM01);
      this.floatValues.put(2, pM02);
      this.floatValues.put(3, pM03);
      this.floatValues.put(4, pM10);
      this.floatValues.put(5, pM11);
      this.floatValues.put(6, pM12);
      this.floatValues.put(7, pM13);
      this.floatValues.put(8, pM20);
      this.floatValues.put(9, pM21);
      this.floatValues.put(10, pM22);
      this.floatValues.put(11, pM23);
      this.markDirty();
   }

   public final void setMat4x2(float pM00, float pM01, float pM02, float pM03, float pM10, float pM11, float pM12, float pM13) {
      this.floatValues.position(0);
      this.floatValues.put(0, pM00);
      this.floatValues.put(1, pM01);
      this.floatValues.put(2, pM02);
      this.floatValues.put(3, pM03);
      this.floatValues.put(4, pM10);
      this.floatValues.put(5, pM11);
      this.floatValues.put(6, pM12);
      this.floatValues.put(7, pM13);
      this.markDirty();
   }

   public final void setMat4x3(float pM00, float pM01, float pM02, float pM03, float pM10, float pM11, float pM12, float pM13, float pM20, float pM21, float pM22, float pM23) {
      this.floatValues.position(0);
      this.floatValues.put(0, pM00);
      this.floatValues.put(1, pM01);
      this.floatValues.put(2, pM02);
      this.floatValues.put(3, pM03);
      this.floatValues.put(4, pM10);
      this.floatValues.put(5, pM11);
      this.floatValues.put(6, pM12);
      this.floatValues.put(7, pM13);
      this.floatValues.put(8, pM20);
      this.floatValues.put(9, pM21);
      this.floatValues.put(10, pM22);
      this.floatValues.put(11, pM23);
      this.markDirty();
   }

   public final void setMat4x4(float pM00, float pM01, float pM02, float pM03, float pM10, float pM11, float pM12, float pM13, float pM20, float pM21, float pM22, float pM23, float pM30, float pM31, float pM32, float pM33) {
      this.floatValues.position(0);
      this.floatValues.put(0, pM00);
      this.floatValues.put(1, pM01);
      this.floatValues.put(2, pM02);
      this.floatValues.put(3, pM03);
      this.floatValues.put(4, pM10);
      this.floatValues.put(5, pM11);
      this.floatValues.put(6, pM12);
      this.floatValues.put(7, pM13);
      this.floatValues.put(8, pM20);
      this.floatValues.put(9, pM21);
      this.floatValues.put(10, pM22);
      this.floatValues.put(11, pM23);
      this.floatValues.put(12, pM30);
      this.floatValues.put(13, pM31);
      this.floatValues.put(14, pM32);
      this.floatValues.put(15, pM33);
      this.markDirty();
   }

   public final void set(Matrix4f pMatrix) {
      this.floatValues.position(0);
      pMatrix.get(this.floatValues);
      this.markDirty();
   }

   public final void set(Matrix3f pMatrix) {
      this.floatValues.position(0);
      pMatrix.get(this.floatValues);
      this.markDirty();
   }

   public void upload() {
      if (!this.dirty) {
      }

      this.dirty = false;
      if (this.type <= 3) {
         this.uploadAsInteger();
      } else if (this.type <= 7) {
         this.uploadAsFloat();
      } else {
         if (this.type > 10) {
            LOGGER.warn("Uniform.upload called, but type value ({}) is not a valid type. Ignoring.", (int)this.type);
            return;
         }

         this.uploadAsMatrix();
      }

   }

   private void uploadAsInteger() {
      this.intValues.rewind();
      switch (this.type) {
         case 0:
            RenderSystem.glUniform1(this.location, this.intValues);
            break;
         case 1:
            RenderSystem.glUniform2(this.location, this.intValues);
            break;
         case 2:
            RenderSystem.glUniform3(this.location, this.intValues);
            break;
         case 3:
            RenderSystem.glUniform4(this.location, this.intValues);
            break;
         default:
            LOGGER.warn("Uniform.upload called, but count value ({}) is  not in the range of 1 to 4. Ignoring.", (int)this.count);
      }

   }

   private void uploadAsFloat() {
      this.floatValues.rewind();
      switch (this.type) {
         case 4:
            RenderSystem.glUniform1(this.location, this.floatValues);
            break;
         case 5:
            RenderSystem.glUniform2(this.location, this.floatValues);
            break;
         case 6:
            RenderSystem.glUniform3(this.location, this.floatValues);
            break;
         case 7:
            RenderSystem.glUniform4(this.location, this.floatValues);
            break;
         default:
            LOGGER.warn("Uniform.upload called, but count value ({}) is not in the range of 1 to 4. Ignoring.", (int)this.count);
      }

   }

   private void uploadAsMatrix() {
      this.floatValues.clear();
      switch (this.type) {
         case 8:
            RenderSystem.glUniformMatrix2(this.location, false, this.floatValues);
            break;
         case 9:
            RenderSystem.glUniformMatrix3(this.location, false, this.floatValues);
            break;
         case 10:
            RenderSystem.glUniformMatrix4(this.location, false, this.floatValues);
      }

   }

   public int getLocation() {
      return this.location;
   }

   public int getCount() {
      return this.count;
   }

   public int getType() {
      return this.type;
   }

   public IntBuffer getIntBuffer() {
      return this.intValues;
   }

   public FloatBuffer getFloatBuffer() {
      return this.floatValues;
   }
}