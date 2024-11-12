package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MouseSettingsScreen extends OptionsSubScreen {
   private OptionsList list;

   private static OptionInstance<?>[] options(Options pOptions) {
      return new OptionInstance[]{pOptions.sensitivity(), pOptions.invertYMouse(), pOptions.mouseWheelSensitivity(), pOptions.discreteMouseScroll(), pOptions.touchscreen()};
   }

   public MouseSettingsScreen(Screen pLastScreen, Options pOptions) {
      super(pLastScreen, pOptions, Component.translatable("options.mouse_settings.title"));
   }

   protected void init() {
      this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
      if (InputConstants.isRawMouseInputSupported()) {
         this.list.addSmall(Stream.concat(Arrays.stream(options(this.options)), Stream.of(this.options.rawMouseInput())).toArray((p_232747_) -> {
            return new OptionInstance[p_232747_];
         }));
      } else {
         this.list.addSmall(options(this.options));
      }

      this.addWidget(this.list);
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280804_) -> {
         this.options.save();
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      this.list.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }
}