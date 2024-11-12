package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BackupConfirmScreen extends Screen {
   private final Screen lastScreen;
   protected final BackupConfirmScreen.Listener listener;
   private final Component description;
   private final boolean promptForCacheErase;
   private MultiLineLabel message = MultiLineLabel.EMPTY;
   protected int id;
   private Checkbox eraseCache;

   public BackupConfirmScreen(Screen pLastScreen, BackupConfirmScreen.Listener pListener, Component pTitle, Component pDescription, boolean pPromptForCacheErase) {
      super(pTitle);
      this.lastScreen = pLastScreen;
      this.listener = pListener;
      this.description = pDescription;
      this.promptForCacheErase = pPromptForCacheErase;
   }

   protected void init() {
      super.init();
      this.message = MultiLineLabel.create(this.font, this.description, this.width - 50);
      int i = (this.message.getLineCount() + 1) * 9;
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.backupJoinConfirmButton"), (p_95564_) -> {
         this.listener.proceed(true, this.eraseCache.selected());
      }).bounds(this.width / 2 - 155, 100 + i, 150, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.backupJoinSkipButton"), (p_95562_) -> {
         this.listener.proceed(false, this.eraseCache.selected());
      }).bounds(this.width / 2 - 155 + 160, 100 + i, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_280786_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 - 155 + 80, 124 + i, 150, 20).build());
      this.eraseCache = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, Component.translatable("selectWorld.backupEraseCache"), false);
      if (this.promptForCacheErase) {
         this.addRenderableWidget(this.eraseCache);
      }

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
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 50, 16777215);
      this.message.renderCentered(pGuiGraphics, this.width / 2, 70);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public boolean shouldCloseOnEsc() {
      return false;
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
      if (pKeyCode == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public interface Listener {
      void proceed(boolean pConfirmed, boolean pEraseCache);
   }
}