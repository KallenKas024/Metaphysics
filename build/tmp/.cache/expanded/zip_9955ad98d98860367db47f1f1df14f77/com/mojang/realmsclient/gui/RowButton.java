package com.mojang.realmsclient.gui;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RowButton {
   public final int width;
   public final int height;
   public final int xOffset;
   public final int yOffset;

   public RowButton(int pWidth, int pHeight, int pXOffset, int pYOffset) {
      this.width = pWidth;
      this.height = pHeight;
      this.xOffset = pXOffset;
      this.yOffset = pYOffset;
   }

   public void drawForRowAt(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY) {
      int i = pX + this.xOffset;
      int j = pY + this.yOffset;
      boolean flag = pMouseX >= i && pMouseX <= i + this.width && pMouseY >= j && pMouseY <= j + this.height;
      this.draw(pGuiGraphics, i, j, flag);
   }

   protected abstract void draw(GuiGraphics pGuiGraphics, int pX, int pY, boolean pShowTooltip);

   public int getRight() {
      return this.xOffset + this.width;
   }

   public int getBottom() {
      return this.yOffset + this.height;
   }

   public abstract void onClick(int pIndex);

   public static void drawButtonsInRow(GuiGraphics pGuiGraphics, List<RowButton> pButtons, RealmsObjectSelectionList<?> pPendingInvitations, int pX, int pY, int pMouseX, int pMouseY) {
      for(RowButton rowbutton : pButtons) {
         if (pPendingInvitations.getRowWidth() > rowbutton.getRight()) {
            rowbutton.drawForRowAt(pGuiGraphics, pX, pY, pMouseX, pMouseY);
         }
      }

   }

   public static void rowButtonMouseClicked(RealmsObjectSelectionList<?> pList, ObjectSelectionList.Entry<?> pEntry, List<RowButton> pButtons, int pButton, double pMouseX, double pMouseY) {
      if (pButton == 0) {
         int i = pList.children().indexOf(pEntry);
         if (i > -1) {
            pList.selectItem(i);
            int j = pList.getRowLeft();
            int k = pList.getRowTop(i);
            int l = (int)(pMouseX - (double)j);
            int i1 = (int)(pMouseY - (double)k);

            for(RowButton rowbutton : pButtons) {
               if (l >= rowbutton.xOffset && l <= rowbutton.getRight() && i1 >= rowbutton.yOffset && i1 <= rowbutton.getBottom()) {
                  rowbutton.onClick(i);
               }
            }
         }
      }

   }
}