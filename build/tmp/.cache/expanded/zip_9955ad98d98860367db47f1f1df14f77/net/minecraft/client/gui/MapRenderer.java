package net.minecraft.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class MapRenderer implements AutoCloseable {
   private static final ResourceLocation MAP_ICONS_LOCATION = new ResourceLocation("textures/map/map_icons.png");
   static final RenderType MAP_ICONS = RenderType.text(MAP_ICONS_LOCATION);
   private static final int WIDTH = 128;
   private static final int HEIGHT = 128;
   final TextureManager textureManager;
   private final Int2ObjectMap<MapRenderer.MapInstance> maps = new Int2ObjectOpenHashMap<>();

   public MapRenderer(TextureManager pTextureManager) {
      this.textureManager = pTextureManager;
   }

   public void update(int pMapId, MapItemSavedData pMapData) {
      this.getOrCreateMapInstance(pMapId, pMapData).forceUpload();
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBufferSource, int pMapId, MapItemSavedData pMapData, boolean pActive, int pPackedLight) {
      this.getOrCreateMapInstance(pMapId, pMapData).draw(pPoseStack, pBufferSource, pActive, pPackedLight);
   }

   private MapRenderer.MapInstance getOrCreateMapInstance(int pMapId, MapItemSavedData pMapData) {
      return this.maps.compute(pMapId, (p_182563_, p_182564_) -> {
         if (p_182564_ == null) {
            return new MapRenderer.MapInstance(p_182563_, pMapData);
         } else {
            p_182564_.replaceMapData(pMapData);
            return p_182564_;
         }
      });
   }

   /**
    * Clears the currently loaded maps and removes their corresponding textures
    */
   public void resetData() {
      for(MapRenderer.MapInstance maprenderer$mapinstance : this.maps.values()) {
         maprenderer$mapinstance.close();
      }

      this.maps.clear();
   }

   public void close() {
      this.resetData();
   }

   @OnlyIn(Dist.CLIENT)
   class MapInstance implements AutoCloseable {
      private MapItemSavedData data;
      private final DynamicTexture texture;
      private final RenderType renderType;
      private boolean requiresUpload = true;

      MapInstance(int pId, MapItemSavedData pData) {
         this.data = pData;
         this.texture = new DynamicTexture(128, 128, true);
         ResourceLocation resourcelocation = MapRenderer.this.textureManager.register("map/" + pId, this.texture);
         this.renderType = RenderType.text(resourcelocation);
      }

      void replaceMapData(MapItemSavedData pData) {
         boolean flag = this.data != pData;
         this.data = pData;
         this.requiresUpload |= flag;
      }

      public void forceUpload() {
         this.requiresUpload = true;
      }

      /**
       * Updates a map texture.
       */
      private void updateTexture() {
         for(int i = 0; i < 128; ++i) {
            for(int j = 0; j < 128; ++j) {
               int k = j + i * 128;
               this.texture.getPixels().setPixelRGBA(j, i, MapColor.getColorFromPackedId(this.data.colors[k]));
            }
         }

         this.texture.upload();
      }

      void draw(PoseStack pPoseStack, MultiBufferSource pBufferSource, boolean pActive, int pPackedLight) {
         if (this.requiresUpload) {
            this.updateTexture();
            this.requiresUpload = false;
         }

         int i = 0;
         int j = 0;
         float f = 0.0F;
         Matrix4f matrix4f = pPoseStack.last().pose();
         VertexConsumer vertexconsumer = pBufferSource.getBuffer(this.renderType);
         vertexconsumer.vertex(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(pPackedLight).endVertex();
         vertexconsumer.vertex(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(pPackedLight).endVertex();
         vertexconsumer.vertex(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(pPackedLight).endVertex();
         vertexconsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(pPackedLight).endVertex();
         int k = 0;

         for(MapDecoration mapdecoration : this.data.getDecorations()) {
            if (!pActive || mapdecoration.renderOnFrame()) {
               if (mapdecoration.render(k)) { k++; continue; }
               pPoseStack.pushPose();
               pPoseStack.translate(0.0F + (float)mapdecoration.getX() / 2.0F + 64.0F, 0.0F + (float)mapdecoration.getY() / 2.0F + 64.0F, -0.02F);
               pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)(mapdecoration.getRot() * 360) / 16.0F));
               pPoseStack.scale(4.0F, 4.0F, 3.0F);
               pPoseStack.translate(-0.125F, 0.125F, 0.0F);
               byte b0 = mapdecoration.getImage();
               float f1 = (float)(b0 % 16 + 0) / 16.0F;
               float f2 = (float)(b0 / 16 + 0) / 16.0F;
               float f3 = (float)(b0 % 16 + 1) / 16.0F;
               float f4 = (float)(b0 / 16 + 1) / 16.0F;
               Matrix4f matrix4f1 = pPoseStack.last().pose();
               float f5 = -0.001F;
               VertexConsumer vertexconsumer1 = pBufferSource.getBuffer(MapRenderer.MAP_ICONS);
               vertexconsumer1.vertex(matrix4f1, -1.0F, 1.0F, (float)k * -0.001F).color(255, 255, 255, 255).uv(f1, f2).uv2(pPackedLight).endVertex();
               vertexconsumer1.vertex(matrix4f1, 1.0F, 1.0F, (float)k * -0.001F).color(255, 255, 255, 255).uv(f3, f2).uv2(pPackedLight).endVertex();
               vertexconsumer1.vertex(matrix4f1, 1.0F, -1.0F, (float)k * -0.001F).color(255, 255, 255, 255).uv(f3, f4).uv2(pPackedLight).endVertex();
               vertexconsumer1.vertex(matrix4f1, -1.0F, -1.0F, (float)k * -0.001F).color(255, 255, 255, 255).uv(f1, f4).uv2(pPackedLight).endVertex();
               pPoseStack.popPose();
               if (mapdecoration.getName() != null) {
                  Font font = Minecraft.getInstance().font;
                  Component component = mapdecoration.getName();
                  float f6 = (float)font.width(component);
                  float f7 = Mth.clamp(25.0F / f6, 0.0F, 6.0F / 9.0F);
                  pPoseStack.pushPose();
                  pPoseStack.translate(0.0F + (float)mapdecoration.getX() / 2.0F + 64.0F - f6 * f7 / 2.0F, 0.0F + (float)mapdecoration.getY() / 2.0F + 64.0F + 4.0F, -0.025F);
                  pPoseStack.scale(f7, f7, 1.0F);
                  pPoseStack.translate(0.0F, 0.0F, -0.1F);
                  font.drawInBatch(component, 0.0F, 0.0F, -1, false, pPoseStack.last().pose(), pBufferSource, Font.DisplayMode.NORMAL, Integer.MIN_VALUE, pPackedLight);
                  pPoseStack.popPose();
               }

               ++k;
            }
         }

      }

      public void close() {
         this.texture.close();
      }
   }
}
