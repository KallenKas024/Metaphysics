package net.minecraft.client.gui.components.toasts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Toast {
   ResourceLocation TEXTURE = new ResourceLocation("textures/gui/toasts.png");
   Object NO_TOKEN = new Object();
   int SLOT_HEIGHT = 32;

   Toast.Visibility render(GuiGraphics pGuiGraphics, ToastComponent pToastComponent, long pTimeSinceLastVisible);

   default Object getToken() {
      return NO_TOKEN;
   }

   default int width() {
      return 160;
   }

   default int height() {
      return 32;
   }

   default int slotCount() {
      return Mth.positiveCeilDiv(this.height(), 32);
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Visibility {
      SHOW(SoundEvents.UI_TOAST_IN),
      HIDE(SoundEvents.UI_TOAST_OUT);

      private final SoundEvent soundEvent;

      private Visibility(SoundEvent pSoundEvent) {
         this.soundEvent = pSoundEvent;
      }

      public void playSound(SoundManager pHandler) {
         pHandler.play(SimpleSoundInstance.forUI(this.soundEvent, 1.0F, 1.0F));
      }
   }
}