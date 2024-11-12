package net.minecraft.client;

import com.mojang.logging.LogUtils;
import com.mojang.text2speech.Narrator;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GameNarrator {
   public static final Component NO_TITLE = CommonComponents.EMPTY;
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Minecraft minecraft;
   private final Narrator narrator = Narrator.getNarrator();

   public GameNarrator(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void sayChat(Component pMessage) {
      if (this.getStatus().shouldNarrateChat()) {
         String s = pMessage.getString();
         this.logNarratedMessage(s);
         this.narrator.say(s, false);
      }

   }

   public void say(Component pMessage) {
      String s = pMessage.getString();
      if (this.getStatus().shouldNarrateSystem() && !s.isEmpty()) {
         this.logNarratedMessage(s);
         this.narrator.say(s, false);
      }

   }

   public void sayNow(Component pMessage) {
      this.sayNow(pMessage.getString());
   }

   public void sayNow(String pMessage) {
      if (this.getStatus().shouldNarrateSystem() && !pMessage.isEmpty()) {
         this.logNarratedMessage(pMessage);
         if (this.narrator.active()) {
            this.narrator.clear();
            this.narrator.say(pMessage, true);
         }
      }

   }

   private NarratorStatus getStatus() {
      return this.minecraft.options.narrator().get();
   }

   private void logNarratedMessage(String pMessage) {
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         LOGGER.debug("Narrating: {}", (Object)pMessage.replaceAll("\n", "\\\\n"));
      }

   }

   public void updateNarratorStatus(NarratorStatus pStatus) {
      this.clear();
      this.narrator.say(Component.translatable("options.narrator").append(" : ").append(pStatus.getName()).getString(), true);
      ToastComponent toastcomponent = Minecraft.getInstance().getToasts();
      if (this.narrator.active()) {
         if (pStatus == NarratorStatus.OFF) {
            SystemToast.addOrUpdate(toastcomponent, SystemToast.SystemToastIds.NARRATOR_TOGGLE, Component.translatable("narrator.toast.disabled"), (Component)null);
         } else {
            SystemToast.addOrUpdate(toastcomponent, SystemToast.SystemToastIds.NARRATOR_TOGGLE, Component.translatable("narrator.toast.enabled"), pStatus.getName());
         }
      } else {
         SystemToast.addOrUpdate(toastcomponent, SystemToast.SystemToastIds.NARRATOR_TOGGLE, Component.translatable("narrator.toast.disabled"), Component.translatable("options.narrator.notavailable"));
      }

   }

   public boolean isActive() {
      return this.narrator.active();
   }

   public void clear() {
      if (this.getStatus() != NarratorStatus.OFF && this.narrator.active()) {
         this.narrator.clear();
      }
   }

   public void destroy() {
      this.narrator.destroy();
   }

   public void checkStatus(boolean pNarratorEnabled) {
      if (pNarratorEnabled && !this.isActive() && !TinyFileDialogs.tinyfd_messageBox("Minecraft", "Failed to initialize text-to-speech library. Do you want to continue?\nIf this problem persists, please report it at bugs.mojang.com", "yesno", "error", true)) {
         throw new GameNarrator.NarratorInitException("Narrator library is not active");
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class NarratorInitException extends SilentInitException {
      public NarratorInitException(String p_288985_) {
         super(p_288985_);
      }
   }
}