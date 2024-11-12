package net.minecraft.client.gui.components.tabs;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabManager {
   private final Consumer<AbstractWidget> addWidget;
   private final Consumer<AbstractWidget> removeWidget;
   @Nullable
   private Tab currentTab;
   @Nullable
   private ScreenRectangle tabArea;

   public TabManager(Consumer<AbstractWidget> pAddWidget, Consumer<AbstractWidget> pRemoveWidget) {
      this.addWidget = pAddWidget;
      this.removeWidget = pRemoveWidget;
   }

   public void setTabArea(ScreenRectangle pTabArea) {
      this.tabArea = pTabArea;
      Tab tab = this.getCurrentTab();
      if (tab != null) {
         tab.doLayout(pTabArea);
      }

   }

   public void setCurrentTab(Tab pTab, boolean pPlayClickSound) {
      if (!Objects.equals(this.currentTab, pTab)) {
         if (this.currentTab != null) {
            this.currentTab.visitChildren(this.removeWidget);
         }

         this.currentTab = pTab;
         pTab.visitChildren(this.addWidget);
         if (this.tabArea != null) {
            pTab.doLayout(this.tabArea);
         }

         if (pPlayClickSound) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }
      }

   }

   @Nullable
   public Tab getCurrentTab() {
      return this.currentTab;
   }

   public void tickCurrent() {
      Tab tab = this.getCurrentTab();
      if (tab != null) {
         tab.tick();
      }

   }
}