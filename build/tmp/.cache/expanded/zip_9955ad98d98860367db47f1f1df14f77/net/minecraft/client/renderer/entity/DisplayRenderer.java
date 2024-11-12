package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public abstract class DisplayRenderer<T extends Display, S> extends EntityRenderer<T> {
   private final EntityRenderDispatcher entityRenderDispatcher;

   protected DisplayRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.entityRenderDispatcher = pContext.getEntityRenderDispatcher();
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(T pEntity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }

   public void render(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      Display.RenderState display$renderstate = pEntity.renderState();
      if (display$renderstate != null) {
         S s = this.getSubState(pEntity);
         if (s != null) {
            float f = pEntity.calculateInterpolationProgress(pPartialTick);
            this.shadowRadius = display$renderstate.shadowRadius().get(f);
            this.shadowStrength = display$renderstate.shadowStrength().get(f);
            int i = display$renderstate.brightnessOverride();
            int j = i != -1 ? i : pPackedLight;
            super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, j);
            pPoseStack.pushPose();
            pPoseStack.mulPose(this.calculateOrientation(display$renderstate, pEntity));
            Transformation transformation = display$renderstate.transformation().get(f);
            pPoseStack.mulPoseMatrix(transformation.getMatrix());
            pPoseStack.last().normal().rotate(transformation.getLeftRotation()).rotate(transformation.getRightRotation());
            this.renderInner(pEntity, s, pPoseStack, pBuffer, j, f);
            pPoseStack.popPose();
         }
      }
   }

   private Quaternionf calculateOrientation(Display.RenderState pRenderState, T pDisplay) {
      Camera camera = this.entityRenderDispatcher.camera;
      Quaternionf quaternionf;
      switch (pRenderState.billboardConstraints()) {
         case FIXED:
            quaternionf = pDisplay.orientation();
            break;
         case HORIZONTAL:
            quaternionf = (new Quaternionf()).rotationYXZ(-0.017453292F * pDisplay.getYRot(), -0.017453292F * camera.getXRot(), 0.0F);
            break;
         case VERTICAL:
            quaternionf = (new Quaternionf()).rotationYXZ((float)Math.PI - ((float)Math.PI / 180F) * camera.getYRot(), ((float)Math.PI / 180F) * pDisplay.getXRot(), 0.0F);
            break;
         case CENTER:
            quaternionf = (new Quaternionf()).rotationYXZ((float)Math.PI - ((float)Math.PI / 180F) * camera.getYRot(), -0.017453292F * camera.getXRot(), 0.0F);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return quaternionf;
   }

   @Nullable
   protected abstract S getSubState(T pTextDisplay);

   protected abstract void renderInner(T pTextDisplay, S pRenderState, PoseStack pPoseStack, MultiBufferSource pBuffer, int pLightmapUV, float pPartialTick);

   @OnlyIn(Dist.CLIENT)
   public static class BlockDisplayRenderer extends DisplayRenderer<Display.BlockDisplay, Display.BlockDisplay.BlockRenderState> {
      private final BlockRenderDispatcher blockRenderer;

      protected BlockDisplayRenderer(EntityRendererProvider.Context p_270283_) {
         super(p_270283_);
         this.blockRenderer = p_270283_.getBlockRenderDispatcher();
      }

      @Nullable
      protected Display.BlockDisplay.BlockRenderState getSubState(Display.BlockDisplay p_277721_) {
         return p_277721_.blockRenderState();
      }

      public void renderInner(Display.BlockDisplay p_277939_, Display.BlockDisplay.BlockRenderState p_277885_, PoseStack p_277831_, MultiBufferSource p_277554_, int p_278071_, float p_277847_) {
         this.blockRenderer.renderSingleBlock(p_277885_.blockState(), p_277831_, p_277554_, p_278071_, OverlayTexture.NO_OVERLAY);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class ItemDisplayRenderer extends DisplayRenderer<Display.ItemDisplay, Display.ItemDisplay.ItemRenderState> {
      private final ItemRenderer itemRenderer;

      protected ItemDisplayRenderer(EntityRendererProvider.Context p_270110_) {
         super(p_270110_);
         this.itemRenderer = p_270110_.getItemRenderer();
      }

      @Nullable
      protected Display.ItemDisplay.ItemRenderState getSubState(Display.ItemDisplay p_277464_) {
         return p_277464_.itemRenderState();
      }

      public void renderInner(Display.ItemDisplay p_277863_, Display.ItemDisplay.ItemRenderState p_277481_, PoseStack p_277889_, MultiBufferSource p_277509_, int p_277861_, float p_277670_) {
         p_277889_.mulPose(Axis.YP.rotation((float)Math.PI));
         this.itemRenderer.renderStatic(p_277481_.itemStack(), p_277481_.itemTransform(), p_277861_, OverlayTexture.NO_OVERLAY, p_277889_, p_277509_, p_277863_.level(), p_277863_.getId());
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class TextDisplayRenderer extends DisplayRenderer<Display.TextDisplay, Display.TextDisplay.TextRenderState> {
      private final Font font;

      protected TextDisplayRenderer(EntityRendererProvider.Context pContext) {
         super(pContext);
         this.font = pContext.getFont();
      }

      private Display.TextDisplay.CachedInfo splitLines(Component p_270823_, int p_270893_) {
         List<FormattedCharSequence> list = this.font.split(p_270823_, p_270893_);
         List<Display.TextDisplay.CachedLine> list1 = new ArrayList<>(list.size());
         int i = 0;

         for(FormattedCharSequence formattedcharsequence : list) {
            int j = this.font.width(formattedcharsequence);
            i = Math.max(i, j);
            list1.add(new Display.TextDisplay.CachedLine(formattedcharsequence, j));
         }

         return new Display.TextDisplay.CachedInfo(list1, i);
      }

      @Nullable
      protected Display.TextDisplay.TextRenderState getSubState(Display.TextDisplay pTextDisplay) {
         return pTextDisplay.textRenderState();
      }

      public void renderInner(Display.TextDisplay pTextDisplay, Display.TextDisplay.TextRenderState pRenderState, PoseStack pPoseStack, MultiBufferSource pBuffer, int pLightmapUV, float pPartialTick) {
         byte b0 = pRenderState.flags();
         boolean flag = (b0 & 2) != 0;
         boolean flag1 = (b0 & 4) != 0;
         boolean flag2 = (b0 & 1) != 0;
         Display.TextDisplay.Align display$textdisplay$align = Display.TextDisplay.getAlign(b0);
         byte b1 = (byte)pRenderState.textOpacity().get(pPartialTick);
         int i;
         if (flag1) {
            float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            i = (int)(f * 255.0F) << 24;
         } else {
            i = pRenderState.backgroundColor().get(pPartialTick);
         }

         float f2 = 0.0F;
         Matrix4f matrix4f = pPoseStack.last().pose();
         matrix4f.rotate((float)Math.PI, 0.0F, 1.0F, 0.0F);
         matrix4f.scale(-0.025F, -0.025F, -0.025F);
         Display.TextDisplay.CachedInfo display$textdisplay$cachedinfo = pTextDisplay.cacheDisplay(this::splitLines);
         int j = 9 + 1;
         int k = display$textdisplay$cachedinfo.width();
         int l = display$textdisplay$cachedinfo.lines().size() * j;
         matrix4f.translate(1.0F - (float)k / 2.0F, (float)(-l), 0.0F);
         if (i != 0) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(flag ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
            vertexconsumer.vertex(matrix4f, -1.0F, -1.0F, 0.0F).color(i).uv2(pLightmapUV).endVertex();
            vertexconsumer.vertex(matrix4f, -1.0F, (float)l, 0.0F).color(i).uv2(pLightmapUV).endVertex();
            vertexconsumer.vertex(matrix4f, (float)k, (float)l, 0.0F).color(i).uv2(pLightmapUV).endVertex();
            vertexconsumer.vertex(matrix4f, (float)k, -1.0F, 0.0F).color(i).uv2(pLightmapUV).endVertex();
         }

         for(Display.TextDisplay.CachedLine display$textdisplay$cachedline : display$textdisplay$cachedinfo.lines()) {
            float f3;
            switch (display$textdisplay$align) {
               case LEFT:
                  f3 = 0.0F;
                  break;
               case RIGHT:
                  f3 = (float)(k - display$textdisplay$cachedline.width());
                  break;
               case CENTER:
                  f3 = (float)k / 2.0F - (float)display$textdisplay$cachedline.width() / 2.0F;
                  break;
               default:
                  throw new IncompatibleClassChangeError();
            }

            float f1 = f3;
            this.font.drawInBatch(display$textdisplay$cachedline.contents(), f1, f2, b1 << 24 | 16777215, flag2, matrix4f, pBuffer, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET, 0, pLightmapUV);
            f2 += (float)j;
         }

      }
   }
}