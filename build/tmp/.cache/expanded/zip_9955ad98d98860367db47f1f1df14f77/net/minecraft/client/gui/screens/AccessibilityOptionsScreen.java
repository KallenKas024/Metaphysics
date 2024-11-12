package net.minecraft.client.gui.screens;

import net.minecraft.Util;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AccessibilityOptionsScreen extends SimpleOptionsSubScreen {
   private static OptionInstance<?>[] options(Options pOptions) {
      return new OptionInstance[]{pOptions.narrator(), pOptions.showSubtitles(), pOptions.highContrast(), pOptions.autoJump(), pOptions.textBackgroundOpacity(), pOptions.backgroundForChatOnly(), pOptions.chatOpacity(), pOptions.chatLineSpacing(), pOptions.chatDelay(), pOptions.notificationDisplayTime(), pOptions.toggleCrouch(), pOptions.toggleSprint(), pOptions.screenEffectScale(), pOptions.fovEffectScale(), pOptions.darknessEffectScale(), pOptions.damageTiltStrength(), pOptions.glintSpeed(), pOptions.glintStrength(), pOptions.hideLightningFlash(), pOptions.darkMojangStudiosBackground(), pOptions.panoramaSpeed()};
   }

   public AccessibilityOptionsScreen(Screen pLastScreen, Options pOptions) {
      super(pLastScreen, pOptions, Component.translatable("options.accessibility.title"), options(pOptions));
   }

   protected void init() {
      super.init();
      AbstractWidget abstractwidget = this.list.findOption(this.options.highContrast());
      if (abstractwidget != null && !this.minecraft.getResourcePackRepository().getAvailableIds().contains("high_contrast")) {
         abstractwidget.active = false;
         abstractwidget.setTooltip(Tooltip.create(Component.translatable("options.accessibility.high_contrast.error.tooltip")));
      }

   }

   protected void createFooter() {
      this.addRenderableWidget(Button.builder(Component.translatable("options.accessibility.link"), (p_280784_) -> {
         this.minecraft.setScreen(new ConfirmLinkScreen((p_280783_) -> {
            if (p_280783_) {
               Util.getPlatform().openUri("https://aka.ms/MinecraftJavaAccessibility");
            }

            this.minecraft.setScreen(this);
         }, "https://aka.ms/MinecraftJavaAccessibility", true));
      }).bounds(this.width / 2 - 155, this.height - 27, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280785_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 + 5, this.height - 27, 150, 20).build());
   }
}