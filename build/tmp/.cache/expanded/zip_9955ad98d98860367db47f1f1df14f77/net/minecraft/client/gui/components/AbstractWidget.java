package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractWidget implements Renderable, GuiEventListener, LayoutElement, NarratableEntry {
   public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
   public static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
   private static final double PERIOD_PER_SCROLLED_PIXEL = 0.5D;
   private static final double MIN_SCROLL_PERIOD = 3.0D;
   protected int width;
   protected int height;
   private int x;
   private int y;
   private Component message;
   protected boolean isHovered;
   public boolean active = true;
   public boolean visible = true;
   protected float alpha = 1.0F;
   private int tabOrderGroup;
   private boolean focused;
   @Nullable
   private Tooltip tooltip;
   private int tooltipMsDelay;
   private long hoverOrFocusedStartTime;
   private boolean wasHoveredOrFocused;

   public AbstractWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
      this.x = pX;
      this.y = pY;
      this.width = pWidth;
      this.height = pHeight;
      this.message = pMessage;
   }

   public int getHeight() {
      return this.height;
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.visible) {
         this.isHovered = pMouseX >= this.getX() && pMouseY >= this.getY() && pMouseX < this.getX() + this.width && pMouseY < this.getY() + this.height;
         this.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         this.updateTooltip();
      }
   }

   private void updateTooltip() {
      if (this.tooltip != null) {
         boolean flag = this.isHovered || this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
         if (flag != this.wasHoveredOrFocused) {
            if (flag) {
               this.hoverOrFocusedStartTime = Util.getMillis();
            }

            this.wasHoveredOrFocused = flag;
         }

         if (flag && Util.getMillis() - this.hoverOrFocusedStartTime > (long)this.tooltipMsDelay) {
            Screen screen = Minecraft.getInstance().screen;
            if (screen != null) {
               screen.setTooltipForNextRenderPass(this.tooltip, this.createTooltipPositioner(), this.isFocused());
            }
         }

      }
   }

   protected ClientTooltipPositioner createTooltipPositioner() {
      return (ClientTooltipPositioner)(!this.isHovered && this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard() ? new BelowOrAboveWidgetTooltipPositioner(this) : new MenuTooltipPositioner(this));
   }

   public void setTooltip(@Nullable Tooltip pTooltip) {
      this.tooltip = pTooltip;
   }

   @Nullable
   public Tooltip getTooltip() {
      return this.tooltip;
   }

   public void setTooltipDelay(int pTooltipMsDelay) {
      this.tooltipMsDelay = pTooltipMsDelay;
   }

   protected MutableComponent createNarrationMessage() {
      return wrapDefaultNarrationMessage(this.getMessage());
   }

   public static MutableComponent wrapDefaultNarrationMessage(Component pMessage) {
      return Component.translatable("gui.narrate.button", pMessage);
   }

   protected abstract void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick);

   protected static void renderScrollingString(GuiGraphics pGuiGraphics, Font pFont, Component pText, int pMinX, int pMinY, int pMaxX, int pMaxY, int pColor) {
      int i = pFont.width(pText);
      int j = (pMinY + pMaxY - 9) / 2 + 1;
      int k = pMaxX - pMinX;
      if (i > k) {
         int l = i - k;
         double d0 = (double)Util.getMillis() / 1000.0D;
         double d1 = Math.max((double)l * 0.5D, 3.0D);
         double d2 = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * d0 / d1)) / 2.0D + 0.5D;
         double d3 = Mth.lerp(d2, 0.0D, (double)l);
         pGuiGraphics.enableScissor(pMinX, pMinY, pMaxX, pMaxY);
         pGuiGraphics.drawString(pFont, pText, pMinX - (int)d3, j, pColor);
         pGuiGraphics.disableScissor();
      } else {
         pGuiGraphics.drawCenteredString(pFont, pText, (pMinX + pMaxX) / 2, j, pColor);
      }

   }

   protected void renderScrollingString(GuiGraphics pGuiGraphics, Font pFont, int pWidth, int pColor) {
      int i = this.getX() + pWidth;
      int j = this.getX() + this.getWidth() - pWidth;
      renderScrollingString(pGuiGraphics, pFont, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), pColor);
   }

   public void renderTexture(GuiGraphics pGuiGraphics, ResourceLocation pTexture, int pX, int pY, int pUOffset, int pVOffset, int pTextureDifference, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight) {
      int i = pVOffset;
      if (!this.isActive()) {
         i = pVOffset + pTextureDifference * 2;
      } else if (this.isHoveredOrFocused()) {
         i = pVOffset + pTextureDifference;
      }

      RenderSystem.enableDepthTest();
      pGuiGraphics.blit(pTexture, pX, pY, (float)pUOffset, (float)i, pWidth, pHeight, pTextureWidth, pTextureHeight);
   }

   public void onClick(double pMouseX, double pMouseY) {
   }

   public void onRelease(double pMouseX, double pMouseY) {
   }

   protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
   }

   /**
    * Called when a mouse button is clicked within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was clicked.
    */
   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (this.active && this.visible) {
         if (this.isValidClickButton(pButton)) {
            boolean flag = this.clicked(pMouseX, pMouseY);
            if (flag) {
               this.playDownSound(Minecraft.getInstance().getSoundManager());
               this.onClick(pMouseX, pMouseY);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   /**
    * Called when a mouse button is released within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that was released.
    */
   public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
      if (this.isValidClickButton(pButton)) {
         this.onRelease(pMouseX, pMouseY);
         return true;
      } else {
         return false;
      }
   }

   protected boolean isValidClickButton(int pButton) {
      return pButton == 0;
   }

   /**
    * Called when the mouse is dragged within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    * @param pButton the button that is being dragged.
    * @param pDragX the X distance of the drag.
    * @param pDragY the Y distance of the drag.
    */
   public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      if (this.isValidClickButton(pButton)) {
         this.onDrag(pMouseX, pMouseY, pDragX, pDragY);
         return true;
      } else {
         return false;
      }
   }

   protected boolean clicked(double pMouseX, double pMouseY) {
      return this.active && this.visible && pMouseX >= (double)this.getX() && pMouseY >= (double)this.getY() && pMouseX < (double)(this.getX() + this.width) && pMouseY < (double)(this.getY() + this.height);
   }

   /**
    * Retrieves the next focus path based on the given focus navigation event.
    * <p>
    * @return the next focus path as a ComponentPath, or {@code null} if there is no next focus path.
    * @param pEvent the focus navigation event.
    */
   @Nullable
   public ComponentPath nextFocusPath(FocusNavigationEvent pEvent) {
      if (this.active && this.visible) {
         return !this.isFocused() ? ComponentPath.leaf(this) : null;
      } else {
         return null;
      }
   }

   /**
    * Checks if the given mouse coordinates are over the GUI element.
    * <p>
    * @return {@code true} if the mouse is over the GUI element, {@code false} otherwise.
    * @param pMouseX the X coordinate of the mouse.
    * @param pMouseY the Y coordinate of the mouse.
    */
   public boolean isMouseOver(double pMouseX, double pMouseY) {
      return this.active && this.visible && pMouseX >= (double)this.getX() && pMouseY >= (double)this.getY() && pMouseX < (double)(this.getX() + this.width) && pMouseY < (double)(this.getY() + this.height);
   }

   public void playDownSound(SoundManager pHandler) {
      pHandler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int pWidth) {
      this.width = pWidth;
   }

   public void setHeight(int value) {
      this.height = value;
   }

   public void setAlpha(float pAlpha) {
      this.alpha = pAlpha;
   }

   public void setMessage(Component pMessage) {
      this.message = pMessage;
   }

   public Component getMessage() {
      return this.message;
   }

   /**
    * {@return {@code true} if the GUI element is focused, {@code false} otherwise}
    */
   public boolean isFocused() {
      return this.focused;
   }

   public boolean isHovered() {
      return this.isHovered;
   }

   public boolean isHoveredOrFocused() {
      return this.isHovered() || this.isFocused();
   }

   /**
    * {@return {@code true} if the element is active, {@code false} otherwise}
    */
   public boolean isActive() {
      return this.visible && this.active;
   }

   /**
    * Sets the focus state of the GUI element.
    * @param pFocused {@code true} to apply focus, {@code false} to remove focus
    */
   public void setFocused(boolean pFocused) {
      this.focused = pFocused;
   }

   public static final int UNSET_FG_COLOR = -1;
   protected int packedFGColor = UNSET_FG_COLOR;
   public int getFGColor() {
      if (packedFGColor != UNSET_FG_COLOR) return packedFGColor;
      return this.active ? 16777215 : 10526880; // White : Light Grey
   }
   public void setFGColor(int color) {
      this.packedFGColor = color;
   }
   public void clearFGColor() {
      this.packedFGColor = UNSET_FG_COLOR;
   }

   /**
    * {@return the narration priority}
    */
   public NarratableEntry.NarrationPriority narrationPriority() {
      if (this.isFocused()) {
         return NarratableEntry.NarrationPriority.FOCUSED;
      } else {
         return this.isHovered ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
      }
   }

   /**
    * Updates the narration output with the current narration information.
    * @param pNarrationElementOutput the output to update with narration information.
    */
   public final void updateNarration(NarrationElementOutput pNarrationElementOutput) {
      this.updateWidgetNarration(pNarrationElementOutput);
      if (this.tooltip != null) {
         this.tooltip.updateNarration(pNarrationElementOutput);
      }

   }

   protected abstract void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput);

   protected void defaultButtonNarrationText(NarrationElementOutput pNarrationElementOutput) {
      pNarrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
      if (this.active) {
         if (this.isFocused()) {
            pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
         } else {
            pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
         }
      }

   }

   public int getX() {
      return this.x;
   }

   public void setX(int pX) {
      this.x = pX;
   }

   public int getY() {
      return this.y;
   }

   public void setY(int pY) {
      this.y = pY;
   }

   public void visitWidgets(Consumer<AbstractWidget> pConsumer) {
      pConsumer.accept(this);
   }

   /**
    * {@return the {@link ScreenRectangle} occupied by the GUI element}
    */
   public ScreenRectangle getRectangle() {
      return LayoutElement.super.getRectangle();
   }

   /**
    * Returns the tab order group of the GUI component.
    * Tab order group determines the order in which the components are traversed when using keyboard navigation.
    * <p>
    * @return The tab order group of the GUI component.
    */
   public int getTabOrderGroup() {
      return this.tabOrderGroup;
   }

   public void setTabOrderGroup(int pTabOrderGroup) {
      this.tabOrderGroup = pTabOrderGroup;
   }
}
