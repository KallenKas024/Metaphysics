package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkBufferBuilderPack {
   private final Map<RenderType, BufferBuilder> builders = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((p_108845_) -> {
      return p_108845_;
   }, (p_108843_) -> {
      return new BufferBuilder(p_108843_.bufferSize());
   }));

   public BufferBuilder builder(RenderType pRenderType) {
      return this.builders.get(pRenderType);
   }

   public void clearAll() {
      this.builders.values().forEach(BufferBuilder::clear);
   }

   public void discardAll() {
      this.builders.values().forEach(BufferBuilder::discard);
   }
}