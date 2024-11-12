package net.minecraft.client.gui.screens.inventory;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CartographyTableScreen extends AbstractContainerScreen<CartographyTableMenu> {
   private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/cartography_table.png");

   public CartographyTableScreen(CartographyTableMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      this.titleLabelY -= 2;
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
   }

   protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
      this.renderBackground(pGuiGraphics);
      int i = this.leftPos;
      int j = this.topPos;
      pGuiGraphics.blit(BG_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
      ItemStack itemstack = this.menu.getSlot(1).getItem();
      boolean flag = itemstack.is(Items.MAP);
      boolean flag1 = itemstack.is(Items.PAPER);
      boolean flag2 = itemstack.is(Items.GLASS_PANE);
      ItemStack itemstack1 = this.menu.getSlot(0).getItem();
      boolean flag3 = false;
      Integer integer;
      MapItemSavedData mapitemsaveddata;
      if (itemstack1.is(Items.FILLED_MAP)) {
         integer = MapItem.getMapId(itemstack1);
         mapitemsaveddata = MapItem.getSavedData(integer, this.minecraft.level);
         if (mapitemsaveddata != null) {
            if (mapitemsaveddata.locked) {
               flag3 = true;
               if (flag1 || flag2) {
                  pGuiGraphics.blit(BG_LOCATION, i + 35, j + 31, this.imageWidth + 50, 132, 28, 21);
               }
            }

            if (flag1 && mapitemsaveddata.scale >= 4) {
               flag3 = true;
               pGuiGraphics.blit(BG_LOCATION, i + 35, j + 31, this.imageWidth + 50, 132, 28, 21);
            }
         }
      } else {
         integer = null;
         mapitemsaveddata = null;
      }

      this.renderResultingMap(pGuiGraphics, integer, mapitemsaveddata, flag, flag1, flag2, flag3);
   }

   private void renderResultingMap(GuiGraphics pGuiGraphics, @Nullable Integer pMapId, @Nullable MapItemSavedData pMapData, boolean pHasMap, boolean pHasPaper, boolean pHasGlassPane, boolean pIsMaxSize) {
      int i = this.leftPos;
      int j = this.topPos;
      if (pHasPaper && !pIsMaxSize) {
         pGuiGraphics.blit(BG_LOCATION, i + 67, j + 13, this.imageWidth, 66, 66, 66);
         this.renderMap(pGuiGraphics, pMapId, pMapData, i + 85, j + 31, 0.226F);
      } else if (pHasMap) {
         pGuiGraphics.blit(BG_LOCATION, i + 67 + 16, j + 13, this.imageWidth, 132, 50, 66);
         this.renderMap(pGuiGraphics, pMapId, pMapData, i + 86, j + 16, 0.34F);
         pGuiGraphics.pose().pushPose();
         pGuiGraphics.pose().translate(0.0F, 0.0F, 1.0F);
         pGuiGraphics.blit(BG_LOCATION, i + 67, j + 13 + 16, this.imageWidth, 132, 50, 66);
         this.renderMap(pGuiGraphics, pMapId, pMapData, i + 70, j + 32, 0.34F);
         pGuiGraphics.pose().popPose();
      } else if (pHasGlassPane) {
         pGuiGraphics.blit(BG_LOCATION, i + 67, j + 13, this.imageWidth, 0, 66, 66);
         this.renderMap(pGuiGraphics, pMapId, pMapData, i + 71, j + 17, 0.45F);
         pGuiGraphics.pose().pushPose();
         pGuiGraphics.pose().translate(0.0F, 0.0F, 1.0F);
         pGuiGraphics.blit(BG_LOCATION, i + 66, j + 12, 0, this.imageHeight, 66, 66);
         pGuiGraphics.pose().popPose();
      } else {
         pGuiGraphics.blit(BG_LOCATION, i + 67, j + 13, this.imageWidth, 0, 66, 66);
         this.renderMap(pGuiGraphics, pMapId, pMapData, i + 71, j + 17, 0.45F);
      }

   }

   private void renderMap(GuiGraphics pGuiGraphics, @Nullable Integer pMapId, @Nullable MapItemSavedData pMapData, int pX, int pY, float pScale) {
      if (pMapId != null && pMapData != null) {
         pGuiGraphics.pose().pushPose();
         pGuiGraphics.pose().translate((float)pX, (float)pY, 1.0F);
         pGuiGraphics.pose().scale(pScale, pScale, 1.0F);
         this.minecraft.gameRenderer.getMapRenderer().render(pGuiGraphics.pose(), pGuiGraphics.bufferSource(), pMapId, pMapData, true, 15728880);
         pGuiGraphics.flush();
         pGuiGraphics.pose().popPose();
      }

   }
}