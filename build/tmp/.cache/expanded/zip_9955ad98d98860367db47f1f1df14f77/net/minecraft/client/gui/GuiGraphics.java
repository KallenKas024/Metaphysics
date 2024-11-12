package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class GuiGraphics implements net.minecraftforge.client.extensions.IForgeGuiGraphics {
   public static final float MAX_GUI_Z = 10000.0F;
   public static final float MIN_GUI_Z = -10000.0F;
   private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
   private final Minecraft minecraft;
   private final PoseStack pose;
   private final MultiBufferSource.BufferSource bufferSource;
   private final GuiGraphics.ScissorStack scissorStack = new GuiGraphics.ScissorStack();
   private boolean managed;

   private GuiGraphics(Minecraft pMinecraft, PoseStack pPose, MultiBufferSource.BufferSource pBufferSource) {
      this.minecraft = pMinecraft;
      this.pose = pPose;
      this.bufferSource = pBufferSource;
   }

   public GuiGraphics(Minecraft pMinecraft, MultiBufferSource.BufferSource pBufferSource) {
      this(pMinecraft, new PoseStack(), pBufferSource);
   }

   /** @deprecated */
   /**
    * Executes a runnable while managing the render state. The render state is flushed before and after executing the
    * runnable.
    * @param pRunnable the runnable to execute.
    */
   @Deprecated
   public void drawManaged(Runnable pRunnable) {
      this.flush();
      this.managed = true;
      pRunnable.run();
      this.managed = false;
      this.flush();
   }

   /** @deprecated */
   /**
    * Flushes the render state if it is not managed.
    * @deprecated This method is deprecated.
    */
   @Deprecated
   private void flushIfUnmanaged() {
      if (!this.managed) {
         this.flush();
      }

   }

   /** @deprecated */
   /**
    * Flushes the render state if it is managed.
    * @deprecated This method is deprecated.
    */
   @Deprecated
   private void flushIfManaged() {
      if (this.managed) {
         this.flush();
      }

   }

   /**
    * {@return returns the width of the GUI screen in pixels}
    */
   public int guiWidth() {
      return this.minecraft.getWindow().getGuiScaledWidth();
   }

   /**
    * {@return returns the height of the GUI screen in pixels}
    */
   public int guiHeight() {
      return this.minecraft.getWindow().getGuiScaledHeight();
   }

   /**
    * {@return returns the PoseStack used for transformations and rendering.}
    */
   public PoseStack pose() {
      return this.pose;
   }

   /**
    * {@return returns the buffer source for rendering.}
    */
   public MultiBufferSource.BufferSource bufferSource() {
      return this.bufferSource;
   }

   /**
    * Flushes the render state, ending the current batch and enabling depth testing.
    */
   public void flush() {
      RenderSystem.disableDepthTest();
      this.bufferSource.endBatch();
      RenderSystem.enableDepthTest();
   }

   /**
    * Draws a horizontal line from minX to maxX at the specified y-coordinate with the given color.
    * @param pMinX the x-coordinate of the start point.
    * @param pMaxX the x-coordinate of the end point.
    * @param pY the y-coordinate of the line.
    * @param pColor the color of the line.
    */
   public void hLine(int pMinX, int pMaxX, int pY, int pColor) {
      this.hLine(RenderType.gui(), pMinX, pMaxX, pY, pColor);
   }

   /**
    * Draws a horizontal line from minX to maxX at the specified y-coordinate with the given color using the specified
    * render type.
    * @param pRenderType the render type to use.
    * @param pMinX the x-coordinate of the start point.
    * @param pMaxX the x-coordinate of the end point.
    * @param pY the y-coordinate of the line.
    * @param pColor the color of the line.
    */
   public void hLine(RenderType pRenderType, int pMinX, int pMaxX, int pY, int pColor) {
      if (pMaxX < pMinX) {
         int i = pMinX;
         pMinX = pMaxX;
         pMaxX = i;
      }

      this.fill(pRenderType, pMinX, pY, pMaxX + 1, pY + 1, pColor);
   }

   /**
    * Draws a vertical line from minY to maxY at the specified x-coordinate with the given color.
    * @param pX the x-coordinate of the line.
    * @param pMinY the y-coordinate of the start point.
    * @param pMaxY the y-coordinate of the end point.
    * @param pColor the color of the line.
    */
   public void vLine(int pX, int pMinY, int pMaxY, int pColor) {
      this.vLine(RenderType.gui(), pX, pMinY, pMaxY, pColor);
   }

   /**
    * Draws a vertical line from minY to maxY at the specified x-coordinate with the given color using the specified
    * render type.
    * @param pRenderType the render type to use.
    * @param pX the x-coordinate of the line.
    * @param pMinY the y-coordinate of the start point.
    * @param pMaxY the y-coordinate of the end point.
    * @param pColor the color of the line.
    */
   public void vLine(RenderType pRenderType, int pX, int pMinY, int pMaxY, int pColor) {
      if (pMaxY < pMinY) {
         int i = pMinY;
         pMinY = pMaxY;
         pMaxY = i;
      }

      this.fill(pRenderType, pX, pMinY + 1, pX + 1, pMaxY, pColor);
   }

   /**
    * Enables scissoring with the specified screen coordinates.
    * @param pMinX the minimum x-coordinate of the scissor region.
    * @param pMinY the minimum y-coordinate of the scissor region.
    * @param pMaxX the maximum x-coordinate of the scissor region.
    * @param pMaxY the maximum y-coordinate of the scissor region.
    */
   public void enableScissor(int pMinX, int pMinY, int pMaxX, int pMaxY) {
      this.applyScissor(this.scissorStack.push(new ScreenRectangle(pMinX, pMinY, pMaxX - pMinX, pMaxY - pMinY)));
   }

   /**
    * Disables scissoring.
    */
   public void disableScissor() {
      this.applyScissor(this.scissorStack.pop());
   }

   /**
    * Applies scissoring based on the provided screen rectangle.
    * @param pRectangle the screen rectangle to apply scissoring with. Can be null to disable scissoring.
    */
   private void applyScissor(@Nullable ScreenRectangle pRectangle) {
      this.flushIfManaged();
      if (pRectangle != null) {
         Window window = Minecraft.getInstance().getWindow();
         int i = window.getHeight();
         double d0 = window.getGuiScale();
         double d1 = (double)pRectangle.left() * d0;
         double d2 = (double)i - (double)pRectangle.bottom() * d0;
         double d3 = (double)pRectangle.width() * d0;
         double d4 = (double)pRectangle.height() * d0;
         RenderSystem.enableScissor((int)d1, (int)d2, Math.max(0, (int)d3), Math.max(0, (int)d4));
      } else {
         RenderSystem.disableScissor();
      }

   }

   /**
    * Sets the current rendering color.
    * @param pRed the red component of the color.
    * @param pGreen the green component of the color.
    * @param pBlue the blue component of the color.
    * @param pAlpha the alpha component of the color.
    */
   public void setColor(float pRed, float pGreen, float pBlue, float pAlpha) {
      this.flushIfManaged();
      RenderSystem.setShaderColor(pRed, pGreen, pBlue, pAlpha);
   }

   /**
    * Fills a rectangle with the specified color using the given coordinates as the boundaries.
    * @param pMinX the minimum x-coordinate of the rectangle.
    * @param pMinY the minimum y-coordinate of the rectangle.
    * @param pMaxX the maximum x-coordinate of the rectangle.
    * @param pMaxY the maximum y-coordinate of the rectangle.
    * @param pColor the color to fill the rectangle with.
    */
   public void fill(int pMinX, int pMinY, int pMaxX, int pMaxY, int pColor) {
      this.fill(pMinX, pMinY, pMaxX, pMaxY, 0, pColor);
   }

   /**
    * Fills a rectangle with the specified color and z-level using the given coordinates as the boundaries.
    * @param pMinX the minimum x-coordinate of the rectangle.
    * @param pMinY the minimum y-coordinate of the rectangle.
    * @param pMaxX the maximum x-coordinate of the rectangle.
    * @param pMaxY the maximum y-coordinate of the rectangle.
    * @param pZ the z-level of the rectangle.
    * @param pColor the color to fill the rectangle with.
    */
   public void fill(int pMinX, int pMinY, int pMaxX, int pMaxY, int pZ, int pColor) {
      this.fill(RenderType.gui(), pMinX, pMinY, pMaxX, pMaxY, pZ, pColor);
   }

   /**
    * Fills a rectangle with the specified color using the given render type and coordinates as the boundaries.
    * @param pRenderType the render type to use.
    * @param pMinX the minimum x-coordinate of the rectangle.
    * @param pMinY the minimum y-coordinate of the rectangle.
    * @param pMaxX the maximum x-coordinate of the rectangle.
    * @param pMaxY the maximum y-coordinate of the rectangle.
    * @param pColor the color to fill the rectangle with.
    */
   public void fill(RenderType pRenderType, int pMinX, int pMinY, int pMaxX, int pMaxY, int pColor) {
      this.fill(pRenderType, pMinX, pMinY, pMaxX, pMaxY, 0, pColor);
   }

   /**
    * Fills a rectangle with the specified color and z-level using the given render type and coordinates as the
    * boundaries.
    * @param pRenderType the render type to use.
    * @param pMinX the minimum x-coordinate of the rectangle.
    * @param pMinY the minimum y-coordinate of the rectangle.
    * @param pMaxX the maximum x-coordinate of the rectangle.
    * @param pMaxY the maximum y-coordinate of the rectangle.
    * @param pZ the z-level of the rectangle.
    * @param pColor the color to fill the rectangle with.
    */
   public void fill(RenderType pRenderType, int pMinX, int pMinY, int pMaxX, int pMaxY, int pZ, int pColor) {
      Matrix4f matrix4f = this.pose.last().pose();
      if (pMinX < pMaxX) {
         int i = pMinX;
         pMinX = pMaxX;
         pMaxX = i;
      }

      if (pMinY < pMaxY) {
         int j = pMinY;
         pMinY = pMaxY;
         pMaxY = j;
      }

      float f3 = (float)FastColor.ARGB32.alpha(pColor) / 255.0F;
      float f = (float)FastColor.ARGB32.red(pColor) / 255.0F;
      float f1 = (float)FastColor.ARGB32.green(pColor) / 255.0F;
      float f2 = (float)FastColor.ARGB32.blue(pColor) / 255.0F;
      VertexConsumer vertexconsumer = this.bufferSource.getBuffer(pRenderType);
      vertexconsumer.vertex(matrix4f, (float)pMinX, (float)pMinY, (float)pZ).color(f, f1, f2, f3).endVertex();
      vertexconsumer.vertex(matrix4f, (float)pMinX, (float)pMaxY, (float)pZ).color(f, f1, f2, f3).endVertex();
      vertexconsumer.vertex(matrix4f, (float)pMaxX, (float)pMaxY, (float)pZ).color(f, f1, f2, f3).endVertex();
      vertexconsumer.vertex(matrix4f, (float)pMaxX, (float)pMinY, (float)pZ).color(f, f1, f2, f3).endVertex();
      this.flushIfUnmanaged();
   }

   /**
    * Fills a rectangle with a gradient color from colorFrom to colorTo using the given coordinates as the boundaries.
    * @param pX1 the x-coordinate of the first corner of the rectangle.
    * @param pY1 the y-coordinate of the first corner of the rectangle.
    * @param pX2 the x-coordinate of the second corner of the rectangle.
    * @param pY2 the y-coordinate of the second corner of the rectangle.
    * @param pColorFrom the starting color of the gradient.
    * @param pColorTo the ending color of the gradient.
    */
   public void fillGradient(int pX1, int pY1, int pX2, int pY2, int pColorFrom, int pColorTo) {
      this.fillGradient(pX1, pY1, pX2, pY2, 0, pColorFrom, pColorTo);
   }

   /**
    * Fills a rectangle with a gradient color from colorFrom to colorTo at the specified z-level using the given
    * coordinates as the boundaries.
    * @param pX1 the x-coordinate of the first corner of the rectangle.
    * @param pY1 the y-coordinate of the first corner of the rectangle.
    * @param pX2 the x-coordinate of the second corner of the rectangle.
    * @param pY2 the y-coordinate of the second corner of the rectangle.
    * @param pZ the z-level of the rectangle.
    * @param pColorFrom the starting color of the gradient.
    * @param pColorTo the ending color of the gradient.
    */
   public void fillGradient(int pX1, int pY1, int pX2, int pY2, int pZ, int pColorFrom, int pColorTo) {
      this.fillGradient(RenderType.gui(), pX1, pY1, pX2, pY2, pColorFrom, pColorTo, pZ);
   }

   /**
    * Fills a rectangle with a gradient color from colorFrom to colorTo at the specified z-level using the given render
    * type and coordinates as the boundaries.
    * @param pRenderType the render type to use.
    * @param pX1 the x-coordinate of the first corner of the rectangle.
    * @param pY1 the y-coordinate of the first corner of the rectangle.
    * @param pX2 the x-coordinate of the second corner of the rectangle.
    * @param pY2 the y-coordinate of the second corner of the rectangle.
    * @param pColorFrom the starting color of the gradient.
    * @param pColorTo the ending color of the gradient.
    * @param pZ the z-level of the rectangle.
    */
   public void fillGradient(RenderType pRenderType, int pX1, int pY1, int pX2, int pY2, int pColorFrom, int pColorTo, int pZ) {
      VertexConsumer vertexconsumer = this.bufferSource.getBuffer(pRenderType);
      this.fillGradient(vertexconsumer, pX1, pY1, pX2, pY2, pZ, pColorFrom, pColorTo);
      this.flushIfUnmanaged();
   }

   /**
    * The core `fillGradient` method.
    * <p>
    * Fills a rectangle with a gradient color from colorFrom to colorTo at the specified z-level using the given render
    * type and coordinates as the boundaries.
    * @param pConsumer the {@linkplain VertexConsumer} object for drawing the vertices on screen.
    * @param pX1 the x-coordinate of the first corner of the rectangle.
    * @param pY1 the y-coordinate of the first corner of the rectangle.
    * @param pX2 the x-coordinate of the second corner of the rectangle.
    * @param pY2 the y-coordinate of the second corner of the rectangle.
    * @param pZ the z-level of the rectangle.
    * @param pColorFrom the starting color of the gradient.
    * @param pColorTo the ending color of the gradient.
    */
   private void fillGradient(VertexConsumer pConsumer, int pX1, int pY1, int pX2, int pY2, int pZ, int pColorFrom, int pColorTo) {
      float f = (float)FastColor.ARGB32.alpha(pColorFrom) / 255.0F;
      float f1 = (float)FastColor.ARGB32.red(pColorFrom) / 255.0F;
      float f2 = (float)FastColor.ARGB32.green(pColorFrom) / 255.0F;
      float f3 = (float)FastColor.ARGB32.blue(pColorFrom) / 255.0F;
      float f4 = (float)FastColor.ARGB32.alpha(pColorTo) / 255.0F;
      float f5 = (float)FastColor.ARGB32.red(pColorTo) / 255.0F;
      float f6 = (float)FastColor.ARGB32.green(pColorTo) / 255.0F;
      float f7 = (float)FastColor.ARGB32.blue(pColorTo) / 255.0F;
      Matrix4f matrix4f = this.pose.last().pose();
      pConsumer.vertex(matrix4f, (float)pX1, (float)pY1, (float)pZ).color(f1, f2, f3, f).endVertex();
      pConsumer.vertex(matrix4f, (float)pX1, (float)pY2, (float)pZ).color(f5, f6, f7, f4).endVertex();
      pConsumer.vertex(matrix4f, (float)pX2, (float)pY2, (float)pZ).color(f5, f6, f7, f4).endVertex();
      pConsumer.vertex(matrix4f, (float)pX2, (float)pY1, (float)pZ).color(f1, f2, f3, f).endVertex();
   }

   /**
    * Draws a centered string at the specified coordinates using the given font, text, and color.
    * @param pFont the font to use for rendering.
    * @param pText the text to draw.
    * @param pX the x-coordinate of the center of the string.
    * @param pY the y-coordinate of the string.
    * @param pColor the color of the string.
    */
   public void drawCenteredString(Font pFont, String pText, int pX, int pY, int pColor) {
      this.drawString(pFont, pText, pX - pFont.width(pText) / 2, pY, pColor);
   }

   /**
    * Draws a centered string at the specified coordinates using the given font, text component, and color.
    * @param pFont the font to use for rendering.
    * @param pText the text component to draw.
    * @param pX the x-coordinate of the center of the string.
    * @param pY the y-coordinate of the string.
    * @param pColor the color of the string.
    */
   public void drawCenteredString(Font pFont, Component pText, int pX, int pY, int pColor) {
      FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
      this.drawString(pFont, formattedcharsequence, pX - pFont.width(formattedcharsequence) / 2, pY, pColor);
   }

   /**
    * Draws a centered string at the specified coordinates using the given font, formatted character sequence, and
    * color.
    * @param pFont the font to use for rendering.
    * @param pText the formatted character sequence to draw.
    * @param pX the x-coordinate of the center of the string.
    * @param pY the y-coordinate of the string.
    * @param pColor the color of the string.
    */
   public void drawCenteredString(Font pFont, FormattedCharSequence pText, int pX, int pY, int pColor) {
      this.drawString(pFont, pText, pX - pFont.width(pText) / 2, pY, pColor);
   }

   /**
    * Draws a string at the specified coordinates using the given font, text, and color. Returns the width of the drawn
    * string.
    * <p>
    * @return the width of the drawn string.
    * @param pFont the font to use for rendering.
    * @param pText the text to draw.
    * @param pX the x-coordinate of the string.
    * @param pY the y-coordinate of the string.
    * @param pColor the color of the string.
    */
   public int drawString(Font pFont, @Nullable String pText, int pX, int pY, int pColor) {
      return this.drawString(pFont, pText, pX, pY, pColor, true);
   }

   /**
    * Draws a string at the specified coordinates using the given font, text, color, and drop shadow. Returns the width
    * of the drawn string.
    * <p>
    * @return the width of the drawn string.
    * @param pFont the font to use for rendering.
    * @param pText the text to draw.
    * @param pX the x-coordinate of the string.
    * @param pY the y-coordinate of the string.
    * @param pColor the color of the string.
    * @param pDropShadow whether to apply a drop shadow to the string.
    */
   public int drawString(Font pFont, @Nullable String pText, int pX, int pY, int pColor, boolean pDropShadow) {
      return this.drawString(pFont, pText, (float)pX, (float)pY, pColor, pDropShadow);
   }

   // Forge: Add float variant for x,y coordinates
   public int drawString(Font pFont, @Nullable String pText, float pX, float pY, int pColor, boolean pDropShadow) {
      if (pText == null) {
         return 0;
      } else {
         int i = pFont.drawInBatch(pText, (float)pX, (float)pY, pColor, pDropShadow, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, pFont.isBidirectional());
         this.flushIfUnmanaged();
         return i;
      }
   }

   /**
    * Draws a formatted character sequence at the specified coordinates using the given font, text, and color. Returns
    * the width of the drawn string.
    * <p>
    * @return the width of the drawn string.
    * @param pFont the font to use for rendering.
    * @param pText the formatted character sequence to draw.
    * @param pX the x-coordinate of the string.
    * @param pY the y-coordinate of the string.
    * @param pColor the color of the string
    */
   public int drawString(Font pFont, FormattedCharSequence pText, int pX, int pY, int pColor) {
      return this.drawString(pFont, pText, pX, pY, pColor, true);
   }

   /**
    * Draws a formatted character sequence at the specified coordinates using the given font, text, color, and drop
    * shadow. Returns the width of the drawn string.
    * <p>
    * @return returns the width of the drawn string.
    * @param pFont the font to use for rendering.
    * @param pText the formatted character sequence to draw.
    * @param pX the x-coordinate of the string.
    * @param pY the y-coordinate of the string.
    * @param pColor the color of the string.
    * @param pDropShadow whether to apply a drop shadow to the string.
    */
   public int drawString(Font pFont, FormattedCharSequence pText, int pX, int pY, int pColor, boolean pDropShadow) {
      return this.drawString(pFont, pText, (float)pX, (float)pY, pColor, pDropShadow);
   }

   // Forge: Add float variant for x,y coordinates
   public int drawString(Font pFont, FormattedCharSequence pText, float pX, float pY, int pColor, boolean pDropShadow) {
      int i = pFont.drawInBatch(pText, (float)pX, (float)pY, pColor, pDropShadow, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
      this.flushIfUnmanaged();
      return i;
   }

   /**
    * Draws a component's visual order text at the specified coordinates using the given font, text component, and
    * color.
    * <p>
    * @return the width of the drawn string.
    * @param pFont the font to use for rendering.
    * @param pText the text component to draw.
    * @param pX the x-coordinate of the string.
    * @param pY the y-coordinate of the string.
    * @param pColor the color of the string.
    */
   public int drawString(Font pFont, Component pText, int pX, int pY, int pColor) {
      return this.drawString(pFont, pText, pX, pY, pColor, true);
   }

   /**
    * Draws a component's visual order text at the specified coordinates using the given font, text component, color,
    * and drop shadow.
    * <p>
    * @return the width of the drawn string.
    * @param pFont the font to use for rendering.
    * @param pText the text component to draw.
    * @param pX the x-coordinate of the string.
    * @param pY the y-coordinate of the string.
    * @param pColor the color of the string.
    * @param pDropShadow whether to apply a drop shadow to the string.
    */
   public int drawString(Font pFont, Component pText, int pX, int pY, int pColor, boolean pDropShadow) {
      return this.drawString(pFont, pText.getVisualOrderText(), pX, pY, pColor, pDropShadow);
   }

   /**
    * Draws a formatted text with word wrapping at the specified coordinates using the given font, text, line width, and
    * color.
    * @param pFont the font to use for rendering.
    * @param pText the formatted text to draw.
    * @param pX the x-coordinate of the starting position.
    * @param pY the y-coordinate of the starting position.
    * @param pLineWidth the maximum width of each line before wrapping.
    * @param pColor the color of the text.
    */
   public void drawWordWrap(Font pFont, FormattedText pText, int pX, int pY, int pLineWidth, int pColor) {
      for(FormattedCharSequence formattedcharsequence : pFont.split(pText, pLineWidth)) {
         this.drawString(pFont, formattedcharsequence, pX, pY, pColor, false);
         pY += 9;
      }

   }

   /**
    * Blits a portion of the specified texture atlas sprite onto the screen at the given coordinates.
    * @param pX the x-coordinate of the blit position.
    * @param pY the y-coordinate of the blit position.
    * @param pBlitOffset the z-level offset for rendering order.
    * @param pWidth the width of the blitted portion.
    * @param pHeight the height of the blitted portion.
    * @param pSprite the texture atlas sprite to blit.
    */
   public void blit(int pX, int pY, int pBlitOffset, int pWidth, int pHeight, TextureAtlasSprite pSprite) {
      this.innerBlit(pSprite.atlasLocation(), pX, pX + pWidth, pY, pY + pHeight, pBlitOffset, pSprite.getU0(), pSprite.getU1(), pSprite.getV0(), pSprite.getV1());
   }

   /**
    * Blits a portion of the specified texture atlas sprite onto the screen at the given coordinates with a color tint.
    * @param pX the x-coordinate of the blit position.
    * @param pY the y-coordinate of the blit position.
    * @param pBlitOffset the z-level offset for rendering order.
    * @param pWidth the width of the blitted portion.
    * @param pHeight the height of the blitted portion.
    * @param pSprite the texture atlas sprite to blit.
    * @param pRed the red component of the color tint.
    * @param pGreen the green component of the color tint.
    * @param pBlue the blue component of the color tint.
    * @param pAlpha the alpha component of the color tint.
    */
   public void blit(int pX, int pY, int pBlitOffset, int pWidth, int pHeight, TextureAtlasSprite pSprite, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.innerBlit(pSprite.atlasLocation(), pX, pX + pWidth, pY, pY + pHeight, pBlitOffset, pSprite.getU0(), pSprite.getU1(), pSprite.getV0(), pSprite.getV1(), pRed, pGreen, pBlue, pAlpha);
   }

   /**
    * Renders an outline rectangle on the screen with the specified color.
    * @param pX the x-coordinate of the top-left corner of the rectangle.
    * @param pY the y-coordinate of the top-left corner of the rectangle.
    * @param pWidth the width of the blitted portion.
    * @param pHeight the height of the rectangle.
    * @param pColor the color of the outline.
    */
   public void renderOutline(int pX, int pY, int pWidth, int pHeight, int pColor) {
      this.fill(pX, pY, pX + pWidth, pY + 1, pColor);
      this.fill(pX, pY + pHeight - 1, pX + pWidth, pY + pHeight, pColor);
      this.fill(pX, pY + 1, pX + 1, pY + pHeight - 1, pColor);
      this.fill(pX + pWidth - 1, pY + 1, pX + pWidth, pY + pHeight - 1, pColor);
   }

   /**
    * Blits a portion of the texture specified by the atlas location onto the screen at the given coordinates.
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX the x-coordinate of the blit position.
    * @param pY the y-coordinate of the blit position.
    * @param pUOffset the horizontal texture coordinate offset.
    * @param pVOffset the vertical texture coordinate offset.
    * @param pUWidth the width of the blitted portion in texture coordinates.
    * @param pVHeight the height of the blitted portion in texture coordinates.
    */
   public void blit(ResourceLocation pAtlasLocation, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
      this.blit(pAtlasLocation, pX, pY, 0, (float)pUOffset, (float)pVOffset, pUWidth, pVHeight, 256, 256);
   }

   /**
    * Blits a portion of the texture specified by the atlas location onto the screen at the given coordinates with a
    * blit offset and texture coordinates.
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX the x-coordinate of the blit position.
    * @param pY the y-coordinate of the blit position.
    * @param pBlitOffset the z-level offset for rendering order.
    * @param pUOffset the horizontal texture coordinate offset.
    * @param pVOffset the vertical texture coordinate offset.
    * @param pUWidth the width of the blitted portion in texture coordinates.
    * @param pVHeight the height of the blitted portion in texture coordinates.
    * @param pTextureWidth the width of the texture.
    * @param pTextureHeight the height of the texture.
    */
   public void blit(ResourceLocation pAtlasLocation, int pX, int pY, int pBlitOffset, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureWidth, int pTextureHeight) {
      this.blit(pAtlasLocation, pX, pX + pUWidth, pY, pY + pVHeight, pBlitOffset, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
   }

   /**
    * Blits a portion of the texture specified by the atlas location onto the screen at the given position and
    * dimensions with texture coordinates.
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX the x-coordinate of the top-left corner of the blit position.
    * @param pY the y-coordinate of the top-left corner of the blit position.
    * @param pWidth the width of the blitted portion.
    * @param pHeight the height of the blitted portion.
    * @param pUOffset the horizontal texture coordinate offset.
    * @param pVOffset the vertical texture coordinate offset.
    * @param pUWidth the width of the blitted portion in texture coordinates.
    * @param pVHeight the height of the blitted portion in texture coordinates.
    * @param pTextureWidth the width of the texture.
    * @param pTextureHeight the height of the texture.
    */
   public void blit(ResourceLocation pAtlasLocation, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureWidth, int pTextureHeight) {
      this.blit(pAtlasLocation, pX, pX + pWidth, pY, pY + pHeight, 0, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
   }

   /**
    * Blits a portion of the texture specified by the atlas location onto the screen at the given position and
    * dimensions with texture coordinates.
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX the x-coordinate of the top-left corner of the blit position.
    * @param pY the y-coordinate of the top-left corner of the blit position.
    * @param pUOffset the horizontal texture coordinate offset.
    * @param pVOffset the vertical texture coordinate offset.
    * @param pWidth the width of the blitted portion.
    * @param pHeight the height of the blitted portion.
    * @param pTextureWidth the width of the texture.
    * @param pTextureHeight the height of the texture.
    */
   public void blit(ResourceLocation pAtlasLocation, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight) {
      this.blit(pAtlasLocation, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight, pTextureWidth, pTextureHeight);
   }

   /**
    * Performs the inner blit operation for rendering a texture with the specified coordinates and texture coordinates.
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX1 the x-coordinate of the first corner of the blit position.
    * @param pX2 the x-coordinate of the second corner of the blit position.
    * @param pY1 the y-coordinate of the first corner of the blit position.
    * @param pY2 the y-coordinate of the second corner of the blit position.
    * @param pBlitOffset the z-level offset for rendering order.
    * @param pUWidth the width of the blitted portion in texture coordinates.
    * @param pVHeight the height of the blitted portion in texture coordinates.
    * @param pUOffset the horizontal texture coordinate offset.
    * @param pVOffset the vertical texture coordinate offset.
    * @param pTextureWidth the width of the texture.
    * @param pTextureHeight the height of the texture.
    */
   void blit(ResourceLocation pAtlasLocation, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, int pUWidth, int pVHeight, float pUOffset, float pVOffset, int pTextureWidth, int pTextureHeight) {
      this.innerBlit(pAtlasLocation, pX1, pX2, pY1, pY2, pBlitOffset, (pUOffset + 0.0F) / (float)pTextureWidth, (pUOffset + (float)pUWidth) / (float)pTextureWidth, (pVOffset + 0.0F) / (float)pTextureHeight, (pVOffset + (float)pVHeight) / (float)pTextureHeight);
   }

   /**
    * Performs the inner blit operation for rendering a texture with the specified coordinates and texture coordinates
    * without color tinting.
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX1 the x-coordinate of the first corner of the blit position.
    * @param pX2 the x-coordinate of the second corner of the blit position.
    * @param pY1 the y-coordinate of the first corner of the blit position.
    * @param pY2 the y-coordinate of the second corner of the blit position.
    * @param pBlitOffset the z-level offset for rendering order.
    * @param pMinU the minimum horizontal texture coordinate.
    * @param pMaxU the maximum horizontal texture coordinate.
    * @param pMinV the minimum vertical texture coordinate.
    * @param pMaxV the maximum vertical texture coordinate.
    */
   void innerBlit(ResourceLocation pAtlasLocation, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV) {
      RenderSystem.setShaderTexture(0, pAtlasLocation);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      Matrix4f matrix4f = this.pose.last().pose();
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      bufferbuilder.vertex(matrix4f, (float)pX1, (float)pY1, (float)pBlitOffset).uv(pMinU, pMinV).endVertex();
      bufferbuilder.vertex(matrix4f, (float)pX1, (float)pY2, (float)pBlitOffset).uv(pMinU, pMaxV).endVertex();
      bufferbuilder.vertex(matrix4f, (float)pX2, (float)pY2, (float)pBlitOffset).uv(pMaxU, pMaxV).endVertex();
      bufferbuilder.vertex(matrix4f, (float)pX2, (float)pY1, (float)pBlitOffset).uv(pMaxU, pMinV).endVertex();
      BufferUploader.drawWithShader(bufferbuilder.end());
   }

   /**
    * Performs the inner blit operation for rendering a texture with the specified coordinates, texture coordinates, and
    * color tint.
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX1 the x-coordinate of the first corner of the blit position.
    * @param pX2 the x-coordinate of the second corner of the blit position.
    * @param pY1 the y-coordinate of the first corner of the blit position.
    * @param pY2 the y-coordinate of the second corner of the blit position.
    * @param pBlitOffset the z-level offset for rendering order.
    * @param pMinU the minimum horizontal texture coordinate.
    * @param pMaxU the maximum horizontal texture coordinate.
    * @param pMinV the minimum vertical texture coordinate.
    * @param pMaxV the maximum vertical texture coordinate.
    * @param pRed the red component of the color tint.
    * @param pGreen the green component of the color tint.
    * @param pBlue the blue component of the color tint.
    * @param pAlpha the alpha component of the color tint.
    */
   void innerBlit(ResourceLocation pAtlasLocation, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV, float pRed, float pGreen, float pBlue, float pAlpha) {
      RenderSystem.setShaderTexture(0, pAtlasLocation);
      RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
      RenderSystem.enableBlend();
      Matrix4f matrix4f = this.pose.last().pose();
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
      bufferbuilder.vertex(matrix4f, (float)pX1, (float)pY1, (float)pBlitOffset).color(pRed, pGreen, pBlue, pAlpha).uv(pMinU, pMinV).endVertex();
      bufferbuilder.vertex(matrix4f, (float)pX1, (float)pY2, (float)pBlitOffset).color(pRed, pGreen, pBlue, pAlpha).uv(pMinU, pMaxV).endVertex();
      bufferbuilder.vertex(matrix4f, (float)pX2, (float)pY2, (float)pBlitOffset).color(pRed, pGreen, pBlue, pAlpha).uv(pMaxU, pMaxV).endVertex();
      bufferbuilder.vertex(matrix4f, (float)pX2, (float)pY1, (float)pBlitOffset).color(pRed, pGreen, pBlue, pAlpha).uv(pMaxU, pMinV).endVertex();
      BufferUploader.drawWithShader(bufferbuilder.end());
      RenderSystem.disableBlend();
   }

   /**
    * Blits a nine-sliced portion of the texture specified by the atlas location onto the screen.
    * @see #blitNineSliced(ResourceLocation, int, int, int, int, int, int, int, int, int, int, int, int)
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX the x-coordinate of the top-left corner of the target position.
    * @param pY the y-coordinate of the top-left corner of the target position.
    * @param pWidth the width of the target portion.
    * @param pHeight the height of the target portion.
    * @param pSliceSize the size of the corner slices.
    * @param pUOffset the x-coordinate of the top-left corner of the source position.
    * @param pVOffset the y-coordinate of the top-left corner of the source position.
    * @param pTextureWidth the width of the source texture.
    * @param pTextureHeight the height of the source texture.
    */
   public void blitNineSliced(ResourceLocation pAtlasLocation, int pX, int pY, int pWidth, int pHeight, int pSliceSize, int pUOffset, int pVOffset, int pTextureWidth, int pTextureHeight) {
      this.blitNineSliced(pAtlasLocation, pX, pY, pWidth, pHeight, pSliceSize, pSliceSize, pSliceSize, pSliceSize, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
   }

   /**
    * Blits a nine-sliced portion of the texture specified by the atlas location onto the screen.
    * @see #blitNineSliced(ResourceLocation, int, int, int, int, int, int, int, int, int, int, int, int)
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX the x-coordinate of the top-left corner of the target position.
    * @param pY the y-coordinate of the top-left corner of the target position.
    * @param pWidth the width of the target portion.
    * @param pHeight the height of the target portion.
    * @param pSliceWidth the width of the corner slices.
    * @param pSliceHeight the height of the corner slices.
    * @param pUWidth the width of the source texture.
    * @param pVHeight the height of the source texture.
    * @param pTextureX the x-coordinate of the top-left corner of the source position.
    * @param pTextureY the y-coordinate of the top-left corner of the source position.
    */
   public void blitNineSliced(ResourceLocation pAtlasLocation, int pX, int pY, int pWidth, int pHeight, int pSliceWidth, int pSliceHeight, int pUWidth, int pVHeight, int pTextureX, int pTextureY) {
      this.blitNineSliced(pAtlasLocation, pX, pY, pWidth, pHeight, pSliceWidth, pSliceHeight, pSliceWidth, pSliceHeight, pUWidth, pVHeight, pTextureX, pTextureY);
   }

   /**
    * Blits a nine-sliced portion of the texture specified by the atlas location onto the screen.
    * <p>
    * 9-slicing is a 2D technique which allows you to reuse an image at various sizes without needing to prepare
    * multiple assets.
    * It involves splitting the image into nine portions, so that when you re-size the sprite, the different portions
    * scale or tile (that is, repeat in a grid formation) in different ways to keep the sprite in proportion.
    * <p>
    * This is useful when creating patterns or textures, such as walls or floors in a 2D environment.
    * <p>
    * This method is mainly used for GUI and Button rendering, to create scaleable/tileable assets.
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX the x-coordinate of the top-left corner of the target position.
    * @param pY the y-coordinate of the top-left corner of the target position.
    * @param pWidth the width of the target portion.
    * @param pHeight the height of the target portion.
    * @param pLeftSliceWidth the width of the left column's slices.
    * @param pTopSliceHeight the height of the top row's slices.
    * @param pRightSliceWidth the width of the right column's slices.
    * @param pBottomSliceHeight the height of the bottom row's slices.
    * @param pUWidth the width of the source texture.
    * @param pVHeight the height of the source texture.
    * @param pTextureX the x-coordinate of the top-left corner of the source.
    * @param pTextureY the y-coordinate of the top-left corner of the source.
    */
   public void blitNineSliced(ResourceLocation pAtlasLocation, int pX, int pY, int pWidth, int pHeight, int pLeftSliceWidth, int pTopSliceHeight, int pRightSliceWidth, int pBottomSliceHeight, int pUWidth, int pVHeight, int pTextureX, int pTextureY) {
      pLeftSliceWidth = Math.min(pLeftSliceWidth, pWidth / 2);
      pRightSliceWidth = Math.min(pRightSliceWidth, pWidth / 2);
      pTopSliceHeight = Math.min(pTopSliceHeight, pHeight / 2);
      pBottomSliceHeight = Math.min(pBottomSliceHeight, pHeight / 2);
      if (pWidth == pUWidth && pHeight == pVHeight) {
         this.blit(pAtlasLocation, pX, pY, pTextureX, pTextureY, pWidth, pHeight);
      } else if (pHeight == pVHeight) {
         this.blit(pAtlasLocation, pX, pY, pTextureX, pTextureY, pLeftSliceWidth, pHeight);
         this.blitRepeating(pAtlasLocation, pX + pLeftSliceWidth, pY, pWidth - pRightSliceWidth - pLeftSliceWidth, pHeight, pTextureX + pLeftSliceWidth, pTextureY, pUWidth - pRightSliceWidth - pLeftSliceWidth, pVHeight);
         this.blit(pAtlasLocation, pX + pWidth - pRightSliceWidth, pY, pTextureX + pUWidth - pRightSliceWidth, pTextureY, pRightSliceWidth, pHeight);
      } else if (pWidth == pUWidth) {
         this.blit(pAtlasLocation, pX, pY, pTextureX, pTextureY, pWidth, pTopSliceHeight);
         this.blitRepeating(pAtlasLocation, pX, pY + pTopSliceHeight, pWidth, pHeight - pBottomSliceHeight - pTopSliceHeight, pTextureX, pTextureY + pTopSliceHeight, pUWidth, pVHeight - pBottomSliceHeight - pTopSliceHeight);
         this.blit(pAtlasLocation, pX, pY + pHeight - pBottomSliceHeight, pTextureX, pTextureY + pVHeight - pBottomSliceHeight, pWidth, pBottomSliceHeight);
      } else {
         this.blit(pAtlasLocation, pX, pY, pTextureX, pTextureY, pLeftSliceWidth, pTopSliceHeight);
         this.blitRepeating(pAtlasLocation, pX + pLeftSliceWidth, pY, pWidth - pRightSliceWidth - pLeftSliceWidth, pTopSliceHeight, pTextureX + pLeftSliceWidth, pTextureY, pUWidth - pRightSliceWidth - pLeftSliceWidth, pTopSliceHeight);
         this.blit(pAtlasLocation, pX + pWidth - pRightSliceWidth, pY, pTextureX + pUWidth - pRightSliceWidth, pTextureY, pRightSliceWidth, pTopSliceHeight);
         this.blit(pAtlasLocation, pX, pY + pHeight - pBottomSliceHeight, pTextureX, pTextureY + pVHeight - pBottomSliceHeight, pLeftSliceWidth, pBottomSliceHeight);
         this.blitRepeating(pAtlasLocation, pX + pLeftSliceWidth, pY + pHeight - pBottomSliceHeight, pWidth - pRightSliceWidth - pLeftSliceWidth, pBottomSliceHeight, pTextureX + pLeftSliceWidth, pTextureY + pVHeight - pBottomSliceHeight, pUWidth - pRightSliceWidth - pLeftSliceWidth, pBottomSliceHeight);
         this.blit(pAtlasLocation, pX + pWidth - pRightSliceWidth, pY + pHeight - pBottomSliceHeight, pTextureX + pUWidth - pRightSliceWidth, pTextureY + pVHeight - pBottomSliceHeight, pRightSliceWidth, pBottomSliceHeight);
         this.blitRepeating(pAtlasLocation, pX, pY + pTopSliceHeight, pLeftSliceWidth, pHeight - pBottomSliceHeight - pTopSliceHeight, pTextureX, pTextureY + pTopSliceHeight, pLeftSliceWidth, pVHeight - pBottomSliceHeight - pTopSliceHeight);
         this.blitRepeating(pAtlasLocation, pX + pLeftSliceWidth, pY + pTopSliceHeight, pWidth - pRightSliceWidth - pLeftSliceWidth, pHeight - pBottomSliceHeight - pTopSliceHeight, pTextureX + pLeftSliceWidth, pTextureY + pTopSliceHeight, pUWidth - pRightSliceWidth - pLeftSliceWidth, pVHeight - pBottomSliceHeight - pTopSliceHeight);
         this.blitRepeating(pAtlasLocation, pX + pWidth - pRightSliceWidth, pY + pTopSliceHeight, pLeftSliceWidth, pHeight - pBottomSliceHeight - pTopSliceHeight, pTextureX + pUWidth - pRightSliceWidth, pTextureY + pTopSliceHeight, pRightSliceWidth, pVHeight - pBottomSliceHeight - pTopSliceHeight);
      }
   }

   /**
    * Blits a repeating pattern of the texture specified by the atlas location onto the screen.
    * @param pAtlasLocation the location of the texture atlas.
    * @param pX the x-coordinate of the top-left corner of the target position.
    * @param pY the y-coordinate of the top-left corner of the target position.
    * @param pWidth the width of the target portion.
    * @param pHeight the height of the target portion.
    * @param pUOffset the x-coordinate of the top-left corner of the source position.
    * @param pVOffset the y-coordinate of the top-left corner of the source position.
    * @param pSourceWidth the width of the source texture.
    * @param pSourceHeight the height of the source texture.
    */
   public void blitRepeating(ResourceLocation pAtlasLocation, int pX, int pY, int pWidth, int pHeight, int pUOffset, int pVOffset, int pSourceWidth, int pSourceHeight) {
      blitRepeating(pAtlasLocation, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pSourceWidth, pSourceHeight, 256, 256);
   }

   public void blitRepeating(ResourceLocation pAtlasLocation, int pX, int pY, int pWidth, int pHeight, int pUOffset, int pVOffset, int pSourceWidth, int pSourceHeight, int textureWidth, int textureHeight) {
      int i = pX;

      int j;
      for(IntIterator intiterator = slices(pWidth, pSourceWidth); intiterator.hasNext(); i += j) {
         j = intiterator.nextInt();
         int k = (pSourceWidth - j) / 2;
         int l = pY;

         int i1;
         for(IntIterator intiterator1 = slices(pHeight, pSourceHeight); intiterator1.hasNext(); l += i1) {
            i1 = intiterator1.nextInt();
            int j1 = (pSourceHeight - i1) / 2;
            this.blit(pAtlasLocation, i, l, pUOffset + k, pVOffset + j1, j, i1, textureWidth, textureHeight);
         }
      }

   }

   /**
    * Returns an iterator for dividing a value into slices of a specified size.
    * <p>
    * @return An iterator for iterating over the slices.
    * @param pTarget the value to be divided.
    * @param pTotal the size of each slice.
    */
   private static IntIterator slices(int pTarget, int pTotal) {
      int i = Mth.positiveCeilDiv(pTarget, pTotal);
      return new Divisor(pTarget, i);
   }

   /**
    * Renders an item stack at the specified coordinates.
    * @param pStack the item stack to render.
    * @param pX the x-coordinate of the rendering position.
    * @param pY the y-coordinate of the rendering position.
    */
   public void renderItem(ItemStack pStack, int pX, int pY) {
      this.renderItem(this.minecraft.player, this.minecraft.level, pStack, pX, pY, 0);
   }

   /**
    * Renders an item stack at the specified coordinates with a random seed.
    * @param pStack the item stack to render.
    * @param pX the x-coordinate of the rendering position.
    * @param pY the y-coordinate of the rendering position.
    * @param pSeed the random seed.
    */
   public void renderItem(ItemStack pStack, int pX, int pY, int pSeed) {
      this.renderItem(this.minecraft.player, this.minecraft.level, pStack, pX, pY, pSeed);
   }

   /**
    * Renders an item stack at the specified coordinates with a random seed and a custom value.
    * @param pStack the item stack to render.
    * @param pX the x-coordinate of the rendering position.
    * @param pY the y-coordinate of the rendering position.
    * @param pSeed the random seed.
    * @param pGuiOffset the GUI offset.
    */
   public void renderItem(ItemStack pStack, int pX, int pY, int pSeed, int pGuiOffset) {
      this.renderItem(this.minecraft.player, this.minecraft.level, pStack, pX, pY, pSeed, pGuiOffset);
   }

   /**
    * Renders a fake item stack at the specified coordinates.
    * @param pStack the fake item stack to render.
    * @param pX the x-coordinate of the rendering position.
    * @param pY the y-coordinate of the rendering position.
    */
   public void renderFakeItem(ItemStack pStack, int pX, int pY) {
      this.renderItem((LivingEntity)null, this.minecraft.level, pStack, pX, pY, 0);
   }

   /**
    * Renders an item stack for a living entity at the specified coordinates with a random seed.
    * @param pEntity the living entity.
    * @param pStack the item stack to render.
    * @param pX the x-coordinate of the rendering position.
    * @param pY the y-coordinate of the rendering position.
    * @param pSeed the random seed.
    */
   public void renderItem(LivingEntity pEntity, ItemStack pStack, int pX, int pY, int pSeed) {
      this.renderItem(pEntity, pEntity.level(), pStack, pX, pY, pSeed);
   }

   /**
    * Renders an item stack for a living entity in a specific level at the specified coordinates with a random seed.
    * @param pEntity the living entity. Can be null.
    * @param pLevel the level in which the rendering occurs. Can be null.
    * @param pStack the item stack to render.
    * @param pX the x-coordinate of the rendering position.
    * @param pY the y-coordinate of the rendering position.
    * @param pSeed the random seed.
    */
   private void renderItem(@Nullable LivingEntity pEntity, @Nullable Level pLevel, ItemStack pStack, int pX, int pY, int pSeed) {
      this.renderItem(pEntity, pLevel, pStack, pX, pY, pSeed, 0);
   }

   /**
    * Renders an item stack for a living entity in a specific level at the specified coordinates with a random seed and
    * a custom GUI offset.
    * @param pEntity the living entity. Can be null.
    * @param pLevel the level in which the rendering occurs. Can be null.
    * @param pStack the item stack to render.
    * @param pX the x-coordinate of the rendering position.
    * @param pY the y-coordinate of the rendering position.
    * @param pSeed the random seed.
    * @param pGuiOffset the GUI offset value.
    */
   private void renderItem(@Nullable LivingEntity pEntity, @Nullable Level pLevel, ItemStack pStack, int pX, int pY, int pSeed, int pGuiOffset) {
      if (!pStack.isEmpty()) {
         BakedModel bakedmodel = this.minecraft.getItemRenderer().getModel(pStack, pLevel, pEntity, pSeed);
         this.pose.pushPose();
         this.pose.translate((float)(pX + 8), (float)(pY + 8), (float)(150 + (bakedmodel.isGui3d() ? pGuiOffset : 0)));

         try {
            this.pose.mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
            this.pose.scale(16.0F, 16.0F, 16.0F);
            boolean flag = !bakedmodel.usesBlockLight();
            if (flag) {
               Lighting.setupForFlatItems();
            }

            this.minecraft.getItemRenderer().render(pStack, ItemDisplayContext.GUI, false, this.pose, this.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
            this.flush();
            if (flag) {
               Lighting.setupFor3DItems();
            }
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
            crashreportcategory.setDetail("Item Type", () -> {
               return String.valueOf((Object)pStack.getItem());
            });
            crashreportcategory.setDetail("Registry Name", () -> String.valueOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(pStack.getItem())));
            crashreportcategory.setDetail("Item Damage", () -> {
               return String.valueOf(pStack.getDamageValue());
            });
            crashreportcategory.setDetail("Item NBT", () -> {
               return String.valueOf((Object)pStack.getTag());
            });
            crashreportcategory.setDetail("Item Foil", () -> {
               return String.valueOf(pStack.hasFoil());
            });
            throw new ReportedException(crashreport);
         }

         this.pose.popPose();
      }
   }

   /**
    * Renders additional decorations for an item stack at the specified coordinates.
    * @param pFont the font used for rendering text.
    * @param pStack the item stack to decorate.
    * @param pX the x-coordinate of the rendering position.
    * @param pY the y-coordinate of the rendering position.
    */
   public void renderItemDecorations(Font pFont, ItemStack pStack, int pX, int pY) {
      this.renderItemDecorations(pFont, pStack, pX, pY, (String)null);
   }

   /**
    * Renders additional decorations for an item stack at the specified coordinates with optional custom text.
    * @param pFont the font used for rendering text.
    * @param pStack the item stack to decorate.
    * @param pX the x-coordinate of the rendering position.
    * @param pY the y-coordinate of the rendering position.
    * @param pText the custom text to display. Can be null.
    */
   public void renderItemDecorations(Font pFont, ItemStack pStack, int pX, int pY, @Nullable String pText) {
      if (!pStack.isEmpty()) {
         this.pose.pushPose();
         if (pStack.getCount() != 1 || pText != null) {
            String s = pText == null ? String.valueOf(pStack.getCount()) : pText;
            this.pose.translate(0.0F, 0.0F, 200.0F);
            this.drawString(pFont, s, pX + 19 - 2 - pFont.width(s), pY + 6 + 3, 16777215, true);
         }

         if (pStack.isBarVisible()) {
            int l = pStack.getBarWidth();
            int i = pStack.getBarColor();
            int j = pX + 2;
            int k = pY + 13;
            this.fill(RenderType.guiOverlay(), j, k, j + 13, k + 2, -16777216);
            this.fill(RenderType.guiOverlay(), j, k, j + l, k + 1, i | -16777216);
         }

         LocalPlayer localplayer = this.minecraft.player;
         float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(pStack.getItem(), this.minecraft.getFrameTime());
         if (f > 0.0F) {
            int i1 = pY + Mth.floor(16.0F * (1.0F - f));
            int j1 = i1 + Mth.ceil(16.0F * f);
            this.fill(RenderType.guiOverlay(), pX, i1, pX + 16, j1, Integer.MAX_VALUE);
         }

         this.pose.popPose();
         net.minecraftforge.client.ItemDecoratorHandler.of(pStack).render(this, pFont, pStack, pX, pY);
      }
   }

   private ItemStack tooltipStack = ItemStack.EMPTY;

   /**
    * Renders a tooltip for an item stack at the specified mouse coordinates.
    * @param pFont the font used for rendering text.
    * @param pStack the item stack to display the tooltip for.
    * @param pMouseX the x-coordinate of the mouse position.
    * @param pMouseY the y-coordinate of the mouse position.
    */
   public void renderTooltip(Font pFont, ItemStack pStack, int pMouseX, int pMouseY) {
      this.tooltipStack = pStack;
      this.renderTooltip(pFont, Screen.getTooltipFromItem(this.minecraft, pStack), pStack.getTooltipImage(), pMouseX, pMouseY);
      this.tooltipStack = ItemStack.EMPTY;
   }

   public void renderTooltip(Font font, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, ItemStack stack, int mouseX, int mouseY) {
      this.tooltipStack = stack;
      this.renderTooltip(font, textComponents, tooltipComponent, mouseX, mouseY);
      this.tooltipStack = ItemStack.EMPTY;
   }

   /**
    * Renders a tooltip with customizable components at the specified mouse coordinates.
    * @param pFont the font used for rendering text.
    * @param pTooltipLines the lines of the tooltip.
    * @param pVisualTooltipComponent the visual tooltip component. Can be empty.
    * @param pMouseX the x-coordinate of the mouse position.
    * @param pMouseY the y-coordinate of the mouse position.
    */
   public void renderTooltip(Font pFont, List<Component> pTooltipLines, Optional<TooltipComponent> pVisualTooltipComponent, int pMouseX, int pMouseY) {
      List<ClientTooltipComponent> list = net.minecraftforge.client.ForgeHooksClient.gatherTooltipComponents(this.tooltipStack, pTooltipLines, pVisualTooltipComponent, pMouseX, guiWidth(), guiHeight(), pFont);
      this.renderTooltipInternal(pFont, list, pMouseX, pMouseY, DefaultTooltipPositioner.INSTANCE);
   }

   /**
    * Renders a tooltip with a single line of text at the specified mouse coordinates.
    * @param pFont the font used for rendering text.
    * @param pText the text to display in the tooltip.
    * @param pMouseX the x-coordinate of the mouse position.
    * @param pMouseY the y-coordinate of the mouse position.
    */
   public void renderTooltip(Font pFont, Component pText, int pMouseX, int pMouseY) {
      this.renderTooltip(pFont, List.of(pText.getVisualOrderText()), pMouseX, pMouseY);
   }

   /**
    * Renders a tooltip with multiple lines of component-based text at the specified mouse coordinates.
    * @param pFont the font used for rendering text.
    * @param pTooltipLines the lines of the tooltip as components.
    * @param pMouseX the x-coordinate of the mouse position.
    * @param pMouseY the y-coordinate of the mouse position.
    */
   public void renderComponentTooltip(Font pFont, List<Component> pTooltipLines, int pMouseX, int pMouseY) {
      List<ClientTooltipComponent> components = net.minecraftforge.client.ForgeHooksClient.gatherTooltipComponents(this.tooltipStack, pTooltipLines, pMouseX, guiWidth(), guiHeight(), pFont);
      this.renderTooltipInternal(pFont, components, pMouseX, pMouseY, DefaultTooltipPositioner.INSTANCE);
   }

   public void renderComponentTooltip(Font font, List<? extends net.minecraft.network.chat.FormattedText> tooltips, int mouseX, int mouseY, ItemStack stack) {
      this.tooltipStack = stack;
      List<ClientTooltipComponent> components = net.minecraftforge.client.ForgeHooksClient.gatherTooltipComponents(stack, tooltips, mouseX, guiWidth(), guiHeight(), font);
      this.renderTooltipInternal(font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
      this.tooltipStack = ItemStack.EMPTY;
   }

   /**
    * Renders a tooltip with multiple lines of formatted text at the specified mouse coordinates.
    * @param pFont the font used for rendering text.
    * @param pTooltipLines the lines of the tooltip as formatted character sequences.
    * @param pMouseX the x-coordinate of the mouse position.
    * @param pMouseY the y-coordinate of the mouse position.
    */
   public void renderTooltip(Font pFont, List<? extends FormattedCharSequence> pTooltipLines, int pMouseX, int pMouseY) {
      this.renderTooltipInternal(pFont, pTooltipLines.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), pMouseX, pMouseY, DefaultTooltipPositioner.INSTANCE);
   }

   /**
    * Renders a tooltip with multiple lines of formatted text using a custom tooltip positioner at the specified mouse
    * coordinates.
    * @param pFont the font used for rendering text.
    * @param pTooltipLines the lines of the tooltip as formatted character sequences.
    * @param pTooltipPositioner the positioner to determine the tooltip's position.
    * @param pMouseX the x-coordinate of the mouse position.
    * @param pMouseY the y-coordinate of the mouse position.
    */
   public void renderTooltip(Font pFont, List<FormattedCharSequence> pTooltipLines, ClientTooltipPositioner pTooltipPositioner, int pMouseX, int pMouseY) {
      this.renderTooltipInternal(pFont, pTooltipLines.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), pMouseX, pMouseY, pTooltipPositioner);
   }

   /**
    * Renders an internal tooltip with customizable tooltip components at the specified mouse coordinates using a
    * tooltip positioner.
    * @param pFont the font used for rendering text.
    * @param pComponents the tooltip components to render.
    * @param pMouseX the x-coordinate of the mouse position.
    * @param pMouseY the y-coordinate of the mouse position.
    * @param pTooltipPositioner the positioner to determine the tooltip's position.
    */
   private void renderTooltipInternal(Font pFont, List<ClientTooltipComponent> pComponents, int pMouseX, int pMouseY, ClientTooltipPositioner pTooltipPositioner) {
      if (!pComponents.isEmpty()) {
         net.minecraftforge.client.event.RenderTooltipEvent.Pre preEvent = net.minecraftforge.client.ForgeHooksClient.onRenderTooltipPre(this.tooltipStack, this, pMouseX, pMouseY, guiWidth(), guiHeight(), pComponents, pFont, pTooltipPositioner);
         if (preEvent.isCanceled()) return;
         int i = 0;
         int j = pComponents.size() == 1 ? -2 : 0;

         for(ClientTooltipComponent clienttooltipcomponent : pComponents) {
            int k = clienttooltipcomponent.getWidth(preEvent.getFont());
            if (k > i) {
               i = k;
            }

            j += clienttooltipcomponent.getHeight();
         }

         int i2 = i;
         int j2 = j;
         Vector2ic vector2ic = pTooltipPositioner.positionTooltip(this.guiWidth(), this.guiHeight(), preEvent.getX(), preEvent.getY(), i2, j2);
         int l = vector2ic.x();
         int i1 = vector2ic.y();
         this.pose.pushPose();
         int j1 = 400;
         this.drawManaged(() -> {
            net.minecraftforge.client.event.RenderTooltipEvent.Color colorEvent = net.minecraftforge.client.ForgeHooksClient.onRenderTooltipColor(this.tooltipStack, this, l, i1, preEvent.getFont(), pComponents);
            TooltipRenderUtil.renderTooltipBackground(this, l, i1, i2, j2, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd(), colorEvent.getBorderStart(), colorEvent.getBorderEnd());
         });
         this.pose.translate(0.0F, 0.0F, 400.0F);
         int k1 = i1;

         for(int l1 = 0; l1 < pComponents.size(); ++l1) {
            ClientTooltipComponent clienttooltipcomponent1 = pComponents.get(l1);
            clienttooltipcomponent1.renderText(preEvent.getFont(), l, k1, this.pose.last().pose(), this.bufferSource);
            k1 += clienttooltipcomponent1.getHeight() + (l1 == 0 ? 2 : 0);
         }

         k1 = i1;

         for(int k2 = 0; k2 < pComponents.size(); ++k2) {
            ClientTooltipComponent clienttooltipcomponent2 = pComponents.get(k2);
            clienttooltipcomponent2.renderImage(preEvent.getFont(), l, k1, this);
            k1 += clienttooltipcomponent2.getHeight() + (k2 == 0 ? 2 : 0);
         }

         this.pose.popPose();
      }
   }

   /**
    * Renders a hover effect for a text component at the specified mouse coordinates.
    * @param pFont the font used for rendering text.
    * @param pStyle the style of the text component. Can be null.
    * @param pMouseX the x-coordinate of the mouse position.
    * @param pMouseY the y-coordinate of the mouse position.
    */
   public void renderComponentHoverEffect(Font pFont, @Nullable Style pStyle, int pMouseX, int pMouseY) {
      if (pStyle != null && pStyle.getHoverEvent() != null) {
         HoverEvent hoverevent = pStyle.getHoverEvent();
         HoverEvent.ItemStackInfo hoverevent$itemstackinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ITEM);
         if (hoverevent$itemstackinfo != null) {
            this.renderTooltip(pFont, hoverevent$itemstackinfo.getItemStack(), pMouseX, pMouseY);
         } else {
            HoverEvent.EntityTooltipInfo hoverevent$entitytooltipinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (hoverevent$entitytooltipinfo != null) {
               if (this.minecraft.options.advancedItemTooltips) {
                  this.renderComponentTooltip(pFont, hoverevent$entitytooltipinfo.getTooltipLines(), pMouseX, pMouseY);
               }
            } else {
               Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
               if (component != null) {
                  this.renderTooltip(pFont, pFont.split(component, Math.max(this.guiWidth() / 2, 200)), pMouseX, pMouseY);
               }
            }
         }

      }
   }

   /**
    * A utility class for managing a stack of screen rectangles for scissoring.
    */
   @OnlyIn(Dist.CLIENT)
   static class ScissorStack {
      private final Deque<ScreenRectangle> stack = new ArrayDeque<>();

      /**
       * Pushes a screen rectangle onto the scissor stack.
       * <p>
       * @return The resulting intersection of the pushed rectangle with the previous top rectangle on the stack, or the
       * pushed rectangle if the stack is empty.
       * @param pScissor the screen rectangle to push.
       */
      public ScreenRectangle push(ScreenRectangle pScissor) {
         ScreenRectangle screenrectangle = this.stack.peekLast();
         if (screenrectangle != null) {
            ScreenRectangle screenrectangle1 = Objects.requireNonNullElse(pScissor.intersection(screenrectangle), ScreenRectangle.empty());
            this.stack.addLast(screenrectangle1);
            return screenrectangle1;
         } else {
            this.stack.addLast(pScissor);
            return pScissor;
         }
      }

      public @Nullable ScreenRectangle pop() {
         if (this.stack.isEmpty()) {
            throw new IllegalStateException("Scissor stack underflow");
         } else {
            this.stack.removeLast();
            return this.stack.peekLast();
         }
      }
   }
}
