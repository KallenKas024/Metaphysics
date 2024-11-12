package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BufferUploader {
   @Nullable
   private static VertexBuffer lastImmediateBuffer;

   public static void reset() {
      if (lastImmediateBuffer != null) {
         invalidate();
         VertexBuffer.unbind();
      }

   }

   public static void invalidate() {
      lastImmediateBuffer = null;
   }

   public static void drawWithShader(BufferBuilder.RenderedBuffer pBuffer) {
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> {
            _drawWithShader(pBuffer);
         });
      } else {
         _drawWithShader(pBuffer);
      }

   }

   private static void _drawWithShader(BufferBuilder.RenderedBuffer pBuffer) {
      VertexBuffer vertexbuffer = upload(pBuffer);
      if (vertexbuffer != null) {
         vertexbuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
      }

   }

   public static void draw(BufferBuilder.RenderedBuffer pBuffer) {
      VertexBuffer vertexbuffer = upload(pBuffer);
      if (vertexbuffer != null) {
         vertexbuffer.draw();
      }

   }

   @Nullable
   private static VertexBuffer upload(BufferBuilder.RenderedBuffer pBuffer) {
      RenderSystem.assertOnRenderThread();
      if (pBuffer.isEmpty()) {
         pBuffer.release();
         return null;
      } else {
         VertexBuffer vertexbuffer = bindImmediateBuffer(pBuffer.drawState().format());
         vertexbuffer.upload(pBuffer);
         return vertexbuffer;
      }
   }

   private static VertexBuffer bindImmediateBuffer(VertexFormat pFormat) {
      VertexBuffer vertexbuffer = pFormat.getImmediateDrawVertexBuffer();
      bindImmediateBuffer(vertexbuffer);
      return vertexbuffer;
   }

   private static void bindImmediateBuffer(VertexBuffer pBuffer) {
      if (pBuffer != lastImmediateBuffer) {
         pBuffer.bind();
         lastImmediateBuffer = pBuffer;
      }

   }
}