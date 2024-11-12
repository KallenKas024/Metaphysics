package com.mojang.blaze3d.vertex;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface BufferVertexConsumer extends VertexConsumer {
   VertexFormatElement currentElement();

   void nextElement();

   void putByte(int pIndex, byte pByteValue);

   void putShort(int pIndex, short pShortValue);

   void putFloat(int pIndex, float pFloatValue);

   default VertexConsumer vertex(double pX, double pY, double pZ) {
      if (this.currentElement().getUsage() != VertexFormatElement.Usage.POSITION) {
         return this;
      } else if (this.currentElement().getType() == VertexFormatElement.Type.FLOAT && this.currentElement().getCount() == 3) {
         this.putFloat(0, (float)pX);
         this.putFloat(4, (float)pY);
         this.putFloat(8, (float)pZ);
         this.nextElement();
         return this;
      } else {
         throw new IllegalStateException();
      }
   }

   default VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() != VertexFormatElement.Usage.COLOR) {
         return this;
      } else if (vertexformatelement.getType() == VertexFormatElement.Type.UBYTE && vertexformatelement.getCount() == 4) {
         this.putByte(0, (byte)pRed);
         this.putByte(1, (byte)pGreen);
         this.putByte(2, (byte)pBlue);
         this.putByte(3, (byte)pAlpha);
         this.nextElement();
         return this;
      } else {
         throw new IllegalStateException();
      }
   }

   default VertexConsumer uv(float pU, float pV) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == 0) {
         if (vertexformatelement.getType() == VertexFormatElement.Type.FLOAT && vertexformatelement.getCount() == 2) {
            this.putFloat(0, pU);
            this.putFloat(4, pV);
            this.nextElement();
            return this;
         } else {
            throw new IllegalStateException();
         }
      } else {
         return this;
      }
   }

   default VertexConsumer overlayCoords(int pU, int pV) {
      return this.uvShort((short)pU, (short)pV, 1);
   }

   default VertexConsumer uv2(int pU, int pV) {
      return this.uvShort((short)pU, (short)pV, 2);
   }

   default VertexConsumer uvShort(short pU, short pV, int pIndex) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == pIndex) {
         if (vertexformatelement.getType() == VertexFormatElement.Type.SHORT && vertexformatelement.getCount() == 2) {
            this.putShort(0, pU);
            this.putShort(2, pV);
            this.nextElement();
            return this;
         } else {
            throw new IllegalStateException();
         }
      } else {
         return this;
      }
   }

   default VertexConsumer normal(float pX, float pY, float pZ) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() != VertexFormatElement.Usage.NORMAL) {
         return this;
      } else if (vertexformatelement.getType() == VertexFormatElement.Type.BYTE && vertexformatelement.getCount() == 3) {
         this.putByte(0, normalIntValue(pX));
         this.putByte(1, normalIntValue(pY));
         this.putByte(2, normalIntValue(pZ));
         this.nextElement();
         return this;
      } else {
         throw new IllegalStateException();
      }
   }

   static byte normalIntValue(float pNum) {
      return (byte)((int)(Mth.clamp(pNum, -1.0F, 1.0F) * 127.0F) & 255);
   }
}