package net.minecraft.client.gui.components.spectator;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpectatorGui implements SpectatorMenuListener {
   private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
   public static final ResourceLocation SPECTATOR_LOCATION = new ResourceLocation("textures/gui/spectator_widgets.png");
   private static final long FADE_OUT_DELAY = 5000L;
   private static final long FADE_OUT_TIME = 2000L;
   private final Minecraft minecraft;
   private long lastSelectionTime;
   @Nullable
   private SpectatorMenu menu;

   public SpectatorGui(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void onHotbarSelected(int pSlot) {
      this.lastSelectionTime = Util.getMillis();
      if (this.menu != null) {
         this.menu.selectSlot(pSlot);
      } else {
         this.menu = new SpectatorMenu(this);
      }

   }

   private float getHotbarAlpha() {
      long i = this.lastSelectionTime - Util.getMillis() + 5000L;
      return Mth.clamp((float)i / 2000.0F, 0.0F, 1.0F);
   }

   public void renderHotbar(GuiGraphics pGuiGraphics) {
      if (this.menu != null) {
         float f = this.getHotbarAlpha();
         if (f <= 0.0F) {
            this.menu.exit();
         } else {
            int i = pGuiGraphics.guiWidth() / 2;
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
            int j = Mth.floor((float)pGuiGraphics.guiHeight() - 22.0F * f);
            SpectatorPage spectatorpage = this.menu.getCurrentPage();
            this.renderPage(pGuiGraphics, f, i, j, spectatorpage);
            pGuiGraphics.pose().popPose();
         }
      }
   }

   protected void renderPage(GuiGraphics pGuiGraphics, float pAlpha, int pX, int pY, SpectatorPage pSpectatorPage) {
      RenderSystem.enableBlend();
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, pAlpha);
      pGuiGraphics.blit(WIDGETS_LOCATION, pX - 91, pY, 0, 0, 182, 22);
      if (pSpectatorPage.getSelectedSlot() >= 0) {
         pGuiGraphics.blit(WIDGETS_LOCATION, pX - 91 - 1 + pSpectatorPage.getSelectedSlot() * 20, pY - 1, 0, 22, 24, 22);
      }

      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

      for(int i = 0; i < 9; ++i) {
         this.renderSlot(pGuiGraphics, i, pGuiGraphics.guiWidth() / 2 - 90 + i * 20 + 2, (float)(pY + 3), pAlpha, pSpectatorPage.getItem(i));
      }

      RenderSystem.disableBlend();
   }

   private void renderSlot(GuiGraphics pGuiGraphics, int pSlot, int pX, float pY, float pAlpha, SpectatorMenuItem pSpectatorMenuItem) {
      if (pSpectatorMenuItem != SpectatorMenu.EMPTY_SLOT) {
         int i = (int)(pAlpha * 255.0F);
         pGuiGraphics.pose().pushPose();
         pGuiGraphics.pose().translate((float)pX, pY, 0.0F);
         float f = pSpectatorMenuItem.isEnabled() ? 1.0F : 0.25F;
         pGuiGraphics.setColor(f, f, f, pAlpha);
         pSpectatorMenuItem.renderIcon(pGuiGraphics, f, i);
         pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         pGuiGraphics.pose().popPose();
         if (i > 3 && pSpectatorMenuItem.isEnabled()) {
            Component component = this.minecraft.options.keyHotbarSlots[pSlot].getTranslatedKeyMessage();
            pGuiGraphics.drawString(this.minecraft.font, component, pX + 19 - 2 - this.minecraft.font.width(component), (int)pY + 6 + 3, 16777215 + (i << 24));
         }
      }

   }

   public void renderTooltip(GuiGraphics pGuiGraphics) {
      int i = (int)(this.getHotbarAlpha() * 255.0F);
      if (i > 3 && this.menu != null) {
         SpectatorMenuItem spectatormenuitem = this.menu.getSelectedItem();
         Component component = spectatormenuitem == SpectatorMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt() : spectatormenuitem.getName();
         if (component != null) {
            int j = (pGuiGraphics.guiWidth() - this.minecraft.font.width(component)) / 2;
            int k = pGuiGraphics.guiHeight() - 35;
            pGuiGraphics.drawString(this.minecraft.font, component, j, k, 16777215 + (i << 24));
         }
      }

   }

   public void onSpectatorMenuClosed(SpectatorMenu pMenu) {
      this.menu = null;
      this.lastSelectionTime = 0L;
   }

   public boolean isMenuActive() {
      return this.menu != null;
   }

   public void onMouseScrolled(int pAmount) {
      int i;
      for(i = this.menu.getSelectedSlot() + pAmount; i >= 0 && i <= 8 && (this.menu.getItem(i) == SpectatorMenu.EMPTY_SLOT || !this.menu.getItem(i).isEnabled()); i += pAmount) {
      }

      if (i >= 0 && i <= 8) {
         this.menu.selectSlot(i);
         this.lastSelectionTime = Util.getMillis();
      }

   }

   public void onMouseMiddleClick() {
      this.lastSelectionTime = Util.getMillis();
      if (this.isMenuActive()) {
         int i = this.menu.getSelectedSlot();
         if (i != -1) {
            this.menu.selectSlot(i);
         }
      } else {
         this.menu = new SpectatorMenu(this);
      }

   }
}