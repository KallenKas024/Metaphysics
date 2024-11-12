package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSliderButton extends AbstractWidget {
   public static final ResourceLocation SLIDER_LOCATION = new ResourceLocation("textures/gui/slider.png");
   protected static final int TEXTURE_WIDTH = 200;
   protected static final int TEXTURE_HEIGHT = 20;
   protected static final int TEXTURE_BORDER_X = 20;
   protected static final int TEXTURE_BORDER_Y = 4;
   protected static final int TEXT_MARGIN = 2;
   private static final int HEIGHT = 20;
   private static final int HANDLE_HALF_WIDTH = 4;
   private static final int HANDLE_WIDTH = 8;
   private static final int BACKGROUND = 0;
   private static final int BACKGROUND_FOCUSED = 1;
   private static final int HANDLE = 2;
   private static final int HANDLE_FOCUSED = 3;
   protected double value;
   private boolean canChangeValue;

   public AbstractSliderButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, double pValue) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.value = pValue;
   }

   protected int getTextureY() {
      int i = this.isFocused() && !this.canChangeValue ? 1 : 0;
      return i * 20;
   }

   protected int getHandleTextureY() {
      int i = !this.isHovered && !this.canChangeValue ? 2 : 3;
      return i * 20;
   }

   protected MutableComponent createNarrationMessage() {
      return Component.translatable("gui.narrate.slider", this.getMessage());
   }

   public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
      pNarrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
      if (this.active) {
         if (this.isFocused()) {
            pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
         } else {
            pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
         }
      }

   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      pGuiGraphics.blitNineSliced(SLIDER_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
      pGuiGraphics.blitNineSliced(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, 20, 20, 4, 200, 20, 0, this.getHandleTextureY());
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int i = this.active ? 16777215 : 10526880;
      this.renderScrollingString(pGuiGraphics, minecraft.font, 2, i | Mth.ceil(this.alpha * 255.0F) << 24);
   }

   public void onClick(double pMouseX, double pMouseY) {
      this.setValueFromMouse(pMouseX);
   }

   /**
    * Sets the focus state of the GUI element.
    * @param pFocused {@code true} to apply focus, {@code false} to remove focus
    */
   public void setFocused(boolean pFocused) {
      super.setFocused(pFocused);
      if (!pFocused) {
         this.canChangeValue = false;
      } else {
         InputType inputtype = Minecraft.getInstance().getLastInputType();
         if (inputtype == InputType.MOUSE || inputtype == InputType.KEYBOARD_TAB) {
            this.canChangeValue = true;
         }

      }
   }

   /**
    * Called when a keyboard key is pressed within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the pressed key.
    * @param pScanCode the scan code of the pressed key.
    * @param pModifiers the keyboard modifiers.
    */
   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (CommonInputs.selected(pKeyCode)) {
         this.canChangeValue = !this.canChangeValue;
         return true;
      } else {
         if (this.canChangeValue) {
            boolean flag = pKeyCode == 263;
            if (flag || pKeyCode == 262) {
               float f = flag ? -1.0F : 1.0F;
               this.setValue(this.value + (double)(f / (float)(this.width - 8)));
               return true;
            }
         }

         return false;
      }
   }

   private void setValueFromMouse(double pMouseX) {
      this.setValue((pMouseX - (double)(this.getX() + 4)) / (double)(this.width - 8));
   }

   private void setValue(double pValue) {
      double d0 = this.value;
      this.value = Mth.clamp(pValue, 0.0D, 1.0D);
      if (d0 != this.value) {
         this.applyValue();
      }

      this.updateMessage();
   }

   protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
      this.setValueFromMouse(pMouseX);
      super.onDrag(pMouseX, pMouseY, pDragX, pDragY);
   }

   public void playDownSound(SoundManager pHandler) {
   }

   public void onRelease(double pMouseX, double pMouseY) {
      super.playDownSound(Minecraft.getInstance().getSoundManager());
   }

   protected abstract void updateMessage();

   protected abstract void applyValue();
}