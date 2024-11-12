package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MultiBufferSource {
   static MultiBufferSource.BufferSource immediate(BufferBuilder pBuilder) {
      return immediateWithBuffers(ImmutableMap.of(), pBuilder);
   }

   static MultiBufferSource.BufferSource immediateWithBuffers(Map<RenderType, BufferBuilder> pMapBuilders, BufferBuilder pBuilder) {
      return new MultiBufferSource.BufferSource(pBuilder, pMapBuilders);
   }

   VertexConsumer getBuffer(RenderType pRenderType);

   @OnlyIn(Dist.CLIENT)
   public static class BufferSource implements MultiBufferSource {
      protected final BufferBuilder builder;
      protected final Map<RenderType, BufferBuilder> fixedBuffers;
      protected Optional<RenderType> lastState = Optional.empty();
      protected final Set<BufferBuilder> startedBuffers = Sets.newHashSet();

      protected BufferSource(BufferBuilder pBuilder, Map<RenderType, BufferBuilder> pFixedBuffers) {
         this.builder = pBuilder;
         this.fixedBuffers = pFixedBuffers;
      }

      public VertexConsumer getBuffer(RenderType pRenderType) {
         Optional<RenderType> optional = pRenderType.asOptional();
         BufferBuilder bufferbuilder = this.getBuilderRaw(pRenderType);
         if (!Objects.equals(this.lastState, optional) || !pRenderType.canConsolidateConsecutiveGeometry()) {
            if (this.lastState.isPresent()) {
               RenderType rendertype = this.lastState.get();
               if (!this.fixedBuffers.containsKey(rendertype)) {
                  this.endBatch(rendertype);
               }
            }

            if (this.startedBuffers.add(bufferbuilder)) {
               bufferbuilder.begin(pRenderType.mode(), pRenderType.format());
            }

            this.lastState = optional;
         }

         return bufferbuilder;
      }

      private BufferBuilder getBuilderRaw(RenderType pRenderType) {
         return this.fixedBuffers.getOrDefault(pRenderType, this.builder);
      }

      public void endLastBatch() {
         if (this.lastState.isPresent()) {
            RenderType rendertype = this.lastState.get();
            if (!this.fixedBuffers.containsKey(rendertype)) {
               this.endBatch(rendertype);
            }

            this.lastState = Optional.empty();
         }

      }

      public void endBatch() {
         this.lastState.ifPresent((p_109917_) -> {
            VertexConsumer vertexconsumer = this.getBuffer(p_109917_);
            if (vertexconsumer == this.builder) {
               this.endBatch(p_109917_);
            }

         });

         for(RenderType rendertype : this.fixedBuffers.keySet()) {
            this.endBatch(rendertype);
         }

      }

      public void endBatch(RenderType pRenderType) {
         BufferBuilder bufferbuilder = this.getBuilderRaw(pRenderType);
         boolean flag = Objects.equals(this.lastState, pRenderType.asOptional());
         if (flag || bufferbuilder != this.builder) {
            if (this.startedBuffers.remove(bufferbuilder)) {
               pRenderType.end(bufferbuilder, RenderSystem.getVertexSorting());
               if (flag) {
                  this.lastState = Optional.empty();
               }

            }
         }
      }
   }
}