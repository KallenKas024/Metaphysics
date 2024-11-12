package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HopperScreen extends AbstractContainerScreen<HopperMenu> {
   /** The ResourceLocation containing the gui texture for the hopper */
   private static final ResourceLocation HOPPER_LOCATION = new ResourceLocation("textures/gui/container/hopper.png");

   public HopperScreen(HopperMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      this.imageHeight = 133;
      this.inventoryLabelY = this.imageHeight - 94;
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
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
   }

   protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      pGuiGraphics.blit(HOPPER_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
   }
}