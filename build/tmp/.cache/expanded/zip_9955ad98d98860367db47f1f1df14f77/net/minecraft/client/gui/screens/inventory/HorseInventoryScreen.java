package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseInventoryScreen extends AbstractContainerScreen<HorseInventoryMenu> {
   private static final ResourceLocation HORSE_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/horse.png");
   /** The EntityHorse whose inventory is currently being accessed. */
   private final AbstractHorse horse;
   /** The mouse x-position recorded during the last rendered frame. */
   private float xMouse;
   /** The mouse y-position recorded during the last rendered frame. */
   private float yMouse;

   public HorseInventoryScreen(HorseInventoryMenu pMenu, Inventory pPlayerInventory, AbstractHorse pHorse) {
      super(pMenu, pPlayerInventory, pHorse.getDisplayName());
      this.horse = pHorse;
   }

   protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      pGuiGraphics.blit(HORSE_INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
      if (this.horse instanceof AbstractChestedHorse) {
         AbstractChestedHorse abstractchestedhorse = (AbstractChestedHorse)this.horse;
         if (abstractchestedhorse.hasChest()) {
            pGuiGraphics.blit(HORSE_INVENTORY_LOCATION, i + 79, j + 17, 0, this.imageHeight, abstractchestedhorse.getInventoryColumns() * 18, 54);
         }
      }

      if (this.horse.isSaddleable()) {
         pGuiGraphics.blit(HORSE_INVENTORY_LOCATION, i + 7, j + 35 - 18, 18, this.imageHeight + 54, 18, 18);
      }

      if (this.horse.canWearArmor()) {
         if (this.horse instanceof Llama) {
            pGuiGraphics.blit(HORSE_INVENTORY_LOCATION, i + 7, j + 35, 36, this.imageHeight + 54, 18, 18);
         } else {
            pGuiGraphics.blit(HORSE_INVENTORY_LOCATION, i + 7, j + 35, 0, this.imageHeight + 54, 18, 18);
         }
      }

      InventoryScreen.renderEntityInInventoryFollowsMouse(pGuiGraphics, i + 51, j + 60, 17, (float)(i + 51) - this.xMouse, (float)(j + 75 - 50) - this.yMouse, this.horse);
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
      this.xMouse = (float)pMouseX;
      this.yMouse = (float)pMouseY;
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
   }
}