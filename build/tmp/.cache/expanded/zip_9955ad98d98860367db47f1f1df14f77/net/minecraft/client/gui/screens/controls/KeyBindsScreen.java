package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.platform.InputConstants;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyBindsScreen extends OptionsSubScreen {
   @Nullable
   public KeyMapping selectedKey;
   public long lastKeySelection;
   private KeyBindsList keyBindsList;
   private Button resetButton;

   public KeyBindsScreen(Screen pLastScreen, Options pOptions) {
      super(pLastScreen, pOptions, Component.translatable("controls.keybinds.title"));
   }

   protected void init() {
      this.keyBindsList = new KeyBindsList(this, this.minecraft);
      this.addWidget(this.keyBindsList);
      this.resetButton = this.addRenderableWidget(Button.builder(Component.translatable("controls.resetAll"), (p_269619_) -> {
         for(KeyMapping keymapping : this.options.keyMappings) {
            keymapping.setToDefault();
         }

         this.keyBindsList.resetMappingAndUpdateButtons();
      }).bounds(this.width / 2 - 155, this.height - 29, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280847_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());
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
      if (this.selectedKey != null) {
         this.options.setKey(this.selectedKey, InputConstants.Type.MOUSE.getOrCreate(pButton));
         this.selectedKey = null;
         this.keyBindsList.resetMappingAndUpdateButtons();
         return true;
      } else {
         return super.mouseClicked(pMouseX, pMouseY, pButton);
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
      if (this.selectedKey != null) {
         if (pKeyCode == 256) {
            this.selectedKey.setKeyModifierAndCode(null, InputConstants.UNKNOWN);
            this.options.setKey(this.selectedKey, InputConstants.UNKNOWN);
         } else {
            this.selectedKey.setKeyModifierAndCode(null, InputConstants.getKey(pKeyCode, pScanCode));
            this.options.setKey(this.selectedKey, InputConstants.getKey(pKeyCode, pScanCode));
         }

         if (pKeyCode == 256 || !net.minecraftforge.client.settings.KeyModifier.isKeyCodeModifier(this.selectedKey.getKey()))
         this.selectedKey = null;
         this.lastKeySelection = Util.getMillis();
         this.keyBindsList.resetMappingAndUpdateButtons();
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   /**
    * Called when a keyboard key is released within the GUI element.
    * <p>
    * @return {@code true} if the event is consumed, {@code false} otherwise.
    * @param pKeyCode the key code of the released key.
    * @param pScanCode the scan code of the released key.
    * @param pModifiers the keyboard modifiers.
    */
   @Override
   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      // Forge: We wait for a second key above if the first press is a modifier
      // but if they release the modifier then set it explicitly.
      var key = InputConstants.getKey(keyCode, scanCode);
      if (this.selectedKey != null && this.selectedKey.getKey() == key) {
         this.selectedKey = null;
         this.lastKeySelection = Util.getMillis();
         this.keyBindsList.resetMappingAndUpdateButtons();
      }
      return super.keyReleased(keyCode, scanCode, modifiers);
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
      this.keyBindsList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      boolean flag = false;

      for(KeyMapping keymapping : this.options.keyMappings) {
         if (!keymapping.isDefault()) {
            flag = true;
            break;
         }
      }

      this.resetButton.active = flag;
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }
}
