package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
   private static final int GROWTH_SIZE = 2097152;
   private static final Logger LOGGER = LogUtils.getLogger();
   private ByteBuffer buffer;
   private int renderedBufferCount;
   private int renderedBufferPointer;
   private int nextElementByte;
   private int vertices;
   @Nullable
   private VertexFormatElement currentElement;
   private int elementIndex;
   private VertexFormat format;
   private VertexFormat.Mode mode;
   private boolean fastFormat;
   private boolean fullFormat;
   private boolean building;
   @Nullable
   private Vector3f[] sortingPoints;
   @Nullable
   private VertexSorting sorting;
   private boolean indexOnly;

   public BufferBuilder(int pCapacity) {
      this.buffer = MemoryTracker.create(pCapacity * 6);
   }

   private void ensureVertexCapacity() {
      this.ensureCapacity(this.format.getVertexSize());
   }

   private void ensureCapacity(int pIncreaseAmount) {
      if (this.nextElementByte + pIncreaseAmount > this.buffer.capacity()) {
         int i = this.buffer.capacity();
         int j = i + roundUp(pIncreaseAmount);
         LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", i, j);
         ByteBuffer bytebuffer = MemoryTracker.resize(this.buffer, j);
         bytebuffer.rewind();
         this.buffer = bytebuffer;
      }
   }

   private static int roundUp(int pX) {
      int i = 2097152;
      if (pX == 0) {
         return i;
      } else {
         if (pX < 0) {
            i *= -1;
         }

         int j = pX % i;
         return j == 0 ? pX : pX + i - j;
      }
   }

   public void setQuadSorting(VertexSorting pQuadSorting) {
      if (this.mode == VertexFormat.Mode.QUADS) {
         this.sorting = pQuadSorting;
         if (this.sortingPoints == null) {
            this.sortingPoints = this.makeQuadSortingPoints();
         }

      }
   }

   public BufferBuilder.SortState getSortState() {
      return new BufferBuilder.SortState(this.mode, this.vertices, this.sortingPoints, this.sorting);
   }

   public void restoreSortState(BufferBuilder.SortState pSortState) {
      this.buffer.rewind();
      this.mode = pSortState.mode;
      this.vertices = pSortState.vertices;
      this.nextElementByte = this.renderedBufferPointer;
      this.sortingPoints = pSortState.sortingPoints;
      this.sorting = pSortState.sorting;
      this.indexOnly = true;
   }

   public void begin(VertexFormat.Mode pMode, VertexFormat pFormat) {
      if (this.building) {
         throw new IllegalStateException("Already building!");
      } else {
         this.building = true;
         this.mode = pMode;
         this.switchFormat(pFormat);
         this.currentElement = pFormat.getElements().get(0);
         this.elementIndex = 0;
         this.buffer.rewind();
      }
   }

   private void switchFormat(VertexFormat pFormat) {
      if (this.format != pFormat) {
         this.format = pFormat;
         boolean flag = pFormat == DefaultVertexFormat.NEW_ENTITY;
         boolean flag1 = pFormat == DefaultVertexFormat.BLOCK;
         this.fastFormat = flag || flag1;
         this.fullFormat = flag;
      }
   }

   private IntConsumer intConsumer(int pNextElementByte, VertexFormat.IndexType pIndexType) {
      MutableInt mutableint = new MutableInt(pNextElementByte);
      IntConsumer intconsumer;
      switch (pIndexType) {
         case SHORT:
            intconsumer = (p_231167_) -> {
               this.buffer.putShort(mutableint.getAndAdd(2), (short)p_231167_);
            };
            break;
         case INT:
            intconsumer = (p_231163_) -> {
               this.buffer.putInt(mutableint.getAndAdd(4), p_231163_);
            };
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return intconsumer;
   }

   private Vector3f[] makeQuadSortingPoints() {
      FloatBuffer floatbuffer = this.buffer.asFloatBuffer();
      int i = this.renderedBufferPointer / 4;
      int j = this.format.getIntegerSize();
      int k = j * this.mode.primitiveStride;
      int l = this.vertices / this.mode.primitiveStride;
      Vector3f[] avector3f = new Vector3f[l];

      for(int i1 = 0; i1 < l; ++i1) {
         float f = floatbuffer.get(i + i1 * k + 0);
         float f1 = floatbuffer.get(i + i1 * k + 1);
         float f2 = floatbuffer.get(i + i1 * k + 2);
         float f3 = floatbuffer.get(i + i1 * k + j * 2 + 0);
         float f4 = floatbuffer.get(i + i1 * k + j * 2 + 1);
         float f5 = floatbuffer.get(i + i1 * k + j * 2 + 2);
         float f6 = (f + f3) / 2.0F;
         float f7 = (f1 + f4) / 2.0F;
         float f8 = (f2 + f5) / 2.0F;
         avector3f[i1] = new Vector3f(f6, f7, f8);
      }

      return avector3f;
   }

   private void putSortedQuadIndices(VertexFormat.IndexType pIndexType) {
      if (this.sortingPoints != null && this.sorting != null) {
         int[] aint = this.sorting.sort(this.sortingPoints);
         IntConsumer intconsumer = this.intConsumer(this.nextElementByte, pIndexType);

         for(int i : aint) {
            intconsumer.accept(i * this.mode.primitiveStride + 0);
            intconsumer.accept(i * this.mode.primitiveStride + 1);
            intconsumer.accept(i * this.mode.primitiveStride + 2);
            intconsumer.accept(i * this.mode.primitiveStride + 2);
            intconsumer.accept(i * this.mode.primitiveStride + 3);
            intconsumer.accept(i * this.mode.primitiveStride + 0);
         }

      } else {
         throw new IllegalStateException("Sorting state uninitialized");
      }
   }

   public boolean isCurrentBatchEmpty() {
      return this.vertices == 0;
   }

   @Nullable
   public BufferBuilder.RenderedBuffer endOrDiscardIfEmpty() {
      this.ensureDrawing();
      if (this.isCurrentBatchEmpty()) {
         this.reset();
         return null;
      } else {
         BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.storeRenderedBuffer();
         this.reset();
         return bufferbuilder$renderedbuffer;
      }
   }

   public BufferBuilder.RenderedBuffer end() {
      this.ensureDrawing();
      BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = this.storeRenderedBuffer();
      this.reset();
      return bufferbuilder$renderedbuffer;
   }

   private void ensureDrawing() {
      if (!this.building) {
         throw new IllegalStateException("Not building!");
      }
   }

   private BufferBuilder.RenderedBuffer storeRenderedBuffer() {
      int i = this.mode.indexCount(this.vertices);
      int j = !this.indexOnly ? this.vertices * this.format.getVertexSize() : 0;
      VertexFormat.IndexType vertexformat$indextype = VertexFormat.IndexType.least(i);
      boolean flag;
      int k;
      if (this.sortingPoints != null) {
         int l = Mth.roundToward(i * vertexformat$indextype.bytes, 4);
         this.ensureCapacity(l);
         this.putSortedQuadIndices(vertexformat$indextype);
         flag = false;
         this.nextElementByte += l;
         k = j + l;
      } else {
         flag = true;
         k = j;
      }

      int i1 = this.renderedBufferPointer;
      this.renderedBufferPointer += k;
      ++this.renderedBufferCount;
      BufferBuilder.DrawState bufferbuilder$drawstate = new BufferBuilder.DrawState(this.format, this.vertices, i, this.mode, vertexformat$indextype, this.indexOnly, flag);
      return new BufferBuilder.RenderedBuffer(i1, bufferbuilder$drawstate);
   }

   private void reset() {
      this.building = false;
      this.vertices = 0;
      this.currentElement = null;
      this.elementIndex = 0;
      this.sortingPoints = null;
      this.sorting = null;
      this.indexOnly = false;
   }

   public void putByte(int pIndex, byte pByteValue) {
      this.buffer.put(this.nextElementByte + pIndex, pByteValue);
   }

   public void putShort(int pIndex, short pShortValue) {
      this.buffer.putShort(this.nextElementByte + pIndex, pShortValue);
   }

   public void putFloat(int pIndex, float pFloatValue) {
      this.buffer.putFloat(this.nextElementByte + pIndex, pFloatValue);
   }

   public void endVertex() {
      if (this.elementIndex != 0) {
         throw new IllegalStateException("Not filled all elements of the vertex");
      } else {
         ++this.vertices;
         this.ensureVertexCapacity();
         if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
            int i = this.format.getVertexSize();
            this.buffer.put(this.nextElementByte, this.buffer, this.nextElementByte - i, i);
            this.nextElementByte += i;
            ++this.vertices;
            this.ensureVertexCapacity();
         }

      }
   }

   public void nextElement() {
      ImmutableList<VertexFormatElement> immutablelist = this.format.getElements();
      this.elementIndex = (this.elementIndex + 1) % immutablelist.size();
      this.nextElementByte += this.currentElement.getByteSize();
      VertexFormatElement vertexformatelement = immutablelist.get(this.elementIndex);
      this.currentElement = vertexformatelement;
      if (vertexformatelement.getUsage() == VertexFormatElement.Usage.PADDING) {
         this.nextElement();
      }

      if (this.defaultColorSet && this.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
         BufferVertexConsumer.super.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA);
      }

   }

   public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
      if (this.defaultColorSet) {
         throw new IllegalStateException();
      } else {
         return BufferVertexConsumer.super.color(pRed, pGreen, pBlue, pAlpha);
      }
   }

   public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
      if (this.defaultColorSet) {
         throw new IllegalStateException();
      } else if (this.fastFormat) {
         this.putFloat(0, pX);
         this.putFloat(4, pY);
         this.putFloat(8, pZ);
         this.putByte(12, (byte)((int)(pRed * 255.0F)));
         this.putByte(13, (byte)((int)(pGreen * 255.0F)));
         this.putByte(14, (byte)((int)(pBlue * 255.0F)));
         this.putByte(15, (byte)((int)(pAlpha * 255.0F)));
         this.putFloat(16, pTexU);
         this.putFloat(20, pTexV);
         int i;
         if (this.fullFormat) {
            this.putShort(24, (short)(pOverlayUV & '\uffff'));
            this.putShort(26, (short)(pOverlayUV >> 16 & '\uffff'));
            i = 28;
         } else {
            i = 24;
         }

         this.putShort(i + 0, (short)(pLightmapUV & '\uffff'));
         this.putShort(i + 2, (short)(pLightmapUV >> 16 & '\uffff'));
         this.putByte(i + 4, BufferVertexConsumer.normalIntValue(pNormalX));
         this.putByte(i + 5, BufferVertexConsumer.normalIntValue(pNormalY));
         this.putByte(i + 6, BufferVertexConsumer.normalIntValue(pNormalZ));
         this.nextElementByte += i + 8;
         this.endVertex();
      } else {
         super.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
      }
   }

   void releaseRenderedBuffer() {
      if (this.renderedBufferCount > 0 && --this.renderedBufferCount == 0) {
         this.clear();
      }

   }

   public void clear() {
      if (this.renderedBufferCount > 0) {
         LOGGER.warn("Clearing BufferBuilder with unused batches");
      }

      this.discard();
   }

   public void discard() {
      this.renderedBufferCount = 0;
      this.renderedBufferPointer = 0;
      this.nextElementByte = 0;
   }

   public VertexFormatElement currentElement() {
      if (this.currentElement == null) {
         throw new IllegalStateException("BufferBuilder not started");
      } else {
         return this.currentElement;
      }
   }

   public boolean building() {
      return this.building;
   }

   ByteBuffer bufferSlice(int pStartPointer, int pEndPointer) {
      return MemoryUtil.memSlice(this.buffer, pStartPointer, pEndPointer - pStartPointer);
   }

   @OnlyIn(Dist.CLIENT)
   public static record DrawState(VertexFormat format, int vertexCount, int indexCount, VertexFormat.Mode mode, VertexFormat.IndexType indexType, boolean indexOnly, boolean sequentialIndex) {
      public int vertexBufferSize() {
         return this.vertexCount * this.format.getVertexSize();
      }

      public int vertexBufferStart() {
         return 0;
      }

      public int vertexBufferEnd() {
         return this.vertexBufferSize();
      }

      public int indexBufferStart() {
         return this.indexOnly ? 0 : this.vertexBufferEnd();
      }

      public int indexBufferEnd() {
         return this.indexBufferStart() + this.indexBufferSize();
      }

      private int indexBufferSize() {
         return this.sequentialIndex ? 0 : this.indexCount * this.indexType.bytes;
      }

      public int bufferSize() {
         return this.indexBufferEnd();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public class RenderedBuffer {
      private final int pointer;
      private final BufferBuilder.DrawState drawState;
      private boolean released;

      RenderedBuffer(int pPointer, BufferBuilder.DrawState pDrawState) {
         this.pointer = pPointer;
         this.drawState = pDrawState;
      }

      public ByteBuffer vertexBuffer() {
         int i = this.pointer + this.drawState.vertexBufferStart();
         int j = this.pointer + this.drawState.vertexBufferEnd();
         return BufferBuilder.this.bufferSlice(i, j);
      }

      public ByteBuffer indexBuffer() {
         int i = this.pointer + this.drawState.indexBufferStart();
         int j = this.pointer + this.drawState.indexBufferEnd();
         return BufferBuilder.this.bufferSlice(i, j);
      }

      public BufferBuilder.DrawState drawState() {
         return this.drawState;
      }

      public boolean isEmpty() {
         return this.drawState.vertexCount == 0;
      }

      public void release() {
         if (this.released) {
            throw new IllegalStateException("Buffer has already been released!");
         } else {
            BufferBuilder.this.releaseRenderedBuffer();
            this.released = true;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class SortState {
      final VertexFormat.Mode mode;
      final int vertices;
      @Nullable
      final Vector3f[] sortingPoints;
      @Nullable
      final VertexSorting sorting;

      SortState(VertexFormat.Mode pMode, int pVertices, @Nullable Vector3f[] pSortingPoints, @Nullable VertexSorting pSorting) {
         this.mode = pMode;
         this.vertices = pVertices;
         this.sortingPoints = pSortingPoints;
         this.sorting = pSorting;
      }
   }

   // Forge start
   public void putBulkData(ByteBuffer buffer) {
      ensureCapacity(buffer.limit() + this.format.getVertexSize());
      this.buffer.position(this.nextElementByte);
      this.buffer.put(buffer);
      this.buffer.position(0);
      this.vertices += buffer.limit() / this.format.getVertexSize();
      this.nextElementByte += buffer.limit();
   }
}
