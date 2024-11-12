package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexFormatElement {
   private final VertexFormatElement.Type type;
   private final VertexFormatElement.Usage usage;
   private final int index;
   private final int count;
   private final int byteSize;

   public VertexFormatElement(int pIndex, VertexFormatElement.Type pType, VertexFormatElement.Usage pUsage, int pCount) {
      if (this.supportsUsage(pIndex, pUsage)) {
         this.usage = pUsage;
         this.type = pType;
         this.index = pIndex;
         this.count = pCount;
         this.byteSize = pType.getSize() * this.count;
      } else {
         throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
      }
   }

   private boolean supportsUsage(int pIndex, VertexFormatElement.Usage pUsage) {
      return pIndex == 0 || pUsage == VertexFormatElement.Usage.UV;
   }

   public final VertexFormatElement.Type getType() {
      return this.type;
   }

   public final VertexFormatElement.Usage getUsage() {
      return this.usage;
   }

   public final int getCount() {
      return this.count;
   }

   public final int getIndex() {
      return this.index;
   }

   public String toString() {
      return this.count + "," + this.usage.getName() + "," + this.type.getName();
   }

   public final int getByteSize() {
      return this.byteSize;
   }

   public final boolean isPosition() {
      return this.usage == VertexFormatElement.Usage.POSITION;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther != null && this.getClass() == pOther.getClass()) {
         VertexFormatElement vertexformatelement = (VertexFormatElement)pOther;
         if (this.count != vertexformatelement.count) {
            return false;
         } else if (this.index != vertexformatelement.index) {
            return false;
         } else if (this.type != vertexformatelement.type) {
            return false;
         } else {
            return this.usage == vertexformatelement.usage;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = this.type.hashCode();
      i = 31 * i + this.usage.hashCode();
      i = 31 * i + this.index;
      return 31 * i + this.count;
   }

   public void setupBufferState(int pStateIndex, long pOffset, int pStride) {
      this.usage.setupBufferState(this.count, this.type.getGlType(), pStride, pOffset, this.index, pStateIndex);
   }

   public void clearBufferState(int pElementIndex) {
      this.usage.clearBufferState(this.index, pElementIndex);
   }

    public int getElementCount() {
       return count;
    }

   @OnlyIn(Dist.CLIENT)
   public static enum Type {
      FLOAT(4, "Float", 5126),
      UBYTE(1, "Unsigned Byte", 5121),
      BYTE(1, "Byte", 5120),
      USHORT(2, "Unsigned Short", 5123),
      SHORT(2, "Short", 5122),
      UINT(4, "Unsigned Int", 5125),
      INT(4, "Int", 5124);

      private final int size;
      private final String name;
      private final int glType;

      private Type(int pSize, String pName, int pGlType) {
         this.size = pSize;
         this.name = pName;
         this.glType = pGlType;
      }

      public int getSize() {
         return this.size;
      }

      public String getName() {
         return this.name;
      }

      public int getGlType() {
         return this.glType;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Usage {
      POSITION("Position", (p_167043_, p_167044_, p_167045_, p_167046_, p_167047_, p_167048_) -> {
         GlStateManager._enableVertexAttribArray(p_167048_);
         GlStateManager._vertexAttribPointer(p_167048_, p_167043_, p_167044_, false, p_167045_, p_167046_);
      }, (p_167040_, p_167041_) -> {
         GlStateManager._disableVertexAttribArray(p_167041_);
      }),
      NORMAL("Normal", (p_167033_, p_167034_, p_167035_, p_167036_, p_167037_, p_167038_) -> {
         GlStateManager._enableVertexAttribArray(p_167038_);
         GlStateManager._vertexAttribPointer(p_167038_, p_167033_, p_167034_, true, p_167035_, p_167036_);
      }, (p_167030_, p_167031_) -> {
         GlStateManager._disableVertexAttribArray(p_167031_);
      }),
      COLOR("Vertex Color", (p_167023_, p_167024_, p_167025_, p_167026_, p_167027_, p_167028_) -> {
         GlStateManager._enableVertexAttribArray(p_167028_);
         GlStateManager._vertexAttribPointer(p_167028_, p_167023_, p_167024_, true, p_167025_, p_167026_);
      }, (p_167020_, p_167021_) -> {
         GlStateManager._disableVertexAttribArray(p_167021_);
      }),
      UV("UV", (p_167013_, p_167014_, p_167015_, p_167016_, p_167017_, p_167018_) -> {
         GlStateManager._enableVertexAttribArray(p_167018_);
         if (p_167014_ == 5126) {
            GlStateManager._vertexAttribPointer(p_167018_, p_167013_, p_167014_, false, p_167015_, p_167016_);
         } else {
            GlStateManager._vertexAttribIPointer(p_167018_, p_167013_, p_167014_, p_167015_, p_167016_);
         }

      }, (p_167010_, p_167011_) -> {
         GlStateManager._disableVertexAttribArray(p_167011_);
      }),
      PADDING("Padding", (p_167003_, p_167004_, p_167005_, p_167006_, p_167007_, p_167008_) -> {
      }, (p_167000_, p_167001_) -> {
      }),
      GENERIC("Generic", (p_166993_, p_166994_, p_166995_, p_166996_, p_166997_, p_166998_) -> {
         GlStateManager._enableVertexAttribArray(p_166998_);
         GlStateManager._vertexAttribPointer(p_166998_, p_166993_, p_166994_, false, p_166995_, p_166996_);
      }, (p_166990_, p_166991_) -> {
         GlStateManager._disableVertexAttribArray(p_166991_);
      });

      private final String name;
      private final VertexFormatElement.Usage.SetupState setupState;
      private final VertexFormatElement.Usage.ClearState clearState;

      private Usage(String pName, VertexFormatElement.Usage.SetupState pSetupState, VertexFormatElement.Usage.ClearState pClearState) {
         this.name = pName;
         this.setupState = pSetupState;
         this.clearState = pClearState;
      }

      void setupBufferState(int pCount, int pGlType, int pStride, long pOffset, int pIndex, int pStateIndex) {
         this.setupState.setupBufferState(pCount, pGlType, pStride, pOffset, pIndex, pStateIndex);
      }

      public void clearBufferState(int pIndex, int pElementIndex) {
         this.clearState.clearBufferState(pIndex, pElementIndex);
      }

      public String getName() {
         return this.name;
      }

      @FunctionalInterface
      @OnlyIn(Dist.CLIENT)
      interface ClearState {
         void clearBufferState(int pIndex, int pElementIndex);
      }

      @FunctionalInterface
      @OnlyIn(Dist.CLIENT)
      interface SetupState {
         void setupBufferState(int pCount, int pGlType, int pStride, long pOffset, int pIndex, int pStateIndex);
      }
   }
}
