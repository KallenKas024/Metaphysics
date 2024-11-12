package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PageButton extends Button {
   private final boolean isForward;
   private final boolean playTurnSound;

   public PageButton(int pX, int pY, boolean pIsForward, Button.OnPress pOnPress, boolean pPlayTurnSound) {
      super(pX, pY, 23, 13, CommonComponents.EMPTY, pOnPress, DEFAULT_NARRATION);
      this.isForward = pIsForward;
      this.playTurnSound = pPlayTurnSound;
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      int i = 0;
      int j = 192;
      if (this.isHoveredOrFocused()) {
         i += 23;
      }

      if (!this.isForward) {
         j += 13;
      }

      pGuiGraphics.blit(BookViewScreen.BOOK_LOCATION, this.getX(), this.getY(), i, j, 23, 13);
   }

   public void playDownSound(SoundManager pHandler) {
      if (this.playTurnSound) {
         pHandler.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
      }

   }
}